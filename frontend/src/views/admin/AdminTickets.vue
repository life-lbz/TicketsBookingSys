<template>
  <div class="admin-tickets">
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
          <h2>票务管理</h2>
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
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button v-if="searchKeyword || filterCity" @click="clearSearch">
            清除
          </el-button>
        </div>

        <!-- 搜索到的赛事列表 -->
        <div class="search-results-section" v-if="showSearchSection && isSearching && filteredEvents.length > 0">
          <div class="search-results-header">
            <h3>搜索到的赛事</h3>
            <span class="search-results-count">共 {{ filteredEvents.length }} 个赛事</span>
          </div>
          <el-table :data="paginatedEvents" style="width: 100%" class="events-table" @row-click="handleEventSelect">
            <el-table-column prop="id" label="赛事ID" width="80" />
            <el-table-column prop="name" label="赛事名称" min-width="200" />
            <el-table-column prop="city" label="城市" width="100" />
            <el-table-column prop="venue" label="场馆" min-width="150" />
            <el-table-column prop="category" label="类别" width="100" />
            <el-table-column prop="time" label="时间" min-width="150" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click.stop="selectEvent(scope.row)">选择</el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页组件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="searchCurrentPage"
              :page-size="searchPageSize"
              :total="filteredEvents.length"
              layout="total, prev, pager, next"
              @current-change="handleSearchCurrentChange"
            />
          </div>
        </div>

        <div class="search-results-section" v-else-if="showSearchSection && isSearching && filteredEvents.length === 0">
          <div class="no-search-results">
            <el-icon :size="48" color="#909399"><Search /></el-icon>
            <p>未找到匹配的赛事</p>
            <el-button type="primary" plain @click="clearSearch">清除搜索</el-button>
          </div>
        </div>

        <!-- 已选赛事信息 -->
        <div class="selected-event-section" v-if="selectedEvent">
          <div class="selected-event-header">
            <h3>当前管理赛事</h3>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="generateSeats" :disabled="selectedEvent && (selectedEvent.status === '售票中' || selectedEvent.status === '售罄')">
                <el-icon><Plus /></el-icon>
                生成座位
              </el-button>
              <el-button type="danger" size="small" @click="deleteAllSeats" v-if="allSeats.length > 0" :disabled="selectedEvent && (selectedEvent.status === '售票中' || selectedEvent.status === '售罄')">
                <el-icon><Delete /></el-icon>
                删除全部座位
              </el-button>
              <el-button link type="primary" @click="changeEvent">更换赛事</el-button>
            </div>
          </div>
          <el-card class="selected-event-card" shadow="hover">
            <div class="selected-event-info">
              <div class="event-detail">
                <h4>{{ selectedEvent.name }}</h4>
                <p><el-icon><Location /></el-icon> {{ selectedEvent.city }} | {{ selectedEvent.venue }}</p>
                <p><el-icon><Calendar /></el-icon> {{ selectedEvent.time }}</p>
                <p><el-icon><Ticket /></el-icon> {{ selectedEvent.category }}</p>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 座位管理 -->
        <el-card shadow="never" class="tickets-card">
          <template #header>
            <div class="card-header">
              <span>座位管理</span>
              <span class="count">({{ total }} 个座位)</span>
            </div>
          </template>
          
          <!-- 座位状态统计 -->
          <div class="seat-stats">
            <el-statistic :value="availableSeats" title="可选座位" />
            <el-statistic :value="soldSeats" title="已售座位" />
            <el-statistic :value="lockedSeats" title="锁定座位" />
          </div>

          <!-- 座位图 -->
          <div class="seat-chart" ref="seatChartRef"></div>

          <!-- 座位检索 -->
          <div class="seat-search-section" v-if="selectedEvent">
            <div class="seat-search-title">座位检索</div>
            <div class="seat-search-form">
              <el-input-number 
                v-model="seatSearchRow" 
                :min="1" 
                :max="50" 
                placeholder="排数"
                class="seat-search-input"
                clearable
              />
              <el-input-number 
                v-model="seatSearchCol" 
                :min="1" 
                :max="50" 
                placeholder="座数"
                class="seat-search-input"
                clearable
              />
              <el-select 
                v-model="seatSearchStatus" 
                placeholder="状态"
                clearable
                class="seat-search-select"
              >
                <el-option label="可选" value="AVAILABLE" />
                <el-option label="已售" value="SOLD" />
                <el-option label="锁定" value="LOCKED" />
              </el-select>
              <el-button type="primary" @click="searchSeats">
                <el-icon><Search /></el-icon> 检索
              </el-button>
              <el-button v-if="isSearchingSeats" @click="clearSeatSearch">
                取消检索
              </el-button>
            </div>
          </div>

          <!-- 座位列表 - 两列布局 -->
          <div class="seats-grid">
            <div v-for="seat in seats" :key="seat.id" class="seat-item">
              <div class="seat-info">
                <span class="seat-position">{{ seat.rowNum }}排{{ seat.colNum }}座</span>
                <span class="seat-price">¥{{ seat.price }}</span>
                <el-tag :type="getStatusType(seat.status)" size="small">
                  {{ getStatusText(seat.status) }}
                </el-tag>
              </div>
              <div class="seat-actions">
                <el-button size="small" @click="editSeat(seat)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteSeat(seat.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-main>
    </div>

    <!-- 生成座位对话框 -->
    <el-dialog
      v-model="generateSeatsDialogVisible"
      title="生成座位"
      width="500px"
    >
      <el-alert
        v-if="selectedEvent"
        :title="'正在为 [' + selectedEvent.name + '] 生成座位'"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      <el-form :model="generateForm" :rules="generateRules" ref="generateFormRef" label-width="100px">
        <el-form-item label="行数" prop="rows">
          <el-input-number v-model="generateForm.rows" :min="1" :max="50" style="width: 200px;" />
          <span class="form-tip">设置座位的排数（1-50排）</span>
        </el-form-item>
        <el-form-item label="列数" prop="cols">
          <el-input-number v-model="generateForm.cols" :min="1" :max="50" style="width: 200px;" />
          <span class="form-tip">设置每排的座位数（1-50座）</span>
        </el-form-item>
        <el-form-item label="票价" prop="price">
          <el-input-number v-model="generateForm.price" :min="0" :precision="2" :step="10" style="width: 200px;" />
          <span class="form-tip">设置所有座位的统一票价（元）</span>
        </el-form-item>
        <el-form-item label="预计生成">
          <el-tag type="success" size="large">{{ generateForm.rows * generateForm.cols }} 个座位</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="generateSeatsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="doGenerateSeats" :loading="generating">
            确定生成
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑座位对话框 -->
    <el-dialog
      v-model="editFormVisible"
      :title="`编辑座位`"
      width="400px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="座位" prop="position">
          <el-input 
            :value="`${currentSeat?.rowNum || ''}排${currentSeat?.colNum || ''}座`" 
            disabled 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="editForm.price" 
            :min="0" 
            :step="10" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select 
            v-model="editForm.status" 
            style="width: 100%"
          >
            <el-option label="可选" value="AVAILABLE" />
            <el-option label="已售" value="SOLD" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Calendar, Ticket, ShoppingCart, ArrowDown, Plus, User, Search, Delete, Service, Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import api from '../../api'

