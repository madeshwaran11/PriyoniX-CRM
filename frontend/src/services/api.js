import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
  // Send the Spring Security session cookie.
  withCredentials: true,
});

/**
 * A 403 can mean either an expired session or an authenticated user
 * lacking permission. Only treat it as a session expiry when the backend
 * response explicitly identifies an authentication/session problem.
 */
function isExpiredSessionPayload(data) {
  let responseText = "";

  if (typeof data === "string") {
    responseText = data;
  } else if (data !== null && data !== undefined) {
    try {
      responseText = JSON.stringify(data);
    } catch {
      responseText = String(data);
    }
  }

  const explicitCode =
    data && typeof data === "object"
      ? `${data.code ?? ""} ${data.errorCode ?? ""} ${data.error ?? ""} ${data.message ?? ""} ${data.detail ?? ""}`
      : responseText;

  const explicitExpiredCode =
    /\b(SESSION_EXPIRED|EXPIRED_SESSION|INVALID_SESSION|SESSION_INVALID|AUTHENTICATION_REQUIRED|UNAUTHENTICATED|JWT_EXPIRED|TOKEN_EXPIRED)\b/i.test(
      explicitCode
    );

  const expiredSessionMessage =
    /(session.{0,50}(expired|invalid|timed\s*out)|(?:expired|invalid).{0,50}session|jwt.{0,30}expired|token.{0,30}expired|full authentication is required|authentication required|not authenticated|unauthenticated|login required|please log\s*in)/i.test(
      responseText
    );

  return explicitExpiredCode || expiredSessionMessage;
}

function isLoginRequest(url = "") {
  return /\/users\/login(?:[/?#]|$)/i.test(url);
}

function redirectToLogin() {
  localStorage.removeItem("crmUser");

  if (window.location.pathname !== "/login") {
    window.location.replace("/login");
  }
}

// ==========================================
// RESPONSE INTERCEPTOR
// ==========================================

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || "";

    // A 401 on a protected request means the session is not authenticated.
    const isUnauthorized = status === 401;

    // A 403 should log out only when the response identifies an expired or
    // invalid session. Do not log out for ordinary role/permission failures.
    const isExpired403 =
      status === 403 &&
      isExpiredSessionPayload(error.response?.data);

    if (!isLoginRequest(requestUrl) && (isUnauthorized || isExpired403)) {
      redirectToLogin();
    } else if (status === 403) {
      console.error(
        "Access denied. You do not have permission to perform this action."
      );
    }

    return Promise.reject(error);
  }
);

export default api;
