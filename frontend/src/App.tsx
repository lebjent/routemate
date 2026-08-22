import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { PasswordReset } from './pages/PasswordReset';
import { Join } from './pages/Join';
import { Lotto } from './pages/Lotto';
import { MyTrips } from './pages/MyTrips';
import { Products } from './pages/Products';
import { ProductDetail } from './pages/ProductDetail';
import { MyProductOrders } from './pages/MyProductOrders';
import { CreateTrip } from './pages/CreateTrip';
import { TripDetail } from './pages/TripDetail';
import { AdminLogin } from './pages/admin/AdminLogin';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { AdminLayout } from './pages/admin/AdminLayout';
import { AdminUsers } from './pages/admin/AdminUsers';
import { AdminStaff } from './pages/admin/AdminStaff';
import { AdminDestinations } from './pages/admin/AdminDestinations';
import { AdminRecommendations } from './pages/admin/AdminRecommendations';
import { AdminProducts } from './pages/admin/AdminProducts';
import { AdminPartners } from './pages/admin/AdminPartners';
import { AdminPartnerOnboarding } from './pages/admin/AdminPartnerOnboarding';
import { PartnerLogin } from './pages/partner/PartnerLogin';
import { PartnerStaff } from './pages/partner/PartnerStaff';
import { AuthProvider } from './contexts/AuthContext';

function AppRoutes() {
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith('/admin');
  const isPartnerRoute = location.pathname.startsWith('/partner');

  return (
    <div className={`min-h-screen flex flex-col justify-between text-gray-100 relative overflow-x-hidden ${isAdminRoute || isPartnerRoute ? 'bg-slate-950' : 'bg-brand-base'}`}>
      {!isAdminRoute && !isPartnerRoute ? <Navbar /> : null}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/password-reset" element={<PasswordReset />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/partner/login" element={<PartnerLogin />} />
        <Route path="/partner" element={<PartnerStaff />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="staff" element={<AdminStaff />} />
          <Route path="destinations" element={<AdminDestinations />} />
          <Route path="recommendations" element={<AdminRecommendations />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="partners" element={<AdminPartners />} />
          <Route path="partners/new" element={<AdminPartnerOnboarding />} />
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
      {!isAdminRoute && !isPartnerRoute ? <Footer /> : null}
    </div>
  );
}

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
