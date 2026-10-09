import { Navigate, Outlet } from "react-router-dom";

function AdminRoute() {
  const storedUser = localStorage.getItem("crmUser");

  if (!storedUser) {
    return <Navigate to="/login" replace />;
  }

  try {
    const user = JSON.parse(storedUser);

    if (user.role !== "ADMIN") {
      return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
  } catch (error) {
    console.error("Invalid user data:", error);

    localStorage.removeItem("crmUser");

    return <Navigate to="/login" replace />;
  }
}

export default AdminRoute;