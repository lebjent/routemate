import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Simulate login for this prototype stage
    alert('로그인 성공! 환영합니다.');
    navigate('/');
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

          <button type="submit" className="w-full theme-btn-primary py-4 mt-2 text-base font-semibold tracking-wide">
            내 여정 불러오기
            <i className="fa-solid fa-right-to-bracket ml-1"></i>
          </button>
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
