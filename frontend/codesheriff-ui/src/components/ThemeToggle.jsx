export default function ThemeToggle({ dark, onToggle }) {
  return (
    <button className="theme-toggle" onClick={onToggle}>
      {dark ? "☀ LIGHT" : "☾ DARK"}
    </button>
  );
}
