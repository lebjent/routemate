import { useEffect, useState, type ReactNode } from 'react';
import { AuthContext, type AuthUser } from './authContextValue';
import { api } from '../lib/http';

/**
 * 로그인 세션을 한 번 확인하고, 하위 화면에 사용자·권한·로그아웃 기능을 제공한다.
 *
 * 새로고침 뒤에도 `/api/auth/me`로 서버 세션을 다시 확인한다. 로그인 실패는 일반적인
 * 비로그인 상태로 취급하며, 화면 진입 자체를 오류로 만들지 않는다.
 */
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<AuthUser>('/api/auth/me')
      .then((response) => {
        if (response.data && typeof response.data === 'object') {
          setUser(response.data);
        } else {
          setUser(null);
        }
      })
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  /** 서버 세션을 종료한 뒤 클라이언트의 사용자 상태도 즉시 비운다. */
  const logout = async () => {
    await api.post('/api/auth/logout');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login: setUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
