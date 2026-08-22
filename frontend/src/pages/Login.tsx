import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import type { AuthUser } from '../contexts/authContextValue';
import { useAuth } from '../hooks/useAuth';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await axios.post<AuthUser>('/api/auth/login', {
        userEmail: email,
        userPwd: password,
      });
      login(response.data);
      const requestedPath = (location.state as { from?: string } | null)?.from;
      navigate(requestedPath?.startsWith('/') ? requestedPath : '/');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.detail || '로그인에 실패했습니다. 다시 시도해 주세요.');
      } else {
        setError('로그인에 실패했습니다. 다시 시도해 주세요.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex-grow flex items-center justify-center px-6 relative z-10 py-12">
      <div className="theme-glass-card w-full max-w-md md:p-10 shadow-2xl relative border-white/[0.05]">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-extrabold text-white tracking-tight mb-2">다시 여정을 잇다</h2>
          <p className="text-sm text-gray-400 font-light">전 세계 스마트 동선 플래너, RouteMate 로그인</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label htmlFor="username" className="text-xs font-semibold text-gray-400 uppercase tracking-wider block">이메일 주소</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500">
                <i className="fa-solid fa-envelope text-sm"></i>
              </span>
              <input
                type="email"
                id="username"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="example@routemate.com"
                className="w-full bg-black/40 border border-gray-800 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition duration-300"
              />
            </div>
          </div>

          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <Link to="/password-reset" className="text-xs text-indigo-400 hover:text-indigo-300 transition font-light">비밀번호 재설정</Link>
              <label htmlFor="password" className="text-xs font-semibold text-gray-400 uppercase tracking-wider block">비밀번호</label>
              <a href="#" className="text-xs text-indigo-400 hover:text-indigo-300 transition font-light">비밀번호 재설정</a>
            </div>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500">
                <i className="fa-solid fa-lock text-sm"></i>
              </span>
              <input
                type="password"
                id="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-black/40 border border-gray-800 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-600 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition duration-300"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full theme-btn-primary py-4 mt-2 text-base font-semibold tracking-wide disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? '로그인 중...' : '내 여정 불러오기'}
            <i className="fa-solid fa-right-to-bracket ml-1"></i>
          </button>
          {error && <p role="alert" className="text-center text-sm text-red-300">{error}</p>}
        </form>

        <div className="text-center mt-8 pt-6 border-t border-gray-800/60 text-sm">
          <span className="text-gray-500 font-light">아직 계정이 없으신가요?</span>
          <Link to="/join" className="text-indigo-400 hover:text-indigo-300 font-medium ml-1 transition underline underline-offset-4">
            지금 회원가입하기
          </Link>
        </div>
      </div>
    </main>
  );
};
