import { useEffect, useState } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import type { AuthUser } from '../../contexts/authContextValue';

/** 활성 파트너사에 소속된 대표·직원 계정을 위한 포털 로그인 화면이다. */
export const PartnerLogin = () => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { user, loading, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading && user?.userRole === 'PARTNER_OWNER') navigate('/partner', { replace: true });
  }, [loading, navigate, user]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const response = await axios.post<AuthUser>('/api/partner/auth/login', { userEmail: loginId, userPwd: password });
      login(response.data);
      if (response.data.userRole !== 'PARTNER_OWNER') {
        setError('대표 직원 계정으로 로그인해 주세요.');
        return;
      }
      navigate('/partner', { replace: true });
    } catch (loginError) {
      setError(axios.isAxiosError(loginError) ? loginError.response?.data?.detail ?? '로그인에 실패했습니다.' : '로그인에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return <main className="flex min-h-screen flex-grow items-center justify-center bg-slate-950 px-5 py-12 text-white"><section className="w-full max-w-md rounded-[28px] border border-white/10 bg-slate-900 p-7 shadow-2xl sm:p-9"><Link to="/" className="text-sm font-bold text-indigo-300"><i className="fa-solid fa-arrow-left mr-2" />RouteMate</Link><p className="mt-9 text-xs font-bold tracking-[.18em] text-indigo-300">PARTNER PORTAL</p><h1 className="mt-3 text-3xl font-extrabold">파트너사 로그인</h1><p className="mt-3 text-sm leading-6 text-slate-400">관리자가 발급한 대표 직원 계정으로 소속 직원을 관리하세요.</p><form onSubmit={(event) => void submit(event)} className="mt-8 grid gap-4"><label className="grid gap-2 text-xs font-semibold text-slate-400">대표 직원 ID<input type="email" required value={loginId} onChange={(event) => setLoginId(event.target.value)} className="h-12 rounded-xl border border-white/10 bg-slate-950 px-4 text-sm text-white outline-none focus:border-indigo-400" /></label><label className="grid gap-2 text-xs font-semibold text-slate-400">비밀번호<input type="password" required value={password} onChange={(event) => setPassword(event.target.value)} className="h-12 rounded-xl border border-white/10 bg-slate-950 px-4 text-sm text-white outline-none focus:border-indigo-400" /></label>{error ? <p className="rounded-xl bg-rose-500/10 px-3 py-3 text-sm text-rose-200">{error}</p> : null}<button disabled={submitting} className="mt-2 h-12 rounded-xl bg-indigo-500 text-sm font-bold disabled:opacity-50">{submitting ? '로그인 중...' : '파트너 포털 로그인'}</button></form></section></main>;
};
