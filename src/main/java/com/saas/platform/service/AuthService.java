package com.saas.platform.service;

import com.saas.platform.dto.request.LoginRequest;
import com.saas.platform.dto.request.RegisterRequest;
import com.saas.platform.dto.response.AuthResponse;
import com.saas.platform.dto.response.UserResponse;
import com.saas.platform.entity.Subscription;
import com.saas.platform.entity.Tenant;
import com.saas.platform.entity.User;
import com.saas.platform.entity.enums.Plan;
import com.saas.platform.entity.enums.PlanStatus;
import com.saas.platform.entity.enums.Role;
import com.saas.platform.entity.enums.SubscriptionStatus;
import com.saas.platform.exception.ResourceNotFoundException;
import com.saas.platform.repository.SubscriptionRepository;
import com.saas.platform.repository.TenantRepository;
import com.saas.platform.repository.UserRepository;
import com.saas.platform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Generate unique slug from tenant name
        String slug = generateSlug(request.getTenantName());
        if (tenantRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // 2. Create tenant
        Tenant tenant = Tenant.builder()
                .name(request.getTenantName())
                .slug(slug)
                .plan(Plan.FREE)
                .planStatus(PlanStatus.ACTIVE)
                .build();
        tenant = tenantRepository.save(tenant);

        // 3. Create owner user
        User user = User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .role(Role.OWNER)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // 4. Create FREE subscription
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(Plan.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        subscriptionRepository.save(subscription);

        // 5. Generate JWT
        String accessToken = jwtService.generateAccessToken(
                user.getId(), tenant.getId(),
                user.getRole().name(), user.getEmail());

        return buildAuthResponse(accessToken, user, tenant);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // find user by email across tenants (email is unique per tenant,
        // so we find by email globally — first match for simplicity)
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is inactive");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getTenant().getId(),
                user.getRole().name(), user.getEmail());

        return buildAuthResponse(accessToken, user, user.getTenant());
    }

    private AuthResponse buildAuthResponse(String accessToken, User user, Tenant tenant) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(userResponse)
                .build();
    }

    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}