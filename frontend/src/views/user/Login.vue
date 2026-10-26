<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="brand">
          <img src="../../assets/images/ticketsale.png" alt="Logo" class="login-logo" @click="$router.push('/')" />
          <h1 @click="$router.push('/')">大型赛事门票预订系统</h1>
          <p>遇见精彩，从这里开始</p>
        </div>
      </div>
      <div class="login-right">
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="账号登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
              <el-form-item prop="username">
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="用户名/手机号" 
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                />
              </el-form-item>
              <div class="form-options">
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
                <el-button link type="primary" @click="showForgotPasswordDialog">忘记密码？</el-button>
              </div>
              <el-button type="primary" class="submit-btn" size="large" @click="handleLogin" :loading="loading">
                登录
              </el-button>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="新用户注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
              <el-form-item prop="username">
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="用户名" 
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input 
                  v-model="registerForm.phone" 
                  placeholder="手机号" 
                  :prefix-icon="Iphone"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="code">
                <div class="code-input">
                  <el-input 
                    v-model="registerForm.code" 
                    placeholder="请输入验证码" 
                    :prefix-icon="Key"
                    size="large"
                  />
                  <div class="captcha-container" @click="generateCaptcha">
                    <canvas ref="captchaCanvas" width="120" height="40" class="captcha-image"></canvas>
                  </div>
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <el-input 
                  v-model="registerForm.password" 
                  type="password" 
                  placeholder="设置密码" 
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input 
                  v-model="registerForm.confirmPassword" 
                  type="password" 
                  placeholder="确认密码" 
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                />
              </el-form-item>
              <el-button type="primary" class="submit-btn" size="large" @click="handleRegister" :loading="loading">
                注册并登录
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="forgotPasswordForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="forgotPasswordForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="forgotPasswordForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="forgotPasswordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="resetLoading">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Iphone, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../api/userApi'

const router = useRouter()
const route = useRoute()
const activeTab = ref('login')

// 监听路由参数变化以切换 Tab
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'register') {
    activeTab.value = 'register'
  } else {
    activeTab.value = 'login'
  }
}, { immediate: true })

const loading = ref(false)
const resetLoading = ref(false)
const captchaCanvas = ref<HTMLCanvasElement | null>(null)
const currentCaptcha = ref('')
const forgotPasswordVisible = ref(false)
const forgotPasswordFormRef = ref()
const loginFormRef = ref()
const registerFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 记住的用户名（用于判断是否是手动修改）
const rememberedUsername = ref('')

