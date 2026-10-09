import { Navigate, Outlet } from "react-router-dom";

function ProtectedRoute() {
  const crmUser = localStorage.getItem("crmUser");

  if (!crmUser) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}

export default ProtectedRoute;