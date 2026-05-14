import ThemeToggle from "./ThemeToggle";

export default function Navbar({ dark, onToggle, onEnter }) {
  return (
    <>
      <nav className="navbar">
        <div className="navbar-links">
          {["HOW IT WORKS"].map((l) => (
            <button key={l} className="navbar-link">
              {l}
            </button>
          ))}
        </div>
        <div className="navbar-logo">CODESHERIFF</div>
        <div className="navbar-actions">
          <ThemeToggle dark={dark} onToggle={onToggle} />
          <span className="navbar-signin">SIGN IN</span>
          <button className="navbar-cta" onClick={onEnter}>
            GET STARTED →
          </button>
        </div>
      </nav>
      <div className="subnav">
        <span> AI CODE INVESTIGATOR FOR SPRING BOOT</span>

        <span>FREE TRIAL · NO CREDIT CARD REQUIRED</span>
      </div>
    </>
  );
}
