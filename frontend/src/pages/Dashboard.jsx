import {useEffect,useState} from 'react';
import {api} from '../lib/api';
import TxnTable from '../components/TxnTable';

export default function Dashboard(){
  const [tx,setTx]=useState([]);
  useEffect(()=>{ load(); const id=setInterval(load, 4000); return ()=>clearInterval(id);},[]);
  async function load(){ setTx(await api('/api/transactions')); }
  async function seed(){
    const cats=['DINING','GROCERIES','TRAVEL'];
    const merch=['UberEats','Ralphs','Delta','Chipotle','Shell'];
    const body={userId:'u1', category: cats[Math.random()*cats.length|0], merchant: merch[Math.random()*merch.length|0], amount: +(Math.random()*80+10).toFixed(2)};
    await api('/api/transactions',{method:'POST', body: JSON.stringify(body)});
  }
  async function big(){
    const body={userId:'u1', merchant:'Chipotle', category:'DINING', amount: 812.45};
    await api('/api/transactions',{method:'POST', body: JSON.stringify(body)});
  }
  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">SafeSpend — Dashboard</h1>
        <a href="/alerts" className="underline">Alerts</a>
      </div>
      <div className="flex gap-2">
        <button onClick={seed} className="px-3 py-2 rounded-xl border">Seed a txn</button>
        <button onClick={big} className="px-3 py-2 rounded-xl border">Trigger outlier</button>
      </div>
      <TxnTable rows={tx}/>
    </div>
  );
}
