<template>
  <div class="admin-events">
    <!-- 顶部导航 -->
    <el-header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <img src="../../assets/images/ticketsale.png" alt="Logo" />
          <span>赛事票务管理系统</span>
        </div>
      </div>
      <div class="header-right">
        <el-button link @click="goToHome" class="home-btn">首页</el-button>
        <el-dropdown trigger="click">
          <el-button link class="user-dropdown-btn">
            <el-avatar :size="24" :src="userAvatar" class="user-avatar" />
            <span class="username">{{ adminUsername }}（{{ adminRole === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}）</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <div class="admin-main">
      <!-- 左侧菜单 -->
      <el-aside class="admin-sidebar">
        <div class="menu-scrollable">
          <el-menu
            :default-active="activeMenu"
            class="admin-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>赛事总览</span>
            </el-menu-item>
            <el-menu-item index="events">
              <el-icon><Calendar /></el-icon>
              <span>赛事管理</span>
            </el-menu-item>
            <el-menu-item index="tickets">
              <el-icon><Ticket /></el-icon>
              <span>票务管理</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><ShoppingCart /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="after-sales">
              <el-icon><Service /></el-icon>
              <span>售后处理</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="menu-fixed">
          <el-menu
            :default-active="activeMenu"
            class="admin-menu"
            @select="handleMenuSelect"
          >
            <el-sub-menu index="user-management">
              <template #title>
                <el-icon><User /></el-icon>
                <span>账号管理</span>
              </template>
              <el-menu-item index="users">
                <span>用户账号管理</span>
              </el-menu-item>
              <el-menu-item index="admins" v-if="isSuperAdmin">
                <span>管理员账号管理</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-aside>

      <!-- 右侧内容 -->
      <el-main class="admin-content">
        <div class="page-header">
          <h2>赛事管理</h2>
        </div>

        <!-- 搜索和筛选 -->
        <div class="search-filter">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索赛事名称、地点、类别"
            :prefix-icon="Search"
            class="search-input"
            clearable
            @clear="clearSearch"
          />
          <el-select
            v-model="filterCity"
            placeholder="选择或输入城市"
            clearable
            filterable
            allow-create
            default-first-option
            class="city-select"
          >
            <el-option label="全部城市" value="" />
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="杭州" value="杭州" />
            <el-option label="成都" value="成都" />
            <el-option label="武汉" value="武汉" />
            <el-option label="南京" value="南京" />
            <el-option label="西安" value="西安" />
            <el-option label="重庆" value="重庆" />
            <el-option label="天津" value="天津" />
            <el-option label="苏州" value="苏州" />
            <el-option label="青岛" value="青岛" />
            <el-option label="大连" value="大连" />
            <el-option label="厦门" value="厦门" />
            <el-option label="长沙" value="长沙" />
            <el-option label="郑州" value="郑州" />
            <el-option label="沈阳" value="沈阳" />
            <el-option label="济南" value="济南" />
            <el-option label="哈尔滨" value="哈尔滨" />
            <el-option label="石家庄" value="石家庄" />
            <el-option label="太原" value="太原" />
            <el-option label="合肥" value="合肥" />
            <el-option label="福州" value="福州" />
            <el-option label="南昌" value="南昌" />
            <el-option label="南宁" value="南宁" />
            <el-option label="昆明" value="昆明" />
            <el-option label="贵阳" value="贵阳" />
            <el-option label="兰州" value="兰州" />
            <el-option label="海口" value="海口" />
            <el-option label="三亚" value="三亚" />
          </el-select>
          <el-button v-if="searchKeyword || filterCity" @click="clearSearch">
            清除
          </el-button>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <div style="flex: 1;"></div>
          <el-button type="primary" @click="openAddEventDialog">
            <el-icon><Plus /></el-icon> 添加赛事
          </el-button>
        </div>

        <!-- 赛事列表 -->
        <el-card shadow="never" class="events-card">
          <template #header>
            <div class="card-header">
              <span>赛事列表</span>
              <span class="count">({{ filteredEvents.length }} 个赛事)</span>
            </div>
          </template>
          <el-table :data="paginatedEvents" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="封面" width="100">
              <template #default="scope">
                <el-image :src="getCoverUrl(scope.row.cover)" fit="cover" class="event-cover" />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="赛事名称" />
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="venue" label="场馆" width="150" />
            <el-table-column prop="city" label="城市" width="100" />
            <el-table-column prop="minPrice" label="最低票价" width="100">
              <template #default="scope">
                ¥{{ scope.row.minPrice }}
              </template>
            </el-table-column>
            <el-table-column prop="hot" label="热门" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.hot" type="success">是</el-tag>
                <el-tag v-else type="info">否</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="openEditEventDialog(scope.row)" :disabled="scope.row.status === 'PUBLISHED' || scope.row.status === '售票中'">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="deleteEvent(scope.row.id)" :disabled="scope.row.status === 'PUBLISHED' || scope.row.status === '售票中'">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            layout="total, prev, pager, next"
            :total="filteredEvents.length"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-main>
    </div>

    <!-- 添加赛事对话框 -->
    <el-dialog
      v-model="addEventDialogVisible"
      title="添加赛事"
      width="600px"
    >
      <el-form :model="newEvent" :rules="eventRules" ref="eventFormRef">
        <el-form-item prop="name">
          <el-input v-model="newEvent.name" placeholder="赛事名称" />
        </el-form-item>
        <el-form-item prop="time">
          <el-input v-model="newEvent.time" placeholder="时间" />
        </el-form-item>
        <el-form-item prop="venue">
          <el-input v-model="newEvent.venue" placeholder="场馆" />
        </el-form-item>
        <el-form-item prop="city">
          <el-select v-model="newEvent.city" placeholder="选择城市" filterable>
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="杭州" value="杭州" />
            <el-option label="成都" value="成都" />
            <el-option label="武汉" value="武汉" />
            <el-option label="南京" value="南京" />
            <el-option label="西安" value="西安" />
            <el-option label="重庆" value="重庆" />
            <el-option label="天津" value="天津" />
            <el-option label="苏州" value="苏州" />
            <el-option label="青岛" value="青岛" />
            <el-option label="大连" value="大连" />
            <el-option label="厦门" value="厦门" />
            <el-option label="长沙" value="长沙" />
            <el-option label="郑州" value="郑州" />
            <el-option label="沈阳" value="沈阳" />
            <el-option label="济南" value="济南" />
            <el-option label="哈尔滨" value="哈尔滨" />
            <el-option label="石家庄" value="石家庄" />
            <el-option label="太原" value="太原" />
            <el-option label="合肥" value="合肥" />
            <el-option label="福州" value="福州" />
            <el-option label="南昌" value="南昌" />
            <el-option label="南宁" value="南宁" />
            <el-option label="昆明" value="昆明" />
            <el-option label="贵阳" value="贵阳" />
            <el-option label="兰州" value="兰州" />
            <el-option label="海口" value="海口" />
            <el-option label="三亚" value="三亚" />
          </el-select>
        </el-form-item>
        <el-form-item prop="minPrice">
          <el-input-number v-model="newEvent.minPrice" placeholder="最低票价" :min="0" />
        </el-form-item>
        <el-form-item label="赛事封面" prop="cover">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/events/upload"
            :show-file-list="false"
            :on-success="handleAddCoverSuccess"
            :before-upload="beforeCoverUpload"
            :on-error="handleUploadError"
            name="file"
            accept="image/jpeg,image/png"
          >
            <img v-if="newEvent.cover" :src="getCoverUrl(newEvent.cover)" class="avatar" alt="赛事封面" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="category">
          <el-select v-model="newEvent.category" placeholder="赛事类别">
            <el-option label="球类运动" value="球类运动" />
            <el-option label="田径" value="田径" />
            <el-option label="跳水/游泳" value="跳水/游泳" />
            <el-option label="格斗搏击" value="格斗搏击" />
            <el-option label="电竞" value="电竞" />
            <el-option label="综合运动" value="综合运动" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否热门" prop="hot">
          <el-switch
            v-model="newEvent.hot"
            active-text="热门"
            inactive-text="普通"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addEventDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addEvent">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑赛事对话框 -->
    <el-dialog
      v-model="editEventDialogVisible"
      title="编辑赛事"
      width="600px"
    >
      <el-form :model="editEvent" :rules="eventRules" ref="editEventFormRef">
        <el-form-item prop="name">
          <el-input v-model="editEvent.name" placeholder="赛事名称" />
        </el-form-item>
        <el-form-item prop="time">
          <el-input v-model="editEvent.time" placeholder="时间" />
        </el-form-item>
        <el-form-item prop="venue">
          <el-input v-model="editEvent.venue" placeholder="场馆" />
        </el-form-item>
        <el-form-item prop="city">
          <el-select v-model="editEvent.city" placeholder="选择城市" filterable>
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="杭州" value="杭州" />
            <el-option label="成都" value="成都" />
            <el-option label="武汉" value="武汉" />
            <el-option label="南京" value="南京" />
            <el-option label="西安" value="西安" />
            <el-option label="重庆" value="重庆" />
            <el-option label="天津" value="天津" />
            <el-option label="苏州" value="苏州" />
            <el-option label="青岛" value="青岛" />
            <el-option label="大连" value="大连" />
            <el-option label="厦门" value="厦门" />
            <el-option label="长沙" value="长沙" />
            <el-option label="郑州" value="郑州" />
            <el-option label="沈阳" value="沈阳" />
            <el-option label="济南" value="济南" />
            <el-option label="哈尔滨" value="哈尔滨" />
            <el-option label="石家庄" value="石家庄" />
            <el-option label="太原" value="太原" />
            <el-option label="合肥" value="合肥" />
            <el-option label="福州" value="福州" />
            <el-option label="南昌" value="南昌" />
            <el-option label="南宁" value="南宁" />
            <el-option label="昆明" value="昆明" />
            <el-option label="贵阳" value="贵阳" />
            <el-option label="兰州" value="兰州" />
            <el-option label="海口" value="海口" />
            <el-option label="三亚" value="三亚" />
          </el-select>
        </el-form-item>
        <el-form-item prop="minPrice">
          <el-input-number v-model="editEvent.minPrice" placeholder="最低票价" :min="0" />
        </el-form-item>
        <el-form-item label="赛事封面" prop="cover">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/events/upload"
            :show-file-list="false"
            :on-success="handleEditCoverSuccess"
            :before-upload="beforeCoverUpload"
            :on-error="handleUploadError"
            name="file"
            accept="image/jpeg,image/png"
          >
            <img v-if="editEvent.cover" :src="getCoverUrl(editEvent.cover)" class="avatar" alt="赛事封面" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="category">
          <el-select v-model="editEvent.category" placeholder="赛事类别">
            <el-option label="球类运动" value="球类运动" />
            <el-option label="田径" value="田径" />
            <el-option label="跳水/游泳" value="跳水/游泳" />
            <el-option label="格斗搏击" value="格斗搏击" />
            <el-option label="电竞" value="电竞" />
            <el-option label="综合运动" value="综合运动" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否热门" prop="hot">
          <el-switch
            v-model="editEvent.hot"
            active-text="热门"
            inactive-text="普通"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editEventDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditEvent">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  Calendar,
  Ticket,
  ShoppingCart,
  ArrowDown,
  Plus,
  Search,
  Service,
  User
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const router = useRouter()
const activeMenu = ref('events')

// 从 localStorage 获取管理员信息（使用 computed 确保始终同步）
const adminUsername = computed(() => {
  return localStorage.getItem('admin_username') || '管理员'
})
const adminRole = computed(() => {
  return localStorage.getItem('admin_role') || 'ADMIN'
})
const userAvatar = '/admin.png'

const isSuperAdmin = computed(() => {
  return localStorage.getItem('admin_role') === 'SUPER_ADMIN'
})

// 赛事数据
const events = ref<any[]>([])

const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterCity = ref('')
const isSearching = ref(false)
const searchKeywordCache = ref('')
const filterCityCache = ref('')

// 过滤后的赛事数据
const filteredEvents = computed(() => {
  let result = events.value

  // 按关键词搜索（名称、场馆、类别）
  if (isSearching.value && searchKeywordCache.value.trim()) {
    const query = searchKeywordCache.value.trim().toLowerCase()
    result = result.filter((e: any) => {
      const nameMatch = e.name?.toLowerCase().includes(query)
      const venueMatch = e.venue?.toLowerCase().includes(query)
      const categoryMatch = e.category?.toLowerCase().includes(query)
      return nameMatch || venueMatch || categoryMatch
    })
  }

  // 按城市筛选
  if (isSearching.value && filterCityCache.value) {
    result = result.filter((e: any) => e.city === filterCityCache.value)
  }

  return result
})

// 分页后的赛事数据
const paginatedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredEvents.value.slice(start, end)
})