const router = useRouter()
const activeMenu = ref('tickets')

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
const selectedEventId = ref<any>(null)
const searchKeyword = ref('')
const filterCity = ref('')
const showSearchSection = ref(false)
const isSearching = ref(false)
const searchKeywordCache = ref('')
const filterCityCache = ref('')

// 分页相关变量
const searchCurrentPage = ref(1)
const searchPageSize = ref(10)

// 过滤后的赛事列表
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

// 分页后的赛事列表
const paginatedEvents = computed(() => {
  const start = (searchCurrentPage.value - 1) * searchPageSize.value
  const end = start + searchPageSize.value
  return filteredEvents.value.slice(start, end)
})

// 处理分页变化
const handleSearchCurrentChange = (val: number) => {
  searchCurrentPage.value = val
}

// 搜索
const handleSearch = () => {
  // 保存搜索条件到缓存
  searchKeywordCache.value = searchKeyword.value
  filterCityCache.value = filterCity.value
  isSearching.value = true
  
  // 重置分页到第一页
  searchCurrentPage.value = 1
  
  // 显示搜索结果区域
  showSearchSection.value = true
  
  // 如果当前选中的赛事不在过滤结果中，清空选择
  if (selectedEventId.value && !filteredEvents.value.find((e: any) => e.id === selectedEventId.value)) {
    selectedEventId.value = null
    seats.value = []
  }
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  filterCity.value = ''
  searchKeywordCache.value = ''
  filterCityCache.value = ''
  isSearching.value = false
  showSearchSection.value = false
  searchCurrentPage.value = 1
}

