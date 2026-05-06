import { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import Ticker from "../components/Ticker";
import MockupCard from "../components/MockupCard";

export default function LandingPage({ onEnter, dark, onToggle, th }) {
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
    <div
      style={{
        background: th.bg,
        color: th.text,
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        transition: "background 0.3s, color 0.3s",
      }}
    >
      <Navbar th={th} dark={dark} onToggle={onToggle} onEnter={onEnter} />

      {/* HERO */}
      <div
        style={{
          maxWidth: 1100,
          margin: "0 auto",
          padding: "5rem 2.5rem 3rem",
          animation: "fadeUp 0.6s ease forwards",
          flex: 1,
        }}
      >
        <div style={{ textAlign: "center" }}>
          {/* Label */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "0.75rem",
              marginBottom: "2.5rem",
            }}
          >
            <div style={{ width: 28, height: 2, background: th.accent }} />
            <span
              style={{
                fontSize: 11,
                letterSpacing: "0.2em",
                color: th.accent,
                fontFamily: "'Barlow Condensed', sans-serif",
                fontWeight: 700,
              }}
            >
              AI CODE INVESTIGATOR
            </span>
          </div>

          {/* Hero headline */}
          <h1 style={{ fontSize: "clamp(4rem, 12vw, 9.5rem)", lineHeight: 0.86, letterSpacing: "-0.02em", color: th.text, marginBottom: "0.1rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>
            YOUR CODE
          </h1>
          <h1 style={{ fontSize: "clamp(4rem, 12vw, 9.5rem)", lineHeight: 0.86, letterSpacing: "-0.02em", marginBottom: "0.1rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>
            <span style={{ background: th.accent, color: dark ? "#0a0a0a" : "#fff", padding: "0 0.1em" }}>ISN'T</span>
          </h1>
          <h1 style={{ fontSize: "clamp(4rem, 12vw, 9.5rem)", lineHeight: 0.86, letterSpacing: "-0.02em", color: th.text, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>
            {typed}
            <span style={{ color: th.accent, animation: "blink 1s step-end infinite" }}>|</span>
          </h1>

          <p
            style={{
              fontSize: 17,
              color: th.textMuted,
              maxWidth: 520,
              margin: "2.5rem auto",
              lineHeight: 1.75,
              fontFamily: "'Barlow', sans-serif",
            }}
          >
            In the era of vibe coding, everyone's shipping fast — but nobody can read what they shipped.
            CodeSheriff uses IBM Bob to investigate your codebase and explain what code{" "}
            <em>actually</em> does.
          </p>

          <div style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
            <button
              onClick={onEnter}
              style={{
                background: th.text, border: "none", borderRadius: 3,
                padding: "1rem 2.5rem", color: th.bg,
                fontSize: 13, fontFamily: "'Barlow Condensed', sans-serif",
                fontWeight: 900, letterSpacing: "0.1em", cursor: "pointer", transition: "background 0.15s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = th.accent)}
              onMouseLeave={(e) => (e.currentTarget.style.background = th.text)}
            >
              INVESTIGATE NOW →
            </button>
            <button
              style={{
                background: "transparent", border: `1px solid ${th.border}`,
                borderRadius: 3, padding: "1rem 2.5rem", color: th.text,
                fontSize: 13, fontFamily: "'Barlow Condensed', sans-serif",
                fontWeight: 900, letterSpacing: "0.1em", cursor: "pointer", transition: "border-color 0.15s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.borderColor = th.text)}
              onMouseLeave={(e) => (e.currentTarget.style.borderColor = th.border)}
            >
              TRY DEMO
            </button>
          </div>
        </div>

        <MockupCard dark={dark} />
      </div>

      {/* STATS */}
      <div style={{ borderTop: `1px solid ${th.border}`, borderBottom: `1px solid ${th.border}`, maxWidth: 820, margin: "3rem auto 0" }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr" }}>
          {[["47", "METHODS ANALYZED"], ["12", "CLASSES MAPPED"], ["5", "BOB INSIGHTS / METHOD"]].map(([n, l], i) => (
            <div key={l} style={{ padding: "2.5rem", textAlign: "center", borderRight: i < 2 ? `1px solid ${th.border}` : "none" }}>
              <div style={{ fontSize: "clamp(2.5rem, 5vw, 4.5rem)", fontWeight: 900, color: th.accent, letterSpacing: "-0.03em", fontFamily: "'Barlow Condensed', sans-serif" }}>{n}</div>
              <div style={{ fontSize: 10, letterSpacing: "0.15em", color: th.textMuted, marginTop: 6, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* FEATURES */}
      <div style={{ maxWidth: 1100, margin: "0 auto", padding: "5rem 2.5rem" }}>
        <div style={{ marginBottom: "4rem" }}>
          <div style={{ fontSize: 10, letterSpacing: "0.2em", color: th.accent, marginBottom: "1rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>— PLATFORM</div>
          <h2 style={{ fontSize: "clamp(3rem, 7vw, 6rem)", lineHeight: 0.88, letterSpacing: "-0.02em", color: th.text, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>THREE PROBLEMS.</h2>
          <h2 style={{ fontSize: "clamp(3rem, 7vw, 6rem)", lineHeight: 0.88, letterSpacing: "-0.02em", color: th.accent, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>ONE SHERIFF.</h2>
        </div>

        {[
          { n: "01", label: "CODEBASE ARCHAEOLOGY", title: "UNDERSTAND CODE\nIN SECONDS.", desc: "Bob reads your entire Spring Boot repo — not just the file you're looking at. Full context: who calls what, what depends on what, and what each method is actually trying to do.", stats: [["< 30s", "ANALYSIS TIME"], ["100%", "BOB-POWERED"]] },
          { n: "02", label: "INTENT VS REALITY", title: "SPOT THE\nLIES IN CODE.", desc: "Methods named 'helper' doing 6 things. Services that are secretly controllers. Bob finds the gap between what code says it does and what it actually does.", stats: [["87", "LINES EXPLAINED"], ["0", "HALLUCINATED LOGIC"]] },
          { n: "03", label: "NAVIGATION", title: "KNOW EXACTLY\nWHERE TO TOUCH.", desc: "Bob points you to the exact line for your specific change, tells you which methods are safe to modify, and maps the blast radius of any edit.", stats: [["Exact", "LINE POINTER"], ["Full", "DEPENDENCY MAP"]] },
        ].map((f, i) => (
          <div key={f.n} style={{ display: "grid", gridTemplateColumns: "1fr 1fr", borderTop: `1px solid ${th.border}`, padding: "4.5rem 0", gap: "5rem", alignItems: "center" }}>
            <div style={{ order: i % 2 === 0 ? 0 : 1 }}>
              <div style={{ fontSize: 10, letterSpacing: "0.2em", color: th.accent, marginBottom: "1.5rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>{f.n} — {f.label}</div>
              <h3 style={{ fontSize: "clamp(2rem, 4.5vw, 3.8rem)", lineHeight: 0.88, letterSpacing: "-0.02em", color: th.text, marginBottom: "1.5rem", whiteSpace: "pre-line", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>{f.title}</h3>
              <p style={{ fontSize: 15, color: th.textMuted, lineHeight: 1.75, marginBottom: "2rem", fontFamily: "'Barlow', sans-serif" }}>{f.desc}</p>
              <div style={{ display: "flex", gap: "3rem" }}>
                {f.stats.map(([n, l]) => (
                  <div key={l}>
                    <div style={{ fontSize: "2.2rem", fontWeight: 900, color: th.accent, fontFamily: "'Barlow Condensed', sans-serif" }}>{n}</div>
                    <div style={{ fontSize: 9, letterSpacing: "0.12em", color: th.textMuted, marginTop: 2, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>{l}</div>
                  </div>
                ))}
              </div>
            </div>
            <div style={{ order: i % 2 === 0 ? 1 : 0 }}>
              <div style={{ background: th.cardBg, border: `1px solid ${th.cardBorder}`, borderRadius: 8, padding: "1.25rem", boxShadow: dark ? "0 20px 60px rgba(0,0,0,0.5)" : "0 8px 30px rgba(0,0,0,0.1)" }}>
                <div style={{ fontSize: 12, fontWeight: 900, color: th.text, marginBottom: "0.75rem", fontFamily: "'Barlow Condensed', sans-serif" }}>
                  {["PaymentService.processTransaction()", "OrderController.createOrder()", "UserRepository.findById()"][i]}
                </div>
                {[
                  { l: "WHAT IT DOES", t: "Processes payment — 3 jobs in one method.", c: dark ? "#aaa" : "#333" },
                  { l: "INTENT VS REALITY", t: "Should be split into 3 focused methods.", c: "#c92a2a" },
                  { l: "WHERE TO START", t: "Line 34 — PaymentGateway.charge()", c: "#2f7a2f" },
                ].slice(0, i === 0 ? 3 : i === 1 ? 2 : 1).map((item) => (
                  <div key={item.l} style={{ background: dark ? "#161616" : "#f8f8f8", borderRadius: 5, padding: "0.6rem 0.8rem", marginBottom: "0.5rem", borderLeft: `3px solid ${item.c}` }}>
                    <div style={{ fontSize: 8, fontWeight: 700, color: item.c, letterSpacing: "0.12em", fontFamily: "'Barlow Condensed', sans-serif" }}>{item.l}</div>
                    <div style={{ fontSize: 11, color: dark ? "#ccc" : "#555", marginTop: 2, fontFamily: "'Barlow', sans-serif" }}>{item.t}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* HOW IT WORKS */}
      <div style={{ borderTop: `1px solid ${th.border}`, padding: "5rem 2.5rem", maxWidth: 1100, margin: "0 auto" }}>
        <div style={{ fontSize: 10, letterSpacing: "0.2em", color: th.accent, marginBottom: "1rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>— HOW IT WORKS</div>
        <h2 style={{ fontSize: "clamp(3rem, 7vw, 6rem)", lineHeight: 0.88, letterSpacing: "-0.02em", color: th.text, marginBottom: "3rem", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900 }}>
          UP AND RUNNING<br /><span style={{ color: th.accent }}>IN 30 SECONDS.</span>
        </h2>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", borderTop: `1px solid ${th.border}` }}>
          {[
            { n: "01", title: "UPLOAD YOUR ZIP", desc: "Drop your Spring Boot project ZIP. Only .java files extracted. Nothing stored." },
            { n: "02", title: "BOB READS EVERYTHING", desc: "IBM Bob receives full repo context — classes, methods, annotations, call chains." },
            { n: "03", title: "CLICK TO INVESTIGATE", desc: "Click any method. Bob explains what it does, why it's confusing, where to start." },
            { n: "04", title: "UNDERSTAND FAST", desc: "Get up to speed in minutes instead of hours. Every insight from Bob." },
          ].map((s, i) => (
            <div key={s.n} style={{ padding: "2rem 1.5rem", borderRight: i < 3 ? `1px solid ${th.border}` : "none" }}>
              <div style={{ fontSize: "2.5rem", fontWeight: 900, color: th.border, marginBottom: "1rem", fontFamily: "'Barlow Condensed', sans-serif" }}>{s.n}</div>
              <div style={{ fontSize: 13, fontWeight: 700, color: th.text, letterSpacing: "0.06em", marginBottom: "0.75rem", fontFamily: "'Barlow Condensed', sans-serif" }}>{s.title}</div>
              <div style={{ fontSize: 13, color: th.textMuted, lineHeight: 1.65, fontFamily: "'Barlow', sans-serif" }}>{s.desc}</div>
            </div>
          ))}
        </div>
        <div style={{ marginTop: "3rem" }}>
          <button onClick={onEnter} style={{ background: "transparent", border: `1px solid ${th.border}`, borderRadius: 3, padding: "0.9rem 2rem", color: th.text, fontSize: 12, fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 900, letterSpacing: "0.1em", cursor: "pointer", transition: "all 0.15s" }}
            onMouseEnter={(e) => { e.currentTarget.style.background = th.text; e.currentTarget.style.color = th.bg; }}
            onMouseLeave={(e) => { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = th.text; }}>
            GET STARTED →
          </button>
        </div>
      </div>

      {/* FOOTER */}
      <div style={{ borderTop: `1px solid ${th.border}`, padding: "1.75rem 2.5rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ fontWeight: 900, fontSize: 16, fontFamily: "'Barlow Condensed', sans-serif", color: th.text }}>CODESHERIFF</div>
        <div style={{ fontSize: 10, color: th.textFaint, letterSpacing: "0.08em", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>BUILT FOR IBM BOB HACKATHON · MAY 15–17 2026</div>
        <div style={{ fontSize: 10, color: th.textFaint, letterSpacing: "0.08em", fontFamily: "'Barlow Condensed', sans-serif", fontWeight: 700 }}>BY TYRA</div>
      </div>

      <Ticker th={th} />
    </div>
  );
}
