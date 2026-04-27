import React from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import StudentsPage from "./pages/StudentsPage";
import "./App.css";

// ─── Protected Route ──────────────────────────────────────────────────────────
function ProtectedRoute({ children }) {
  const isLoggedIn = Boolean(localStorage.getItem("authUser"));
  return isLoggedIn ? children : <Navigate to="/login" replace />;
}

// ─── App ──────────────────────────────────────────────────────────────────────
function App() {
  const navigate  = useNavigate();
  const rawUser   = localStorage.getItem("authUser");
  const user      = rawUser ? JSON.parse(rawUser) : null;

  function handleLogout() {
    localStorage.removeItem("authUser");
    navigate("/login");
  }

  return (
    <div className="app-container">
      {/* ── Header ── */}
      <header className="app-header">
        <div className="header-inner">
          <span className="header-logo">🎓</span>
          <div>
            <h1>Student Onboarding System</h1>
            <p className="header-subtitle">Manage student records with ease</p>
          </div>
          {user && (
            <div className="header-user">
              <span className="header-username">👤 {user.name}</span>
              <button
                id="logout-btn"
                className="btn btn-logout"
                onClick={handleLogout}
                title="Logout"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </header>

      {/* ── Routes ── */}
      <Routes>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/signup"   element={<SignupPage />} />
        <Route
          path="/students"
          element={
            <ProtectedRoute>
              <StudentsPage />
            </ProtectedRoute>
          }
        />
        {/* Default: redirect based on auth state */}
        <Route
          path="*"
          element={<Navigate to={user ? "/students" : "/login"} replace />}
        />
      </Routes>

      {/* ── Footer ── */}
      <footer className="app-footer">
        <p>Student Onboarding System &mdash; Spring Boot + React</p>
      </footer>
    </div>
  );
}

export default App;

