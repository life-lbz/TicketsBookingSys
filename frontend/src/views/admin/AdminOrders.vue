cd<template>
  <div class="admin-orders">
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
        <!-- 订单管理 -->
        <div v-if="activeMenu === 'orders'">
          <div class="page-header">
            <h2>订单管理</h2>
            <el-button type="primary" @click="exportOrders">
              <el-icon><Download /></el-icon>
              导出订单
            </el-button>
          </div>

          <!-- 搜索和筛选 -->
          <div class="search-filter">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号、赛事或用户名"
              :prefix-icon="Search"
              class="search-input"
              clearable
              @keyup.enter="fetchOrders"
            />
            <el-select v-model="filterStatus" placeholder="订单状态" clearable class="status-select">
              <el-option label="全部" value="" />
              <el-option label="待支付" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-date-picker
              v-model="singleDate"
              type="date"
              placeholder="选择日期"
              class="date-picker"
            />
            <el-button type="primary" @click="fetchOrders">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>

          <!-- 订单列表 - 待支付 -->
          <el-card shadow="never" class="orders-card" v-loading="loading" style="margin-bottom: 20px;">
            <template #header>
              <div class="card-header">
                <span>待支付订单</span>
                <div>
                  <span class="count">({{ pendingOrders.length }} 个订单)</span>
                </div>
              </div>
            </template>
            <el-table :data="paginatedPendingOrders" style="width: 100%; text-align: center;" v-loading="loading">
              <el-table-column prop="username" label="用户" width="120" align="center" />
              <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
              <el-table-column prop="eventId" label="赛事ID" width="100" align="center" />
              <el-table-column prop="eventName" label="赛事名称" align="center" />
              <el-table-column prop="seats" label="座位" width="200" align="center" />
              <el-table-column prop="totalPrice" label="总金额" width="100" align="center">
                <template #default="scope">
                  ¥{{ scope.row.totalPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center" />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="scope">
                  <el-button size="small" @click="viewOrder(scope.row)">
                    查看
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteOrder(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页组件 -->
            <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
              <el-pagination
                v-model:current-page="pendingCurrentPage"
                v-model:page-size="pendingPageSize"
                layout="total, prev, pager, next"
                :total="pendingOrders.length"
                @size-change="handlePendingSizeChange"
                @current-change="handlePendingCurrentChange"
              />
            </div>
          </el-card>

          <!-- 订单列表 - 售后中 -->
          <el-card shadow="never" class="orders-card" v-loading="loading" style="margin-bottom: 20px;">
            <template #header>
              <div class="card-header">
                <span>售后中订单</span>
                <div>
                  <span class="count">({{ afterSalesOrders.length }} 个订单)</span>
                </div>
              </div>
            </template>
            <el-table :data="paginatedAfterSalesOrders" style="width: 100%; text-align: center;" v-loading="loading">
              <el-table-column prop="username" label="用户" width="120" align="center" />
              <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
              <el-table-column prop="eventId" label="赛事ID" width="100" align="center" />
              <el-table-column prop="eventName" label="赛事名称" align="center" />
              <el-table-column prop="seats" label="座位" width="200" align="center" />
              <el-table-column prop="totalPrice" label="总金额" width="100" align="center">
                <template #default="scope">
                  ¥{{ scope.row.totalPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center" />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="scope">
                  <el-button size="small" @click="viewOrder(scope.row)">
                    查看
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteOrder(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页组件 -->
            <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
              <el-pagination
              v-model:current-page="afterSalesCurrentPage"
              v-model:page-size="afterSalesOrderPageSize"
              layout="total, prev, pager, next"
              :total="afterSalesOrders.length"
              @size-change="handleAfterSalesSizeChange"
              @current-change="handleAfterSalesCurrentChange"
            />
            </div>
          </el-card>

          <!-- 订单列表 - 超时 -->
          <el-card shadow="never" class="orders-card" v-loading="loading" style="margin-bottom: 20px;">
            <template #header>
              <div class="card-header">
                <span>超时订单</span>
                <div>
                  <span class="count">({{ timeoutOrders.length }} 个订单)</span>
                </div>
              </div>
            </template>
            <el-table :data="paginatedTimeoutOrders" style="width: 100%; text-align: center;" v-loading="loading">
              <el-table-column prop="username" label="用户" width="120" align="center" />
              <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
              <el-table-column prop="eventId" label="赛事ID" width="100" align="center" />
              <el-table-column prop="eventName" label="赛事名称" align="center" />
              <el-table-column prop="seats" label="座位" width="200" align="center" />
              <el-table-column prop="totalPrice" label="总金额" width="100" align="center">
                <template #default="scope">
                  ¥{{ scope.row.totalPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center" />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="scope">
                  <el-button size="small" @click="viewOrder(scope.row)">
                    查看
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteOrder(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页组件 -->
            <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
              <el-pagination
                v-model:current-page="timeoutCurrentPage"
                v-model:page-size="timeoutPageSize"
                layout="total, prev, pager, next"
                :total="timeoutOrders.length"
                @size-change="handleTimeoutSizeChange"
                @current-change="handleTimeoutCurrentChange"
              />
            </div>
          </el-card>

          <!-- 订单列表 - 已完成 -->
          <el-card shadow="never" class="orders-card" v-loading="loading" style="margin-bottom: 20px;">
            <template #header>
              <div class="card-header">
                <span>已完成订单</span>
                <div>
                  <span class="count">({{ completedOrders.length }} 个订单)</span>
                </div>
              </div>
            </template>
            <el-table :data="paginatedCompletedOrders" style="width: 100%; text-align: center;" v-loading="loading">
              <el-table-column prop="username" label="用户" width="120" align="center" />
              <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
              <el-table-column prop="eventId" label="赛事ID" width="100" align="center" />
              <el-table-column prop="eventName" label="赛事名称" align="center" />
              <el-table-column prop="seats" label="座位" width="200" align="center" />
              <el-table-column prop="totalPrice" label="总金额" width="100" align="center">
                <template #default="scope">
                  ¥{{ scope.row.totalPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center" />
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="scope">
                  <el-button size="small" @click="viewOrder(scope.row)">
                    查看
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteOrder(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <!-- 分页组件 -->
            <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
              <el-pagination
                v-model:current-page="completedCurrentPage"
                v-model:page-size="completedPageSize"
                layout="total, prev, pager, next"
                :total="completedOrders.length"
                @size-change="handleCompletedSizeChange"
                @current-change="handleCompletedCurrentChange"
              />
            </div>
          </el-card>
        </div>

        <!-- 售后处理 -->
        <div v-if="activeMenu === 'after-sales'">
          <div class="page-header">
            <h2>售后处理</h2>
          </div>
          
          <!-- 待处理售后请求 -->
          <el-card shadow="never" class="after-sales-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>待处理售后请求</span>
                <div>
                  <span class="count">({{ pendingAfterSales.length }} 个请求)</span>
                </div>
              </div>
            </template>
            <div class="after-sales-content">
              <el-table :data="paginatedPendingAfterSales" style="width: 100%; text-align: center;">
                <el-table-column prop="id" label="售后ID" width="80" align="center" />
                <el-table-column prop="username" label="用户" width="100" align="center" />
                <el-table-column prop="type" label="售后类型" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getAfterSalesType(scope.row.type)">{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="问题描述" align="center" />
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="getAfterSalesStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180" align="center">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right" align="center">
                  <template #default="scope">
                    <el-button 
                      size="small" 
                      @click="handleAfterSales(scope.row)"
                      :disabled="scope.row.status === '已处理' || scope.row.status === '已拒绝'"
                    >
                      处理
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 分页组件 -->
              <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
                <el-pagination
                  v-model:current-page="pendingAfterSalesCurrentPage"
                  v-model:page-size="afterSalesPageSize"
                  layout="total, prev, pager, next"
                  :total="pendingAfterSales.length"
                  @size-change="handlePendingAfterSalesSizeChange"
                  @current-change="handlePendingAfterSalesCurrentChange"
                />
              </div>
            </div>
          </el-card>
          
          <!-- 已处理售后请求 -->
          <el-card shadow="never" class="after-sales-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>已处理售后请求</span>
                <div>
                  <span class="count">({{ processedAfterSales.length }} 个请求)</span>
                </div>
              </div>
            </template>
            <div class="after-sales-content">
              <el-table :data="paginatedProcessedAfterSales" style="width: 100%; text-align: center;">
                <el-table-column prop="id" label="售后ID" width="80" align="center" />
                <el-table-column prop="username" label="用户" width="100" align="center" />
                <el-table-column prop="type" label="售后类型" width="120" align="center">
                  <template #default="scope">
                    <el-tag :type="getAfterSalesType(scope.row.type)">{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="问题描述" align="center" />
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag :type="getAfterSalesStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="创建时间" width="180" align="center">
                  <template #default="scope">
                    {{ formatDateTime(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right" align="center">
                  <template #default="scope">
                    <el-button 
                      size="small" 
                      @click="handleAfterSales(scope.row)"
                      :disabled="scope.row.status === '已处理' || scope.row.status === '已拒绝'"
                    >
                      处理
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 分页组件 -->
              <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: center; align-items: center;">
                <el-pagination
                  v-model:current-page="processedAfterSalesCurrentPage"
                  v-model:page-size="afterSalesPageSize"
                  layout="total, prev, pager, next"
                  :total="processedAfterSales.length"
                  @size-change="handleProcessedAfterSalesSizeChange"
                  @current-change="handleProcessedAfterSalesCurrentChange"
                />
              </div>
            </div>
          </el-card>
        </div>
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  HomeFilled, 
  Calendar, 
  Ticket, 
  ShoppingCart, 
  ArrowDown,
  Search,
  Download,
  User,
  Service
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const router = useRouter()
const route = useRoute()

// 根据路由参数初始化 activeMenu
const tab = route.query.tab as string
const activeMenu = ref(tab === 'after-sales' ? 'after-sales' : 'orders')
const loading = ref(false)

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

// 订单数据
const orders = ref<any[]>([])

// 按状态分类的订单
const pendingOrders = computed(() => {
  return orders.value.filter(order => order.status === 'PENDING')
})

const afterSalesOrders = computed(() => {
  return orders.value.filter(order => order.status === 'AFTER_SALES')
})

const timeoutOrders = computed(() => {
  return orders.value.filter(order => order.status === 'TIMEOUT')
})

const completedOrders = computed(() => {
  return orders.value.filter(order => order.status === 'COMPLETED')
})

// 待支付订单分页
const pendingCurrentPage = ref(1)
const pendingPageSize = ref(5)
const paginatedPendingOrders = computed(() => {
  const start = (pendingCurrentPage.value - 1) * pendingPageSize.value
  const end = start + pendingPageSize.value
  return pendingOrders.value.slice(start, end)
})

// 售后中订单分页
const afterSalesCurrentPage = ref(1)
const afterSalesOrderPageSize = ref(5)
const paginatedAfterSalesOrders = computed(() => {
  const start = (afterSalesCurrentPage.value - 1) * afterSalesOrderPageSize.value
  const end = start + afterSalesOrderPageSize.value
  return afterSalesOrders.value.slice(start, end)
})

// 超时订单分页
const timeoutCurrentPage = ref(1)
const timeoutPageSize = ref(5)
const paginatedTimeoutOrders = computed(() => {
  const start = (timeoutCurrentPage.value - 1) * timeoutPageSize.value
  const end = start + timeoutPageSize.value
  return timeoutOrders.value.slice(start, end)
})

// 已完成订单分页
const completedCurrentPage = ref(1)
const completedPageSize = ref(5)
const paginatedCompletedOrders = computed(() => {
  const start = (completedCurrentPage.value - 1) * completedPageSize.value
  const end = start + completedPageSize.value
  return completedOrders.value.slice(start, end)
})

const searchKeyword = ref('')
const filterStatus = ref('')
const singleDate = ref<any>(null)

// 售后处理相关
const afterSalesRequests = ref<any[]>([])
const currentAfterSales = ref<any>(null)

// 按状态分类的售后请求
const pendingAfterSales = computed(() => {
  return afterSalesRequests.value.filter(request => request.status === '待处理')
})

const processedAfterSales = computed(() => {
  return afterSalesRequests.value.filter(request => request.status === '已处理' || request.status === '已拒绝')
})

// 售后请求分页
const afterSalesPageSize = ref(6)

// 待处理售后请求分页
const pendingAfterSalesCurrentPage = ref(1)
const paginatedPendingAfterSales = computed(() => {
  const start = (pendingAfterSalesCurrentPage.value - 1) * afterSalesPageSize.value
  const end = start + afterSalesPageSize.value
  return pendingAfterSales.value.slice(start, end)
})

// 已处理售后请求分页
const processedAfterSalesCurrentPage = ref(1)
const paginatedProcessedAfterSales = computed(() => {
  const start = (processedAfterSalesCurrentPage.value - 1) * afterSalesPageSize.value
  const end = start + afterSalesPageSize.value
  return processedAfterSales.value.slice(start, end)
})

// 获取售后请求列表
const fetchAfterSalesRequests = async () => {
  try {
    console.log('开始获取售后请求...')
    const response = await api.get('/api/after-sales')
    console.log('获取售后请求成功:', response.data)
    // 后端返回的是ApiResponse格式，需要从data.data中获取实际数据
    const data = response.data.data || []
    afterSalesRequests.value = Array.isArray(data) ? data : []
    console.log('售后请求数据:', afterSalesRequests.value)
  } catch (error: any) {
    console.error('获取售后请求失败:', error)
    ElMessage.error('获取售后请求失败')
  }
}

// 分页相关事件处理
// 待支付订单分页
const handlePendingSizeChange = (size: number) => {
  pendingPageSize.value = size
  pendingCurrentPage.value = 1
}

const handlePendingCurrentChange = (current: number) => {
  pendingCurrentPage.value = current
}

// 售后中订单分页
const handleAfterSalesSizeChange = (size: number) => {
  afterSalesOrderPageSize.value = size
  afterSalesCurrentPage.value = 1
}

const handleAfterSalesCurrentChange = (current: number) => {
  afterSalesCurrentPage.value = current
}

// 超时订单分页
const handleTimeoutSizeChange = (size: number) => {
  timeoutPageSize.value = size
  timeoutCurrentPage.value = 1
}

const handleTimeoutCurrentChange = (current: number) => {
  timeoutCurrentPage.value = current
}

// 已完成订单分页
const handleCompletedSizeChange = (size: number) => {
  completedPageSize.value = size
  completedCurrentPage.value = 1
}

const handleCompletedCurrentChange = (current: number) => {
  completedCurrentPage.value = current
}

// 待处理售后请求分页
const handlePendingAfterSalesSizeChange = (size: number) => {
  afterSalesPageSize.value = size
  pendingAfterSalesCurrentPage.value = 1
}

const handlePendingAfterSalesCurrentChange = (current: number) => {
  pendingAfterSalesCurrentPage.value = current
}

// 已处理售后请求分页
const handleProcessedAfterSalesSizeChange = (size: number) => {
  afterSalesPageSize.value = size
  processedAfterSalesCurrentPage.value = 1
}

const handleProcessedAfterSalesCurrentChange = (current: number) => {
  processedAfterSalesCurrentPage.value = current
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
      // 售后处理页面，保持在当前页面但切换内容
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
  localStorage.removeItem('admin_role')
  localStorage.removeItem('admin_id')
  localStorage.removeItem('admin_token')
  ElMessage.success('已退出登录')
  router.push('/admin')
}

const goToHome = () => {
  router.push('/')
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    console.log('开始获取订单列表...')
    const response = await api.get('/orders')
    console.log('获取订单列表成功:', response.data)
    // 后端返回的是ApiResponse格式，需要从data.data中获取实际数据
    let result = response.data.data || []
    console.log('订单数据:', result)
    
    // 确保result是数组
    if (!Array.isArray(result)) {
      result = []
    }
    
    // 按关键词搜索（订单号、赛事名称、用户名）
    if (searchKeyword.value.trim()) {
      const query = searchKeyword.value.trim().toLowerCase()
      result = result.filter((order: any) => {
        const orderNoMatch = order.orderNo?.toLowerCase().includes(query)
        const eventNameMatch = order.eventName?.toLowerCase().includes(query)
        const usernameMatch = order.username?.toLowerCase().includes(query)
        return orderNoMatch || eventNameMatch || usernameMatch
      })
    }
    
    // 按状态筛选
    if (filterStatus.value) {
      result = result.filter((order: any) => order.status === filterStatus.value)
    }
    
    // 按单日筛选
    if (singleDate.value) {
      const selectedDate = new Date(singleDate.value)
      const startOfDay = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate()).getTime()
      const endOfDay = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate(), 23, 59, 59, 999).getTime()
      result = result.filter((order: any) => {
        const orderDate = new Date(order.createTime).getTime()
        return orderDate >= startOfDay && orderDate <= endOfDay
      })
    }
    
    // 按创建时间倒序排列
    result.sort((a: any, b: any) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
    
    orders.value = result
  } catch (error: any) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 导出订单
const exportOrders = () => {
  if (orders.value.length === 0) {
    ElMessage.warning('没有可导出的订单')
    return
  }
  
  // 创建CSV内容
  const headers = ['用户名', '订单号', '赛事ID', '赛事名称', '座位', '总金额', '状态', '创建时间', '支付方式']
  const rows = orders.value.map((order: any) => [
    order.username || '-',
    order.orderNo,
    order.eventId,
    order.eventName,
    order.seats,
    order.totalPrice,
    getStatusText(order.status),
    order.createTime,
    order.paymentMethod || '-'
  ])
  
  const csvContent = [headers.join(','), ...rows.map((row: any[]) => row.join(','))].join('\n')
  
  // 创建下载链接
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = `订单列表_${new Date().toLocaleDateString()}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('订单导出成功')
}

const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger',
    'TIMEOUT': 'danger',
    'AFTER_SALES': 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    'PENDING': '待支付',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'TIMEOUT': '超时',
    'AFTER_SALES': '售后中',
    'REFUNDED': '已退款'
  }
  return textMap[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 查看订单详情
const viewOrder = (order: any) => {
  const htmlContent: string = [
    '<div style="line-height: 1.8;">',
    `<p><strong>订单号：</strong>${order.orderNo}</p>`,
    `<p><strong>用户名：</strong>${order.username || '-'}</p>`,
    `<p><strong>赛事名称：</strong>${order.eventName}</p>`,
    `<p><strong>场馆：</strong>${order.venue}</p>`,
    `<p><strong>座位：</strong>${order.seats}</p>`,
    `<p><strong>总金额：</strong>¥${order.totalPrice}</p>`,
    `<p><strong>状态：</strong>${getStatusText(order.status)}</p>`,
    `<p><strong>创建时间：</strong>${formatDateTime(order.createTime)}</p>`,
    `<p><strong>支付时间：</strong>${formatDateTime(order.payTime)}</p>`,
    `<p><strong>支付方式：</strong>${order.paymentMethod || '-'}</p>`,
    '</div>'
  ].join('')
  
  ;(ElMessageBox as any).alert(
    htmlContent,
    '订单详情',
    {
      confirmButtonText: '关闭',
      dangerouslyUseHTMLString: true,
      width: '500px'
    }
  )
}

// 删除订单
const deleteOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个订单吗？此操作不可恢复！', {
      title: '警告',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await api.delete(`/orders/${id}`)
    ElMessage.success('订单已删除')
    await fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }
}



// 售后处理相关函数
const handleAfterSales = (afterSales: any) => {
  currentAfterSales.value = afterSales
  
  // 处理退款请求
  if (afterSales.type === '退款') {
    ;(ElMessageBox as any).confirm(
      `<div style="padding: 10px 0;">
        <div style="margin-bottom: 10px; font-size: 16px; font-weight: 500;">确定要处理退款请求吗？</div>
        <div style="margin-bottom: 8px; display: flex; align-items: center;">
          <span style="width: 80px; font-weight: 500;">用户：</span>
          <span>${afterSales.username}</span>
        </div>
        <div style="margin-bottom: 8px; display: flex; align-items: center;">
          <span style="width: 80px; font-weight: 500;">赛事：</span>
          <span>${afterSales.eventName}</span>
        </div>
        <div style="margin-bottom: 8px; display: flex; align-items: flex-start;">
          <span style="width: 80px; font-weight: 500; margin-top: 4px;">问题描述：</span>
          <span>${afterSales.description}</span>
        </div>
      </div>`,
      '处理退款请求',
      {
        confirmButtonText: '同意退款',
        cancelButtonText: '拒绝退款',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        customClass: 'refund-dialog',
        center: true
      }
    ).then(async () => {
      try {
        // 调用后端API处理退款
        await api.put(`/api/after-sales/${afterSales.id}/process`, {
          status: '已处理',
          type: '退款'
        })
        // 刷新售后请求列表和订单列表
        await fetchAfterSalesRequests()
        await fetchOrders()
        ElMessage.success('退款处理成功')
      } catch (error: any) {
        console.error('处理退款请求失败:', error)
        ElMessage.error('处理退款请求失败')
      }
    }).catch(async (error: any) => {
      // 用户点击了拒绝退款按钮
      if (error === 'cancel') {
        // 拒绝退款
        try {
          await api.put(`/api/after-sales/${afterSales.id}`, {}, {
            params: { status: '已拒绝' }
          })
          // 刷新售后请求列表和订单列表
          await fetchAfterSalesRequests()
          await fetchOrders()
          ElMessage.success('已拒绝退款请求')
        } catch (error: any) {
          console.error('拒绝退款请求失败:', error)
          ElMessage.error('拒绝退款请求失败')
        }
      }
    })
  } 

  // 处理其他类型的售后请求
  else {
    ;(ElMessageBox as any).prompt(
      `请输入处理结果（类型：${afterSales.type}）\n用户：${afterSales.username}\n赛事：${afterSales.eventName}\n问题描述：${afterSales.description}`,
      '处理售后请求',
      {
        confirmButtonText: '已处理',
        cancelButtonText: '已拒绝',
        inputPlaceholder: '请输入处理结果'
      }
    ).then(async ({ value }: { value: string }) => {
      if (value) {
        try {
          // 调用后端API更新售后状态
          await api.put(`/api/after-sales/${afterSales.id}`, {}, {
            params: { status: '已处理' }
          })
          // 刷新售后请求列表和订单列表
          await fetchAfterSalesRequests()
          await fetchOrders()
          ElMessage.success('售后处理成功')
        } catch (error: any) {
          console.error('处理售后请求失败:', error)
          ElMessage.error('处理售后请求失败')
        }
      }
    }).catch(async (error: any) => {
      // 检查是否是用户点击了取消按钮
      if (error !== 'cancel') {
        // 拒绝请求
        try {
          await api.put(`/api/after-sales/${afterSales.id}`, {}, {
            params: { status: '已拒绝' }
          })
          // 刷新售后请求列表和订单列表
          await fetchAfterSalesRequests()
          await fetchOrders()
          ElMessage.success('已拒绝售后请求')
        } catch (error: any) {
          console.error('拒绝售后请求失败:', error)
          ElMessage.error('拒绝售后请求失败')
        }
      }
    })
  }
}

const getAfterSalesType = (type: string): string => {
  const typeMap: Record<string, string> = {
    '退款': 'danger',
    '换票': 'warning',
    '投诉': 'info'
  }
  return typeMap[type] || 'info'
}

const getAfterSalesStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    '待处理': 'warning',
    '已处理': 'success',
    '已拒绝': 'danger'
  }
  return typeMap[status] || 'info'
}

// 移除自动监听，改为点击搜索按钮手动刷新

// 组件挂载时获取订单数据
onMounted(() => {
  fetchOrders()
  fetchAfterSalesRequests()
  
  // 定期刷新订单和售后请求列表（每5秒刷新一次）
  const refreshInterval = setInterval(async () => {
    await fetchOrders()
    await fetchAfterSalesRequests()
  }, 5000)
  
  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(refreshInterval)
  })
})
</script>

<style scoped>
.admin-orders {
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
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.status-select {
  width: 140px;
}

.date-picker {
  width: 180px;
}

.orders-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 600;
  font-size: 16px;
}

.count {
  color: #64748b;
  font-weight: 400;
  font-size: 14px;
}

.pagination-container {
  margin-top: 16px;
}

/* 售后处理样式 */
.after-sales-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.after-sales-content {
  padding-top: 16px;
}
</style>