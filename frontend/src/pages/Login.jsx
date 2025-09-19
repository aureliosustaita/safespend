export default function Login(){
  function submit(e){ e.preventDefault();
    const f=new FormData(e.target);
    localStorage.setItem('user', f.get('u'));
    localStorage.setItem('pass', f.get('p'));
    location.href='/';
  }
  return (
    <form onSubmit={submit} className="p-6 max-w-sm mx-auto space-y-3">
      <h1 className="text-xl font-bold">Sign in</h1>
      <input name="u" className="border p-2 w-full rounded-xl" defaultValue="user" />
      <input name="p" className="border p-2 w-full rounded-xl" defaultValue="user123" />
      <button className="px-4 py-2 rounded-2xl bg-black text-white">Continue</button>
    </form>
  );
}
