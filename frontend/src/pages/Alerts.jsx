import { useEffect, useRef, useState } from "react";
import { api, clearToken } from "../lib/api";

export default function Alerts({ onLogout }) {
  const [rows, setRows] = useState([]);
  const [err, setErr] = useState("");
  const timer = useRef();

  const fmtZ = (a) =>
    a.zScore?.toFixed ? a.zScore.toFixed(2)
    : a.zscore?.toFixed ? a.zscore.toFixed(2)
    : "—";

  async function load() {
    try {
      setErr("");
      const data = await api("/api/alerts");
      setRows(data);
    } catch (e) {
      const msg = String(e?.message || e);
      setErr(msg);
      if (msg === "unauthorized" || msg === "forbidden") {
        clearToken();
        onLogout?.();
        return; // no hard reload
      }
      
    }
  }

  useEffect(() => {
    load();
    timer.current = setInterval(load, 5000);
    return () => clearInterval(timer.current);
  }, []);

  return (
    <div className="p-6 space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">Alerts</h2>
        <a href="/" className="underline">Dashboard</a>
      </div>

      {err && (
        <div className="p-3 rounded-xl border bg-yellow-50">
          Error: {err} <button className="underline" onClick={load}>Retry</button>
        </div>
      )}

      <ul className="space-y-2">
        {rows.map(a => (
          <li key={a.id} className="p-3 rounded-xl bg-red-50 border">
            <div className="font-semibold">{a.category} • {a.reason}</div>
            <div className="text-xs opacity-70">
              z≈{fmtZ(a)} • amount ${a.amount} • median ${a.median} • MAD {a.mad}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
