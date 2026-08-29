import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import type { AuthUser } from '../../contexts/authContextValue';
import { useAuth } from '../../hooks/useAuth';
import { isStaffUser } from '../../features/admin/permissions';

/** 관리자 역할 계정만 `/api/admin/auth/login`으로 인증하는 전용 로그인 화면이다. */
export const AdminLogin = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { user, loading, login } = useAuth();

  useEffect(() => {
    if (!loading && isStaffUser(user)) {
      navigate('/admin', { replace: true });
    }
  }, [loading, navigate, user]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const response = await axios.post<AuthUser>('/api/admin/auth/login', {
        userEmail: email,
        userPwd: password,
      });
      login(response.data);
      navigate('/admin', { replace: true });
    } catch (loginError) {
      if (axios.isAxiosError(loginError)) {
        setError(loginError.response?.data?.detail || '관리자 로그인에 실패했습니다.');
      } else {
        setError('관리자 로그인에 실패했습니다.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="relative flex min-h-screen flex-grow items-center justify-center overflow-hidden px-5 py-12">
      <div className="pointer-events-none absolute -left-32 top-[-180px] h-[520px] w-[520px] rounded-full bg-indigo-600/20 blur-[150px]" />
      <div className="pointer-events-none absolute -bottom-48 right-[-120px] h-[520px] w-[520px] rounded-full bg-cyan-500/10 blur-[150px]" />

      <section className="relative grid w-full max-w-5xl overflow-hidden rounded-[32px] border border-white/10 bg-slate-950/80 shadow-[0_40px_120px_rgba(0,0,0,0.45)] backdrop-blur-xl lg:grid-cols-[1.05fr_0.95fr]">
        <div className="hidden min-h-[620px] flex-col justify-between border-r border-white/10 bg-gradient-to-br from-indigo-600/20 via-slate-950 to-cyan-500/10 p-12 lg:flex">
          <div>
            <Link to="/" className="inline-flex items-center gap-3 text-xl font-extrabold text-white">
              <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-indigo-500 text-white shadow-lg shadow-indigo-500/25">
                <i className="fa-solid fa-map-location-dot" />
              </span>
              RouteMate Admin
            </Link>
          </div>
          <div>
            <span className="inline-flex items-center gap-2 rounded-full border border-indigo-300/20 bg-indigo-400/10 px-3 py-1.5 text-xs font-bold tracking-[0.18em] text-indigo-200">
              CONTROL CENTER
            </span>
            <h1 className="mt-6 text-4xl font-extrabold leading-tight text-white">
              여행 서비스 운영을<br />한곳에서 관리하세요.
            </h1>
            <p className="mt-5 max-w-md text-sm leading-7 text-slate-400">
              회원, 공개 여행 일정, 추천 여행지를 안전하게 관리하기 위한 관리자 전용 공간입니다.
            </p>
          </div>
          <p className="text-xs text-slate-600">Authorized personnel only · RouteMate</p>
        </div>

        <div className="flex min-h-[620px] flex-col justify-center p-7 sm:p-12">
          <div className="mb-9 lg:hidden">
            <Link to="/" className="inline-flex items-center gap-2 text-lg font-extrabold text-white">
              <i className="fa-solid fa-map-location-dot text-indigo-400" /> RouteMate Admin
            </Link>
          </div>

          <div>
            <p className="text-xs font-bold tracking-[0.2em] text-indigo-300">ADMIN SIGN IN</p>
            <h2 className="mt-3 text-3xl font-extrabold tracking-tight text-white">관리자 로그인</h2>
            <p className="mt-3 text-sm leading-6 text-slate-500">관리 권한이 등록된 계정으로 로그인해 주세요.</p>
          </div>

          <form onSubmit={handleSubmit} className="mt-9 space-y-5">
            <div>
              <label htmlFor="admin-email" className="mb-2 block text-xs font-semibold text-slate-400">관리자 이메일</label>
              <div className="relative">
                <i className="fa-solid fa-envelope absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-600" />
                <input
                  id="admin-email"
                  type="email"
                  autoComplete="username"
                  required
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="admin@routemate.com"
                  className="w-full rounded-2xl border border-white/10 bg-white/[0.035] py-4 pl-11 pr-4 text-sm text-white outline-none transition placeholder:text-slate-700 focus:border-indigo-400/60 focus:bg-white/[0.055] focus:ring-2 focus:ring-indigo-500/10"
                />
              </div>
            </div>

            <div>
              <label htmlFor="admin-password" className="mb-2 block text-xs font-semibold text-slate-400">비밀번호</label>
              <div className="relative">
                <i className="fa-solid fa-lock absolute left-4 top-1/2 -translate-y-1/2 text-sm text-slate-600" />
                <input
                  id="admin-password"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="비밀번호를 입력하세요"
                  className="w-full rounded-2xl border border-white/10 bg-white/[0.035] py-4 pl-11 pr-4 text-sm text-white outline-none transition placeholder:text-slate-700 focus:border-indigo-400/60 focus:bg-white/[0.055] focus:ring-2 focus:ring-indigo-500/10"
                />
              </div>
            </div>

            {error ? (
              <p role="alert" className="rounded-2xl border border-rose-400/15 bg-rose-400/10 px-4 py-3 text-sm text-rose-200">
                <i className="fa-solid fa-circle-exclamation mr-2" />{error}
              </p>
            ) : null}

            <button
              type="submit"
              disabled={submitting}
              className="flex w-full items-center justify-center gap-2 rounded-2xl bg-indigo-500 px-5 py-4 text-sm font-bold text-white shadow-lg shadow-indigo-500/20 transition hover:bg-indigo-400 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {submitting ? <i className="fa-solid fa-spinner fa-spin" /> : <i className="fa-solid fa-shield-halved" />}
              {submitting ? '권한 확인 중...' : '관리자 로그인'}
            </button>
          </form>

          <div className="mt-8 flex items-center justify-between border-t border-white/10 pt-6 text-xs text-slate-600">
            <span><i className="fa-solid fa-lock mr-1.5" />보안 접속</span>
            <div className="flex items-center gap-4"><Link to="/password-reset" className="font-semibold text-indigo-300 transition hover:text-indigo-200">비밀번호 재설정</Link><Link to="/" className="font-semibold text-slate-400 transition hover:text-white">RouteMate 둘러보기</Link></div>
          </div>
        </div>
      </section>
    </main>
  );
};
