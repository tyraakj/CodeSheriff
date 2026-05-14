export default function DependencyMap({ method, dark }) {
  const callerFill = dark ? "#0f1a2a" : "#e8edf8";
  const callerStroke = dark ? "#1e3a5f" : "#b0c4de";
  const callerText = dark ? "#748ffc" : "#2c5282";
  const calleeFill = dark ? "#1a0f2a" : "#f3e8ff";
  const calleeStroke = dark ? "#4a1a7a" : "#c084fc";
  const calleeText = dark ? "#b77fda" : "#7e22ce";

  return (
    <div className="dep-map">
      <svg width="100%" viewBox="0 0 620 300">
        {/* Center node */}
        <rect
          x="210"
          y="118"
          width="200"
          height="60"
          rx="3"
          fill="var(--surface-alt)"
          stroke="var(--accent)"
          strokeWidth="1.5"
        />
        <text
          x="310"
          y="145"
          textAnchor="middle"
          fill="var(--accent)"
          fontSize="12"
          fontFamily="Barlow Condensed, sans-serif"
          fontWeight="900"
        >
          {method.name.toUpperCase()}
        </text>
        <text
          x="310"
          y="163"
          textAnchor="middle"
          fill="var(--text-muted)"
          fontSize="10"
          fontFamily="Barlow, sans-serif"
        >
          {method.returnType}
        </text>

        {/* Labels */}
        <text
          x="100"
          y="20"
          textAnchor="middle"
          fill="var(--text-faint)"
          fontSize="9"
          fontFamily="Barlow Condensed, sans-serif"
          fontWeight="700"
        >
          CALLED BY
        </text>
        <text
          x="522"
          y="20"
          textAnchor="middle"
          fill="var(--text-faint)"
          fontSize="9"
          fontFamily="Barlow Condensed, sans-serif"
          fontWeight="700"
        >
          CALLS INTO
        </text>

        {/* Called By */}
        {method.calledBy.map((c, i) => {
          const y = 50 + i * 80;
          return (
            <g key={c}>
              <rect
                x="20"
                y={y}
                width="160"
                height="36"
                rx="3"
                fill={callerFill}
                stroke={callerStroke}
                strokeWidth="0.5"
              />
              <text
                x="100"
                y={y + 21}
                textAnchor="middle"
                fill={callerText}
                fontSize="11"
                fontFamily="Barlow, sans-serif"
              >
                {c}
              </text>
              <line
                x1="180"
                y1={y + 18}
                x2="210"
                y2="148"
                stroke={callerStroke}
                strokeWidth="0.5"
                strokeDasharray="4 3"
              />
            </g>
          );
        })}

        {/* Calls */}
        {method.calls.map((c, i) => {
          const y = 30 + i * 70;
          return (
            <g key={c}>
              <rect
                x="440"
                y={y}
                width="165"
                height="36"
                rx="3"
                fill={calleeFill}
                stroke={calleeStroke}
                strokeWidth="0.5"
              />
              <text
                x="522"
                y={y + 21}
                textAnchor="middle"
                fill={calleeText}
                fontSize="9"
                fontFamily="Barlow, sans-serif"
              >
                {c}
              </text>
              <line
                x1="410"
                y1="148"
                x2="440"
                y2={y + 18}
                stroke={calleeStroke}
                strokeWidth="0.5"
                strokeDasharray="4 3"
              />
            </g>
          );
        })}
      </svg>
    </div>
  );
}
