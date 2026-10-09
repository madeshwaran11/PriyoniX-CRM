import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",

  headers: {
    "Content-Type": "application/json",
  },

  // Send Spring Security session cookie
  withCredentials: true,
});


// ==========================================
// RESPONSE INTERCEPTOR
// ==========================================

api.interceptors.response.use(
  (response) => {
    return response;
  },

  (error) => {

    const status =
      error.response?.status;

    const requestUrl =
      error.config?.url || "";


    // ========================================
    // 401 UNAUTHORIZED
    // ========================================

    if (
      status === 401 &&
      !requestUrl.includes(
        "/users/login"
      )
    ) {

      localStorage.removeItem(
        "crmUser"
      );

      if (
        window.location.pathname !==
        "/login"
      ) {
        window.location.href =
          "/login";
      }
    }


    // ========================================
    // 403 FORBIDDEN
    // ========================================

    if (status === 403) {

      console.error(
        "Access denied. You do not have permission to perform this action."
      );
    }


    return Promise.reject(error);
  }
);


export default api;
