// src/api.js
const base = import.meta.env.VITE_API_BASE || ""; // "" -> use Vite proxy

export const getToken = () => localStorage.getItem("token") || "";
export const setToken = (t) => localStorage.setItem("token", t);
export const clearToken = () => localStorage.removeItem("token");

// one-time auth failure callback
let onAuthFailure = () => {};
let authHandled = false;
export const setOnAuthFailure = (fn) => { onAuthFailure = fn; };

/** Login → stores JWT */
export async function login(username, password) {
  const res = await fetch(`${base}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    throw new Error(await res.text().catch(() => "login failed"));
  }
  const { token } = await res.json();
  setToken(token);
  localStorage.setItem("user", username);
  return token;
}

/** Generic API with one-time 401/403 handling */
export async function api(path, { method = "GET", body, headers = {}, token } = {}) {
  const t = token ?? getToken();
  const merged = { "Content-Type": "application/json", ...headers };
  if (t) merged.Authorization = `Bearer ${t}`;

  const res = await fetch(`${base}${path}`, {
    method,
    headers: merged,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    credentials: "omit",
  });

  if (res.status === 401 || res.status === 403) {
    if (!authHandled) {
      authHandled = true;       // stop loops
      clearToken();
      try { onAuthFailure(res.status); } catch {}
      setTimeout(() => (authHandled = false), 1500);
    }
    throw new Error(res.status === 401 ? "unauthorized" : "forbidden");
  }

  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(`${res.status} ${res.statusText}${txt ? ` — ${txt}` : ""}`);
  }

  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.text();
}
