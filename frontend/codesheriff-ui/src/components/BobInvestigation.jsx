const ITEMS = [
  { label: "WHAT IT DOES", key: "whatItDoes", color: "var(--text)" },
  { label: "INTENT VS REALITY", key: "intentVsReality", color: "#d4913a" },
  { label: "WHERE TO START", key: "whereToStart", color: "var(--accent)" },
  { label: "LINE COUNT", key: "lineCount", color: "#9c6fc5" },
  { label: "HAS TESTS", key: "hasTests", color: "#5ea85e" },
];

export default function BobInvestigation({ method }) {
  return (
    <div className="bob-cards">
      {ITEMS.map((item) => (
        <div
          key={item.key}
          className="bob-card"
          style={{ borderLeft: `3px solid ${item.color}` }}
        >
          <div className="bob-card-label" style={{ color: item.color }}>
            <span className="bob-card-dot" style={{ background: item.color }} />
            {item.label}
          </div>
          <div className="bob-card-text">
            {item.key === "hasTests"
              ? (method.bob[item.key] ? "✓ Yes" : "✗ No")
              : method.bob[item.key]}
          </div>
        </div>
      ))}
    </div>
  );
}
