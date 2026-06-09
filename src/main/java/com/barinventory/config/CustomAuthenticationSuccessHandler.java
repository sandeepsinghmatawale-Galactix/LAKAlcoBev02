package com.barinventory.config; 
import java.io.IOException;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import com.barinventory.auth.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // Spring's utility to remember what page the unauthenticated user was originally trying to access
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // 1. If the user was trying to access a specific page before logging in, send them there first
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        // 2. Otherwise, route them to their designated domain dashboard
        String targetUrl = determineTargetUrlByRole(authentication.getAuthorities());
        
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Determines the fallback landing URL based on the user's maximum assigned authority.
     */
    private String determineTargetUrlByRole(Collection<? extends GrantedAuthority> authorities) {
        String adminAuthority = Role.ADMIN.getAuthority();       // "ROLE_ADMIN"
        
        for (GrantedAuthority grantedAuthority : authorities) {
            String authority = grantedAuthority.getAuthority();
            
            if (authority.equals(adminAuthority)) {
                return "/admin/dashboard";
            }
        }
        
        // Fallback for all inventory staff/owners
        return "/sessions/create-page";
    }
}