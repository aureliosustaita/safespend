import { useEffect, useRef, useState } from "react";
import { api, clearToken } from "../lib/api";
import TxnTable from "../components/TxnTable";

export default function Dashboard({ onLogout }) {
  const [tx, setTx] = useState([]);
  const [err, setErr] = useState("");
  const timer = useRef();

  async function load() {
    try {
      setErr("");
      const data = await api("/api/transactions");
      setTx(data);
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
    timer.current = setInterval(load, 4000);
    return () => clearInterval(timer.current);
  }, []);

  async function seed() {
    const cats = ["DINING", "GROCERIES", "TRAVEL"];
    const merch = ["UberEats", "Ralphs", "Delta", "Chipotle", "Shell"];
    const body = {
      userId: "u1",
      category: cats[(Math.random() * cats.length) | 0],
      merchant: merch[(Math.random() * merch.length) | 0],
      amount: +(Math.random() * 80 + 10).toFixed(2),
    };
    await api("/api/transactions", { method: "POST", body });
    await load();
  }

  async function big() {
    const body = { userId: "u1", merchant: "Chipotle", category: "DINING", amount: 812.45 };
    await api("/api/transactions", { method: "POST", body });
    await load();
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">SafeSpend — Dashboard</h1>
        <div className="flex items-center gap-3">
          {onLogout && (
            <button onClick={onLogout} className="underline">Logout</button>
          )}
          <a href="/alerts" className="underline">Alerts</a>
        </div>
      </div>

      {err && (
        <div className="p-3 rounded-xl border bg-yellow-50">
          Error: {err}
        </div>
      )}

      <div className="flex gap-2">
        <button onClick={seed} className="px-3 py-2 rounded-xl border">Seed a txn</button>
        <button onClick={big} className="px-3 py-2 rounded-xl border">Trigger outlier</button>
      </div>

      <TxnTable rows={tx} />
    </div>
  );
}