// 对话框状态
const addEventDialogVisible = ref(false)
const editEventDialogVisible = ref(false)
const eventFormRef = ref<any>()
const editEventFormRef = ref<any>()
const newEvent = reactive({
  name: '',
  time: '',
  venue: '',
  city: '',
  minPrice: 0,
  cover: '',
  category: '',
  hot: false,
  status: '未发售'
})
const editEvent = reactive({
  id: 0,
  name: '',
  time: '',
  venue: '',
  city: '',
  minPrice: 0,
  cover: '',
  category: '',
  hot: false,
  status: '未发售'
})

const eventRules = {
  name: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  time: [{ required: true, message: '请输入时间', trigger: 'blur' }],
  venue: [{ required: true, message: '请输入场馆', trigger: 'blur' }],
  city: [{ required: true, message: '请选择城市', trigger: 'blur' }],
  minPrice: [{ required: true, message: '请输入最低票价', trigger: 'blur' }],
  cover: [{ required: true, message: '请输入封面图片URL', trigger: 'blur' }],
  category: [{ required: true, message: '请选择赛事类别', trigger: 'blur' }]
}

const handleMenuSelect = (key: string) => {
  activeMenu.value = key
  switch (key) {
    case 'dashboard':
      router.push('/admin/dashboard')
      break
    case 'events':
      router.push('/admin/events')
      break
    case 'tickets':
      router.push('/admin/tickets')
      break
    case 'orders':
      router.push('/admin/orders')
      break
    case 'after-sales':
      router.push('/admin/orders')
      break
    case 'users':
      router.push('/admin/users')
      break
    case 'admins':
      router.push('/admin/admins')
      break
  }
}

