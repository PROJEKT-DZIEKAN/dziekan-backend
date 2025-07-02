// src/components/AuthPage.tsx
import { useState } from 'react';
import LoginPanel from './auth/LoginPanel';
import RegistrationPanel from './auth/RegistrationPanel';

type AuthPageProps = {
  onLogin: (creds: { userId: string }) => Promise<void>;
  onRegister: (creds: { firstName: string; surname: string }) => Promise<number>;
};

export default function AuthPage({ onLogin, onRegister }: AuthPageProps) {
  const [isLoginMode, setIsLoginMode] = useState(true);

  return (
    <div className="login-container">
      <div className="login-wrapper">
        {isLoginMode ? (
          <LoginPanel onLogin={onLogin} />
        ) : (
          <RegistrationPanel
            onRegister={onRegister}
            switchToLogin={() => setIsLoginMode(true)}
          />
        )}
        <div className="auth-switch">
          {isLoginMode ? 'Nie masz konta?' : 'Masz już konto?'}{' '}
          <button
            className="auth-switch-btn"
            onClick={() => setIsLoginMode(!isLoginMode)}
          >
            {isLoginMode ? 'Zarejestruj się' : 'Zaloguj się'}
          </button>
        </div>
      </div>
    </div>
  );
}
