import React from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Alerts from './pages/Alerts'
import Login from './pages/Login'

const authed = () => !!localStorage.getItem('user')
const router = createBrowserRouter([
  { path: '/login', element: <Login/> },
  { path: '/', element: authed()? <Dashboard/> : <Login/> },
  { path: '/alerts', element: authed()? <Alerts/> : <Login/> },
])
ReactDOM.createRoot(document.getElementById('root')).render(<RouterProvider router={router}/>)
