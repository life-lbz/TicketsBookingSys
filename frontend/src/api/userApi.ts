import axios from 'axios';

const userApi = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
});

userApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('user_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

userApi.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('user_token');
      localStorage.removeItem('user_isLoggedIn');
      localStorage.removeItem('user_userId');
      localStorage.removeItem('user_username');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default userApi;
