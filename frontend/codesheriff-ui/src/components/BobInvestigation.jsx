const ITEMS = [
  { label: "WHAT IT DOES", key: "whatItDoes", color: "var(--text)" },
  { label: "INTENT VS REALITY", key: "intentVsReality", color: "#d4913a" },
  { label: "WHY IT'S CONFUSING", key: "whyConfusing", color: "#e05555" },
  { label: "WHO DEPENDS ON IT", key: "dependencies", color: "#9c6fc5" },
  { label: "WHERE TO START", key: "whereToStart", color: "var(--accent)" },
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
          <div className="bob-card-text">{method.bob[item.key]}</div>
        </div>
      ))}
    </div>
  );
}
