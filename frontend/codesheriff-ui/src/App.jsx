import { useState } from "react";
import { LIGHT, DARK } from "./theme";
import LandingPage from "./pages/LandingPage";
import Dashboard from "./pages/Dashboard";
import "./styles/global.css";

export default function App() {
  const [page, setPage] = useState("landing");
  const [dark, setDark] = useState(true);
  const th = dark ? DARK : LIGHT;
  const toggle = () => setDark((d) => !d);

  return (
    <div className={dark ? "theme-dark" : "theme-light"}>
      {page === "landing" ? (
        <LandingPage
          onEnter={() => setPage("dashboard")}
          dark={dark}
          onToggle={toggle}
          th={th}
        />
      ) : (
        <Dashboard
          onBack={() => setPage("landing")}
          dark={dark}
          onToggle={toggle}
          th={th}
        />
      )}
    </div>
  );
}
