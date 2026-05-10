export default function Dashboard({ onBack, dark, onToggle, th }) {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState("idle");
  const [analysis, setAnalysis] = useState(null);
  const [selectedMethod, setSelectedMethod] = useState(null);
  const [expandedClass, setExpandedClass] = useState("c1");

  
  const inferType = (annotations = []) => {
    if (annotations.includes('Service') || annotations.includes('Component')) return 'Service';
    if (annotations.includes('RestController') || annotations.includes('Controller')) return 'Controller';
    if (annotations.includes('Repository')) return 'Repository';
    return 'Service';
  };

  const handleFile = (f) => {
    if (f) { setFile(f); setStatus("ready"); }
  };

  
  const runAnalysis = async () => {
    setStatus("loading");
    try {
      const classTree = await uploadAndParse(file);
      const shaped = {
        projectName: file.name.replace('.zip', ''),
        totalClasses: classTree.length,
        totalMethods: classTree.reduce((sum, c) => sum + c.methods.length, 0),
        classes: classTree.map((cls, ci) => ({
          id: `c${ci}`,
          name: cls.className,
          type: inferType(cls.annotations),   
          methods: cls.methods.map((m, mi) => ({
            id: `m${ci}_${mi}`,
            name: m.name,
            params: m.params ? m.params.split(',').map(p => p.trim()) : [],
            returnType: m.returnType,
            lineCount: m.body ? m.body.split('\n').length : 0,
            hasTests: false,
            calledBy: [],
            calls: [],
            annotations: m.annotations || [],
            body: m.body,
            bob: null,
          }))
        }))
      };
      setAnalysis(shaped);
      setStatus("success");
      setSelectedMethod(shaped.classes[0]?.methods[0] || null);
    } catch (err) {
      console.error(err);
      setStatus("error");
    }
  }; 

  const handleMethodSelect = async (method) => {
    if (method.bob) { setSelectedMethod(method); return; }
    setSelectedMethod({ ...method, bobLoading: true });
    try {
      const parentClass = analysis.classes.find(cls =>
        cls.methods.some(m => m.id === method.id)
      );
      const classContext = parentClass.methods
        .filter(m => m.id !== method.id)
        .map(m => m.body || '')
        .join('\n\n');
      const bobResult = await analyzeMethod(
        parentClass.name, method.name, method.body || '', classContext
      );
      const updatedMethod = { ...method, bob: bobResult, bobLoading: false };
      setAnalysis(prev => ({
        ...prev,
        classes: prev.classes.map(cls => ({
          ...cls,
          methods: cls.methods.map(m => m.id === method.id ? updatedMethod : m)
        }))
      }));
