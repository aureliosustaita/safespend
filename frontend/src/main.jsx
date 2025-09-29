// frontend/src/main.jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Alerts from './pages/Alerts'
import Login from './pages/Login'
import { getToken, clearToken } from './lib/api' // adjust path if needed

const authed = () => !!getToken();

const logoutAndGoLogin = () => {
  clearToken();
  localStorage.removeItem('user'); // legacy UI key, not needed for auth
  // client-side redirect to /login without a full page reload
  window.history.pushState({}, '', '/login');
  window.dispatchEvent(new PopStateEvent('popstate'));
};

const router = createBrowserRouter([
  { path: '/login', element: <Login/> },
  { path: '/', element: authed() ? <Dashboard onLogout={logoutAndGoLogin}/> : <Login/> },
  { path: '/alerts', element: authed() ? <Alerts onLogout={logoutAndGoLogin}/> : <Login/> },
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router}/>
  </React.StrictMode>
);
