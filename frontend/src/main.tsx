import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import App from './App';
import LoginPage from './pages/LoginPage';
import AgendaPage from './pages/AgendaPage';
import BookingPage from './pages/BookingPage';
import RequireAuth from './shared/ui/RequireAuth';
import { AuthProvider } from './features/auth/AuthContext';
import './index.css';

const rootElement = document.getElementById('root');

if (rootElement === null) {
  throw new Error('No se encontró el elemento #root en index.html');
}

createRoot(rootElement).render(
  <StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/agenda"
            element={
              <RequireAuth>
                <AgendaPage />
              </RequireAuth>
            }
          />
          <Route path="/booking/:slug" element={<BookingPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
);