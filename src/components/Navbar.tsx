import { Link } from 'react-router-dom';
import { User, Home, MessageSquare } from 'lucide-react';
import '../assets/NavBar.css';

interface NavBarProps {
  onLogout: () => void;
}

export default function NavBar({ onLogout }: NavBarProps) {
  return (
    <nav className="navbar">
      <ul className="navbar-list">
        <li className="navbar-item">
          <Link to="/" className="navbar-link">
            <Home className="navbar-icon" /> Home
          </Link>
        </li>
        <li className="navbar-item">
          <Link to="/profile" className="navbar-link">
            <User className="navbar-icon" /> Profil
          </Link>
        </li>
        <li className="navbar-item">
          <Link to="/chats" className="navbar-link">
            <MessageSquare className="navbar-icon" /> Chaty
          </Link>
        </li>
        <li className="navbar-item">
          <button onClick={onLogout} className="navbar-link navbar-logout">
            Wyloguj
          </button>
        </li>
      </ul>
    </nav>
  );
}
