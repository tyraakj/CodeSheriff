import { useState } from "react";
import ThemeToggle from "../components/ThemeToggle";
import UploadZone from "../components/UploadZone";
import ClassTree from "../components/ClassTree";
import MethodPanel from "../components/MethodPanel";
import Ticker from "../components/Ticker";
import { uploadAndParse, analyzeMethod } from "../api/api";

export default function Dashboard({ onBack, dark, onToggle, th }) {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [analysis, setAnalysis] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState(null);
  const [expandedClass, setExpandedClass] = useState("c1");

  const inferType = (annotations = []) => {
    if (annotations.includes("Service") || annotations.includes("Component"))
      return "Service";
    if (
      annotations.includes("RestController") ||
      annotations.includes("Controller")
    )
      return "Controller";
    if (annotations.includes("Repository")) return "Repository";
    if (annotations.includes("Configuration")) return "Configuration";
    if (annotations.includes("Aspect")) return "Aspect";

    return "Service";
  };

  const handleFile = (f) => {
    if (f) {
      setFile(f);
      setStatus("ready");
    }
  };

  const runAnalysis = async () => {
    setStatus("loading");
    try {
      const classTree = await uploadAndParse(file);

      const shaped = {
        projectName: file.name.replace(".zip", ""),
        totalClasses: classTree.length,
        totalMethods: classTree.reduce((sum, c) => sum + c.methods.length, 0),
        classes: classTree.map((cls, ci) => ({
          id: `c${ci}`,
          name: cls.className,
          type: inferType(cls.annotations),
          methods: cls.methods.map((m, mi) => ({
            id: `m${ci}_${mi}`,
            name: m.name,
            params: m.params ? m.params.split(",").map((p) => p.trim()) : [],
            returnType: m.returnType,
            lineCount: m.body ? m.body.split("\n").length : 0,
            hasTests: false,
            calledBy: [],
            calls: [],
            annotations: m.annotations || [],
            body: m.body,
            bob: null,
          })),
        })),
      };

      setAnalysis(shaped);
      setStatus("success");
      setSelectedMethod(shaped.classes[0]?.methods[0] || null);
    } catch (err) {
      console.error("Upload failed:", err);
      setStatus("error");
    }
  };

  const handleMethodSelect = async (method) => {
    if (method.bob) {
      setSelectedMethod(method);
      return;
    }

    setSelectedMethod({ ...method, bobLoading: true });

    try {
      const parentClass = analysis.classes.find((cls) =>
        cls.methods.some((m) => m.id === method.id),
      );

      if (!parentClass) {
        console.error("Parent class not found for method:", method.name);
        setSelectedMethod({ ...method, bobLoading: false });
        return;
      }

      const classContext = parentClass.methods
        .filter((m) => m.id !== method.id)
        .map((m) => m.body || "")
        .join("\n\n");

      const bobResult = await analyzeMethod(
        parentClass.name,
        method.name,
        method.body || "",
        classContext,
      );

      const updatedMethod = { ...method, bob: bobResult, bobLoading: false };

      setAnalysis((prev) => ({
        ...prev,
        classes: prev.classes.map((cls) => ({
          ...cls,
          methods: cls.methods.map((m) =>
            m.id === method.id ? updatedMethod : m,
          ),
        })),
      }));

      setSelectedMethod(updatedMethod);
    } catch (err) {
      console.error("Bob analysis failed:", err);
      setSelectedMethod((prev) => ({
        ...prev,
        bobLoading: false,
        bob: null,
      }));
    }
  };

  return (
    <div className={`dashboard ${dark ? "theme-dark" : "theme-light"}`}>
      <div className="dashboard-topbar">
        <div className="dashboard-topbar-left">
          <button className="btn-back" onClick={onBack}>
            ← BACK
          </button>

          <span className="dashboard-logo">CODESHERIFF</span>

          {analysis && (
            <span className="dashboard-meta">
              / {analysis.projectName.toUpperCase()} · {analysis.totalClasses}{" "}
              CLASSES · {analysis.totalMethods} METHODS
            </span>
          )}
        </div>

        <div className="dashboard-topbar-right">
          <span className="protected-badge">🛡 PROTECTED</span>
          <span className="powered-by">POWERED BY IBM BOB</span>

          <ThemeToggle dark={dark} onToggle={onToggle} th={th} />
        </div>
      </div>

      <div className="dashboard-body">
        <div className="sidebar">
          <UploadZone
            th={th}
            file={file}
            status={status}
            onFile={handleFile}
            onAnalyze={runAnalysis}
          />

          <ClassTree
            th={th}
            analysis={analysis}
            expandedClass={expandedClass}
            setExpandedClass={setExpandedClass}
            selectedMethod={selectedMethod}
            setSelectedMethod={handleMethodSelect}
          />
        </div>

        <div className="main-panel">
          {!selectedMethod && status !== "loading" && (
            <div className="empty-state">
              <div className="empty-state-logo">CS</div>
              <div className="empty-state-text">
                {status === "idle"
                  ? "UPLOAD A CODEBASE TO INVESTIGATE"
                  : status === "error"
                    ? "SOMETHING WENT WRONG — CHECK CONSOLE AND TRY AGAIN"
                    : "SELECT A METHOD FROM THE TREE"}
              </div>
            </div>
          )}

          {status === "loading" && (
            <div className="loading-state">
              <div className="loading-title">
                IBM BOB IS READING YOUR CODEBASE
              </div>
              <div className="loading-subtitle">
                PARSING CLASSES · MAPPING DEPS · DETECTING INTENT...
              </div>
            </div>
          )}

          {selectedMethod && status === "success" && (
            <MethodPanel th={th} method={selectedMethod} dark={dark} />
          )}
        </div>
      </div>
    </div>
  );
}