// 座位检索相关
const seatSearchRow = ref<number | null>(null)
const seatSearchCol = ref<number | null>(null)
const seatSearchStatus = ref<string>('')
const isSearchingSeats = ref(false)

// 座位检索
const searchSeats = () => {
  if (!seatSearchRow.value && !seatSearchCol.value && !seatSearchStatus.value) {
    ElMessage.warning('请至少输入一个检索条件')
    return
  }
  
  isSearchingSeats.value = true
  
  // 从所有座位中筛选
  let result = allSeats.value
  
  if (seatSearchRow.value) {
    result = result.filter((s: any) => s.rowNum === seatSearchRow.value)
  }
  
  if (seatSearchCol.value) {
    result = result.filter((s: any) => s.colNum === seatSearchCol.value)
  }
  
  if (seatSearchStatus.value) {
    result = result.filter((s: any) => s.status === seatSearchStatus.value)
  }
  
  seats.value = result
  total.value = result.length
  
  if (result.length === 0) {
    ElMessage.info('未找到符合条件的座位')
  } else {
    ElMessage.success(`找到 ${result.length} 个符合条件的座位`)
  }
}

// 取消座位检索
const clearSeatSearch = () => {
  seatSearchRow.value = null
  seatSearchCol.value = null
  seatSearchStatus.value = ''
  isSearchingSeats.value = false
  
  // 恢复默认分页显示
  updatePagedSeats()
  total.value = allSeats.value.length
  
  ElMessage.success('已取消检索，恢复默认显示')
}

// 当前选中的赛事
const selectedEvent = computed(() => {
  return events.value.find((e: any) => e.id === selectedEventId.value) || null
})

// 选择赛事
const selectEvent = (event: any) => {
  selectedEventId.value = event.id
  showSearchSection.value = false
  // 重置生成座位表单
  generateForm.rows = 10
  generateForm.cols = 15
  if (event.minPrice) {
    generateForm.price = event.minPrice
  } else {
    generateForm.price = 0
  }
  fetchSeats()
}

// 点击行选择赛事
const handleEventSelect = (row: any) => {
  selectEvent(row)
}

// 更换赛事 - 显示搜索区域
const changeEvent = () => {
  showSearchSection.value = true
  // 滚动到搜索区域
  setTimeout(() => {
    const searchSection = document.querySelector('.search-filter')
    if (searchSection) {
      searchSection.scrollIntoView({ behavior: 'smooth' })
    }
  }, 100)
}

// 座位数据
const seats = ref<any[]>([])

const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(36)

// 统计数据
const availableSeats = ref(0)
const soldSeats = ref(0)
const lockedSeats = ref(0)

// 固定的座位图行列数（生成座位时设置，删除座位后保持不变）
const fixedRows = ref(0)
const fixedCols = ref(0)

// 定时刷新间隔
const refreshInterval = ref<number | null>(null)

// 引用
const seatChartRef = ref<any>()
const generateFormRef = ref<any>()

// 从后端API获取赛事数据
const fetchEvents = async () => {
  try {
    const response = await api.get('/events')
    events.value = response.data.data || []
  } catch (error: any) {
    console.error('获取赛事列表失败:', error)
    ElMessage.error('获取赛事列表失败：' + (error.response?.data || error.message))
  }
}

// 页面加载时加载赛事数据
onMounted(() => {
  fetchEvents()
  // 初始化座位图表
  initSeatChart()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (seatChart) {
      seatChart.resize()
    }
  })

  // 启动定时刷新，每5秒刷新一次座位状态
  refreshInterval.value = window.setInterval(() => {
    if (selectedEventId.value) {
      fetchSeats()
    }
  }, 5000)
})

onUnmounted(() => {
  // 清除定时刷新
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }
  window.removeEventListener('resize', () => {
    if (seatChart) {
      seatChart.resize()
    }
  })
})

// 对话框状态
const generateSeatsDialogVisible = ref(false)
const generating = ref(false)
const generateForm = reactive({
  rows: 10,
  cols: 15,
  price: 0
})

