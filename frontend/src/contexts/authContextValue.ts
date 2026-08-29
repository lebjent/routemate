import { createContext } from 'react';

/** 로그인 성공 뒤 서버가 반환하는 현재 사용자와 접근 권한 정보다. */
export interface AuthUser {
  userId: number;
  userEmail: string;
  userNicknm: string;
  userRole: string;
  permissions: string[];
  menuCodes: string[];
}

/** 모든 화면에서 사용하는 인증 상태와 상태 변경 함수의 계약이다. */
export interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (user: AuthUser) => void;
  logout: () => Promise<void>;
}

/** 인증 공급자 밖에서 잘못 사용한 경우를 구분하기 위해 기본값을 null로 둔다. */
export const AuthContext = createContext<AuthContextValue | null>(null);
