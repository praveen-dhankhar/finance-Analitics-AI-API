import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/Card';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import { Lock, Mail, User } from 'lucide-react';
import api from '../api/axios';

const Login: React.FC = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    username: '',
    emailOrUsername: '',
    email: '',
    password: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (isLogin) {
        const res = await api.post('/auth/login', {
          emailOrUsername: formData.emailOrUsername,
          password: formData.password
        });
        localStorage.setItem('accessToken', res.data.token.accessToken);
        localStorage.setItem('refreshToken', res.data.token.refreshToken);
        navigate('/dashboard');
      } else {
        await api.post('/auth/register', {
          username: formData.username,
          email: formData.email,
          password: formData.password
        });
        setIsLogin(true); // Switch to login after register
      }
    } catch (err: any) {
      const data = err.response?.data;
      if (typeof data === 'string') {
        setError(data);
      } else if (data?.error) {
        setError(data.error);
      } else if (data?.message) {
        setError(data.message);
      } else if (data?.errors) {
        // Spring validation returns an array of field errors
        const messages = Array.isArray(data.errors) 
          ? data.errors.map((e: any) => e.defaultMessage || e.message || JSON.stringify(e)).join(', ')
          : JSON.stringify(data.errors);
        setError(messages);
      } else {
        setError(err.message || 'An error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
      <div style={{ width: '100%', maxWidth: '420px' }}>
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <h1 className="text-gradient" style={{ fontSize: '36px', fontWeight: 'bold', marginBottom: '8px' }}>FinFlow AI</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Advanced Financial Analytics</p>
        </div>

        <Card glow className="animate-fade-in">
          <CardHeader>
            <CardTitle>{isLogin ? 'Welcome Back' : 'Create Account'}</CardTitle>
          </CardHeader>
          <CardContent>
            {error && (
              <div style={{ padding: '12px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--error)', borderRadius: '8px', color: 'var(--error)', marginBottom: '16px', fontSize: '14px' }}>
                {error}
              </div>
            )}
            
            <form onSubmit={handleSubmit}>
              {!isLogin && (
                <>
                  <Input 
                    id="username"
                    label="Username" 
                    placeholder="johndoe" 
                    icon={<User size={18} />}
                    value={formData.username}
                    onChange={handleChange}
                    required
                  />
                  <Input 
                    id="email"
                    type="email" 
                    label="Email Address" 
                    placeholder="john@example.com" 
                    icon={<Mail size={18} />}
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </>
              )}
              
              {isLogin && (
                <Input 
                  id="emailOrUsername"
                  label="Email or Username" 
                  placeholder="Enter email or username" 
                  icon={<User size={18} />}
                  value={formData.emailOrUsername}
                  onChange={handleChange}
                  required
                />
              )}

              <Input 
                id="password"
                type="password" 
                label="Password" 
                placeholder="••••••••" 
                icon={<Lock size={18} />}
                value={formData.password}
                onChange={handleChange}
                required
              />

              <div style={{ marginTop: '24px' }}>
                <Button type="submit" fullWidth isLoading={loading}>
                  {isLogin ? 'Sign In' : 'Sign Up'}
                </Button>
              </div>
            </form>

            <div style={{ marginTop: '20px', textAlign: 'center' }}>
              <button 
                type="button" 
                onClick={() => setIsLogin(!isLogin)}
                style={{ color: 'var(--text-secondary)', fontSize: '14px' }}
                className="hover:text-text-primary transition-colors"
              >
                {isLogin ? "Don't have an account? Sign Up" : "Already have an account? Sign In"}
              </button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Login;