const generateRules = {
  rows: [{ required: true, message: '请输入行数', trigger: 'blur' }],
  cols: [{ required: true, message: '请输入列数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入票价', trigger: 'blur' }]
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

const generateSeats = () => {
  // 检查是否已经有座位
  if (allSeats.value.length > 0) {
    ElMessage.warning('该赛事已生成座位，请先删除全部座位后再重新生成')
    return
  }
  
  // 使用选中赛事的最低票价作为默认值
  if (selectedEvent.value && selectedEvent.value.minPrice) {
    generateForm.price = selectedEvent.value.minPrice
  }
  generateSeatsDialogVisible.value = true
}

const doGenerateSeats = async () => {
  if (!selectedEventId.value) {
    ElMessage.warning('请先选择赛事')
    return
  }

  generating.value = true
  
  try {
    // 调用后端API生成座位
    const response = await api.post(`/events/${selectedEventId.value}/seats/generate`, {
      rows: generateForm.rows,
      cols: generateForm.cols,
      price: generateForm.price
    })
    
    // 保存固定的行列数
    fixedRows.value = generateForm.rows
    fixedCols.value = generateForm.cols
    
    ElMessage.success(`成功生成 ${response.data.count} 个座位`)
    generateSeatsDialogVisible.value = false
    
    // 重新获取座位列表
    await fetchSeats()
  } catch (error: any) {
    console.error('生成座位失败:', error)
    ElMessage.error('生成座位失败：' + (error.response?.data || error.message))
  } finally {
    generating.value = false
  }
}

// 所有座位数据（用于分页）
const allSeats = ref<any[]>([])
const editForm = ref({
  price: 0,
  status: 'AVAILABLE'
})

// 座位图表实例
let seatChart: echarts.ECharts | null = null

// 初始化座位图表
const initSeatChart = () => {
  if (seatChartRef.value) {
    seatChart = echarts.init(seatChartRef.value)
    updateSeatChart()
  }
}

// 更新座位图表
const updateSeatChart = () => {
  if (!seatChart) return
  
  if (allSeats.value.length === 0) {
    seatChart.setOption({
      title: {
        text: '暂无座位数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#909399', fontSize: 16 }
      },
      xAxis: { show: false, data: [] },
      yAxis: { show: false, data: [] },
      grid: { show: false },
      series: []
    }, true)
    return
  }
  
  const seats = allSeats.value
  // 使用固定的行列数，如果没有固定值则使用当前座位的最大值
  const rows = fixedRows.value > 0 ? fixedRows.value : Math.max(...seats.map((s: any) => s.rowNum))
  const cols = fixedCols.value > 0 ? fixedCols.value : Math.max(...seats.map((s: any) => s.colNum))
  
  const colorMap: Record<string, string> = {
    'AVAILABLE': '#67c23a',
    'SOLD': '#f56c6c',
    'LOCKED': '#909399'
  }
  
  const seatData = seats.map((seat: any) => ({
    value: [seat.colNum - 1, rows - seat.rowNum],
    itemStyle: { color: colorMap[String(seat.status)] || '#909399' }
  }))
  
  const option = {
    backgroundColor: '#fafafa',
    title: { show: false },
    tooltip: {
      position: 'top',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 13 },
      padding: [10, 15],
      extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); border-radius: 8px;',
      formatter: (params: any) => {
        const colNum = params.value[0] + 1
        const rowNum = rows - params.value[1]
        const seat = seats.find((s: any) => s.colNum === colNum && s.rowNum === rowNum)
        if (!seat) return ''
        const statusColor = seat.status === 'AVAILABLE' ? '#67c23a' : seat.status === 'SOLD' ? '#f56c6c' : '#909399'
        return `<div style="font-weight: bold; margin-bottom: 8px; color: #303133;">${seat.rowNum}排${seat.colNum}座</div>
                <div style="color: #606266; line-height: 1.8;">
                  <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: ${statusColor}; margin-right: 6px;"></span>
                  价格: <span style="color: #f56c6c; font-weight: 500;">¥${seat.price}</span><br/>
                  状态: <span style="color: ${statusColor}; font-weight: 500;">${getStatusText(seat.status)}</span>
                </div>`
      }
    },
    legend: {
      show: false
    },
    grid: {
      left: '10%',
      right: '6%',
      bottom: '10%',
      top: '14%',
      containLabel: true,
      backgroundColor: 'rgba(255, 255, 255, 0.5)',
      show: true,
      borderColor: '#e4e7ed',
      borderWidth: 1
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: cols }, (_, i) => (i + 1).toString()),
      name: '列',
      nameLocation: 'end',
      nameTextStyle: { fontSize: 13, fontWeight: 'bold', color: '#606266' },
      nameGap: 10,
      axisLine: { show: true, lineStyle: { color: '#dcdfe6', width: 2 } },
      axisTick: { show: true, alignWithLabel: true, lineStyle: { color: '#dcdfe6' } },
      axisLabel: { show: true, interval: 0, fontSize: 11, color: '#909399' },
      splitLine: { show: false },
      splitArea: { 
        show: true, 
        areaStyle: { color: ['rgba(255,255,255,0.8)', 'rgba(245,247,250,0.8)'] }
      }
    },
    yAxis: {
      type: 'category',
      data: Array.from({ length: rows }, (_, i) => (rows - i).toString()),
      name: '排',
      nameLocation: 'end',
      nameTextStyle: { fontSize: 13, fontWeight: 'bold', color: '#606266' },
      nameGap: 10,
      axisLine: { show: true, lineStyle: { color: '#dcdfe6', width: 2 } },
      axisTick: { show: true, alignWithLabel: true, lineStyle: { color: '#dcdfe6' } },
      axisLabel: { show: true, interval: 0, fontSize: 11, color: '#909399' },
      splitLine: { show: false },
      splitArea: { 
        show: true, 
        areaStyle: { color: ['rgba(255,255,255,0.8)', 'rgba(245,247,250,0.8)'] }
      }
    },
    series: [{
      name: '座位',
      type: 'scatter',
      symbolSize: 22,
      data: seatData,
      coordinateSystem: 'cartesian2d',
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        scale: 1.2
      }
    }]
  }
  
  seatChart.setOption(option, true)
}

