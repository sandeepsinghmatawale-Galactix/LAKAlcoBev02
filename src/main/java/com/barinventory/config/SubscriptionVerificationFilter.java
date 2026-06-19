package com.barinventory.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.barinventory.auth.servcies.CustomUserDetails;
import com.barinventory.subscriptions.enums.SubscriptionStatus;
import com.barinventory.subscriptions.repository.SubscriptionModuleApi;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionVerificationFilter extends OncePerRequestFilter {

    private final SubscriptionModuleApi subscriptionApi;

    private static final Set<String> BYPASS_PATHS = Set.of(
            "/login",
            "/logout",
            "/select-store",
            "/subscription-expired"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (BYPASS_PATHS.contains(path)) {
            return true;
        }

        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.startsWith("/favicon");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        Long selectedBarId = SecurityUtils.getSelectedBarIdFromSession();

        if (selectedBarId == null) {
            selectedBarId = userDetails.getBarId(); // fallback for single-store old flow
        }

        if (selectedBarId == null) {
            response.sendRedirect("/select-store");
            return;
        }

        SubscriptionStatus status =
                subscriptionApi.getSubscriptionStatusForBar(selectedBarId);

        if (status != SubscriptionStatus.SUBSCRIBED) {
            response.sendRedirect("/subscription-expired");
            return;
        }

        filterChain.doFilter(request, response);
    }
}