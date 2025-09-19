import {useEffect,useState} from 'react';
import {api} from '../lib/api';

export default function Alerts(){
  const [rows,setRows]=useState([]);
  useEffect(()=>{ load(); const id=setInterval(load, 5000); return ()=>clearInterval(id);},[]);
  async function load(){ setRows(await api('/api/alerts')); }
  const fmtZ = (a) => a.zScore?.toFixed ? a.zScore.toFixed(2) : (a.zscore?.toFixed ? a.zscore.toFixed(2) : '—');
  return (
    <div className="p-6 space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">Alerts</h2>
        <a href="/" className="underline">Dashboard</a>
      </div>
      <ul className="space-y-2">
        {rows.map(a=>(
          <li key={a.id} className="p-3 rounded-xl bg-red-50 border">
            <div className="font-semibold">{a.category} • {a.reason}</div>
            <div className="text-xs opacity-70">z≈{fmtZ(a)} • amount ${a.amount} • median ${a.median} • MAD {a.mad}</div>
          </li>
        ))}
      </ul>
    </div>
  );
}
