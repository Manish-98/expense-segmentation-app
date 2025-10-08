import { createContext, useState, useContext, useEffect } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Fetch user data on mount if token exists
  useEffect(() => {
    const fetchUser = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const response = await axiosClient.get('/auth/me');
          setUser(response.data);
          setIsAuthenticated(true);
        } catch (error) {
          console.error('Failed to fetch user:', error);
          // Token might be invalid, clear it
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setToken(null);
          setIsAuthenticated(false);
        }
      }
      setLoading(false);
    };

    fetchUser();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await axiosClient.post('/auth/login', { email, password });
      const { token: newToken } = response.data;

      // Store token first
      localStorage.setItem('token', newToken);
      setToken(newToken);

      // Fetch complete user data
      const userResponse = await axiosClient.get('/auth/me');
      const userData = userResponse.data;

      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      setIsAuthenticated(true);

      return { success: true };
    } catch (error) {
      console.error('Login failed:', error);
      // Clean up on error
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
      setIsAuthenticated(false);
      return {
        success: false,
        message: error.response?.data?.message || 'Login failed. Please try again.',
      };
    }
  };

  const register = async (name, email, password) => {
    try {
      const response = await axiosClient.post('/auth/register', { name, email, password });
      const { token: newToken } = response.data;

      // Store token first
      localStorage.setItem('token', newToken);
      setToken(newToken);

      // Fetch complete user data
      const userResponse = await axiosClient.get('/auth/me');
      const userData = userResponse.data;

      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      setIsAuthenticated(true);

      return { success: true };
    } catch (error) {
      console.error('Registration failed:', error);
      // Clean up on error
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
      setIsAuthenticated(false);
      return {
        success: false,
        message: error.response?.data?.message || 'Registration failed. Please try again.',
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
  };

  const value = {
    user,
    token,
    isAuthenticated,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
