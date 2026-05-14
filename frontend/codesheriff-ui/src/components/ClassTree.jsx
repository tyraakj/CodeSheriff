import { typeColor, getRisk } from "../theme";

export default function ClassTree({
  th,
  analysis,
  expandedClass,
  setExpandedClass,
  selectedMethod,
  setSelectedMethod,
}) {
  if (!analysis) {
    return (
      <div className="tree-wrapper">
        <div className="tree-empty">
          UPLOAD A SPRING BOOT
          <br />
          .ZIP TO BEGIN
        </div>
      </div>
    );
  }

  return (
    <div className="tree-wrapper">
      <div className="tree-heading">CLASS TREE</div>
      {analysis.classes.map((cls) => {
        const tc = typeColor(cls.type, th);
        const isExp = expandedClass === cls.id;

        return (
          <div key={cls.id}>
            <div
              className="tree-class-row"
              onClick={() => setExpandedClass(isExp ? null : cls.id)}
              style={{
                borderLeft: `2px solid ${isExp ? tc : "transparent"}`,
                background: isExp ? "var(--surface-alt)" : "transparent",
              }}
            >
              <span
                className="tree-class-arrow"
                style={{ color: isExp ? tc : "var(--text-faint)" }}
              >
                {isExp ? "▾" : "▸"}
              </span>
              <span
                className="tree-class-name"
                style={{ color: isExp ? "var(--text)" : "var(--text-muted)" }}
              >
                {cls.name.toUpperCase()}
              </span>
              <span
                className="tree-type-tag"
                style={{ border: `1px solid ${tc}66`, color: tc }}
              >
                {cls.type.toUpperCase()}
              </span>
            </div>

            {isExp &&
              cls.methods.map((m) => {
                const [, rc] = getRisk(m);
                const isSel = selectedMethod?.id === m.id;

                return (
                  <div
                    key={m.id}
                    className="tree-method-row"
                    onClick={() => setSelectedMethod(m)}
                    style={{
                      background: isSel
                        ? "var(--sidebar-active)"
                        : "transparent",
                      borderLeft: `2px solid ${isSel ? "var(--accent)" : "transparent"}`,
                    }}
                  >
                    <span
                      className="tree-risk-dot"
                      style={{ background: rc }}
                    />
                    <span
                      className="tree-method-name"
                      style={{
                        color: isSel ? "var(--text)" : "var(--text-muted)",
                      }}
                    >
                      {m.name}()
                    </span>
                    {!m.hasTests && (
                      <span className="tree-no-tests">NO TESTS</span>
                    )}
                  </div>
                );
              })}
          </div>
        );
      })}
    </div>
  );
}
