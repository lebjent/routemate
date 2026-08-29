/** 일반 사용자 화면 하단에 표시하는 브랜드와 서비스 링크 영역이다. */
export const Footer = () => {
  return (
    <footer className="w-full max-w-7xl mx-auto px-6 py-8 border-t border-gray-800/60 flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-gray-500 z-10 relative">
      <div>&copy; 2026 RouteMate. Next-Generation Global Journey Planner.</div>
      <div className="flex gap-4 text-gray-600">
        <a href="#" className="hover:text-gray-400 transition">이용약관</a>
        <a href="#" className="hover:text-gray-400 transition">개인정보처리방침</a>
        <a href="#" className="hover:text-gray-400 transition">고객센터</a>
      </div>
    </footer>
  );
};