const handleLogout = () => {
  localStorage.removeItem('admin_isLoggedIn')
  localStorage.removeItem('admin_username')
  localStorage.removeItem('admin_id')
  localStorage.removeItem('admin_role')
  ElMessage.success('已退出登录')
  router.push('/admin')
}

const goToHome = () => {
  router.push('/')
}

const openAddEventDialog = () => {
  // 清空表单数据
  newEvent.name = ''
  newEvent.time = ''
  newEvent.venue = ''
  newEvent.city = ''
  newEvent.minPrice = 0
  newEvent.cover = ''
  newEvent.category = ''
  newEvent.hot = false
  // 重置表单验证
  if (eventFormRef.value) {
    eventFormRef.value.resetFields()
  }
  addEventDialogVisible.value = true
}

const addEvent = async () => {
  try {
    const eventData = {
      name: newEvent.name,
      time: newEvent.time,
      venue: newEvent.venue,
      city: newEvent.city,
      minPrice: newEvent.minPrice,
      cover: newEvent.cover,
      category: newEvent.category,
      hot: newEvent.hot
    }
    await api.post('/events', eventData)
    ElMessage.success('赛事添加成功')
    addEventDialogVisible.value = false
    // 重置表单
    newEvent.name = ''
    newEvent.time = ''
    newEvent.venue = ''
    newEvent.city = ''
    newEvent.minPrice = 0
    newEvent.cover = ''
    newEvent.category = ''
    newEvent.hot = false
    // 刷新列表
    await fetchEvents()
  } catch (error: any) {
    ElMessage.error('添加赛事失败：' + (error.response?.data || error.message))
  }
}