// 忘记密码表单
const forgotPasswordForm = reactive({
  username: '',
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const forgotPasswordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== forgotPasswordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度应在1-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^[A-Z]{4}$/, message: '请输入4个大写字母', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 监听登录表单的用户名变化，清空密码（仅当手动修改时）
watch(() => loginForm.username, (newVal, oldVal) => {
  // 如果是页面加载时的初始化（oldVal为空）且与记住的用户名相同，不清空密码
  if (!oldVal && newVal === rememberedUsername.value) {
    return
  }
  // 如果是用户手动修改用户名（与记住的用户名不同），清空密码
  if (newVal !== rememberedUsername.value) {
    loginForm.password = ''
  }
})

// 监听注册表单的用户名或手机号变化，清空密码
watch([() => registerForm.username, () => registerForm.phone], () => {
  registerForm.password = ''
  registerForm.confirmPassword = ''
})

const handleLogin = async () => {
  // 检查是否已登录管理员，如果是则阻止用户登录
  const isAdminLoggedIn = localStorage.getItem('admin_isLoggedIn') === 'true'
  if (isAdminLoggedIn) {
    ElMessage.warning('您已登录管理员账号，请先退出管理员账号后再登录用户端')
    return
  }
  
  // 表单验证
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const response = await api.post('/users/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    // 直接使用后端返回的用户数据，包含从数据库读取的头像信息
    const userData = { ...response.data.data }
    
    console.log('登录成功，保存用户数据:', userData)
    console.log('response.data.data:', response.data.data)
    
    // 清除旧的全局头像缓存
    localStorage.removeItem('avatar')
    
    // 使用用户端专用的存储键名，避免与管理端冲突
    localStorage.setItem('user_token', response.data.data.token)
    localStorage.setItem('user_isLoggedIn', 'true')
    localStorage.setItem('user_isAdmin', response.data.data.isAdmin.toString())
    localStorage.setItem('user_username', response.data.data.username)
    localStorage.setItem('user_userId', response.data.data.id?.toString() || '')
    localStorage.setItem('user_user', JSON.stringify(userData))
    
    console.log('localStorage user已保存:', localStorage.getItem('user'))

    // 记住我功能
    if (loginForm.remember) {
      localStorage.setItem('rememberUsername', loginForm.username)
      localStorage.setItem('rememberPassword', loginForm.password)
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberUsername')
      localStorage.removeItem('rememberPassword')
      localStorage.removeItem('rememberMe')
    }

    ElMessage.success('登录成功')
    await router.push('/')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || error.response?.data || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  // 验证验证码
  if (registerForm.code.toUpperCase() !== currentCaptcha.value) {
    ElMessage.error('验证码错误')
    generateCaptcha() // 刷新验证码
    return
  }

  // 验证表单（包括密码确认验证）
  if (!registerFormRef.value) return
  
  try {
    const valid = await registerFormRef.value.validate()
    if (!valid) {
      ElMessage.error('表单验证失败，请检查输入')
      return
    }

    loading.value = true

    await api.post('/users/register', {
      username: registerForm.username,
      phone: registerForm.phone,
      password: registerForm.password
    })

    // 注册成功后自动登录
    const response = await api.post('/users/login', {
      username: registerForm.username,
      password: registerForm.password
    })

    // 使用用户端专用的存储键名，避免与管理端冲突
    localStorage.setItem('user_token', response.data.data.token)
    localStorage.setItem('user_isLoggedIn', 'true')
    localStorage.setItem('user_isAdmin', response.data.data.isAdmin.toString())
    localStorage.setItem('user_username', response.data.data.username)
    localStorage.setItem('user_userId', response.data.data.id?.toString() || '')
    localStorage.setItem('user_user', JSON.stringify(response.data.data))

    ElMessage.success('注册成功')
    await router.push('/')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || error.response?.data || '注册失败')
  } finally {
    loading.value = false
  }
}

// 显示忘记密码对话框
const showForgotPasswordDialog = () => {
  forgotPasswordVisible.value = true
  forgotPasswordForm.username = ''
  forgotPasswordForm.phone = ''
  forgotPasswordForm.newPassword = ''
  forgotPasswordForm.confirmPassword = ''
}

// 重置密码
const handleResetPassword = async () => {
  const valid = await forgotPasswordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  resetLoading.value = true
  try {
    await api.post('/users/reset-password', {
      username: forgotPasswordForm.username,
      phone: forgotPasswordForm.phone,
      newPassword: forgotPasswordForm.newPassword
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    forgotPasswordVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data || '密码重置失败')
  } finally {
    resetLoading.value = false
  }
}

// 生成4个随机字母的验证码
const generateRandomCode = () => {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += letters[Math.floor(Math.random() * letters.length)]
  }
  return code
}

// 生成图形验证码
const generateCaptcha = () => {
  const canvas = captchaCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  // 清除画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 生成验证码
  const code = generateRandomCode()
  currentCaptcha.value = code
  
  // 绘制背景
  ctx.fillStyle = '#f5f5f5'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  // 添加噪点
  for (let i = 0; i < 50; i++) {
    ctx.fillStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.5)`
    ctx.beginPath()
    ctx.arc(Math.random() * canvas.width, Math.random() * canvas.height, 1, 0, Math.PI * 2)
    ctx.fill()
  }
  
  // 添加干扰线
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.5)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }
  
  // 绘制验证码
  const fontSizes = [18, 20, 22, 24]
  for (let i = 0; i < code.length; i++) {
    const fontSize = fontSizes[Math.floor(Math.random() * fontSizes.length)]
    ctx.font = `${fontSize}px Arial`
    ctx.fillStyle = `rgb(${Math.random() * 100 + 100}, ${Math.random() * 100 + 50}, ${Math.random() * 100 + 50})`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    
    // 字符倾斜
    const x = 20 + i * 25
    const y = canvas.height / 2
    const angle = (Math.random() - 0.5) * 0.5 // -0.25 到 0.25 弧度
    
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(code[i], 0, 0)
    ctx.restore()
  }
}

// 组件挂载后生成验证码，并读取记住的账号密码
onMounted(() => {
  setTimeout(() => {
    generateCaptcha()
  }, 100)

  // 读取记住的账号密码
  const rememberMe = localStorage.getItem('rememberMe')
  if (rememberMe === 'true') {
    const savedUsername = localStorage.getItem('rememberUsername')
    const savedPassword = localStorage.getItem('rememberPassword')
    if (savedUsername && savedPassword) {
      loginForm.username = savedUsername
      loginForm.password = savedPassword
      loginForm.remember = true
      // 记录记住的用户名，用于判断是否是手动修改
      rememberedUsername.value = savedUsername
    }
  }
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff; /* 修改为纯白色背景 */
}

.login-box {
  width: 900px;
  height: 550px;
  display: flex;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1); /* 优化阴影，使其在白色背景上更柔和 */
  overflow: hidden;
  border: 1px solid #f0f0f0; /* 添加微弱边框增加层次感 */
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
}

.login-left::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(29, 78, 216, 0.4); /* Dynamic Blue overlay */
}

.brand {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.brand h1 {
  font-size: 32px;
  margin-bottom: 10px;
  cursor: pointer;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.login-logo {
  height: 70px;
  width: auto;
  margin-bottom: 25px;
  cursor: pointer;
  transition: all 0.3s;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.login-logo:hover {
  transform: scale(1.05);
}

.login-right {
  width: 400px;
  padding: 40px;
  display: flex;
  flex-direction: column;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  margin-top: 10px;
}

.code-input {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.captcha-container {
  cursor: pointer;
  user-select: none;
}

.captcha-image {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
  height: 40px;
  vertical-align: middle;
}

.captcha-container:hover .captcha-image {
  border-color: #409eff;
}
</style>
