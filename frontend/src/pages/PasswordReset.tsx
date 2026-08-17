import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

export const PasswordReset = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault(); setError(null); setMessage(null);
    if (password !== confirmPassword) { setError('새 비밀번호가 서로 일치하지 않습니다.'); return; }
    setLoading(true);
    try {
      await axios.post('/api/auth/password-reset', { userEmail: email, newPassword: password });
      setMessage('비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.');
      setTimeout(() => navigate('/login'), 900);
    } catch (resetError) {
      const serverMessage = axios.isAxiosError(resetError) ? resetError.response?.data?.detail || resetError.response?.data?.message : null;
      setError(serverMessage || '비밀번호 재설정에 실패했습니다.');
    } finally { setLoading(false); }
  };

  return <main className="flex flex-grow items-center justify-center px-6 py-12"><div className="theme-glass-card w-full max-w-md p-6 md:p-10">
    <h1 className="text-3xl font-extrabold text-white">비밀번호 재설정</h1>
    <p className="mt-2 text-sm leading-6 text-slate-400">가입한 이메일을 입력해 새 비밀번호를 설정하세요.</p>
    <p className="mt-3 rounded-xl border border-amber-400/20 bg-amber-400/10 p-3 text-xs leading-5 text-amber-100">개발용 기능입니다. 이메일 인증 없이 변경됩니다.</p>
    <form onSubmit={handleSubmit} className="mt-6 space-y-4">
      <input type="email" required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="이메일" className="w-full rounded-xl border border-white/10 bg-black/30 px-4 py-3 text-sm text-white outline-none focus:border-indigo-400" />
      <input type="password" required minLength={4} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="새 비밀번호" className="w-full rounded-xl border border-white/10 bg-black/30 px-4 py-3 text-sm text-white outline-none focus:border-indigo-400" />
      <input type="password" required minLength={4} value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} placeholder="새 비밀번호 확인" className="w-full rounded-xl border border-white/10 bg-black/30 px-4 py-3 text-sm text-white outline-none focus:border-indigo-400" />
      <button type="submit" disabled={loading} className="theme-btn-primary w-full py-3 disabled:opacity-60">{loading ? '변경 중...' : '비밀번호 변경'}</button>
      {message ? <p className="text-sm text-emerald-300">{message}</p> : null}{error ? <p className="text-sm text-rose-300">{error}</p> : null}
    </form><Link to="/login" className="mt-6 block text-center text-sm text-indigo-300 hover:text-white">로그인으로 돌아가기</Link>
  </div></main>;
};
