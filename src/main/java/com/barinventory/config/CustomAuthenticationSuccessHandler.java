package com.barinventory.config;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.barinventory.auth.entities.UserBarAccess;
import com.barinventory.auth.enums.Role;
import com.barinventory.auth.repos.UserBarAccessRepository;
import com.barinventory.auth.servcies.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserBarAccessRepository userBarAccessRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		String targetUrl = determineTargetUrl(request, authentication);

		if (response.isCommitted()) {
			logger.debug("Response already committed. Unable to redirect to " + targetUrl);
			return;
		}

		clearAuthenticationAttributes(request);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	private String determineTargetUrl(HttpServletRequest request, Authentication authentication) {

		if (hasRole(authentication.getAuthorities(), Role.ADMIN.getAuthority())) {
			return "/admin/dashboard";
		}

		if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
			return "/login";
		}

		List<UserBarAccess> accesses = userBarAccessRepository.findByUserIdAndActiveTrue(userDetails.getUserId());

		if (accesses.isEmpty()) {

			if (userDetails.getBarId() != null) {
				request.getSession(true).setAttribute(SecurityUtils.SELECTED_BAR_ID, userDetails.getBarId());

				return "/bar/dashboard";
			}

			return "/select-store";
		}

		if (accesses.size() == 1) {
			Long barId = accesses.get(0).getBarId();

			request.getSession(true).setAttribute(SecurityUtils.SELECTED_BAR_ID, barId);

			return "/bar/dashboard";
		}

		return "/select-store";
	}

	private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
		return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(role));
	}
}