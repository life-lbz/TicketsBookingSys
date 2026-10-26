import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
});

api.interceptors.request.use(
  (config) => {
    // 根据当前路径决定使用哪个token
    // 如果路径以/admin开头，使用管理端token
    // 否则，使用用户端token
    const isAdminPath = window.location.pathname.startsWith('/admin');
    const token = isAdminPath 
      ? localStorage.getItem('admin_token') 
      : localStorage.getItem('user_token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 根据当前路径只清除对应端的登录状态
      const isAdminPath = window.location.pathname.startsWith('/admin');
      if (isAdminPath) {
        // 只清除管理端登录状态
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_isLoggedIn');
        localStorage.removeItem('admin_id');
        localStorage.removeItem('admin_username');
        localStorage.removeItem('admin_role');
        window.location.href = '/admin';
      } else {
        // 只清除用户端登录状态
        localStorage.removeItem('user_token');
        localStorage.removeItem('user_isLoggedIn');
        localStorage.removeItem('user_userId');
        localStorage.removeItem('user_username');
        localStorage.removeItem('user_isAdmin');
        localStorage.removeItem('user_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
