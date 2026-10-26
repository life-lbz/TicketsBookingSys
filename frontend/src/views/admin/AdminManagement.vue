<template>
  <div class="admin-management">
    <div class="page-header">
      <h2>管理员账号管理</h2>
      <div class="header-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回管理端
        </el-button>
        <el-button type="primary" @click="handleAdd" v-if="isSuperAdmin">
          <el-icon><Plus /></el-icon>
          添加管理员
        </el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="sortedAdminList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'">
              {{ scope.row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              @click="handleEdit(scope.row)"
              v-if="canEditAdmin(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="warning" 
              @click="handleResetPassword(scope.row)"
              v-if="canEditAdmin(scope.row)"
            >
              重置密码
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(scope.row)"
              v-if="canDeleteAdmin(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑管理员' : '添加管理员'"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" v-if="isSuperAdminUser" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="400px"
    >
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import api from '../../api'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const resetPasswordVisible = ref(false)
const isEdit = ref(false)
const adminList = ref<any[]>([])
const resettingAdminId = ref<number | null>(null)
const resettingAdminUsername = ref('')

// 判断是否为超级管理员
const isSuperAdmin = computed(() => {
  return localStorage.getItem('admin_role') === 'SUPER_ADMIN'
})

// 获取当前登录的管理员用户名
const currentAdminUsername = computed(() => {
  return localStorage.getItem('admin_username') || ''
})

// 判断当前用户是否为最高权限的superadmin（不区分大小写）
const isSuperAdminUser = computed(() => {
  return currentAdminUsername.value.toLowerCase() === 'superadmin'
})

// 判断是否可以编辑指定管理员
// superadmin可以编辑任何人，其他超级管理员只能编辑自己和普通管理员
const canEditAdmin = (admin: any) => {
  if (!isSuperAdmin.value) return false
  
  // superadmin 可以编辑所有人
  if (isSuperAdminUser.value) {
    return true
  }
  
  // 其他超级管理员：可以编辑自己或普通管理员
  if (admin.role === 'ADMIN') {
    return true
  }
  
  // 其他超级管理员：只能编辑自己
  return admin.username === currentAdminUsername.value
}

// 判断是否可以删除指定管理员
// superadmin可以删除其他超级管理员和普通管理员（但不能删除自己），其他超级管理员只能删除普通管理员
const canDeleteAdmin = (admin: any) => {
  if (!isSuperAdmin.value) return false
  
  // 不能删除自己
  if (admin.username === currentAdminUsername.value) {
    return false
  }
  
  // superadmin 可以删除除自己外的所有人
  if (isSuperAdminUser.value) {
    return true
  }
  
  // 其他超级管理员：只能删除普通管理员
  return admin.role === 'ADMIN'
}

// 排序后的管理员列表（超级管理员放第一位）
const sortedAdminList = computed(() => {
  return [...adminList.value].sort((a: any, b: any) => {
    if (a.role === 'SUPER_ADMIN' && b.role !== 'SUPER_ADMIN') {
      return -1
    }
    if (a.role !== 'SUPER_ADMIN' && b.role === 'SUPER_ADMIN') {
      return 1
    }
    return a.id - b.id
  })
})

const formRef = ref()
const resetPasswordFormRef = ref()
const form = reactive({
  id: null as number | null,
  username: '',
  password: '',
  phone: '',
  role: 'ADMIN'
})

const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_rule: any, value: string, callback: any) => {
      if (value !== resetPasswordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

// 获取管理员列表
const fetchAdminList = async () => {
  loading.value = true
  try {
    const response = await api.get('/admins')
    adminList.value = response.data.data || []
  } catch (error) {
    ElMessage.error('获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

// 添加管理员
const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.password = ''
  form.phone = ''
  form.role = 'ADMIN'
  dialogVisible.value = true
}

// 编辑管理员
const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.phone = row.phone
  form.role = row.role
  dialogVisible.value = true
}

// 删除管理员
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除管理员 "${row.username}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await api.delete(`/admins/${row.id}`)
      ElMessage.success('删除成功')
      await fetchAdminList()
    } catch (error: any) {
      ElMessage.error(error.response?.data || '删除失败')
    }
  })
}

// 重置密码
const handleResetPassword = (row: any) => {
  resettingAdminId.value = row.id
  resettingAdminUsername.value = row.username
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  const valid = await resetPasswordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await api.put(`/admins/${resettingAdminId.value}/password`, {
      password: resetPasswordForm.newPassword
    })
    ElMessage.success('密码重置成功')
    resetPasswordVisible.value = false
    
    // 判断是否是修改当前登录用户自己的密码
    const currentUser = localStorage.getItem('admin_username') || ''
    if (currentUser === resettingAdminUsername.value) {
      // 清除登录状态
      localStorage.removeItem('admin_isLoggedIn')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_role')
      ElMessage.info('密码已修改，请重新登录')
      // 跳转到登录页面
      await router.push('/admin')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || error.response?.data || '密码重置失败')
  } finally {
    submitLoading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await api.put(`/admins/${form.id}`, {
        username: form.username,
        phone: form.phone,
        role: form.role
      })
      ElMessage.success('更新成功')
    } else {
      await api.post('/admins', {
        username: form.username,
        password: form.password,
        phone: form.phone,
        role: form.role
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await fetchAdminList()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await fetchAdminList()
})

// 返回管理端
const goBack = async () => {
  await router.push('/admin/dashboard')
}
</script>

<style scoped>
.admin-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #1e3a8a;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
