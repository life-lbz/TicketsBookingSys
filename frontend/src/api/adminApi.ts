import axios from 'axios';

const adminApi = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
});

adminApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

adminApi.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_isLoggedIn');
      localStorage.removeItem('admin_id');
      localStorage.removeItem('admin_username');
      localStorage.removeItem('admin_role');
      window.location.href = '/admin';
    }
    return Promise.reject(error);
  }
);

export default adminApi;