// 获取座位列表
const fetchSeats = async () => {
  if (!selectedEventId.value) {
    seats.value = []
    allSeats.value = []
    availableSeats.value = 0
    soldSeats.value = 0
    lockedSeats.value = 0
    return
  }

  try {
    const response = await api.get(`/seats/events/${selectedEventId.value}`)
    allSeats.value = response.data.data || []
    total.value = allSeats.value.length
    
    // 如果没有固定行列数，根据当前座位数据设置
    if (allSeats.value.length > 0 && fixedRows.value === 0) {
      fixedRows.value = Math.max(...allSeats.value.map((s: any) => s.rowNum))
      fixedCols.value = Math.max(...allSeats.value.map((s: any) => s.colNum))
    }
    
    // 统计座位状态
    availableSeats.value = allSeats.value.filter((s: any) => s.status === 'AVAILABLE').length
    soldSeats.value = allSeats.value.filter((s: any) => s.status === 'SOLD').length
    lockedSeats.value = allSeats.value.filter((s: any) => s.status === 'LOCKED').length
    
    // 初始化分页
    updatePagedSeats()
    
    // 更新座位图表
    updateSeatChart()
  } catch (error: any) {
    console.error('获取座位列表失败:', error)
    ElMessage.error('获取座位列表失败：' + (error.response?.data || error.message))
    seats.value = []
    allSeats.value = []
  }
}

// 更新分页后的座位数据
const updatePagedSeats = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  seats.value = allSeats.value.slice(start, end)
}

const getStatusType = (status: string | number): string => {
  const typeMap: Record<string, string> = {
    'AVAILABLE': 'success',
    'SOLD': 'danger',
    'LOCKED': 'info'
  }
  const statusStr = String(status)
  return typeMap[statusStr] || 'info'
}

const getStatusText = (status: string | number): string => {
  const textMap: Record<string, string> = {
    'AVAILABLE': '可选',
    'SOLD': '已售',
    'LOCKED': '锁定'
  }
  const statusStr = String(status)
  return textMap[statusStr] || statusStr
}

const editFormVisible = ref(false)
const currentSeat = ref<any>(null)

const editSeat = (seat: any) => {
  currentSeat.value = seat
  editForm.value = {
    price: seat.price,
    status: seat.status
  }
  editFormVisible.value = true
}

