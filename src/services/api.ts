import axios from 'axios';

// For Vite, environment variables start with VITE_
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
//   headers: {
//     'Content-Type': 'application/json',
//   },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken'); // Or get from AuthContext
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Handle unauthorized: clear token, redirect to login
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      // Use a more robust way to redirect if using context or a global state
      if (window.location.pathname !== '/login') {
         window.location.href = '/login'; // Simple redirect
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;