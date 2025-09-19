const base = import.meta.env.VITE_API_BASE || "http://localhost:8080";
const auth = () => btoa(`${localStorage.getItem('user')||'user'}:${localStorage.getItem('pass')||'user123'}`);
export async function api(path, opts = {}) {
  const res = await fetch(`${base}${path}`, {
    headers: { 'Authorization': `Basic ${auth()}`, 'Content-Type': 'application/json' },
    ...opts,
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}