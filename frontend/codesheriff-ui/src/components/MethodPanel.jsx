import { useState } from "react";
import { getRisk } from "../theme";
import BobInvestigation from "./BobInvestigation";
import DependencyMap from "./DependencyMap";

export default function MethodPanel({ method, dark }) {
  const [activeTab, setActiveTab] = useState("bob");
  const [rl, rc] = getRisk(method);

  const callerStyle = {
    background: dark ? "#0f1a2a" : "#e8edf8",
    border: `1px solid ${dark ? "#1e3a5f" : "#b0c4de"}`,
    color: dark ? "#748ffc" : "#2c5282",
  };

  const calleeStyle = {
    background: dark ? "#1a0f2a" : "#f3e8ff",
    border: `1px solid ${dark ? "#4a1a7a" : "#c084fc"}`,
    color: dark ? "#b77fda" : "#7e22ce",
  };

  return (
    <div className="method-panel">
      <div className="method-header">
        <div
          style={{
            display: "flex",
            alignItems: "flex-start",
            justifyContent: "space-between",
            marginBottom: "0.75rem",
          }}
        >
          <div>
            <div className="method-title">{method.name.toUpperCase()}()</div>
            <div className="method-meta">
              {method.returnType} · {method.lineCount} LINES
            </div>
          </div>
          <div className="method-header-right">
            {method.annotations.map((a) => (
              <span key={a} className="method-annotation">
                {a}
              </span>
            ))}
            <span
              className="method-risk-badge"
              style={{
                background: rc + "18",
                border: `1px solid ${rc}`,
                color: rc,
              }}
            >
              {rl}
            </span>
          </div>
        </div>

        <div className="method-params">
          {method.params.map((p) => (
            <span key={p} className="method-param">
              {p}
            </span>
          ))}
        </div>

        <div className="method-deps">
          <span className="method-deps-label">CALLED BY</span>
          {method.calledBy.map((c) => (
            <span key={c} className="method-dep-tag" style={callerStyle}>
              {c}
            </span>
          ))}
          <span className="method-deps-label">CALLS</span>
          {method.calls.map((c) => (
            <span key={c} className="method-dep-tag" style={calleeStyle}>
              {c}
            </span>
          ))}
        </div>
      </div>

      <div className="tabs">
        {[
          { id: "bob", label: "BOB'S INVESTIGATION" },
          { id: "map", label: "DEPENDENCY MAP" },
        ].map((tab) => (
          <button
            key={tab.id}
            className="tab-btn"
            onClick={() => setActiveTab(tab.id)}
            style={{
              borderBottom: `2px solid ${activeTab === tab.id ? "var(--accent)" : "transparent"}`,
              color:
                activeTab === tab.id ? "var(--accent)" : "var(--text-muted)",
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="tab-content">
        {activeTab === "bob" && <BobInvestigation method={method} />}
        {activeTab === "map" && <DependencyMap method={method} dark={dark} />}
      </div>
    </div>
  );
}
