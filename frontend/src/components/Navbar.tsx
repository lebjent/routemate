import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const Navbar = () => {
  const { user, loading, logout } = useAuth();

  return (
    <nav className="w-full max-w-7xl mx-auto px-6 py-5 flex justify-between items-center z-50 relative">
      <Link to="/" className="text-2xl font-bold tracking-tight flex items-center gap-2 cursor-pointer text-white">
        <span className="text-brand-primary"><i className="fa-solid fa-map-location-dot"></i></span>
        <span>Route<span className="text-indigo-400">Mate</span></span>
      </Link>
      <div className="flex items-center gap-6">
        <Link to="/" className="text-sm text-gray-400 hover:text-white transition hidden md:block">탐색하기</Link>
        <Link to="/lotto" className="text-sm text-gray-400 hover:text-white transition hidden md:block">행운의 로또</Link>
        {!loading && (user ? (
          <>
            <span className="text-sm font-medium text-indigo-200">{user.userNicknm}님</span>
            <button
              type="button"
              onClick={() => void logout()}
              className="text-sm text-gray-300 hover:text-indigo-400 font-medium transition"
            >
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="text-sm text-gray-300 hover:text-indigo-400 font-medium transition">로그인</Link>
            <Link to="/join" className="text-sm bg-brand-primary hover:bg-indigo-500 text-white px-5 py-2.5 rounded-xl font-semibold shadow-lg shadow-indigo-600/20 transition duration-300">
              시작하기
            </Link>
          </>
        ))}
      </div>
    </nav>
  );
};
