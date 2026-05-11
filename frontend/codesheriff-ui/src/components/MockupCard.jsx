export default function MockupCard() {
  const classes = [
    { name: "PaymentService", active: true },
    { name: "OrderController", active: false },
    { name: "UserRepository", active: false },
  ];

  const insights = [
    {
      label: "WHAT IT DOES",
      text: "Processes payment — looks up user, charges gateway, logs audit.",
      color: "#333",
    },
    {
      label: "INTENT VS REALITY",
      text: "Doing 3 jobs. Should be 3 separate focused methods.",
      color: "#c92a2a",
    },
    {
      label: "WHERE TO START",
      text: "Line ~34 — PaymentGateway.charge() is your entry point.",
      color: "#2f7a2f",
    },
  ];

  return (
    <div className="mockup-card">
      <div className="mockup-chrome">
        <div className="mockup-dot" style={{ background: "#ff5f57" }} />
        <div className="mockup-dot" style={{ background: "#ffbd2e" }} />
        <div className="mockup-dot" style={{ background: "#28c840" }} />
        <span className="mockup-title">CodeSheriff — payment-service</span>
        <span className="mockup-status">● BOB ACTIVE</span>
      </div>

      <div className="mockup-body">
        <div className="mockup-sidebar">
          <div className="mockup-sidebar-heading">CLASS TREE</div>
          {classes.map(({ name, active }) => (
            <div
              key={name}
              className="mockup-class-item"
              style={{
                color: active ? "#f0f0f0" : "#666",
                borderLeft: `2px solid ${active ? "var(--accent)" : "transparent"}`,
              }}
            >
              {name}
            </div>
          ))}
          <div className="mockup-method-item" style={{ color: "#bbb" }}>
            ↳ processTransaction()
          </div>
          <div className="mockup-method-item" style={{ color: "#555" }}>
            ↳ validatePayment()
          </div>
        </div>

        <div className="mockup-panel">
          <div className="mockup-panel-title">PROCESSTRANSACTION()</div>
          <div className="mockup-panel-meta">
            TransactionResult · 87 lines · NO TESTS
          </div>
          {insights.map((item) => (
            <div
              key={item.label}
              className="mockup-insight"
              style={{ borderLeft: `3px solid ${item.color}` }}
            >
              <div
                className="mockup-insight-label"
                style={{ color: item.color }}
              >
                {item.label}
              </div>
              <div className="mockup-insight-text">{item.text}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
