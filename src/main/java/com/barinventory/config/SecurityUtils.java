package com.barinventory.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.barinventory.auth.servcies.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SecurityUtils {

	public static final String SELECTED_BAR_ID = "SELECTED_BAR_ID";

	public static CustomUserDetails getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (!(principal instanceof CustomUserDetails userDetails)) {
			throw new IllegalStateException("Authenticated user not found.");
		}

		return userDetails;
	}

	public static Long getUserId() {
		return getCurrentUser().getUserId();
	}

	public static Long getBarId() {
		Long selectedBarId = getSelectedBarIdFromSession();

		if (selectedBarId != null) {
			return selectedBarId;
		}

		return getCurrentUser().getBarId(); // fallback for current old flow
	}

	public static String getUsername() {
		return getCurrentUser().getUsername();
	}

	public static void setSelectedBarId(Long barId) {
		HttpSession session = getSession();

		if (session == null) {
			throw new IllegalStateException("HTTP session not found.");
		}

		session.setAttribute(SELECTED_BAR_ID, barId);
	}

	public static Long getSelectedBarIdFromSession() {
		HttpSession session = getSession();

		if (session == null) {
			return null;
		}

		Object value = session.getAttribute(SELECTED_BAR_ID);

		if (value == null) {
			return null;
		}

		if (value instanceof Long longValue) {
			return longValue;
		}

		if (value instanceof Integer intValue) {
			return intValue.longValue();
		}

		if (value instanceof String strValue && !strValue.isBlank()) {
			return Long.valueOf(strValue);
		}

		return null;
	}

	public static void clearSelectedBarId() {
		HttpSession session = getSession();

		if (session != null) {
			session.removeAttribute(SELECTED_BAR_ID);
		}
	}

	private static HttpSession getSession() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		if (attrs == null) {
			return null;
		}

		HttpServletRequest request = attrs.getRequest();

		if (request == null) {
			return null;
		}

		return request.getSession(false);
	}
}