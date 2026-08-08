import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { CustomCalendar } from '../components/CustomCalendar';

interface PwdStrength {
  color: string;
  label: string;
  hint: string;
  score: number;
}

export const Join = () => {
  const navigate = useNavigate();

  // Form states
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [emailSuccess, setEmailSuccess] = useState('');
  const [nicknm, setNicknm] = useState('');
  const [nicknmError, setNicknmError] = useState('');
  const [nicknmSuccess, setNicknmSuccess] = useState('');
  const [pwd, setPwd] = useState('');
  const [pwdCheck, setPwdCheck] = useState('');
  const [birth, setBirth] = useState('');
  const [phone, setPhone] = useState('');
  const [zipcode, setZipcode] = useState('');
  const [addr, setAddr] = useState('');
  const [addrDetail, setAddrDetail] = useState('');

  // Password strength state
  const [pwdStrength, setPwdStrength] = useState<PwdStrength | null>(null);

  // Password strength check
  useEffect(() => {
    if (!pwd) {
      setPwdStrength(null);
      return;
    }

    let score = 0;
    if (pwd.length >= 8) score++;
    if (pwd.length >= 12) score++;
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^a-zA-Z0-9]/.test(pwd)) score++;

    const levels = [
      { color: '#ef4444', label: '매우 약함 🔴', hint: '8자 이상 입력해 주세요', score: 1 },
      { color: '#f97316', label: '약함 🟠',     hint: '대소문자를 더해보세요', score: 2 },
      { color: '#eab308', label: '보통 🟡',     hint: '숫자 또는 특수문자를 더해보세요', score: 3 },
      { color: '#22c55e', label: '강함 🟢',     hint: '조금만 더 입력하면 완벽해요!', score: 4 },
      { color: '#6366f1', label: '매우 강함 💎', hint: '완벽한 비밀번호입니다!', score: 5 },
    ];

    const idx = Math.max(0, Math.min(score - 1, 4));
    setPwdStrength(levels[idx]);
  }, [pwd]);

  // Daum Postcode Search
  const handlePostcodeSearch = () => {
    const daumObj = (window as any).daum;
    if (!daumObj || !daumObj.Postcode) {
      alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.');
      return;
    }

    new daumObj.Postcode({
      oncomplete: (data: any) => {
        let roadAddr = data.roadAddress;
        let extraRoadAddr = '';

        if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
          extraRoadAddr += data.bname;
        }
        if (data.buildingName !== '' && data.apartment === 'Y') {
          extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
        }
        if (extraRoadAddr !== '') {
          extraRoadAddr = ' (' + extraRoadAddr + ')';
        }

        setZipcode(data.zonecode);
        setAddr(roadAddr + extraRoadAddr);
        
        // Focus detail input after selecting postcode
        const detailInput = document.getElementById('userAddrDetail');
        if (detailInput) {
          (detailInput as HTMLInputElement).focus();
        }
      }
    }).open();
  };

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setEmail(value);
    setEmailSuccess('');
    
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (value && !emailRegex.test(value)) {
      setEmailError('이메일 주소 양식에 맞지 않습니다.');
    } else {
      setEmailError('');
    }
  };

  const checkEmailDuplicate = async () => {
    if (!email || emailError) {
      return;
    }
    
    try {
      const response = await axios.get(`/api/user/check-email?email=${encodeURIComponent(email)}`);
      if (response.data === true) {
        setEmailError('이미 가입된 이메일 주소입니다.');
        setEmailSuccess('');
      } else {
        setEmailError('');
        setEmailSuccess('사용 가능한 이메일입니다.');
      }
    } catch (error) {
      console.error('Email check failed:', error);
    }
  };

  const checkNicknmDuplicate = async () => {
    if (!nicknm) {
      setNicknmError('');
      setNicknmSuccess('');
      return;
    }
    
    try {
      const response = await axios.get(`/api/user/check-nickname?nicknm=${encodeURIComponent(nicknm)}`);
      if (response.data === true) {
        setNicknmError('이미 사용 중인 닉네임입니다.');
        setNicknmSuccess('');
      } else {
        setNicknmError('');
        setNicknmSuccess('사용 가능한 닉네임입니다.');
      }
    } catch (error) {
      console.error('Nickname check failed:', error);
    }
  };

  // Submit form
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (emailError) {
      alert('이메일 양식을 확인해 주세요.');
      return;
    }

    if (nicknmError) {
      alert('닉네임 중복을 확인해 주세요.');
      return;
    }

    if (pwd !== pwdCheck) {
      alert('입력하신 비밀번호가 서로 일치하지 않습니다.');
      return;
    }

    const joinData = {
      userEmail: email,
      userNicknm: nicknm,
      userPwd: pwd,
      userPwdCheck: pwdCheck,
      userPhone: phone,
      userZipcode: zipcode,
      userAddr: addr,
      userAddrDetail: addrDetail,
      userBirth: birth
    };

    try {
      const response = await axios.post('/api/user/join', joinData);
      alert(response.data);
      navigate('/login');
    } catch (error: any) {
      console.error('Error during join:', error);
      if (error.response && error.response.data) {
        alert('회원가입 실패: ' + error.response.data);
      } else {
        alert('서버 통신 중 에러가 발생했습니다.');
      }
    }
  };

  return (
    <main className="w-full max-w-md mx-auto px-6 py-12 flex-grow flex flex-col justify-center z-10">
      <div className="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] bg-purple-600/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-[-10%] left-[-10%] w-[500px] h-[500px] bg-indigo-600/10 rounded-full blur-[120px] pointer-events-none"></div>

      <div className="theme-glass-card shadow-2xl backdrop-blur-xl">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-extrabold tracking-tight mb-2 text-white">반갑습니다 🗺️</h2>
          <p className="text-sm text-gray-400">RouteMate와 함께 스마트한 여행 동선을 설계해 보세요.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5 text-left">
          <div>
            <label htmlFor="userEmail" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">이메일 주소</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-regular fa-envelope"></i></span>
              <input
                type="email"
                id="userEmail"
                required
                value={email}
                onChange={handleEmailChange}
                onBlur={checkEmailDuplicate}
                className={`w-full bg-black/40 border ${emailError ? 'border-red-500/80 focus:border-red-500 focus:ring-red-500' : emailSuccess ? 'border-green-500/80 focus:border-green-500 focus:ring-green-500' : 'border-white/10 focus:border-brand-primary focus:ring-brand-primary'} rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 transition`}
                placeholder="name@example.com"
              />
            </div>
            {emailError && <p className="text-red-500 text-xs mt-1.5 ml-1"><i className="fa-solid fa-circle-exclamation mr-1"></i>{emailError}</p>}
            {emailSuccess && <p className="text-green-500 text-xs mt-1.5 ml-1"><i className="fa-solid fa-circle-check mr-1"></i>{emailSuccess}</p>}
          </div>

          <div>
            <label htmlFor="userNicknm" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">닉네임</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-regular fa-user"></i></span>
              <input
                type="text"
                id="userNicknm"
                required
                value={nicknm}
                onChange={(e) => {
                  setNicknm(e.target.value);
                  setNicknmError('');
                  setNicknmSuccess('');
                }}
                onBlur={checkNicknmDuplicate}
                className={`w-full bg-black/40 border ${nicknmError ? 'border-red-500/80 focus:border-red-500 focus:ring-red-500' : nicknmSuccess ? 'border-green-500/80 focus:border-green-500 focus:ring-green-500' : 'border-white/10 focus:border-brand-primary focus:ring-brand-primary'} rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 transition`}
                placeholder="닉네임을 입력해주세요."
              />
            </div>
            {nicknmError && <p className="text-red-500 text-xs mt-1.5 ml-1"><i className="fa-solid fa-circle-exclamation mr-1"></i>{nicknmError}</p>}
            {nicknmSuccess && <p className="text-green-500 text-xs mt-1.5 ml-1"><i className="fa-solid fa-circle-check mr-1"></i>{nicknmSuccess}</p>}
          </div>

          <div>
            <label htmlFor="userPwd" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">비밀번호</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-lock"></i></span>
              <input
                type="password"
                id="userPwd"
                required
                value={pwd}
                onChange={(e) => setPwd(e.target.value)}
                className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                placeholder="••••••••"
              />
            </div>

            {/* Password security strength indicator */}
            {pwdStrength && (
              <div className="mt-2.5">
                <div className="flex gap-1.5 mb-1.5">
                  {[1, 2, 3, 4, 5].map((levelNum) => (
                    <div
                      key={levelNum}
                      className="h-1.5 flex-1 rounded-full transition-all duration-300"
                      style={{
                        backgroundColor: levelNum <= pwdStrength.score ? pwdStrength.color : 'rgba(255,255,255,0.1)',
                        opacity: levelNum <= pwdStrength.score ? 1 : 0.15
                      }}
                    ></div>
                  ))}
                </div>
                <div className="flex justify-between items-center">
                  <p className="text-xs font-medium" style={{ color: pwdStrength.color }}>보안 등급: {pwdStrength.label}</p>
                  <p className="text-xs text-gray-500">{pwdStrength.hint}</p>
                </div>
              </div>
            )}
          </div>

          <div>
            <label htmlFor="userPwdCheck" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">비밀번호 확인</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-shield-halved"></i></span>
              <input
                type="password"
                id="userPwdCheck"
                required
                value={pwdCheck}
                onChange={(e) => setPwdCheck(e.target.value)}
                className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                placeholder="••••••••"
              />
            </div>
          </div>

          <div>
            <label htmlFor="userBirth" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">생년월일</label>
            <CustomCalendar value={birth} onChange={setBirth} placeholder="달력에서 날짜를 선택해 주세요" />
          </div>

          <div>
            <label htmlFor="userPhone" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">휴대폰 번호</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-mobile-screen-button"></i></span>
              <input
                type="tel"
                id="userPhone"
                required
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                placeholder="010-1234-5678"
              />
            </div>
          </div>

          <div>
            <label htmlFor="userZipcode" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">우편번호</label>
            <div className="flex gap-2">
              <div className="relative flex-grow">
                <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-location-arrow"></i></span>
                <input
                  type="text"
                  id="userZipcode"
                  required
                  readOnly
                  value={zipcode}
                  className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                  placeholder="우편번호"
                />
              </div>
              <button
                type="button"
                onClick={handlePostcodeSearch}
                className="px-4 py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-sm font-semibold transition shrink-0"
              >
                주소 검색
              </button>
            </div>
          </div>

          <div>
            <label htmlFor="userAddr" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">기본 주소</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-house"></i></span>
              <input
                type="text"
                id="userAddr"
                required
                readOnly
                value={addr}
                className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                placeholder="주소 검색 버튼을 눌러주세요"
              />
            </div>
          </div>

          <div>
            <label htmlFor="userAddrDetail" className="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">상세 주소</label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-4 text-gray-500"><i className="fa-solid fa-house-user"></i></span>
              <input
                type="text"
                id="userAddrDetail"
                required
                value={addrDetail}
                onChange={(e) => setAddrDetail(e.target.value)}
                className="w-full bg-black/40 border border-white/10 rounded-xl pl-11 pr-4 py-3.5 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-brand-primary focus:ring-1 focus:ring-brand-primary transition"
                placeholder="101동 1004호"
              />
            </div>
          </div>

          <button type="submit" className="w-full theme-btn-primary py-4 mt-4 font-semibold text-sm">
            가입 완료하기
          </button>
        </form>

        <div className="text-center mt-6 pt-6 border-t border-white/5 text-xs text-gray-400">
          이미 계정이 있으신가요? <Link to="/login" className="text-brand-primary hover:underline font-medium ml-1">로그인하기</Link>
        </div>
      </div>
    </main>
  );
};
