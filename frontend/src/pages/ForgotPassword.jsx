import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function ForgotPassword() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (!email.trim()) {
      setError("Please enter your registered email address.");
      return;
    }

    try {
      setLoading(true);

      const response = await api.post("/password/forgot", {
        email: email.trim(),
      });

      setMessage(
        response.data.message ||
          "If an account exists for this email, a password reset link has been sent."
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to process your request. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="forgot-page">
      {/* Stars */}
      <div className="stars stars-one"></div>
      <div className="stars stars-two"></div>
      <div className="stars stars-three"></div>

      {/* Background glow */}
      <div className="space-glow glow-one"></div>
      <div className="space-glow glow-two"></div>

      {/* Main card */}
      <div className="forgot-card">
        <div className="forgot-icon">
          🔐
        </div>

        <h1>Forgot Password?</h1>

        <p className="forgot-subtitle">
          Enter your registered email address and we'll send you
          a secure password reset link.
        </p>

        <form onSubmit={handleSubmit}>
          <label>Email Address</label>

          <input
            type="email"
            placeholder="Enter your registered email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />

          {error && (
            <div className="forgot-error">
              {error}
            </div>
          )}

          {message && (
            <div className="forgot-success">
              ✓ {message}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? "Sending..." : "Send Reset Link"}
          </button>
        </form>

        <button
          className="back-login"
          onClick={() => navigate("/login")}
        >
          ← Back to Login
        </button>

        <div className="forgot-footer">
          <span>PRIYONIX CRM</span>
          <small>Secure Account Recovery</small>
        </div>
      </div>

      <style>{`
        * {
          box-sizing: border-box;
        }

        .forgot-page {
          min-height: 100vh;
          width: 100%;
          position: relative;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 30px;
          background:
            radial-gradient(
              circle at 20% 20%,
              rgba(67, 97, 238, 0.18),
              transparent 30%
            ),
            radial-gradient(
              circle at 80% 80%,
              rgba(124, 58, 237, 0.16),
              transparent 30%
            ),
            linear-gradient(
              135deg,
              #020617 0%,
              #07132e 48%,
              #030712 100%
            );
        }

        .stars {
          position: absolute;
          inset: 0;
          pointer-events: none;
          opacity: 0.7;
        }

        .stars-one {
          background-image:
            radial-gradient(circle, white 1px, transparent 1px),
            radial-gradient(circle, white 1px, transparent 1px);
          background-size: 90px 90px, 140px 140px;
          background-position: 10px 20px, 50px 80px;
        }

        .stars-two {
          background-image:
            radial-gradient(circle, #93c5fd 1px, transparent 1px);
          background-size: 170px 170px;
          background-position: 30px 70px;
          opacity: 0.4;
        }

        .stars-three {
          background-image:
            radial-gradient(circle, #c4b5fd 1.2px, transparent 1.2px);
          background-size: 220px 220px;
          background-position: 100px 30px;
          opacity: 0.3;
        }

        .space-glow {
          position: absolute;
          border-radius: 50%;
          filter: blur(70px);
          pointer-events: none;
        }

        .glow-one {
          width: 260px;
          height: 260px;
          background: rgba(37, 99, 235, 0.16);
          top: -80px;
          left: -70px;
        }

        .glow-two {
          width: 300px;
          height: 300px;
          background: rgba(124, 58, 237, 0.14);
          right: -100px;
          bottom: -100px;
        }

        .forgot-card {
          position: relative;
          z-index: 5;
          width: 100%;
          max-width: 460px;
          padding: 42px;
          border-radius: 28px;
          background: rgba(15, 23, 42, 0.88);
          border: 1px solid rgba(148, 163, 184, 0.2);
          box-shadow:
            0 30px 80px rgba(0, 0, 0, 0.55),
            inset 0 1px 0 rgba(255, 255, 255, 0.06);
          backdrop-filter: blur(20px);
          color: white;
        }

        .forgot-icon {
          width: 70px;
          height: 70px;
          margin: 0 auto 22px;
          border-radius: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32px;
          background: linear-gradient(
            135deg,
            #2563eb,
            #7c3aed
          );
          box-shadow:
            0 15px 35px rgba(37, 99, 235, 0.3);
        }

        .forgot-card h1 {
          margin: 0;
          text-align: center;
          font-size: 31px;
          font-weight: 900;
          letter-spacing: -1px;
        }

        .forgot-subtitle {
          margin: 12px auto 30px;
          max-width: 350px;
          text-align: center;
          color: #94a3b8;
          font-size: 14px;
          line-height: 1.7;
        }

        .forgot-card label {
          display: block;
          margin-bottom: 8px;
          color: #cbd5e1;
          font-size: 12px;
          font-weight: 800;
        }

        .forgot-card input {
          width: 100%;
          height: 50px;
          padding: 0 15px;
          border-radius: 12px;
          border: 1px solid #334155;
          outline: none;
          background: rgba(2, 6, 23, 0.75);
          color: white;
          font-size: 14px;
          transition: 0.2s;
        }

        .forgot-card input::placeholder {
          color: #64748b;
        }

        .forgot-card input:focus {
          border-color: #60a5fa;
          box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.12);
        }

        .forgot-card form button[type="submit"] {
          width: 100%;
          height: 50px;
          margin-top: 18px;
          border: none;
          border-radius: 12px;
          cursor: pointer;
          color: white;
          font-size: 13px;
          font-weight: 900;
          background: linear-gradient(
            135deg,
            #2563eb,
            #7c3aed
          );
          box-shadow:
            0 12px 25px rgba(37, 99, 235, 0.22);
          transition: 0.2s;
        }

        .forgot-card form button[type="submit"]:hover {
          transform: translateY(-2px);
          box-shadow:
            0 16px 30px rgba(37, 99, 235, 0.3);
        }

        .forgot-card form button[type="submit"]:disabled {
          opacity: 0.65;
          cursor: not-allowed;
          transform: none;
        }

        .forgot-error,
        .forgot-success {
          margin-top: 12px;
          padding: 11px 13px;
          border-radius: 10px;
          font-size: 12px;
          line-height: 1.5;
        }

        .forgot-error {
          color: #fecaca;
          background: rgba(127, 29, 29, 0.25);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .forgot-success {
          color: #bbf7d0;
          background: rgba(20, 83, 45, 0.25);
          border: 1px solid rgba(74, 222, 128, 0.25);
        }

        .back-login {
          width: 100%;
          margin-top: 20px;
          padding: 10px;
          border: none;
          background: transparent;
          color: #93c5fd;
          cursor: pointer;
          font-size: 12px;
          font-weight: 800;
        }

        .back-login:hover {
          color: white;
        }

        .forgot-footer {
          margin-top: 24px;
          padding-top: 20px;
          border-top: 1px solid rgba(148, 163, 184, 0.12);
          text-align: center;
        }

        .forgot-footer span {
          display: block;
          color: #e2e8f0;
          font-size: 11px;
          font-weight: 900;
          letter-spacing: 2px;
        }

        .forgot-footer small {
          display: block;
          margin-top: 5px;
          color: #64748b;
          font-size: 10px;
        }

        @media (max-width: 600px) {
          .forgot-page {
            padding: 18px;
          }

          .forgot-card {
            padding: 30px 22px;
            border-radius: 22px;
          }

          .forgot-card h1 {
            font-size: 27px;
          }
        }
      `}</style>
    </div>
  );
}

export default ForgotPassword;