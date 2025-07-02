import { useState } from 'react';
import '../../assets/LoginPanel.css';

type LoginPanelProps = {
  onLogin: (creds: { userId: string }) => Promise<void>;
};

export default function LoginPanel({ onLogin }: LoginPanelProps) {
  const [userId, setUserId] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await onLogin({ userId });
    } catch (err: any) {
      setError(err.message || 'Coś poszło nie tak przy logowaniu');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="login-form">
      <h2 className="login-title">Logowanie (po ID)</h2>
      {error && <div className="login-error">{error}</div>}
      <div className="login-group">
        <label>User ID</label>
        <input
          type="text"
          value={userId}
          onChange={e => setUserId(e.target.value)}
          required
        />
      </div>
      <button className="login-button" type="submit">
        Zaloguj
      </button>
    </form>
  );
}
