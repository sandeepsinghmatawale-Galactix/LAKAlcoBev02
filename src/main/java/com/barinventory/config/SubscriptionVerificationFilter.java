package com.barinventory.config;

import java.io.IOException;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            
            // Bypass subscription checks completely for Platform Admins
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                SubscriptionStatus status = subscriptionApi.getSubscriptionStatusForBar(userDetails.getBarId());

                if (status != SubscriptionStatus.SUBSCRIBED) {
                    response.sendRedirect("/subscription-expired");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}