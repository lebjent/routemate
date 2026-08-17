import { createContext } from 'react';

export interface AuthUser {
  userId: number;
  userEmail: string;
  userNicknm: string;
  userRole: string;
  permissions: string[];
}

export interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (user: AuthUser) => void;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
