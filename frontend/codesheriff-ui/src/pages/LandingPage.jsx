import { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import MockupCard from "../components/MockupCard";

const FEATURES = [
  {
    n: "01",
    label: "CODEBASE ARCHAEOLOGY",
    title: "UNDERSTAND CODE\nIN SECONDS.",
    desc: "Bob reads your entire Spring Boot repo — not just the file you're looking at. Full context: who calls what, what depends on what, and what each method is actually trying to do.",
    stats: [
      ["< 30s", "ANALYSIS TIME"],
      ["100%", "BOB-POWERED"],
    ],
    card: { title: "PaymentService.processTransaction()", items: 3 },
  },
  {
    n: "02",
    label: "INTENT VS REALITY",
    title: "SPOT THE\nLIES IN CODE.",
    desc: "Methods named 'helper' doing 6 things. Services that are secretly controllers. Bob finds the gap between what code says it does and what it actually does.",
    stats: [
      ["87", "LINES EXPLAINED"],
      ["0", "HALLUCINATED LOGIC"],
    ],
    card: { title: "OrderController.createOrder()", items: 2 },
  },
  {
    n: "03",
    label: "NAVIGATION",
    title: "KNOW EXACTLY\nWHERE TO TOUCH.",
    desc: "Bob points you to the exact line for your specific change, tells you which methods are safe to modify, and maps the blast radius of any edit.",
    stats: [
      ["Exact", "LINE POINTER"],
      ["Full", "DEPENDENCY MAP"],
    ],
    card: { title: "UserRepository.findById()", items: 1 },
  },
];

const CARD_ITEMS = [
  {
    label: "WHAT IT DOES",
    text: "Processes payment — 3 jobs in one method.",
    colorVar: "var(--text)",
  },
  {
    label: "INTENT VS REALITY",
    text: "Should be split into 3 focused methods.",
    color: "#c92a2a",
  },
  {
    label: "WHERE TO START",
    text: "Line 34 — PaymentGateway.charge()",
    color: "#2f7a2f",
  },
];

const STEPS = [
  {
    n: "01",
    title: "UPLOAD YOUR ZIP",
    desc: "Drop your Spring Boot project ZIP. Only .java files extracted. Nothing stored.",
  },
  {
    n: "02",
    title: "BOB READS EVERYTHING",
    desc: "IBM Bob receives full repo context — classes, methods, annotations, call chains.",
  },
  {
    n: "03",
    title: "CLICK TO INVESTIGATE",
    desc: "Click any method. Bob explains what it does, why it's confusing, where to start.",
  },
  {
    n: "04",
    title: "UNDERSTAND FAST",
    desc: "Get up to speed in minutes instead of hours. Every insight from Bob.",
  },
];

export default function LandingPage({ onEnter, dark, onToggle }) {
  const [typed, setTyped] = useState("");
  const full = "MESSY.";

  useEffect(() => {
    let i = 0;
    const t = setInterval(() => {
      setTyped(full.slice(0, ++i));
      if (i >= full.length) clearInterval(t);
    }, 110);
    return () => clearInterval(t);
  }, []);

  return (
    <div className="landing">
      <Navbar dark={dark} onToggle={onToggle} onEnter={onEnter} />

      {/* HERO */}
      <div className="hero">
        <div className="hero-label">
          <div className="hero-label-line" />
          <span className="hero-label-text">AI CODE INVESTIGATOR</span>
        </div>

        <h1 className="hero-h1">YOUR CODE</h1>

        <div className="hero-h1-highlight">
          <span style={{ color: dark ? "#0a0a0a" : "#fff" }}>ISN'T</span>
        </div>

        <h1 className="hero-h1">
          {typed}
          <span className="hero-cursor">|</span>
        </h1>

        <p className="hero-desc">
          In the era of vibe coding, everyone's shipping fast but nobody can
          read what they shipped. CodeSheriff uses <em>IBM Bob</em> to
          investigate your codebase and explain what code <em>actually</em>{" "}
          does.
        </p>

        <div className="hero-actions">
          <button className="btn-primary" onClick={onEnter}>
            INVESTIGATE NOW →
          </button>
        </div>

        <MockupCard />
      </div>

      {/* STATS */}
      <div className="stats-bar">
        <div className="stats-grid">
          {[
            ["47", "METHODS ANALYZED"],
            ["12", "CLASSES MAPPED"],
            ["5", "BOB INSIGHTS / METHOD"],
          ].map(([n, l]) => (
            <div key={l} className="stat-cell">
              <div className="stat-number">{n}</div>
              <div className="stat-label">{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* FEATURES */}
      <div className="features">
        <div style={{ marginBottom: "4rem" }}>
          <div className="section-label">— PLATFORM</div>
          <h2 className="section-title">THREE PROBLEMS.</h2>
          <h2 className="section-title section-title-accent">ONE SHERIFF.</h2>
        </div>

        {FEATURES.map((f, i) => (
          <div key={f.n} className="feature-row">
            <div style={{ order: i % 2 === 0 ? 0 : 1 }}>
              <div className="feature-n">
                {f.n} — {f.label}
              </div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
              <div className="feature-stats">
                {f.stats.map(([n, l]) => (
                  <div key={l}>
                    <div className="feature-stat-n">{n}</div>
                    <div className="feature-stat-l">{l}</div>
                  </div>
                ))}
              </div>
            </div>

            <div style={{ order: i % 2 === 0 ? 1 : 0 }}>
              <div className="feature-card">
                <div className="feature-card-title">{f.card.title}</div>
                {CARD_ITEMS.slice(0, f.card.items).map((item) => (
                  <div
                    key={item.label}
                    className="feature-card-item"
                    style={{
                      background: dark ? "#161616" : "#f8f8f8",
                      borderLeft: `3px solid ${item.color || item.colorVar}`,
                    }}
                  >
                    <div
                      className="feature-card-item-label"
                      style={{ color: item.color || item.colorVar }}
                    >
                      {item.label}
                    </div>
                    <div
                      className="feature-card-item-text"
                      style={{ color: dark ? "#ccc" : "#555" }}
                    >
                      {item.text}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* HOW IT WORKS */}
      <div className="how-it-works">
        <div className="section-label">— HOW IT WORKS</div>
        <h2 className="section-title" style={{ marginBottom: "3rem" }}>
          UP AND RUNNING
          <br />
          <span className="section-title-accent">IN 30 SECONDS.</span>
        </h2>

        <div className="steps-grid">
          {STEPS.map((s) => (
            <div key={s.n} className="step">
              <div className="step-n">{s.n}</div>
              <div className="step-title">{s.title}</div>
              <div className="step-desc">{s.desc}</div>
            </div>
          ))}
        </div>

        <div style={{ marginTop: "3rem" }}>
          <button className="btn-outline" onClick={onEnter}>
            GET STARTED →
          </button>
        </div>
      </div>

      {/* FOOTER */}
      <div className="footer">
        <div className="footer-logo">CODESHERIFF</div>
        <div className="footer-text">
          Copyright © 2026 CodeSheriff. All rights reserved.
        </div>
        <div className="footer-text">BY TYRA</div>
      </div>
    </div>
  );
}
