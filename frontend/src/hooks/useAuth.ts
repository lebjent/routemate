import { useContext } from 'react';
import { AuthContext } from '../contexts/authContextValue';

/**
 * 인증 컨텍스트를 안전하게 가져오는 공통 훅이다.
 *
 * AuthProvider 밖에서 사용하면 개발 시점에 원인을 알 수 있도록 즉시 오류를 발생시킨다.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
