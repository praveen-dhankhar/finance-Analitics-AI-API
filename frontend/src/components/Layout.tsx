import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, TrendingUp, LogOut } from 'lucide-react';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  const navItems = [
    { path: '/dashboard', icon: <LayoutDashboard size={20} />, label: 'Dashboard' },
    { path: '/forecast', icon: <TrendingUp size={20} />, label: 'Forecasting' },
  ];

  return (
    <div className="app-container">
      <aside style={{
        width: '260px',
        background: 'var(--glass-bg)',
        borderRight: '1px solid var(--glass-border)',
        backdropFilter: 'var(--glass-blur)',
        padding: '24px',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        height: '100vh'
      }}>
        <div style={{ marginBottom: '40px', paddingLeft: '12px' }}>
          <h1 className="text-gradient" style={{ fontSize: '24px', fontWeight: 'bold' }}>FinFlow AI</h1>
        </div>

        <nav style={{ flex: 1 }}>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <li key={item.path} style={{ marginBottom: '8px' }}>
                  <Link to={item.path} style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    color: isActive ? 'var(--text-primary)' : 'var(--text-secondary)',
                    background: isActive ? 'rgba(255,255,255,0.08)' : 'transparent',
                    fontWeight: isActive ? 600 : 500,
                    transition: 'all 0.2s ease',
                  }}
                  className="hover:bg-[rgba(255,255,255,0.05)] hover:text-text-primary"
                  >
                    <span style={{ marginRight: '12px', color: isActive ? 'var(--accent-primary)' : 'inherit' }}>
                      {item.icon}
                    </span>
                    {item.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <div style={{ marginTop: 'auto' }}>
          <button 
            onClick={handleLogout}
            style={{
              display: 'flex',
              alignItems: 'center',
              padding: '12px 16px',
              width: '100%',
              borderRadius: '12px',
              color: 'var(--text-secondary)',
              transition: 'all 0.2s ease',
            }}
            className="hover:bg-[rgba(239,68,68,0.1)] hover:text-error"
          >
            <LogOut size={20} style={{ marginRight: '12px' }} />
            Sign Out
          </button>
        </div>
      </aside>

      <main className="main-content" style={{ marginLeft: '260px' }}>
        {children}
      </main>
    </div>
  );
};
