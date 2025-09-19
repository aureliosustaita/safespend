export default function TxnTable({rows=[]}){
  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead><tr><th>Time</th><th>User</th><th>Merchant</th><th>Category</th><th className="text-right">Amount</th></tr></thead>
        <tbody>{rows.map(r=>(
          <tr key={r.id} className="border-b">
            <td>{new Date(r.timestamp).toLocaleString()}</td>
            <td>{r.userId}</td><td>{r.merchant}</td><td>{r.category}</td>
            <td className="text-right">${r.amount}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}
