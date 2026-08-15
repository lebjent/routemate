import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Join } from './pages/Join';
import { Lotto } from './pages/Lotto';
import { MyTrips } from './pages/MyTrips';
import { CreateTrip } from './pages/CreateTrip';
import { TripDetail } from './pages/TripDetail';
import { AuthProvider } from './contexts/AuthContext';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen flex flex-col justify-between bg-brand-base text-gray-100 relative overflow-x-hidden">
          <Navbar />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/join" element={<Join />} />
            <Route path="/lotto" element={<Lotto />} />
            <Route path="/my-trips" element={<MyTrips />} />
            <Route path="/my-trips/new" element={<CreateTrip />} />
            <Route path="/my-trips/:planId" element={<TripDetail />} />
          </Routes>
          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
