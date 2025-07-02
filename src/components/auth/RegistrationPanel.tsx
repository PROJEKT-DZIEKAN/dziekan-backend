import { useState } from 'react';
import '../../assets/LoginPanel.css';

type RegistrationPanelProps = {
  onRegister: (creds: { firstName: string; surname: string }) => Promise<number>;
  switchToLogin: () => void;
};

export default function RegistrationPanel({ onRegister, switchToLogin }: RegistrationPanelProps) {
  const [firstName, setFirstName] = useState('');
  const [surname, setSurname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [newUserId, setNewUserId] = useState<number | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const id = await onRegister({ firstName, surname });
      setNewUserId(id);
    } catch (err: any) {
      setError(err.message || 'Coś poszło nie tak przy rejestracji');
    }
  };

  if (newUserId !== null) {
    return (
      <div className="login-form">
        <h2 className="login-title">Zarejestrowano pomyślnie!</h2>
        <p>Twoje User ID to: <strong>{newUserId}</strong></p>
        <button className="login-button" onClick={switchToLogin}>
          Przejdź do logowania
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="login-form">
      <h2 className="login-title">Rejestracja</h2>
      {error && <div className="login-error">{error}</div>}
      <div className="login-group">
        <label>Imię</label>
        <input
          type="text"
          value={firstName}
          onChange={e => setFirstName(e.target.value)}
          required
        />
      </div>
      <div className="login-group">
        <label>Nazwisko</label>
        <input
          type="text"
          value={surname}
          onChange={e => setSurname(e.target.value)}
          required
        />
      </div>
      <button className="login-button" type="submit">
        Zarejestruj się
      </button>
    </form>
  );
}
