import { useState } from "react";
import ThemeToggle from "../components/ThemeToggle";
import UploadZone from "../components/UploadZone";
import ClassTree from "../components/ClassTree";
import MethodPanel from "../components/MethodPanel";
import { uploadAndParse, analyzeMethod } from "../api/api";

export default function Dashboard({ onBack, dark, onToggle, th }) {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [analysis, setAnalysis] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState(null);
  const [expandedClass, setExpandedClass] = useState("c1");

  const handleFile = async (f) => {
    if (f) {
      setFile(f);
      setStatus("ready");
    }
  };

  const inferText = (annotations = []) => {
    if (annotations.includes("Service"))
      return "This class is likely a Service , responsible for handling business logic or HTTP requests.";
    if (annotations.includes("Repository"))
      return "This class is likely a Repository, responsible for data access and database interactions.";
    if (
      annotations.includes("Controller") ||
      annotations.includes("RestController")
    )
      return "This class is likely a Controller, responsible for handling HTTP requests and responses.";
    if (annotations.includes("Model"))
      return "This class is likely a Model, representing data structures and business entities.";
    if (annotations.includes("Configuration"))
      return "This class is likely a Configuration, responsible for defining beans and application settings.";
    if (annotations.includes("Aspect"))
      return "This class is likely an Aspect, responsible for cross-cutting concerns like logging or security.";
    if (annotations.includes("Component"))
      return "This class is likely a Component, a generic stereotype for any Spring-managed component.";
    if (annotations.includes("Entity"))
      return "This class is likely an Entity, representing a persistent data model in JPA.";
    if (annotations.includes("DTO"))
      return "This class is likely a DTO (Data Transfer Object), used for transferring data between layers.";
    return "This class does not have recognizable Spring annotations, so its role is unclear. It may serve a custom purpose or be a utility class.";
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
      console.error(err);
      setStatus("error");
    }
  };
}