const deleteEvent = (id: number) => {
  ElMessageBox.confirm('确定要删除这个赛事吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('删除赛事:', id)
      const response = await api.delete(`/events/${id}`)
      console.log('删除赛事响应:', response.data)
      ElMessage.success('赛事删除成功')
      // 刷新列表
      await fetchEvents()
    } catch (error: any) {
      console.error('删除赛事失败:', error)
      ElMessage.error('删除赛事失败：' + (error.response?.data || error.message))
    }
  })
}

const openEditEventDialog = (event: any) => {
  // 填充编辑表单
  editEvent.id = event.id
  editEvent.name = event.name
  editEvent.time = event.time
  editEvent.venue = event.venue
  editEvent.city = event.city
  editEvent.minPrice = event.minPrice
  editEvent.cover = event.cover
  editEvent.category = event.category
  editEvent.hot = event.hot
  editEvent.status = event.status
  editEventDialogVisible.value = true
}

const saveEditEvent = async () => {
  try {
    const eventData = {
      name: editEvent.name,
      time: editEvent.time,
      venue: editEvent.venue,
      city: editEvent.city,
      minPrice: editEvent.minPrice,
      cover: editEvent.cover,
      category: editEvent.category,
      hot: editEvent.hot,
      status: editEvent.status
    }
    console.log('更新赛事:', editEvent.id, eventData)
    const response = await api.put(`/events/${editEvent.id}`, eventData)
    console.log('更新赛事响应:', response.data)
    ElMessage.success('赛事更新成功')
    editEventDialogVisible.value = false
    // 刷新列表
    await fetchEvents()
  } catch (error: any) {
    console.error('更新赛事失败:', error)
    ElMessage.error('更新赛事失败：' + (error.response?.data || error.message))
  }
}

