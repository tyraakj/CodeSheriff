import { authService } from "../lib/supabase";

const BASE = "http://localhost:8080";

// Helper to get auth headers
async function getAuthHeaders() {
  const token = await authService.getAccessToken();
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

export async function uploadAndParse(file) {
  const form = new FormData();
  form.append("file", file);

  const token = await authService.getAccessToken();
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE}/api/upload`, {
    method: "POST",
    headers,
    body: form,
  });

  if (!res.ok) throw new Error(`Upload failed: ${res.status}`);
  return await res.json();
}

export async function analyzeMethod(
  className,
  methodName,
  methodBody,
  classContext,
) {
  const headers = await getAuthHeaders();
  
  const res = await fetch(`${BASE}/analyze`, {
    method: "POST",
    headers,
    body: JSON.stringify({
      className,
      methodName,
      methodBody,
      allClassContext: classContext,
    }),
  });

  if (!res.ok) throw new Error(`Analysis failed: ${res.status}`);
  return await res.json();
}
