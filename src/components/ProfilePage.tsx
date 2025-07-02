import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

type ProfilePageProps = {
  onLogout: () => void;
};

export default function ProfilePage({ onLogout }: ProfilePageProps) {
  const [firstName, setFirstName] = useState<string>('');
  const [surname, setSurname] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const API = import.meta.env.VITE_API_BASE_URL;

  useEffect(() => {
    (async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        onLogout();
        navigate('/');
        return;
      }

      try {
        const res = await fetch(`${API}/api/users/me`, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
        });
        if (res.status === 401 || res.status === 403) {
          onLogout();
          navigate('/');
          return;
        }
        if (!res.ok) {
          throw new Error(await res.text());
        }
        const data = await res.json();
        setFirstName(data.firstName);
        setSurname(data.surname);
      } catch (err: any) {
        setError(err.message || 'Błąd podczas pobierania profilu');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return <div>Ładowanie…</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="profile-page">
      <h2>Witaj, {firstName} {surname}</h2>
      <button
        onClick={() => {
          onLogout();
          navigate('/');
        }}
      >
        Wyloguj
      </button>
    </div>
  );
}
