import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

/** 개발 중 부수 효과를 점검하는 StrictMode로 React 애플리케이션을 DOM에 마운트한다. */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
