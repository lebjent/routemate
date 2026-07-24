import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Join } from './pages/Join';
import { Lotto } from './pages/Lotto';

function App() {
  return (
    <Router>
      <div className="min-h-screen flex flex-col justify-between bg-brand-base text-gray-100 relative overflow-x-hidden">
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/join" element={<Join />} />
          <Route path="/lotto" element={<Lotto />} />
        </Routes>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
