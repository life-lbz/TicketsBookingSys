<template>
  <div class="admin-dashboard">
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
            <span class="username">{{ adminUsername }}</span>
            <el-tag v-if="isSuperAdmin" type="danger" size="small" class="role-tag">超级管理员</el-tag>
            <el-tag v-else type="primary" size="small" class="role-tag">管理员</el-tag>
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
        <el-card shadow="never" class="welcome-card">
          <div class="welcome-header">
            <span>欢迎回来，{{ adminUsername }}</span>
          </div>
        </el-card>

        <!-- 数据概览 -->
        <div class="stats-grid">
          <el-card shadow="never" class="stat-card" @click="toggleEventsList">
            <div class="stat-item" style="cursor: pointer;">
              <el-icon class="stat-icon"><Calendar /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.events }}</div>
                <div class="stat-label">赛事总数</div>
              </div>
            </div>
          </el-card>
          <el-card shadow="never" class="stat-card" @click="toggleSeatedEventsList">
            <div class="stat-item" style="cursor: pointer;">
              <el-icon class="stat-icon"><Ticket /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.tickets }}</div>
                <div class="stat-label">票务</div>
              </div>
            </div>
          </el-card>
          <el-card shadow="never" class="stat-card" @click="toggleOrdersList">
            <div class="stat-item" style="cursor: pointer;">
              <el-icon class="stat-icon"><ShoppingCart /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ stats.orders }}</div>
                <div class="stat-label">订单总数</div>
              </div>
            </div>
          </el-card>
          <el-card shadow="never" class="stat-card" @click="toggleSalesData">
            <div class="stat-item" style="cursor: pointer;">
              <el-icon class="stat-icon"><Money /></el-icon>
              <div class="stat-content">
                <div class="stat-value">¥{{ stats.revenue }}</div>
                <div class="stat-label">总销售额</div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 赛事名称列表 -->
        <div v-if="showEventsList" class="events-list-container">
          <el-card shadow="never" class="events-list-card">
            <template #header>
              <div class="card-header">
                <span>赛事名称列表</span>
                <el-button type="primary" size="small" @click="toggleEventsList">关闭</el-button>
              </div>
            </template>
            <el-table :data="paginatedEvents" style="width: 100%; text-align: center;">
              <el-table-column prop="id" label="赛事ID" width="80" />
              <el-table-column prop="name" label="赛事名称" min-width="180" />
              <el-table-column prop="time" label="时间" min-width="150" />
              <el-table-column prop="venue" label="场馆" min-width="150" />
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, prev, pager, next"
                :total="eventsList.length"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
        </div>

        <!-- 已生成座位的赛事列表 -->
        <div v-if="showSeatedEventsList" class="events-list-container">
          <!-- 大表囊括三个状态的赛事 -->
          <el-card shadow="never" class="events-list-card">
            <template #header>
              <div class="card-header">
                <span>票务</span>
                <el-button type="primary" size="small" @click="toggleSeatedEventsList">关闭</el-button>
              </div>
            </template>
            
            <!-- 未发售/停售赛事表 -->
            <div v-if="unpublishedEvents.length > 0" class="status-section">
              <h3 class="section-title">未发售/停售</h3>
              <el-table :data="unpublishedEvents" style="width: 100%; text-align: center;">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="名称" min-width="180" />
                <el-table-column prop="totalSeats" label="总座位" width="100" />
                <el-table-column prop="lockedSeats" label="锁定座位" width="100" />
                <el-table-column prop="soldSeats" label="已售座位" width="100" />
                <el-table-column prop="availableSeats" label="可选座位" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getTagType(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button v-if="scope.row.status === '未发售'" type="primary" size="small" @click="handleSell(scope.row)">发售</el-button>
                    <el-button v-else-if="scope.row.status === '停售'" type="success" size="small" @click="handlePublish(scope.row)">发售</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="unpublishedCurrentPage"
                  v-model:page-size="statusPageSize"
                  layout="total, prev, pager, next"
                  :total="unpublishedEventsAll.length"
                  @current-change="handleUnpublishedCurrentChange"
                />
              </div>
            </div>
            
            <!-- 售票中赛事表 -->
            <div v-if="sellingEvents.length > 0" class="status-section">
              <h3 class="section-title">售票中</h3>
              <el-table :data="sellingEvents" style="width: 100%; text-align: center;">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="名称" min-width="180" />
                <el-table-column prop="totalSeats" label="总座位" width="100" />
                <el-table-column prop="lockedSeats" label="锁定座位" width="100" />
                <el-table-column prop="soldSeats" label="已售座位" width="100" />
                <el-table-column prop="availableSeats" label="可选座位" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getTagType(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button type="danger" size="small" @click="handlePublish(scope.row)">停售</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="sellingCurrentPage"
                  v-model:page-size="statusPageSize"
                  layout="total, prev, pager, next"
                  :total="sellingEventsAll.length"
                  @current-change="handleSellingCurrentChange"
                />
              </div>
            </div>
            
            <!-- 售罄赛事表 -->
            <div v-if="soldOutEvents.length > 0" class="status-section">
              <h3 class="section-title">售罄</h3>
              <el-table :data="soldOutEvents" style="width: 100%; text-align: center;">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="名称" min-width="180" />
                <el-table-column prop="totalSeats" label="总座位" width="100" />
                <el-table-column prop="lockedSeats" label="锁定座位" width="100" />
                <el-table-column prop="soldSeats" label="已售座位" width="100" />
                <el-table-column prop="availableSeats" label="可选座位" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getTagType(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                <template #default>
                  <el-button type="warning" size="small" disabled>售罄</el-button>
                </template>
              </el-table-column>
              </el-table>
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="soldOutCurrentPage"
                  v-model:page-size="statusPageSize"
                  layout="total, prev, pager, next"
                  :total="soldOutEventsAll.length"
                  @current-change="handleSoldOutCurrentChange"
                />
              </div>
            </div>
          </el-card>
        </div>

        <!-- 赛事订单数列表 -->
        <div v-if="showOrdersList" class="events-list-container">
          <el-card shadow="never" class="events-list-card">
            <template #header>
              <div class="card-header">
                <span>赛事订单统计</span>
                <el-button type="primary" size="small" @click="toggleOrdersList">关闭</el-button>
              </div>
            </template>
            
            <el-table :data="paginatedEventsWithOrders" style="width: 100%; text-align: center;">
              <el-table-column prop="id" label="赛事ID" width="100" />
              <el-table-column prop="name" label="赛事名称" min-width="300" />
              <el-table-column prop="orderCount" label="订单数" width="120" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewEventOrders(scope.row.id, scope.row.name)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="ordersCurrentPage"
                v-model:page-size="ordersPageSize"
                layout="prev, pager, next"
                :total="eventsWithOrders.length"
                @current-change="handleOrdersCurrentChange"
              />
            </div>
          </el-card>
        </div>

        <!-- 销售数据列表 -->
        <div v-if="showSalesList" class="events-list-container">
          <el-card shadow="never" class="events-list-card">
            <template #header>
              <div class="card-header">
                <span>赛事销售数据</span>
                <el-button type="primary" size="small" @click="toggleSalesData">关闭</el-button>
              </div>
            </template>
            <div v-if="salesDataLoading" class="loading-container">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <el-table v-else :data="paginatedSalesData" style="width: 100%; text-align: center;">
              <el-table-column prop="eventId" label="赛事ID" width="100" />
              <el-table-column prop="eventName" label="赛事名称" min-width="200" />
              <el-table-column prop="totalSales" label="销售额" width="150">
                <template #default="scope">
                  ¥{{ scope.row.totalSales.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="orderCount" label="订单数" width="100" />
              <el-table-column prop="viewCount" label="观看人数" width="100" />
              <el-table-column prop="salesPercentage" label="销售百分比" width="150">
                <template #default="scope">
                  {{ scope.row.salesPercentage.toFixed(2) }}%
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="salesCurrentPage"
                v-model:page-size="salesPageSize"
                layout="prev, pager, next"
                :total="salesData.length"
                @current-change="handleSalesCurrentChange"
              />
            </div>
          </el-card>
        </div>

        <!-- 赛事订单详情弹窗 -->
        <el-dialog
          v-model="eventOrdersDialogVisible"
          :title="currentEventName + ' 订单总览'"
          width="80%"
          destroy-on-close
        >
          <el-table :data="eventOrders" style="width: 100%; text-align: center;">
            <el-table-column prop="username" label="用户" width="150" />
            <el-table-column prop="orderNo" label="订单号" min-width="200" />
            <el-table-column label="座位" min-width="200">
              <template #default="scope">
                <span v-if="scope.row.seatNumbers">
                  {{ scope.row.seatNumbers }}
                </span>
                <span v-else-if="scope.row.seats && typeof scope.row.seats === 'string'">
                  {{ scope.row.seats }}
                </span>
                <span v-else-if="scope.row.seats && scope.row.seats.length > 0">
                  <span v-for="(seat, index) in scope.row.seats" :key="index">
                    {{ seat && typeof seat === 'object' ? (seat.rowNum || '?') + '排' + (seat.colNum || '?') + '座' : seat }}
                    <span v-if="index < scope.row.seats.length - 1">、</span>
                  </span>
                </span>
                <span v-else-if="scope.row.seatIds">
                  {{ scope.row.seatIds }}
                </span>
                <span v-else>
                  无座位信息
                </span>
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="120">
              <template #default="scope">
                <el-tag :type="getOrderStatusTagType(scope.row.status)">
                  {{ getOrderStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-dialog>
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  HomeFilled, 
  Calendar, 
  Ticket, 
  ShoppingCart, 
  ArrowDown,
  Money,
  User,
  Loading,
  Service
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'


const router = useRouter()
const route = useRoute()
const activeMenu = ref('dashboard')
const userAvatar = '/admin.png'

// 获取管理员信息
const adminUsername = computed(() => {
  return localStorage.getItem('admin_username') || '管理员'
})

const isSuperAdmin = computed(() => {
  return localStorage.getItem('admin_role') === 'SUPER_ADMIN'
})

const stats = reactive({
  events: 0,
  tickets: 1500,
  orders: 892,
  revenue: 458000
})

// 获取总销售额
const fetchTotalRevenue = async () => {
  try {
    // 直接从订单数据中计算总销售额
    const ordersResponse = await api.get('/orders')
    const orders = ordersResponse.data.data
    
    // 计算总销售额
    const totalRevenue = orders.reduce((sum: number, order: any) => sum + (order.totalPrice || 0), 0)
    stats.revenue = totalRevenue
    console.log('计算总销售额成功:', totalRevenue)
  } catch (error: any) {
    console.error('获取总销售额失败:', error.message)
  }
}

const showEventsList = ref(false)
const eventsList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

// 已生成座位的赛事列表
const showSeatedEventsList = ref(false)
const seatedEventsList = ref<any[]>([])
const seatedCurrentPage = ref(1)
const totalSeatsCount = ref(0)

// 赛事订单数列表
const showOrdersList = ref(false)
const eventsWithOrders = ref<any[]>([])
const ordersCurrentPage = ref(1)
const ordersPageSize = ref(6)

// 销售数据列表
const showSalesList = ref(false)
const salesData = ref<any[]>([])
const salesDataLoading = ref(false)
const salesCurrentPage = ref(1)
const salesPageSize = ref(8)

// 分页后的赛事订单数据
const paginatedEventsWithOrders = computed(() => {
  const start = (ordersCurrentPage.value - 1) * ordersPageSize.value
  const end = start + ordersPageSize.value
  return eventsWithOrders.value.slice(start, end)
})

// 分页后的销售数据
const paginatedSalesData = computed(() => {
  const start = (salesCurrentPage.value - 1) * salesPageSize.value
  const end = start + salesPageSize.value
  return salesData.value.slice(start, end)
})

// 分页相关变量
const unpublishedCurrentPage = ref(1)
const sellingCurrentPage = ref(1)
const soldOutCurrentPage = ref(1)
const statusPageSize = ref(5)

// 按状态过滤赛事列表
const unpublishedEventsAll = computed(() => {
  return seatedEventsList.value
    .filter(event => event.status === '未发售' || event.status === '停售')
    .sort((a, b) => {
      // 停售状态优先于未发售状态
      if (a.status === '停售' && b.status === '未发售') return -1
      if (a.status === '未发售' && b.status === '停售') return 1
      return 0
    })
})

const sellingEventsAll = computed(() => {
  return seatedEventsList.value.filter(event => event.status === '售票中')
})

const soldOutEventsAll = computed(() => {
  return seatedEventsList.value.filter(event => event.status === '售罄')
})

// 分页后的数据
const unpublishedEvents = computed(() => {
  const start = (unpublishedCurrentPage.value - 1) * statusPageSize.value
  const end = start + statusPageSize.value
  return unpublishedEventsAll.value.slice(start, end)
})

const sellingEvents = computed(() => {
  const start = (sellingCurrentPage.value - 1) * statusPageSize.value
  const end = start + statusPageSize.value
  return sellingEventsAll.value.slice(start, end)
})

const soldOutEvents = computed(() => {
  const start = (soldOutCurrentPage.value - 1) * statusPageSize.value
  const end = start + statusPageSize.value
  return soldOutEventsAll.value.slice(start, end)
})

// 切换赛事列表显示
const toggleEventsList = async () => {
  if (showEventsList.value) {
    // 如果当前列表已经显示，则关闭
    showEventsList.value = false
  } else {
    // 如果当前列表未显示，则显示，并关闭其他列表
    showEventsList.value = true
    showSeatedEventsList.value = false // 关闭已生成座位的赛事列表
    showOrdersList.value = false // 关闭订单列表
    showSalesList.value = false // 关闭销售数据列表
    try {
      const eventsResponse = await api.get('/events')
      eventsList.value = eventsResponse.data.data || []
      currentPage.value = 1 // 重置到第一页
    } catch (error: any) {
      console.error('获取赛事列表失败:', error)
      ElMessage.error('获取赛事列表失败')
    }
  }
}

// 获取标签类型
const getTagType = (status: string) => {
  switch (status) {
    case '未发售':
      return 'info'
    case '售票中':
      return 'success'
    case '停售':
      return 'warning'
    case '售罄':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取订单状态标签类型
const getOrderStatusTagType = (status: string) => {
  switch (status) {
    case '待支付':
    case 'PENDING':
      return 'warning'
    case '已完成':
    case 'COMPLETED':
      return 'success'
    case '已取消':
    case 'CANCELLED':
    case 'TIMEOUT':
    case '超时':
    case 'REFUNDED':
    case '已退款':
      return 'danger'
    default:
      return ''
  }
}

// 获取订单状态中文文本
const getOrderStatusText = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待支付'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    case 'TIMEOUT':
      return '超时'
    case 'REFUNDED':
      return '已退款'
    default:
      return status
  }
}

// 发售赛事
const handleSell = async (event: any) => {
  // 这里可以实现发售赛事的逻辑
  console.log('发售赛事:', event)
  
  try {
    await ElMessageBox.confirm('确定要发布此赛事到用户端吗？', '发布确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 用户确认发布
    event.status = '售票中'
    event.isReadyToSell = true
    await api.put(`/events/${event.id}`, event)
    ElMessage.success('赛事已发布到用户端')
    
    // 检查分页状态，确保删除后分页有效
    updatePaginationAfterStatusChange()
  } catch (error: any) {
    // 用户取消发布
    if (error !== 'cancel') {
      console.error('发布赛事失败:', error)
      ElMessage.error('发布赛事失败：' + (error.response?.data || error.message))
    }
  }
}

// 检查赛事时间是否已过期
const isEventExpired = (eventTime: string): boolean => {
  try {
    // 支持多种时间格式解析
    const formats = ['yyyy-MM-dd HH:mm:ss', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd HH:mm:ss', 'yyyy/MM/dd HH:mm']
    let eventDateTime: Date | null = null
    
    for (const _format of formats) {
      try {
        // 简单的日期解析
        const parts = eventTime.replace(/[\/T]/g, '-').split(/[- :]/)
        if (parts.length >= 6) {
          const year = parseInt(parts[0])
          const month = parseInt(parts[1]) - 1
          const day = parseInt(parts[2])
          const hour = parseInt(parts[3])
          const minute = parseInt(parts[4])
          eventDateTime = new Date(year, month, day, hour, minute)
          break
        }
      } catch (e) {
      }
    }
    
    if (!eventDateTime) {
      // 尝试直接解析
      eventDateTime = new Date(eventTime)
    }
    
    const now = new Date()
    return eventDateTime < now
  } catch (error) {
    console.error('解析赛事时间失败:', error)
    return false
  }
}

// 发布/停售赛事
const handlePublish = async (event: any) => {
  // 这里可以实现发布或停售赛事的逻辑
  console.log('发布/停售赛事:', event)
  
  try {
    if (event.status === '售票中') {
      // 停售逻辑
      event.status = '停售'
      await api.put(`/events/${event.id}`, event)
      ElMessage.success('赛事已停售')
      
      // 更新售票中列表的分页状态
      updateSellingPagination()
    } else if (event.status === '停售') {
      // 重新发售逻辑 - 检查赛事时间是否已过期
      if (isEventExpired(event.time)) {
        ElMessage.warning('该赛事时间已过，无法重新发售')
        return
      }
      event.status = '售票中'
      await api.put(`/events/${event.id}`, event)
      ElMessage.success('赛事已重新发售')
      
      // 检查分页状态，确保删除后分页有效
      updatePaginationAfterStatusChange()
    } else {
      // 发布逻辑
      if (!event.isReadyToSell) {
        ElMessage.warning('请先点击发售按钮准备赛事')
        return
      }
      // 检查赛事时间是否已过期
      if (isEventExpired(event.time)) {
        ElMessage.warning('该赛事时间已过，无法发布')
        return
      }
      // 发布到用户端
      event.status = '售票中'
      await api.put(`/events/${event.id}`, event)
      ElMessage.success('赛事已发布到用户端')
      
      // 检查分页状态，确保删除后分页有效
      updatePaginationAfterStatusChange()
    }
  } catch (error: any) {
    console.error('更新赛事状态失败:', error)
    ElMessage.error('更新赛事状态失败：' + (error.response?.data || error.message))
  }
}

// 更新未发售/停售列表的分页状态
const updatePaginationAfterStatusChange = () => {
  // 计算当前页应该显示的数据
  const currentPageData = unpublishedEvents.value
  
  // 如果当前页数据为空且当前页码大于1，则回退到上一页
  if (currentPageData.length === 0 && unpublishedCurrentPage.value > 1) {
    unpublishedCurrentPage.value = Math.max(1, unpublishedCurrentPage.value - 1)
  }
}

// 更新售票中列表的分页状态
const updateSellingPagination = () => {
  // 计算当前页应该显示的数据
  const currentPageData = sellingEvents.value
  
  // 如果当前页数据为空且当前页码大于1，则回退到上一页
  if (currentPageData.length === 0 && sellingCurrentPage.value > 1) {
    sellingCurrentPage.value = Math.max(1, sellingCurrentPage.value - 1)
  }
}

// 切换已生成座位的赛事列表显示
const toggleSeatedEventsList = async () => {
  if (showSeatedEventsList.value) {
    // 如果当前列表已经显示，则关闭
    showSeatedEventsList.value = false
  } else {
    // 如果当前列表未显示，则显示，并关闭其他列表
    showSeatedEventsList.value = true
    showEventsList.value = false // 关闭赛事列表
    showOrdersList.value = false // 关闭订单列表
    showSalesList.value = false // 关闭销售数据列表
    try {
      // 获取所有赛事
      const eventsResponse = await api.get('/events')
      const allEvents = eventsResponse.data.data || []
      
      // 获取所有座位
      const seatsResponse = await api.get('/seats')
      const allSeats = seatsResponse.data.data || []
      
      // 统计每个赛事的座位数和状态
      const eventSeatStats = new Map<number, {
        total: number,
        locked: number,
        sold: number,
        available: number
      }>()
      
      allSeats.forEach((seat: any) => {
        const eventId = seat.eventId
        if (!eventSeatStats.has(eventId)) {
          eventSeatStats.set(eventId, {
            total: 0,
            locked: 0,
            sold: 0,
            available: 0
          })
        }
        
        const stats = eventSeatStats.get(eventId)!;
        stats.total++;
        
        // 根据座位状态进行统计
        if (seat.status === 'LOCKED') {
          stats.locked++;
        } else if (seat.status === 'SOLD') {
          stats.sold++;
        } else if (seat.status === 'AVAILABLE') {
          stats.available++;
        }
        
        eventSeatStats.set(eventId, stats);
      })
      
      // 过滤出有座位的赛事
      const seatedEvents = allEvents.filter((event: any) => eventSeatStats.has(event.id))
      
      // 添加座位统计信息到赛事对象
      seatedEvents.forEach((event: any) => {
        const stats = eventSeatStats.get(event.id)!;
        event.totalSeats = stats.total;
        event.lockedSeats = stats.locked;
        event.soldSeats = stats.sold;
        event.availableSeats = stats.available;
        // 初始化发售状态
        event.isReadyToSell = false;
        // 计算状态
        if (!event.status) {
          // 初始状态为未发售
          event.status = '未发售';
        } else if (stats.available === 0) {
          event.status = '售罄';
        }
      })
      
      seatedEventsList.value = seatedEvents
      // 计算总座位数
      totalSeatsCount.value = seatedEvents.reduce((total: number, event: any) => total + (event.totalSeats || 0), 0)
      seatedCurrentPage.value = 1 // 重置到第一页
    } catch (error: any) {
      console.error('获取已生成座位的赛事列表失败:', error)
      ElMessage.error('获取已生成座位的赛事列表失败')
    }
  }
}

// 计算分页后的赛事数据
const paginatedEvents = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return eventsList.value.slice(startIndex, endIndex)
})

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
}

// 分页事件处理函数
const handleUnpublishedCurrentChange = (val: number) => {
  unpublishedCurrentPage.value = val
}

const handleSellingCurrentChange = (val: number) => {
  sellingCurrentPage.value = val
}

const handleSoldOutCurrentChange = (val: number) => {
  soldOutCurrentPage.value = val
}

// 处理订单列表页码变化
const handleOrdersCurrentChange = (val: number) => {
  ordersCurrentPage.value = val
}

// 处理销售数据列表页码变化
const handleSalesCurrentChange = (val: number) => {
  salesCurrentPage.value = val
}

// 获取实时统计数据
const fetchStats = async () => {
  try {
    // 获取赛事总数
    const eventsResponse = await api.get('/events')
    stats.events = (eventsResponse.data.data || []).length
    
    // 获取票务总数
    const seatsResponse = await api.get('/seats')
    const allSeats = seatsResponse.data.data || []
    // 计算可选座位数
    const availableSeats = allSeats.filter((seat: any) => seat.status === 'AVAILABLE')
    stats.tickets = availableSeats.length
    
    // 获取订单总数
    const ordersResponse = await api.get('/orders/count')
    stats.orders = ordersResponse.data.data.count
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 监听路由参数变化，强制刷新数据
watch(() => route.query, () => {
  fetchStats()
  fetchTotalRevenue()
}, { immediate: false })

// 组件挂载时获取数据
onMounted(() => {
  fetchStats()
  fetchTotalRevenue()
  // 定时刷新数据，每30秒更新一次
  const interval = setInterval(() => {
    fetchStats()
    fetchTotalRevenue()
  }, 30000)
  
  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(interval)
  })
})

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
      router.push('/admin/orders?tab=after-sales')
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
  // 跳转到用户端首页
  router.push('/')
}

// 切换赛事订单数列表显示
const toggleOrdersList = () => {
  if (showOrdersList.value) {
    // 如果当前列表已经显示，则关闭
    showOrdersList.value = false
  } else {
    // 如果当前列表未显示，则显示，并关闭其他列表
    showOrdersList.value = true
    showEventsList.value = false // 关闭赛事列表
    showSeatedEventsList.value = false // 关闭票务列表
    showSalesList.value = false // 关闭销售数据列表
    fetchEventsWithOrders()
  }
}

// 获取赛事订单数
const fetchEventsWithOrders = async () => {
  try {
    // 获取所有赛事
    const eventsResponse = await api.get('/events')
    const allEvents = eventsResponse.data.data || []
    
    // 获取所有订单
    const ordersResponse = await api.get('/orders')
    const allOrders = ordersResponse.data.data || []
    
    // 统计每个赛事的订单数
    const eventOrderCounts = allEvents.map((event: any) => {
      const orderCount = allOrders.filter((order: any) => order.eventId === event.id).length
      return {
        id: event.id,
        name: event.name,
        orderCount: orderCount
      }
    })
    
    // 过滤掉订单数为0的赛事
    const eventsWithOrdersOnly = eventOrderCounts.filter((event: any) => event.orderCount > 0)
    
    // 按订单数降序排序
    eventsWithOrdersOnly.sort((a: any, b: any) => b.orderCount - a.orderCount)
    
    eventsWithOrders.value = eventsWithOrdersOnly
  } catch (error) {
    console.error('获取赛事订单数失败:', error)
    ElMessage.error('获取赛事订单数失败')
  }
}

// 赛事订单详情弹窗
const eventOrdersDialogVisible = ref(false)
const eventOrders = ref<any[]>([])
const currentEventName = ref('')

// 查看赛事订单详情
const viewEventOrders = async (eventId: number, eventName: string) => {
  currentEventName.value = eventName
  try {
    // 获取该赛事的所有订单
    const ordersResponse = await api.get('/orders')
    const allOrders = ordersResponse.data.data || []
    let filteredOrders = allOrders.filter((order: any) => order.eventId === eventId)
    
    // 按订单状态排序：待支付 > 超时 > 已完成
    filteredOrders.sort((a: any, b: any) => {
      const statusOrder: Record<string, number> = {
        'PENDING': 0,
        '待支付': 0,
        'TIMEOUT': 1,
        '超时': 1,
        'COMPLETED': 2,
        '已完成': 2
      }
      const statusA = statusOrder[a.status] !== undefined ? statusOrder[a.status] : 3
      const statusB = statusOrder[b.status] !== undefined ? statusOrder[b.status] : 3
      return statusA - statusB
    })
    
    eventOrders.value = filteredOrders
    eventOrdersDialogVisible.value = true
  } catch (error) {
    console.error('获取赛事订单详情失败:', error)
    ElMessage.error('获取赛事订单详情失败')
  }
}

// 切换销售数据列表
const toggleSalesData = async () => {
  showSalesList.value = !showSalesList.value
  showEventsList.value = false // 关闭赛事列表
  showSeatedEventsList.value = false // 关闭已生成座位的赛事列表
  showOrdersList.value = false // 关闭订单列表
  if (showSalesList.value) {
    await fetchSalesData()
  }
}

// 获取销售数据
const fetchSalesData = async () => {
  salesDataLoading.value = true
  try {
    // 直接从订单和赛事数据中计算销售数据
    console.log('开始获取销售数据')
    
    // 获取所有赛事
    const eventsResponse = await api.get('/events')
    let events = eventsResponse.data.data || []
    // 仅显示已发售的赛事
    events = events.filter((event: any) => event.status === 'PUBLISHED' || event.status === '售票中')
    
    // 获取所有订单
    const ordersResponse = await api.get('/orders')
    const orders = ordersResponse.data.data || []
    console.log('获取订单成功，数量:', orders.length)
    
    // 获取所有座位
    const seatsResponse = await api.get('/seats')
    const seats = seatsResponse.data.data || []
    console.log('获取座位成功，数量:', seats.length)
    
    // 计算每个赛事的销售额和销售百分比
    const salesDataMap = new Map<number, any>()
    
    // 初始化销售数据
    events.forEach((event: any) => {
      salesDataMap.set(event.id, {
        eventId: event.id,
        eventName: event.name,
        totalSales: 0,
        orderCount: 0,
        viewCount: 0,
        salesPercentage: 0
      })
    })
    
    // 计算销售额
    orders.forEach((order: any) => {
      if (order.eventId && salesDataMap.has(order.eventId)) {
        const salesData = salesDataMap.get(order.eventId)
        salesData.totalSales += order.totalPrice || 0
        salesData.orderCount++
        salesData.viewCount++ // 假设每个订单对应一个人
      }
    })
    
    // 计算每个赛事的销售百分比
    events.forEach((event: any) => {
      if (salesDataMap.has(event.id)) {
        const eventSeats = seats.filter((seat: any) => seat.eventId === event.id)
        const totalSeats = eventSeats.length
        const soldSeats = eventSeats.filter((seat: any) => seat.status === 'SOLD').length
        const lockedSeats = eventSeats.filter((seat: any) => seat.status === 'LOCKED').length
        
        // 计算销售百分比：（已售座位+锁定座位）/总座位*100%
        let salesPercentage = 0
        if (totalSeats > 0) {
          salesPercentage = ((soldSeats + lockedSeats) / totalSeats) * 100
        }
        
        const salesData = salesDataMap.get(event.id)
        salesData.salesPercentage = salesPercentage
      }
    })
    
    // 转换为数组并按销售额降序排序
    const salesDataArray = Array.from(salesDataMap.values())
    salesDataArray.sort((a, b) => b.totalSales - a.totalSales)
    
    console.log('计算销售数据成功，数据长度:', salesDataArray.length)
    console.log('销售数据:', salesDataArray)
    salesData.value = salesDataArray
    // 重置分页
    salesCurrentPage.value = 1
  } catch (error: any) {
    console.error('获取销售数据失败:', error.message)
    ElMessage.error('获取销售数据失败')
  } finally {
    salesDataLoading.value = false
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  font-size: 16px;
  color: #606266;
}

.loading-icon {
  margin-right: 10px;
  animation: spin 1s infinite linear;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

.role-tag {
  margin-left: 4px;
}

.admin-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.menu-scrollable {
  flex: 1;
  overflow-y: auto;
}

.menu-fixed {
  border-top: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.admin-menu {
  border-right: none;
  padding: 10px 0;
}

.admin-content {
  flex: 1;
  padding: 20px;
  background: #f5f7fa;
  overflow-y: auto;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-header {
  font-size: 18px;
  font-weight: bold;
  color: #1e3a8a;
  text-align: center;
  padding: 15px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 40px;
  color: #3b82f6;
  background: #eff6ff;
  padding: 10px;
  border-radius: 8px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1e3a8a;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.events-list-container {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #1e3a8a;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #3b82f6;
}

.status-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e3a8a;
  margin-bottom: 15px;
  padding-left: 5px;
  border-left: 4px solid #3b82f6;
}

</style>
