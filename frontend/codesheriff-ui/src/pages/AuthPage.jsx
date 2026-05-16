import { useState } from "react";
import { authService } from "../lib/supabase";
import ThemeToggle from "../components/ThemeToggle";

export default function AuthPage({ onBack, onAuthSuccess, dark, onToggle }) {
  const [isSignUp, setIsSignUp] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    // Validation
    if (!email || !password) {
      setError("Email and password are required");
      return;
    }

    if (isSignUp && password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }

    setLoading(true);

    try {
      if (isSignUp) {
        await authService.signUp(email, password);
        setSuccess("Account created! Check your email to verify.");
        setTimeout(() => {
          setIsSignUp(false);
          setSuccess("");
        }, 3000);
      } else {
        const { session } = await authService.signIn(email, password);
        if (session) {
          onAuthSuccess(session.user);
        }
      }
    } catch (err) {
      setError(err.message || "Authentication failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`auth-page ${dark ? "theme-dark" : "theme-light"}`}>
      <div className="auth-topbar">
        <button className="btn-back" onClick={onBack}>
          ← BACK
        </button>
        <span className="auth-logo">CODESHERIFF</span>
        <ThemeToggle dark={dark} onToggle={onToggle} />
      </div>

      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="auth-icon">🛡️</div>
            <h1 className="auth-title">
              {isSignUp ? "CREATE ACCOUNT" : "SIGN IN"}
            </h1>
            <p className="auth-subtitle">
              {isSignUp
                ? "Join CodeSheriff to analyze your codebase"
                : "Access your code analysis dashboard"}
            </p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">EMAIL</label>
              <input
                type="email"
                className="form-input"
                placeholder="your.email@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={loading}
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <label className="form-label">PASSWORD</label>
              <input
                type="password"
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                autoComplete={isSignUp ? "new-password" : "current-password"}
              />
            </div>

            {isSignUp && (
              <div className="form-group">
                <label className="form-label">CONFIRM PASSWORD</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={loading}
                  autoComplete="new-password"
                />
              </div>
            )}

            {error && <div className="auth-error">{error}</div>}
            {success && <div className="auth-success">{success}</div>}

            <button
              type="submit"
              className="btn-auth-submit"
              disabled={loading}
            >
              {loading
                ? "PROCESSING..."
                : isSignUp
                  ? "CREATE ACCOUNT →"
                  : "SIGN IN →"}
            </button>
          </form>

          <div className="auth-footer">
            <button
              className="btn-auth-toggle"
              onClick={() => {
                setIsSignUp(!isSignUp);
                setError("");
                setSuccess("");
              }}
              disabled={loading}
            >
              {isSignUp
                ? "Already have an account? Sign in"
                : "Don't have an account? Sign up"}
            </button>
          </div>

          <div className="auth-security-note">
            🔒 Secured by Supabase Authentication
          </div>
        </div>
      </div>

      <style jsx>{`
        .auth-page {
          min-height: 100vh;
          background: var(--bg);
          color: var(--text);
        }

        .auth-topbar {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 1.5rem 2rem;
          border-bottom: 1px solid var(--border);
        }

        .btn-back {
          background: transparent;
          border: 1px solid var(--border);
          color: var(--text-muted);
          padding: 0.5rem 1rem;
          border-radius: 4px;
          cursor: pointer;
          font-family: "Courier New", monospace;
          font-size: 0.85rem;
          transition: all 0.2s;
        }

        .btn-back:hover {
          border-color: var(--accent);
          color: var(--accent);
        }

        .auth-logo {
          font-family: "Courier New", monospace;
          font-weight: bold;
          font-size: 1.2rem;
          color: var(--accent);
          letter-spacing: 2px;
        }

        .auth-container {
          display: flex;
          justify-content: center;
          align-items: center;
          min-height: calc(100vh - 80px);
          padding: 2rem;
        }

        .auth-card {
          background: var(--panel-bg);
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 3rem;
          max-width: 450px;
          width: 100%;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        }

        .auth-header {
          text-align: center;
          margin-bottom: 2rem;
        }

        .auth-icon {
          font-size: 3rem;
          margin-bottom: 1rem;
        }

        .auth-title {
          font-family: "Courier New", monospace;
          font-size: 1.5rem;
          font-weight: bold;
          color: var(--accent);
          margin-bottom: 0.5rem;
          letter-spacing: 1px;
        }

        .auth-subtitle {
          color: var(--text-muted);
          font-size: 0.9rem;
          line-height: 1.5;
        }

        .auth-form {
          display: flex;
          flex-direction: column;
          gap: 1.5rem;
        }

        .form-group {
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
        }

        .form-label {
          font-family: "Courier New", monospace;
          font-size: 0.75rem;
          font-weight: bold;
          color: var(--text-muted);
          letter-spacing: 1px;
        }

        .form-input {
          background: var(--bg);
          border: 1px solid var(--border);
          color: var(--text);
          padding: 0.75rem 1rem;
          border-radius: 4px;
          font-family: "Courier New", monospace;
          font-size: 0.9rem;
          transition: all 0.2s;
        }

        .form-input:focus {
          outline: none;
          border-color: var(--accent);
          box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
        }

        .form-input:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .auth-error {
          background: rgba(224, 85, 85, 0.1);
          border: 1px solid #e05555;
          color: #e05555;
          padding: 0.75rem;
          border-radius: 4px;
          font-size: 0.85rem;
          text-align: center;
        }

        .auth-success {
          background: rgba(94, 168, 94, 0.1);
          border: 1px solid #5ea85e;
          color: #5ea85e;
          padding: 0.75rem;
          border-radius: 4px;
          font-size: 0.85rem;
          text-align: center;
        }

        .btn-auth-submit {
          background: var(--accent);
          color: var(--bg);
          border: none;
          padding: 1rem;
          border-radius: 4px;
          font-family: "Courier New", monospace;
          font-weight: bold;
          font-size: 0.9rem;
          letter-spacing: 1px;
          cursor: pointer;
          transition: all 0.2s;
        }

        .btn-auth-submit:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 212, 255, 0.3);
        }

        .btn-auth-submit:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .auth-footer {
          margin-top: 1.5rem;
          text-align: center;
        }

        .btn-auth-toggle {
          background: transparent;
          border: none;
          color: var(--accent);
          font-size: 0.85rem;
          cursor: pointer;
          text-decoration: underline;
          transition: opacity 0.2s;
        }

        .btn-auth-toggle:hover:not(:disabled) {
          opacity: 0.8;
        }

        .btn-auth-toggle:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .auth-security-note {
          margin-top: 2rem;
          text-align: center;
          font-size: 0.75rem;
          color: var(--text-faint);
          padding-top: 1.5rem;
          border-top: 1px solid var(--border);
        }
      `}</style>
    </div>
  );
}