const submitEditForm = async () => {
  if (!currentSeat.value) return
  
  try {
    const updateData = {
      price: editForm.value.price,
      status: editForm.value.status
    }
    
    const oldStatus = currentSeat.value.status
    const response: any = await api.put(`/seats/${currentSeat.value.id}`, updateData)
    const updatedSeat = response.data || response
    
    const index = allSeats.value.findIndex((s: any) => s.id === currentSeat.value.id)
    if (index !== -1) {
      if (oldStatus === 'AVAILABLE') availableSeats.value--
      else if (oldStatus === 'SOLD') soldSeats.value--
      else if (oldStatus === 'LOCKED') lockedSeats.value--
      
      allSeats.value[index] = { ...allSeats.value[index], ...updatedSeat }
      
      if (updatedSeat.status === 'AVAILABLE') availableSeats.value++
      else if (updatedSeat.status === 'SOLD') soldSeats.value++
      else if (updatedSeat.status === 'LOCKED') lockedSeats.value++
    }
    
    updatePagedSeats()
    updateSeatChart()
    
    // 通知其他页面座位状态已更新
    const bc = new BroadcastChannel('seat_updates')
    bc.postMessage({
      type: 'SEAT_UPDATED',
      eventId: selectedEventId.value,
      seatId: currentSeat.value.id,
      timestamp: Date.now()
    })
    bc.close()
    
    ElMessage.success('座位编辑成功')
    editFormVisible.value = false
  } catch (error: any) {
    console.error('编辑座位失败:', error)
    const errorMsg = error.response?.data?.message || error.response?.data || error.message || '未知错误'
    ElMessage.error('编辑失败：' + errorMsg)
  }
}

const deleteSeat = async (id: number) => {
  ElMessageBox.confirm('确定要删除这个座位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端API删除座位
      await api.delete(`/seats/${id}`)
      
      const seat = seats.value.find((s: any) => s.id === id)
      if (seat) {
        if (seat.status === 'AVAILABLE') availableSeats.value--
        else if (seat.status === 'SOLD') soldSeats.value--
        else if (seat.status === 'LOCKED') lockedSeats.value--
      }
      seats.value = seats.value.filter((s: any) => s.id !== id)
      allSeats.value = allSeats.value.filter((s: any) => s.id !== id)
      total.value--
      console.log('删除单个座位后，allSeats长度:', allSeats.value.length)
      // 强制更新图表
      setTimeout(() => {
        updateSeatChart()
        console.log('更新座位图表')
      }, 100)
      ElMessage.success('座位删除成功')
    } catch (error: any) {
      console.error('删除座位失败:', error)
      ElMessage.error('删除失败：' + (error.response?.data || error.message))
    }
  })
}

// 删除全部座位
const deleteAllSeats = () => {
  if (allSeats.value.length === 0) {
    ElMessage.warning('当前没有座位可删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除该赛事的全部 ${allSeats.value.length} 个座位吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      // 调用后端API删除全部座位
      await api.delete(`/events/${selectedEventId.value}/seats`)
      
      // 清空本地数据
      seats.value = []
      allSeats.value = []
      availableSeats.value = 0
      soldSeats.value = 0
      lockedSeats.value = 0
      total.value = 0
      
      // 重置固定行列数
      fixedRows.value = 0
      fixedCols.value = 0
      
      console.log('删除全部座位后，allSeats长度:', allSeats.value.length)
      
      // 强制更新图表
      setTimeout(() => {
        updateSeatChart()
        console.log('更新座位图表')
      }, 100)
      
      ElMessage.success('全部座位已删除')
    } catch (error: any) {
      console.error('删除全部座位失败:', error)
      ElMessage.error('删除失败：' + (error.response?.data || error.message))
    }
  })
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  updatePagedSeats()
}
</script>

<style scoped>
.admin-tickets {
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

.search-results-section {
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.search-results-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.search-results-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-results-count {
  font-size: 14px;
  color: #606266;
  background: #f0f7ff;
  padding: 4px 12px;
  border-radius: 12px;
}

.events-table {
  cursor: pointer;
}

.no-search-results {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-search-results p {
  margin: 15px 0;
  font-size: 16px;
}

.selected-event-section {
  margin-bottom: 20px;
}

.selected-event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.selected-event-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.selected-event-card {
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border: 1px solid #e6f2ff;
}

.selected-event-info {
  display: flex;
  align-items: center;
}

.event-detail h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1e3a8a;
  margin: 0 0 10px 0;
}

.event-detail p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.tickets-card {
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

/* 座位检索 */
.seat-search-section {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 20px 0;
}

.seat-search-title {
  font-weight: 600;
  font-size: 15px;
  color: #1e293b;
  margin-bottom: 12px;
}

.seat-search-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.seat-search-input {
  width: 120px;
}

.seat-search-select {
  width: 140px;
}

.seat-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
}

.seat-chart {
  height: 300px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

/* 座位三列网格布局 */
.seats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 20px;
  max-height: 600px;
  overflow-y: auto;
}

.seat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.seat-item:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.seat-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seat-position {
  font-weight: 600;
  color: #1e293b;
  min-width: 80px;
}

.seat-price {
  color: #dc2626;
  font-weight: 500;
  min-width: 60px;
}

.seat-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 10px 0;
}
</style>