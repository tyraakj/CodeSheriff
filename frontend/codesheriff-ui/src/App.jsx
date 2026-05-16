import { useState, useEffect } from "react";
import { LIGHT, DARK } from "./theme";
import LandingPage from "./pages/LandingPage";
import Dashboard from "./pages/Dashboard";
import AuthPage from "./pages/AuthPage";
import { authService } from "./lib/supabase";
import "./global.css";

export default function App() {
  const [page, setPage] = useState("landing");
  const [dark, setDark] = useState(true);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const th = dark ? DARK : LIGHT;
  const toggle = () => setDark((d) => !d);

  useEffect(() => {
    checkUser();

    const {
      data: { subscription },
    } = authService.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null);
      setLoading(false);
    });

    return () => subscription.unsubscribe();
  }, []);

  const checkUser = async () => {
    try {
      const currentUser = await authService.getUser();
      setUser(currentUser);
    } catch (error) {
      console.error("Error checking user:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAuthSuccess = (authenticatedUser) => {
    setUser(authenticatedUser);
    setPage("dashboard");
  };

  const handleSignOut = async () => {
    try {
      await authService.signOut();
      setUser(null);
      setPage("landing");
    } catch (error) {
      console.error("Error signing out:", error);
    }
  };

  // Navigate to dashboard only if user exists
  const handleEnter = () => {
    if (user) {
      setPage("dashboard");
    } else {
      setPage("auth");
    }
  };

  if (loading) {
    return (
      <div
        className={dark ? "theme-dark" : "theme-light"}
        style={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "var(--bg)",
          color: "var(--text)",
        }}
      >
        <div>Loading...</div>
      </div>
    );
  }

  const renderPage = () => {
    switch (page) {
      case "landing":
        return (
          <LandingPage
            onEnter={handleEnter}
            onSignIn={() => setPage("auth")}
            dark={dark}
            onToggle={toggle}
            user={user}
            th={th}
          />
        );

      case "auth":
        // If already logged in redirect to dashboard
        if (user) {
          setPage("dashboard");
          return null;
        }
        return (
          <AuthPage
            onBack={() => setPage("landing")}
            onAuthSuccess={handleAuthSuccess}
            dark={dark}
            onToggle={toggle}
            th={th}
          />
        );

      case "dashboard":
        // Protected — redirect to auth if no user
        if (!user) {
          setPage("auth");
          return null;
        }
        return (
          <Dashboard
            onBack={() => setPage("landing")}
            onSignOut={handleSignOut}
            dark={dark}
            onToggle={toggle}
            th={th}
            user={user}
          />
        );

      default:
        return (
          <LandingPage
            onEnter={handleEnter}
            onSignIn={() => setPage("auth")}
            dark={dark}
            onToggle={toggle}
            user={user}
            th={th}
          />
        );
    }
  };

  return (
    <div className={dark ? "theme-dark" : "theme-light"}>{renderPage()}</div>
  );
}
