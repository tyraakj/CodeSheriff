import { useRef } from "react";

export default function UploadZone({ file, status, onFile, onAnalyze }) {
  const fileRef = useRef();

  return (
    <div className="upload-zone-wrapper">
      {(status === "idle" || status === "ready") && (
        <div
          className="upload-dropzone"
          onClick={() => fileRef.current.click()}
          onDrop={(e) => {
            e.preventDefault();
            onFile(e.dataTransfer.files[0]);
          }}
          onDragOver={(e) => e.preventDefault()}
        >
          <input
            ref={fileRef}
            type="file"
            accept=".zip"
            style={{ display: "none" }}
            onChange={(e) => onFile(e.target.files[0])}
          />
          <div
            className="upload-icon"
            style={{ color: file ? "var(--accent)" : "var(--text-faint)" }}
          >
            ◈
          </div>
          <div
            className="upload-label"
            style={{ color: file ? "var(--accent)" : "var(--text-muted)" }}
          >
            {file ? file.name.toUpperCase().slice(0, 22) : "DROP .ZIP HERE"}
          </div>
        </div>
      )}

      {status === "ready" && (
        <button className="btn-investigate" onClick={onAnalyze}>
          INVESTIGATE →
        </button>
      )}

      {status === "loading" && (
        <div>
          <div className="loading-label">BOB IS READING...</div>
          <div className="progress-bar-bg">
            <div className="progress-bar-fill" />
          </div>
        </div>
      )}
    </div>
  );
}
