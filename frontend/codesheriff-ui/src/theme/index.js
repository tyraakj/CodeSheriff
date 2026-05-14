export const AMBER = "#e8c547";
export const AMBER_DARK = "#c9a93a";

export const LIGHT = {
  bg: "#f5f4f0",
  surface: "#eeece7",
  surfaceAlt: "#e8e6e0",
  nav: "#f0ede6",
  border: "#d8d5cc",
  text: "#0a0a0a",
  textMuted: "#555",
  textFaint: "#888",
  accent: AMBER_DARK,
  sidebarBg: "#e8e5de",
  sidebarActive: "#fff",
  cardBg: "#ffffff",
  cardBorder: "#ddd8ce",
  tickerBg: AMBER_DARK,
  tickerText: "#0a0a0a",
};

export const DARK = {
  bg: "#0a0a0a",
  surface: "#111111",
  surfaceAlt: "#161616",
  nav: "#111111",
  border: "#1e1e1e",
  text: "#f0f0f0",
  textMuted: "#aaa",
  textFaint: "#666",
  accent: AMBER,
  sidebarBg: "#0d0d0d",
  sidebarActive: "#181818",
  cardBg: "#0d0d0d",
  cardBorder: "#1e1e1e",
  tickerBg: AMBER,
  tickerText: "#0a0a0a",
};

export const typeColor = (type, th) =>
  ({
    Service: th.accent,
    Controller: "#4caf7d",
    Repository: "#9c6fc5",
  })[type] || th.accent;

export const getRisk = (m) =>
  !m.hasTests && m.lineCount > 50 ? ["HIGH RISK", "#e05555"] : !m.has;
