const BASE = "http://localhost:8080";

export async function uploadAndParse(file) {
  const form = new FormData();
  form.append("file", file);

  const res = await fetch(`${BASE}/upload`, {
    method: "POST",
    body: form,
  });

  if (res.ok) throw new Error(`Upload failed: ${res.status}`);
  return await res.json();
}

export async function analyzeMethod(
  className,
  methodName,
  methodBody,
  classContext,
) {
  const res = await fetch(`${BASE}/analyze`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      className,
      methodName,
      methodBody,
      allClassContext: classContext,
    }),
  });

  if (res.ok) throw new Error(`Analysis failed: ${res.status}`);
  return await res.json();
}
