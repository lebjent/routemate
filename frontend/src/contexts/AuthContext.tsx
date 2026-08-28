import { useEffect, useState, type ReactNode } from 'react';
import { AuthContext, type AuthUser } from './authContextValue';
import { api } from '../lib/http';

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
