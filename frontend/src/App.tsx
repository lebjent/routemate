import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { AuthProvider } from './contexts/AuthContext';

/**
 * 화면 단위 지연 로딩 목록이다.
 *
 * 초기 번들에서 관리자 포털, PDF 생성기, 일정 편집기처럼 크기가 큰 코드를 제외하고, 사용자가
 * 해당 경로로 이동할 때만 별도 청크를 내려받는다. 페이지는 named export를 사용하므로 각 모듈의
 * export를 React.lazy가 요구하는 default 형식으로 변환한다.
 */
const Home = lazy(() => import('./pages/Home').then((module) => ({ default: module.Home })));
const Login = lazy(() => import('./pages/Login').then((module) => ({ default: module.Login })));
const PasswordReset = lazy(() => import('./pages/PasswordReset').then((module) => ({ default: module.PasswordReset })));
const Join = lazy(() => import('./pages/Join').then((module) => ({ default: module.Join })));
const Lotto = lazy(() => import('./pages/Lotto').then((module) => ({ default: module.Lotto })));
const MyTrips = lazy(() => import('./pages/MyTrips').then((module) => ({ default: module.MyTrips })));
const Products = lazy(() => import('./pages/Products').then((module) => ({ default: module.Products })));
const ProductDetail = lazy(() => import('./pages/ProductDetail').then((module) => ({ default: module.ProductDetail })));
const MyProductOrders = lazy(() => import('./pages/MyProductOrders').then((module) => ({ default: module.MyProductOrders })));
const CreateTrip = lazy(() => import('./pages/CreateTrip').then((module) => ({ default: module.CreateTrip })));
const TripDetail = lazy(() => import('./pages/TripDetail').then((module) => ({ default: module.TripDetail })));
const AdminLogin = lazy(() => import('./pages/admin/AdminLogin').then((module) => ({ default: module.AdminLogin })));
const AdminDashboard = lazy(() => import('./pages/admin/AdminDashboard').then((module) => ({ default: module.AdminDashboard })));
const AdminLayout = lazy(() => import('./pages/admin/AdminLayout').then((module) => ({ default: module.AdminLayout })));
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers').then((module) => ({ default: module.AdminUsers })));
const AdminStaff = lazy(() => import('./pages/admin/AdminStaff').then((module) => ({ default: module.AdminStaff })));
const AdminDestinations = lazy(() => import('./pages/admin/AdminDestinations').then((module) => ({ default: module.AdminDestinations })));
const AdminRecommendations = lazy(() => import('./pages/admin/AdminRecommendations').then((module) => ({ default: module.AdminRecommendations })));
const AdminPartners = lazy(() => import('./pages/admin/AdminPartners').then((module) => ({ default: module.AdminPartners })));
const AdminPartnerOnboarding = lazy(() => import('./pages/admin/AdminPartnerOnboarding').then((module) => ({ default: module.AdminPartnerOnboarding })));
const AdminProductApprovals = lazy(() => import('./pages/admin/AdminProductApprovals').then((module) => ({ default: module.AdminProductApprovals })));
const PartnerLogin = lazy(() => import('./pages/partner/PartnerLogin').then((module) => ({ default: module.PartnerLogin })));
const PartnerStaffManagement = lazy(() => import('./pages/partner/PartnerStaffManagement').then((module) => ({ default: module.PartnerStaffManagement })));
const PartnerDashboard = lazy(() => import('./pages/partner/PartnerDashboard').then((module) => ({ default: module.PartnerDashboard })));
const PartnerProductManagement = lazy(() => import('./pages/partner/PartnerProductManagement').then((module) => ({ default: module.PartnerProductManagement })));

/**
 * 현재 URL에 맞는 화면과 공통 레이아웃을 조합한다.
 *
 * 일반 화면은 상단 내비게이션과 푸터를 사용하고, 관리자·파트너 포털은 각 전용 레이아웃이
 * 화면을 책임지도록 공통 영역에서 제외한다.
 */
function AppRoutes() {
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith('/admin');
  const isPartnerRoute = location.pathname.startsWith('/partner');

  return (
    <div className={`min-h-screen flex flex-col justify-between text-gray-100 relative overflow-x-hidden ${isAdminRoute || isPartnerRoute ? 'bg-slate-950' : 'bg-brand-base'}`}>
      {!isAdminRoute && !isPartnerRoute ? <Navbar /> : null}
      <Suspense fallback={<main className="flex min-h-[60vh] items-center justify-center text-sm text-slate-300">화면을 불러오는 중입니다.</main>}>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/password-reset" element={<PasswordReset />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/partner/login" element={<PartnerLogin />} />
        <Route path="/partner" element={<PartnerDashboard />} />
        <Route path="/partner/products" element={<PartnerProductManagement />} />
        <Route path="/partner/staff" element={<PartnerStaffManagement />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="staff" element={<AdminStaff />} />
          <Route path="destinations" element={<AdminDestinations />} />
          <Route path="recommendations" element={<AdminRecommendations />} />
          <Route path="partners" element={<AdminPartners />} />
          <Route path="partners/new" element={<AdminPartnerOnboarding />} />
          <Route path="product-approvals" element={<AdminProductApprovals />} />
        </Route>
        <Route path="/join" element={<Join />} />
        <Route path="/lotto" element={<Lotto />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:productId" element={<ProductDetail />} />
        <Route path="/my-product-orders" element={<MyProductOrders />} />
        <Route path="/my-trips" element={<MyTrips />} />
        <Route path="/my-trips/new" element={<CreateTrip />} />
        <Route path="/my-trips/:planId/edit" element={<CreateTrip />} />
        <Route path="/my-trips/:planId" element={<TripDetail />} />
        <Route path="/travel-plans/:planId" element={<TripDetail publicView />} />
      </Routes>
      </Suspense>
      {!isAdminRoute && !isPartnerRoute ? <Footer /> : null}
    </div>
  );
}

/** 인증 상태 공급자와 브라우저 라우터를 애플리케이션 전체에 연결한다. */
function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

export default App;
