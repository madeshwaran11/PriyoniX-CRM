import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminRoute from "./components/AdminRoute";

import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

import Dashboard from "./pages/Dashboard";
import Leads from "./pages/Leads";
import Customers from "./pages/Customers";
import FollowUps from "./pages/FollowUps";
import Tasks from "./pages/Tasks";
import Activities from "./pages/Activities";
import Reports from "./pages/Reports";
import Users from "./pages/Users";
import Profile from "./pages/Profile";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* =========================
            PUBLIC PAGES
        ========================= */}

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/forgot-password"
          element={<ForgotPassword />}
        />

        <Route
          path="/reset-password"
          element={<ResetPassword />}
        />

        <Route
          path="/"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />

        {/* =========================
            PROTECTED PAGES
        ========================= */}

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>

            {/* Dashboard */}
            <Route
              path="/dashboard"
              element={<Dashboard />}
            />

            {/* Lead Management */}
            <Route
              path="/leads"
              element={<Leads />}
            />

            {/* Customer Management */}
            <Route
              path="/customers"
              element={<Customers />}
            />

            {/* Follow-up Management */}
            <Route
              path="/follow-ups"
              element={<FollowUps />}
            />

            {/* Task Management */}
            <Route
              path="/tasks"
              element={<Tasks />}
            />

            {/* Activity Management */}
            <Route
              path="/activities"
              element={<Activities />}
            />

            {/* Reports */}
            <Route
              path="/reports"
              element={<Reports />}
            />

            {/* =========================
                ADMIN ONLY
            ========================= */}

            <Route element={<AdminRoute />}>
              <Route
                path="/users"
                element={<Users />}
              />
            </Route>

            {/* Profile */}
            <Route
              path="/profile"
              element={<Profile />}
            />

          </Route>
        </Route>

        {/* =========================
            UNKNOWN URL
        ========================= */}

        <Route
          path="*"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />

      </Routes>
    </BrowserRouter>
  );
}

export default App;