// 封面上传成功回调 - 添加赛事
const handleAddCoverSuccess = (response: any) => {
  newEvent.cover = response.url
  ElMessage.success('封面上传成功')
}

// 封面上传成功回调 - 编辑赛事
const handleEditCoverSuccess = (response: any) => {
  editEvent.cover = response.url
  ElMessage.success('封面上传成功')
}

// 封面上传前的校验
const beforeCoverUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt8M = file.size / 1024 / 1024 < 8

  if (!isJPG && !isPNG) {
    ElMessage.error('封面图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt8M) {
    ElMessage.error('封面图片大小不能超过 8MB!')
    return false
  }
  return true
}

// 上传失败回调
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
  ElMessage.error('封面上传失败，请检查网络连接或后端服务是否正常')
}

// 获取封面图片完整URL
const getCoverUrl = (cover: string) => {
  if (!cover) return ''
  if (cover.startsWith('http')) {
    return cover
  }
  // 直接使用返回的URL路径
  return `http://localhost:8080${cover}`
}

// 从后端获取赛事列表
const fetchEvents = async () => {
  try {
    const response = await api.get('/events')
    console.log('获取赛事列表:', response.data)
    events.value = response.data.data || []
  } catch (error: any) {
    console.error('获取赛事列表失败:', error)
    ElMessage.error('获取赛事列表失败：' + (error.response?.data || error.message))
  }
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
}

// 搜索
const handleSearch = () => {
  // 保存搜索条件到缓存
  searchKeywordCache.value = searchKeyword.value
  filterCityCache.value = filterCity.value
  isSearching.value = true
  currentPage.value = 1 // 重置到第一页
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  filterCity.value = ''
  searchKeywordCache.value = ''
  filterCityCache.value = ''
  isSearching.value = false
  currentPage.value = 1
}

// 页面加载时从后端获取赛事数据
onMounted(() => {
  fetchEvents()
})
</script>

<style scoped>
.admin-events {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 60px;
  background: #1e3a8a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo img {
  height: 40px;
}

.logo span {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.home-btn {
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
  height: 40px;
  display: flex;
  align-items: center;
}

.home-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
  color: #fff;
}

.user-dropdown-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  flex-shrink: 0;
}

.username {
  color: #fff;
}

.admin-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.admin-sidebar {
  width: 200px;
  background: #f8fafc;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.menu-scrollable {
  flex: 1;
  overflow-y: auto;
}

.menu-fixed {
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.admin-menu {
  border-right: none;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f1f5f9;
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

.search-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
}

.search-input {
  width: 280px;
}

.city-select {
  width: 140px;
}

.events-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count {
  color: #64748b;
  font-size: 14px;
}

.event-cover {
  width: 80px;
  height: 60px;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>