<template>
  <div class="admin-login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="../../assets/images/ticketsale.png" alt="Logo" class="login-logo" @click="goToHome" />
        <h1>管理后台</h1>
        <p>赛事票务管理系统</p>
      </div>
      
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="管理员账号" 
            :prefix-icon="User"
            size="large"
            :disabled="loading"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            :prefix-icon="Lock"
            show-password
            size="large"
            :disabled="loading"
          />
        </el-form-item>
        
        <div class="login-options">
          <el-checkbox v-model="loginForm.rememberMe" :disabled="loading">
            记住密码
          </el-checkbox>
          <span class="forgot-password" @click="handleForgotPassword">
            忘记密码？
          </span>
        </div>
        
        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-message">
          <span>{{ errorMessage }}</span>
        </div>
        
        <el-button type="primary" class="submit-btn" size="large" @click="handleLogin" :loading="loading" :disabled="loading">
          登录
        </el-button>
        
        <!-- 登录失败提示 -->
        <div v-if="loginAttempts > 0 && loginAttempts < MAX_ATTEMPTS && !isLocked" class="attempts-warning">
          已失败 {{ loginAttempts }} 次，剩余 {{ MAX_ATTEMPTS - loginAttempts }} 次机会
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api/adminApi'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const loginAttempts = ref(0)
const MAX_ATTEMPTS = 5
const LOCKOUT_DURATION = 30 // 锁定时长（秒）
const lastFailedTime = ref(0) // 上次失败时间
const countdown = ref(0) // 倒计时秒数
let countdownTimer: number | null = null
const isLocked = ref(false) // 是否被锁定

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '账号只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// 检查是否已登录
const checkAuth = async () => {
  if (localStorage.getItem('admin_isLoggedIn') === 'true' && localStorage.getItem('admin_token')) {
    await router.push('/admin/dashboard')
  }
}

// 加载记住的密码
const loadRememberedUser = () => {
  const savedUser = localStorage.getItem('rememberedAdmin')
  if (savedUser) {
    try {
      const user = JSON.parse(savedUser)
      loginForm.username = user.username || ''
      loginForm.password = user.password || ''
      loginForm.rememberMe = true
    } catch (e) {
      console.error('加载记住的用户信息失败:', e)
    }
  }
}

// 检查是否可以登录
const canLogin = () => {
  // 如果没有被锁定，可以登录
  if (!isLocked.value) {
    return true
  }
  
  // 检查锁定是否已过期
  const now = Date.now()
  if (now - lastFailedTime.value >= LOCKOUT_DURATION * 1000) {
    // 锁定过期，允许尝试一次
    isLocked.value = false
    return true
  }
  
  // 更新倒计时
  countdown.value = Math.ceil((LOCKOUT_DURATION * 1000 - (now - lastFailedTime.value)) / 1000)
  return false
}

// 开始锁定倒计时
const startLockout = () => {
  isLocked.value = true
  lastFailedTime.value = Date.now()
  countdown.value = LOCKOUT_DURATION
  
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  
  countdownTimer = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      // 锁定时间到期，允许尝试一次
      isLocked.value = false
      countdown.value = 0
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000)
}

// 登录
const handleLogin = async () => {
  // 检查是否已登录普通用户，如果是则阻止管理员登录
  const isUserLoggedIn = localStorage.getItem('user_isLoggedIn') === 'true'
  if (isUserLoggedIn) {
    ElMessage.warning('您已登录普通用户账号，请先退出用户账号后再登录管理端')
    return
  }
  
  // 检查是否可以登录
  if (!canLogin()) {
    ElMessage.error(`账号已被锁定，请等待 ${countdown.value} 秒后重试`)
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await api.post('/admins/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 使用管理端专用的存储键名，避免与用户端冲突
    localStorage.setItem('admin_token', response.data.data.token)
    localStorage.setItem('admin_isLoggedIn', 'true')
    localStorage.setItem('admin_username', response.data.data.username)
    localStorage.setItem('admin_id', response.data.data.id.toString())
    localStorage.setItem('admin_role', response.data.data.role)
    
    // 记住密码
    if (loginForm.rememberMe) {
      localStorage.setItem('rememberedAdmin', JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      }))
    } else {
      localStorage.removeItem('rememberedAdmin')
    }
    
    // 重置登录失败次数和锁定状态
    loginAttempts.value = 0
    isLocked.value = false
    lastFailedTime.value = 0
    countdown.value = 0
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
    
    ElMessage.success('登录成功')
    await router.push('/admin/dashboard')
  } catch (error: any) {
    loginAttempts.value++
    
    const errorText = error.response?.data?.message || error.response?.data || '账号或密码错误'
    errorMessage.value = errorText
    ElMessage.error(errorText)
    
    // 如果达到最大尝试次数，锁定账号
    if (loginAttempts.value >= MAX_ATTEMPTS) {
      startLockout()
    }
  } finally {
    loading.value = false
  }
}

// 忘记密码
const handleForgotPassword = async () => {
  try {
    await ElMessageBox.confirm(
      '请联系系统管理员重置密码',
      '忘记密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
  } catch {
    // 用户取消
  }
}

const goToHome = async () => {
  // 如果已经登录为管理员，跳转到管理端首页
  if (localStorage.getItem('admin_isLoggedIn') === 'true') {
    await router.push('/admin/dashboard')
  } else {
    // 未登录时跳转到用户端首页
    await router.push('/')
  }
}

const isInitialized = ref(false)

// 监听用户名变化，清空密码和错误信息（仅在初始化完成后）
watch(() => loginForm.username, () => {
  if (isInitialized.value) {
    loginForm.password = ''
    errorMessage.value = ''
  }
})

// 监听密码变化，清空错误信息
watch(() => loginForm.password, () => {
  errorMessage.value = ''
})

// 页面加载时检查登录状态
onMounted(() => {
  checkAuth()
  loadRememberedUser()
  // 标记初始化完成
  setTimeout(() => {
    isInitialized.value = true
  }, 0)
})
</script>

<style scoped>
.admin-login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
}

.login-box {
  width: 450px;
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.login-header {
  margin-bottom: 30px;
}

.login-logo {
  height: 60px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.login-logo:hover {
  transform: scale(1.05);
}

.login-header h1 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #1e3a8a;
}

.login-header p {
  color: #64748b;
  font-size: 14px;
}

.login-form {
  text-align: left;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 15px;
}

.forgot-password {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.error-message {
  background-color: #fef2f2;
  color: #dc2626;
  padding: 10px 15px;
  border-radius: 6px;
  margin-bottom: 15px;
  font-size: 14px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
  background: #1e3a8a;
  border-color: #1e3a8a;
}

.submit-btn:hover:not(:disabled) {
  background: #1e40af;
  border-color: #1e40af;
}

.attempts-warning {
  margin-top: 15px;
  padding: 10px 15px;
  background-color: #fffbeb;
  color: #d97706;
  border-radius: 6px;
  font-size: 13px;
  text-align: center;
}
</style>