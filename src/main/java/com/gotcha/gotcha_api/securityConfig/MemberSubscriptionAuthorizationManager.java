package com.gotcha.gotcha_api.securityConfig;

import com.gotcha.gotcha_api.enums.SubscriptionStatus;
import com.gotcha.gotcha_api.model.UserPrincipal;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

public class MemberSubscriptionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return new AuthorizationDecision(true);
        }

        boolean isMember = auth.getAuthorities().stream()
                .anyMatch(authority -> "MEMBER".equals(authority.getAuthority()));
        if (!isMember) {
            return new AuthorizationDecision(false);
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return new AuthorizationDecision(
                    userPrincipal.getUser().getSubscriptionStatus() == SubscriptionStatus.ACTIVE
            );
        }

        return new AuthorizationDecision(false);
    }
}
