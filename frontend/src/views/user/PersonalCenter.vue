<template>
  <div class="personal-center-container">
    <el-header class="header">
      <div class="logo-container" @click="$router.push('/')">
        <img src="../../assets/images/ticketsale.png" alt="Logo" class="logo-img" />
        <span class="logo-text">大型赛事门票预订与座位选择系统</span>
      </div>
      <div class="header-right">
        <el-button link @click="$router.push('/')" class="home-btn" style="margin-right: 20px;">首页</el-button>
        <div class="user-profile" style="margin-right: 20px;">
          <el-avatar :size="32" :src="currentUser?.avatar" :icon="User" />
          <span class="username">{{ currentUser?.username || '我的账户' }}</span>
        </div>
        <el-button link type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <div class="main-layout">
      <!-- 侧边栏 (左侧) -->
      <el-aside width="240px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleSelect"
        >
          <el-menu-item index="orders">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 内容区 (右侧) -->
      <main class="content-area">
        <div class="page-title">
          <h2>{{ activeTitle }}</h2>
        </div>

        <div v-if="activeMenu === 'orders'" class="order-list-page">
          <div class="filter-bar">
            <el-tabs v-model="orderStatus" @tab-click="filterOrders">
              <el-tab-pane label="全部订单" name="all" />
              <el-tab-pane label="待支付" name="pending" />
              <el-tab-pane label="已完成" name="completed" />
              <el-tab-pane label="已取消" name="cancelled" />
              <el-tab-pane label="售后处理" name="afterSales" />
            </el-tabs>
          </div>

          <div class="order-list">
            <el-card 
              v-for="order in paginatedOrders" 
              :key="order.id" 
              class="order-item-card" 
              shadow="hover"
            >
              <div class="order-item-header">
                <span class="order-time">下单时间: {{ order.time }}</span>
                <span class="order-id">订单号: {{ order.orderNo }}</span>
                <el-tag :type="getStatusTag(order.status)" size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-item-body">
                <img :src="order.cover" class="order-thumb" :alt="order.eventName" />
                <div class="order-info">
                  <h3>{{ order.eventName }}</h3>
                  <p>{{ order.venue }}</p>
                  <p class="seat-info">座位: {{ order.seats }}</p>
                  <p class="order-price">
                    <span class="amount">¥{{ order.totalPrice }}</span>
                  </p>
                </div>
                <div class="order-actions">
                  <el-button link type="primary" @click="handleViewDetails(order)">查看详情</el-button>
                  <el-button 
                    v-if="order.status === 'PENDING'" 
                    type="primary" 
                    size="small"
                    @click="handlePay(order)"
                  >
                    立即支付
                  </el-button>
                  <el-button 
                    v-if="order.status === 'PENDING'" 
                    link
                    @click="handleCancel(order)"
                  >
                    取消订单
                  </el-button>
                  <el-button 
                    v-if="order.status === 'COMPLETED'" 
                    link
                    @click="handleRefund(order)"
                  >
                    售后
                  </el-button>
                  <el-button 
                    v-if="order.status === 'AFTER_SALES'" 
                    link
                    type="danger"
                    @click="handleCancelAfterSales(order)"
                  >
                    取消售后
                  </el-button>
                  <el-button 
                    v-if="order.status === 'COMPLETED' || order.status === 'CANCELLED' || order.status === 'TIMEOUT' || order.status === 'REFUNDED'" 
                    link
                    type="danger"
                    :disabled="order.status === 'AFTER_SALES'"
                    @click="handleDeleteOrder(order)"
                  >
                    删除订单
                  </el-button>
                </div>
                <div v-if="order.status === 'PENDING'" class="countdown">
                  <span class="countdown-label">支付倒计时：</span>
                  <span class="countdown-time">{{ getCountdown(order.createTime) }}</span>
                </div>
              </div>
            </el-card>
            
            <!-- 订单分页组件 -->
            <div class="pagination" v-if="orderTotalPages > 1">
              <el-pagination
                v-model:current-page="orderCurrentPage"
                v-model:page-size="orderPageSize"
                layout="total, prev, pager, next"
                :total="filteredOrders.length"
                @current-change="handleOrderCurrentChange"
              />
            </div>
          </div>
        </div>

        <div v-else-if="activeMenu === 'favorites'" class="collection-list-page">
          <div class="collection-list">
            <el-card 
              v-for="collection in collections" 
              :key="collection.collectionId" 
              class="event-card" 
              :body-style="{ padding: '0px' }" 
              shadow="hover"
            >
              <div class="event-cover">
                <img :src="getCoverUrl(collection.id)" class="image" :alt="collection.name" />
                <div class="collection-time-tag">收藏时间: {{ collection.collectionTime }}</div>
              </div>
              <div class="event-info">
                <h4 class="event-name">{{ collection.name }}</h4>
                <div class="event-meta">
                  <p><el-icon><Calendar /></el-icon> {{ collection.time }}</p>
                  <p><el-icon><Location /></el-icon> {{ collection.venue }}</p>
                </div>
                <div class="event-footer">
                  <span class="price">¥{{ collection.price }} <small>起</small></span>
                  <div class="event-actions">
                    <el-button type="primary" size="small" @click="$router.push(`/seat-selection/${collection.id}`)">查看详情</el-button>
                    <el-button type="danger" size="small" @click="handleRemoveCollection(collection)">取消收藏</el-button>
                  </div>
                </div>
              </div>
            </el-card>
            <div v-if="collections.length === 0" class="empty-container">
              <el-empty description="暂无收藏" />
            </div>
          </div>
        </div>

        <div v-else-if="activeMenu === 'profile'" class="profile-page">
          <el-card shadow="hover" class="profile-card">

            
            <div class="profile-form">
              <div class="form-item" @click="handleChangeAvatar">
                <label class="form-label">头像</label>
                <div class="form-value avatar-container">
                  <div class="avatar-wrapper">
                    <el-avatar :size="80" class="avatar uploadable" :src="currentUser?.avatar">
                      {{ currentUser?.username?.charAt(0)?.toUpperCase() || 'U' }}
                    </el-avatar>
                    <input type="file" accept="image/*" style="display: none" ref="avatarInput" @change="handleAvatarUpload">
                  </div>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('username')">
                <label class="form-label">用户名</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.username || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('nickname')">
                <label class="form-label">昵称</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.nickname || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('gender')">
                <label class="form-label">性别</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.gender || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('birthday')">
                <label class="form-label">生日</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.birthday || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('city')">
                <label class="form-label">所在城市</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.city || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('lifeStatus')">
                <label class="form-label">生活状态</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.lifeStatus || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('industry')">
                <label class="form-label">从事行业</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.industry || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('hobbies')">
                <label class="form-label">兴趣爱好</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.hobbies || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item" @click="handleEditProfile('signature')">
                <label class="form-label">个性签名</label>
                <div class="form-value with-arrow">
                  <span>{{ currentUser?.signature || '待完善' }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">手机号码</label>
                <div class="form-value">
                  <span>{{ currentUser?.phone || '待完善' }}</span>
                </div>
              </div>
              
              <!-- 保存按钮区域 -->
              <div class="form-actions">
                <el-button type="primary" @click="saveAllProfile">保存所有修改</el-button>
              </div>
            </div>
          </el-card>
          
          <el-card shadow="hover" class="dynamic-card" style="margin-top: 20px;">
            <template #header>
              <div class="dynamic-header">
                <h2>动态</h2>
              </div>
            </template>
            
            <div class="dynamic-content">
              <div v-if="completedEvents.length === 0" class="empty-dynamic">
                <div class="empty-icon">
                  <el-empty description="暂无动态" />
                </div>
              </div>
              <div v-else class="completed-events-list">
                <el-card 
                  v-for="event in paginatedEvents" 
                  :key="event.id" 
                  class="order-item-card" 
                  shadow="hover"
                >
                  <div class="order-item-body">
                    <img :src="getCoverUrl(event.id)" class="order-thumb" :alt="event.name" />
                    <div class="order-info">
                      <h3>{{ event.name }}</h3>
                      <p>{{ event.venue }}</p>
                      <p>购票时间: {{ event.orderTime }}</p>
                      <p>座位: {{ event.seats }}</p>
                    </div>
                    <div class="order-actions">
                      <el-button link type="primary" @click="handleDynamicViewDetails(event)">查看详情</el-button>
                      <el-button link type="danger" @click="handleDeleteDynamic(event)">删除</el-button>
                    </div>
                  </div>
                </el-card>
                
                <!-- 分页组件 -->
                <div class="pagination" v-if="totalPages > 1">
                  <el-pagination
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    layout="total, prev, pager, next"
                    :total="completedEvents.length"
                    @current-change="handleCurrentChange"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </div>
        
        <el-empty v-else description="该功能正在开发中..." />
      </main>
    </div>

    <!-- 售后申请弹窗 -->
    <el-dialog
      v-model="afterSalesDialogVisible"
      title="售后申请"
      width="550px"
      center
    >
      <el-form :model="afterSalesForm" label-width="100px" style="padding: 10px 0;">
        <el-form-item label="售后类型" style="margin-bottom: 20px;">
          <el-radio-group v-model="afterSalesForm.type" size="large" style="display: flex; align-items: center;">
            <el-radio label="退款" style="font-size: 16px; margin-left: 0;">退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="问题描述" style="margin-bottom: 20px;">
          <el-input
            v-model="afterSalesForm.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述您的退款原因，例如：时间冲突、个人原因等"
            style="font-size: 14px;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer" style="display: flex; justify-content: center; gap: 20px;">
          <el-button size="large" style="padding: 10px 30px; font-size: 14px;" @click="afterSalesDialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" style="padding: 10px 30px; font-size: 14px;" @click="submitAfterSales">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 支付方式选择弹窗 -->
    <el-dialog
      v-model="payDialogVisible"
      title="选择支付方式"
      width="400px"
      center
    >
      <div v-if="currentOrderForPay" style="padding: 20px 0;">
        <div class="order-info" style="margin-bottom: 30px; padding: 20px; background-color: #f5f7fa; border-radius: 8px;">
          <p style="font-size: 16px; font-weight: bold; margin-bottom: 10px;">订单信息</p>
          <p style="margin: 5px 0;">订单号: {{ currentOrderForPay.orderNo }}</p>
          <p style="margin: 5px 0; font-size: 18px; color: #ff4d4f;">支付金额: ¥{{ currentOrderForPay.totalPrice }}</p>
        </div>
        
        <div class="payment-methods" style="margin-bottom: 30px;">
          <h3 style="font-size: 16px; font-weight: bold; margin-bottom: 15px;">选择支付方式</h3>
          <el-radio-group v-model="selectedPaymentMethod" @change="generateQRCode" size="large" style="display: flex; flex-direction: row; gap: 15px; justify-content: space-between;">
            <el-radio label="支付宝" style="flex: 1; display: block; padding: 20px; border: 2px solid #e4e7ed; border-radius: 12px; cursor: pointer; transition: all 0.3s ease; background-color: #ffffff; text-align: center;">
              <div class="payment-option" style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                <div class="payment-icon" style="margin-right: 0;">
                  <i class="el-icon-eleme" style="font-size: 36px; color: #1677FF;"></i>
                </div>
                <div class="payment-name" style="font-size: 18px; font-weight: 500;">支付宝</div>
              </div>
            </el-radio>
            <el-radio label="微信支付" style="flex: 1; display: block; padding: 20px; border: 2px solid #e4e7ed; border-radius: 12px; cursor: pointer; transition: all 0.3s ease; background-color: #ffffff; text-align: center;">
              <div class="payment-option" style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                <div class="payment-icon" style="margin-right: 0;">
                  <i class="el-icon-message" style="font-size: 36px; color: #07C160;"></i>
                </div>
                <div class="payment-name" style="font-size: 18px; font-weight: 500;">微信支付</div>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
        
        <div class="qr-code-section" v-if="qrCodeUrl" style="text-align: center; margin-bottom: 20px;">
          <h3 style="font-size: 16px; font-weight: bold; margin-bottom: 15px;">扫描二维码支付</h3>
          <div class="qr-code-container" style="display: flex; justify-content: center; margin-bottom: 15px;">
            <img :src="qrCodeUrl" alt="支付二维码" class="qr-code" style="width: 200px; height: 200px;" />
          </div>
          <p class="payment-tip" style="font-size: 14px; color: #606266;">请使用{{ selectedPaymentMethod }}扫描上方二维码进行支付</p>
        </div>
        
        <div class="loading-section" v-if="paymentLoading" style="text-align: center; padding: 30px 0;">
          <el-icon class="is-loading" style="font-size: 32px; color: #1677FF; margin-right: 10px;"><Loading /></el-icon>
          <span style="font-size: 16px;">正在生成支付二维码...</span>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer" style="display: flex; justify-content: center; gap: 20px;">
          <el-button size="large" style="padding: 10px 30px; font-size: 14px;" @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" style="padding: 10px 30px; font-size: 14px;" @click="confirmPayment" :loading="paymentLoading">确认支付</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`${eventDetails?.name || '赛事详情'}`"
      width="80%"
      top="10vh"
    >
      <div v-if="eventDetails" class="order-detail-content">
        <div class="seat-map-container">
          <div class="stage-container">
            <div class="stage-label">赛场中央</div>
            <div class="stage-line"></div>
          </div>
          <div class="seat-grid" v-if="seatMap.length > 0">
            <div class="seat-layout">
              <!-- 顶部座号标签 -->
              <div class="seat-header">
                <div class="row-label"></div>
                <div class="seat-numbers-row">
                  <div v-for="col in Math.max(...seatMap.map(s => s.colNum))" :key="`col-${col}`" class="seat-number-cell">
                    {{ col }}
                  </div>
                  <div class="seat-number-label">座号</div>
                </div>
              </div>
              <!-- 座位行 -->
              <div v-for="row in Math.max(...seatMap.map(s => s.rowNum))" :key="row" class="seat-row">
                <div class="row-label">第{{ row }}排</div>
                <div class="seat-cells">
                  <div 
                    v-for="col in Math.max(...seatMap.map(s => s.colNum))" 
                    :key="`${row}-${col}`"
                    class="seat-cell"
                  >
                    <div 
                      class="seat"
                      :class="{
                        'selected': seatMap.some(s => s.rowNum === row && s.colNum === col && s.isSelected),
                        'sold': seatMap.some(s => s.rowNum === row && s.colNum === col && s.status === 'SOLD'),
                        'locked': seatMap.some(s => s.rowNum === row && s.colNum === col && s.status === 'LOCKED'),
                        'available': seatMap.some(s => s.rowNum === row && s.colNum === col && s.status === 'AVAILABLE')
                      }"
                    ></div>
                  </div>
                </div>
              </div>
              <!-- 底部排号标签 -->
              <div class="seat-row row-number-row">
                <div class="row-label row-number-label">排号</div>
                <div class="seat-cells"></div>
              </div>
            </div>
          </div>
          <div v-else class="no-seats">暂无座位信息</div>
          
          <div class="seat-legend">
            <div class="legend-item"><span class="color-box available"></span> 可选</div>
            <div class="legend-item"><span class="color-box sold"></span> 已售</div>
            <div class="legend-item"><span class="color-box selected"></span> 已选</div>
            <div class="legend-item"><span class="color-box locked"></span> 锁定</div>
          </div>
        </div>
      </div>
      <div v-else class="loading">加载中...</div>
    </el-dialog>
    
    <!-- 编辑个人信息弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="`编辑${getFieldName(currentEditField)}`"
      width="500px"
    >
      <el-form :model="{ value: editValue }" label-width="80px">
        <el-form-item :label="getFieldName(currentEditField)">
          <!-- 性别使用下拉选择 -->
          <el-select v-if="currentEditField === 'gender'" v-model="editValue" placeholder="请选择性别">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
          <!-- 生日使用日期选择器 -->
          <el-date-picker 
            v-else-if="currentEditField === 'birthday'" 
            v-model="editValue" 
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
          <!-- 其他字段使用输入框 -->
          <el-input v-else v-model="editValue" :placeholder="`请输入${getFieldName(currentEditField)}`"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { List, Star, User, ArrowRight, Loading, Calendar, Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElDialog } from 'element-plus'
import api from '../../api'

const router = useRouter()
const activeMenu = ref('orders')
const orderStatus = ref('all')
const orders = ref<any[]>([])
const collections = ref<any[]>([])
const eventCache = ref<Record<number, any>>({})
const currentUser = reactive<any>({})
const completedEvents = ref<any[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(3)

// 订单分页相关
const orderCurrentPage = ref(1)
const orderPageSize = ref(4)

// 计算分页后的赛事列表
const paginatedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return completedEvents.value.slice(start, end)
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(completedEvents.value.length / pageSize.value)
})

// 详情弹窗相关
const dialogVisible = ref(false)
const currentOrder = ref<any>(null)
const seatMap = ref<any[]>([])
const eventDetails = ref<any>(null)

// 编辑个人信息弹窗相关
const editDialogVisible = ref(false)
const currentEditField = ref('')
const editValue = ref('')

// 获取当前用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('user_user')
  if (userStr) {
    let userData = JSON.parse(userStr)
    // 修复可能的双层包装问题
    if (userData && userData.value) {
      userData = userData.value
    }
    // 从localStorage中读取当前用户的头像信息
    if (userData.username) {
      const savedAvatar = localStorage.getItem(`avatar_${userData.username}`)
      if (savedAvatar) {
        userData.avatar = savedAvatar
      }
    }
    // 逐个属性赋值，确保Vue响应式系统能够检测到变化
    if (userData.username !== undefined) currentUser.username = userData.username
    if (userData.nickname !== undefined) currentUser.nickname = userData.nickname
    if (userData.gender !== undefined) currentUser.gender = userData.gender
    if (userData.birthday !== undefined) currentUser.birthday = userData.birthday
    if (userData.city !== undefined) currentUser.city = userData.city
    if (userData.lifeStatus !== undefined) currentUser.lifeStatus = userData.lifeStatus
    if (userData.industry !== undefined) currentUser.industry = userData.industry
    if (userData.hobbies !== undefined) currentUser.hobbies = userData.hobbies
    if (userData.signature !== undefined) currentUser.signature = userData.signature
    if (userData.phone !== undefined) currentUser.phone = userData.phone
    if (userData.avatar !== undefined) currentUser.avatar = userData.avatar
    if (userData.id !== undefined) currentUser.id = userData.id
    if (userData.isAdmin !== undefined) currentUser.isAdmin = userData.isAdmin
    if (userData.token !== undefined) currentUser.token = userData.token
  } else {
    // 清空所有属性
    Object.keys(currentUser).forEach(key => delete currentUser[key])
  }
}

// 强制更新currentUser对象，确保视图能够更新
const updateCurrentUser = (newUser: any) => {
  Object.keys(currentUser).forEach(key => delete currentUser[key])
  Object.assign(currentUser, newUser)
}

const handleLogout = () => {
  localStorage.removeItem('user_isLoggedIn')
  localStorage.removeItem('user_user')
  localStorage.removeItem('user_userId')
  localStorage.removeItem('user_username')
  localStorage.removeItem('user_isAdmin')
  localStorage.removeItem('avatar') // 清除旧的全局头像缓存
  
  // 保留头像信息，不清除avatar_${username}键
  
  ElMessage.success('已退出登录')
  router.push('/')
}

const fetchEvents = async () => {
  try {
    const response = await api.get('/events')
    const events = response.data.data || []
    // 将赛事列表转换为缓存对象
    events.forEach((event: any) => {
      eventCache.value[event.id] = event
    })
  } catch (error: any) {
    console.error('获取赛事列表失败:', error)
  }
}

const fetchOrders = async () => {
  try {
    if (!currentUser?.id) {
      ElMessage.warning('请先登录')
      return
    }
    
    // 获取所有订单，由前端进行筛选
    const response = await api.get(`/orders/user/${currentUser.id}`)
    
    // 转换订单数据，添加封面图片
    orders.value = response.data.data.map((order: any) => ({
      ...order,
      time: new Date(order.createTime).toLocaleString(),
      cover: getCoverUrl(order.eventId)
    }))
    
    // 订单更新后，更新动态部分的赛事卡片
    await fetchCompletedEvents()
  } catch (error: any) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  }
}

// 保存动态到数据库
const saveMotionsToDatabase = async () => {
  try {
    if (!currentUser?.username) {
      return
    }
    
    // 将动态转换为JSON字符串
    const motionsJson = JSON.stringify(completedEvents.value)
    
    // 调用API保存到数据库
    await api.put('/users/profile', {
      username: currentUser.username,
      motions: motionsJson
    })
    
    console.log('动态保存到数据库成功')
  } catch (error: any) {
    console.error('保存动态到数据库失败:', error)
  }
}

// 从数据库加载动态
const loadMotionsFromDatabase = async () => {
  try {
    if (!currentUser?.username) {
      return
    }
    
    // 获取用户信息，包括动态
    const response = await api.get(`/users/info/${currentUser.username}`)
    const userInfo = response.data.data || {}
    
    if (userInfo && userInfo.motions) {
      try {
        completedEvents.value = JSON.parse(userInfo.motions)
        console.log('从数据库加载动态成功')
      } catch (error) {
        console.error('解析动态数据失败:', error)
      }
    }
  } catch (error: any) {
    console.error('从数据库加载动态失败:', error)
  }
}

// 获取已完成订单的赛事
const fetchCompletedEvents = async () => {
  try {
    if (!currentUser?.id) {
      return
    }
    
    // 从订单中筛选已完成的订单
    const completedOrders = orders.value.filter(order => order.status === 'COMPLETED')
    
    // 提取赛事信息
    const events = []
    for (const order of completedOrders) {
      // 从eventCache中获取赛事信息，如果没有则从API获取
      let event = eventCache.value[order.eventId]
      if (!event) {
        try {
          const response = await api.get(`/events/${order.eventId}`)
          event = response.data
          eventCache.value[order.eventId] = event
        } catch (error) {
          console.error(`获取赛事 ${order.eventId} 失败:`, error)
          continue
        }
      }
      
      if (event) {
        events.push({
          ...event,
          orderTime: order.time,
          orderId: order.id,
          totalPrice: order.totalPrice,
          seats: order.seats
        })
      }
    }
    
    // 直接使用所有赛事，不进行去重，确保每个订单都显示
    completedEvents.value = events
    
    // 保存到数据库
    await saveMotionsToDatabase()
  } catch (error: any) {
    console.error('获取已完成赛事失败:', error)
  }
}

const fetchCollections = async () => {
  try {
    if (!currentUser?.username) {
      ElMessage.warning('请先登录')
      return
    }
    
    const response = await api.get(`/users/collections/${currentUser.username}`)
    
    // 转换收藏数据
    collections.value = response.data.data.map((collection: any) => ({
      ...collection,
      collectionTime: new Date(collection.collectionTime).toLocaleString()
    }))
  } catch (error: any) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  }
}

// 获取封面图片URL
const getCoverUrl = (eventId: number) => {
  // 直接从收藏数据中获取封面URL
  const collection = collections.value.find(c => c.id === eventId)
  if (collection?.cover) {
    if (collection.cover.startsWith('http')) {
      return collection.cover
    }
    return `http://localhost:8080${collection.cover}`
  }
  
  // 从缓存中获取
  const event = eventCache.value[eventId]
  if (event?.cover) {
    if (event.cover.startsWith('http')) {
      return event.cover
    }
    return `http://localhost:8080${event.cover}`
  }
  
  return 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512"%3E%3Crect fill="%23f5f5f5" width="512" height="512"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="24" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3E%E8%B5%9B%E4%BA%8B%E5%9B%BE%E7%89%87%3C/text%3E%3C/svg%3E'
}

// 支付相关变量
const payDialogVisible = ref(false)
const currentOrderForPay = ref<any>(null)
const selectedPaymentMethod = ref('')
const qrCodeUrl = ref('')
const paymentLoading = ref(false)

// 打开支付方式选择弹窗
const handlePay = (order: any) => {
  currentOrderForPay.value = order
  selectedPaymentMethod.value = ''
  qrCodeUrl.value = ''
  payDialogVisible.value = true
}

// 生成支付二维码
const generateQRCode = async () => {
  if (!currentOrderForPay.value) return
  
  paymentLoading.value = true
  try {
    // 这里应该调用后端API生成支付二维码
    // 模拟生成二维码URL
    const orderId = currentOrderForPay.value.id
    const amount = currentOrderForPay.value.totalPrice
    
    // 模拟二维码生成
    // 实际项目中，这里应该调用后端API获取真实的支付二维码
    const baseUrl = selectedPaymentMethod.value === '支付宝' 
      ? 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data='
      : 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data='
    
    const paymentData = {
      orderId,
      amount,
      method: selectedPaymentMethod.value,
      timestamp: Date.now()
    }
    
    qrCodeUrl.value = baseUrl + encodeURIComponent(JSON.stringify(paymentData))
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败')
  } finally {
    paymentLoading.value = false
  }
}

// 确认支付
const confirmPayment = async () => {
  if (!currentOrderForPay.value) return
  
  try {
    await api.post(`/orders/${currentOrderForPay.value.id}/pay`, { 
      paymentMethod: selectedPaymentMethod.value 
    })
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    await fetchOrders()
  } catch (error: any) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}

// 取消订单
const handleCancel = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', {
      title: '取消确认',
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.post(`/orders/${order.id}/cancel`)
    ElMessage.success('订单已取消')
    await fetchOrders()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 售后申请相关变量
const afterSalesDialogVisible = ref(false)
const currentOrderForAfterSales = ref<any>(null)
const afterSalesForm = reactive({
  type: '退款',
  description: ''
})

// 售后请求列表
const afterSalesRequests = ref<any[]>([])

// 获取售后请求列表
const fetchAfterSalesRequests = async () => {
  try {
    const response = await api.get('/api/after-sales')
    afterSalesRequests.value = response.data?.data || []
  } catch (error: any) {
    console.error('获取售后请求失败:', error)
    ElMessage.error('获取售后请求失败')
  }
}

// 打开售后申请弹窗
const handleRefund = (order: any) => {
  currentOrderForAfterSales.value = order
  afterSalesForm.type = '退款'
  afterSalesForm.description = ''
  afterSalesDialogVisible.value = true
}

// 提交售后申请
const submitAfterSales = async () => {
  if (!afterSalesForm.description.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }
  
  try {
    const afterSalesData = {
      username: currentUser?.username || '',
      orderId: currentOrderForAfterSales.value.id,
      eventId: currentOrderForAfterSales.value.eventId,
      eventName: currentOrderForAfterSales.value.eventName,
      type: afterSalesForm.type,
      description: afterSalesForm.description
    }
    
    // 打印提交的数据
    console.log('Submitting after sales data:', afterSalesData)
    console.log('Current order:', currentOrderForAfterSales.value)
    
    await api.post('/api/after-sales', afterSalesData)
    ElMessage.success('售后申请已提交')
    afterSalesDialogVisible.value = false
    
    // 刷新订单列表，实时更新订单状态为售后中
    await fetchOrders()
  } catch (error: any) {
    console.error('售后申请失败:', error)
    ElMessage.error('售后申请失败')
  }
}

// 取消售后
const handleCancelAfterSales = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认取消售后申请吗？', {
      title: '取消售后确认',
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 打印订单信息
    console.log('Order info:', order)
    
    // 直接从后端获取售后请求，根据订单ID查找
    try {
      const response = await api.get('/api/after-sales')
      const allAfterSales = response.data?.data || []
      console.log('All after sales from backend:', allAfterSales)
      
      // 过滤出当前用户的售后请求
      // 处理orderId为0的情况，尝试根据eventId和username来匹配
      const userAfterSales = allAfterSales.filter((as: any) => {
        console.log('Checking after sales:', as)
        if (as.username === currentUser?.username) {
          // 如果orderId不为0，使用orderId匹配
          if (as.orderId !== 0 && as.orderId === order.id) {
            return true
          }
          // 如果orderId为0，使用eventId匹配
          if (as.orderId === 0 && as.eventId === order.eventId) {
            return true
          }
        }
        return false
      })
      console.log('Filtered after sales:', userAfterSales)
      
      if (userAfterSales.length > 0) {
        // 删除售后请求（后端会自动更新订单状态）
        await api.delete(`/api/after-sales/${userAfterSales[0].id}`)
        
        ElMessage.success('售后申请已取消')
        // 刷新订单列表，实时更新订单状态
        await fetchOrders()
      } else {
        // 未找到售后请求，但订单状态为售后中，直接更新订单状态为已完成
        try {
          // 直接更新订单状态为已完成
          await api.put(`/orders/${order.id}`, {
            status: 'COMPLETED'
          })
          
          ElMessage.success('售后申请已取消')
          // 刷新订单列表，实时更新订单状态
          await fetchOrders()
        } catch (updateError: any) {
          console.error('更新订单状态失败:', updateError)
          ElMessage.error('取消售后申请失败')
        }
      }
    } catch (apiError: any) {
      console.error('获取售后请求失败:', apiError)
      ElMessage.error('获取售后请求失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消售后申请失败:', error)
      ElMessage.error('取消售后申请失败')
    }
  }
}

// 删除订单
const handleDeleteOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个订单吗？此操作不可恢复！', {
      title: '删除确认',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    await api.delete(`/orders/${order.id}`)
    ElMessage.success('订单已删除')
    
    // 重新获取订单列表
    await fetchOrders()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }
}

// 查看详情
const handleViewDetails = async (order: any) => {
  try {
    currentOrder.value = order
    
    // 加载赛事详情
    const eventResponse = await api.get(`/events/${order.eventId}`)
    eventDetails.value = eventResponse.data
    
    // 加载座位信息
    const seatsResponse = await api.get(`/seats/events/${order.eventId}`)
    const seats = seatsResponse.data.data || []
    
    // 解析订单中的座位信息
    const orderSeats = order.seats.split(',').map((seatStr: string) => {
      const match = seatStr.trim().match(/(\d+)排(\d+)座/)
      if (match) {
        return { row: parseInt(match[1]), col: parseInt(match[2]) }
      }
      return null
    }).filter(Boolean)
    
    // 生成座位图数据
    seatMap.value = seats.map((seat: any) => {
      const isSelected = orderSeats.some((orderSeat: any) => 
        orderSeat.row === seat.rowNum && orderSeat.col === seat.colNum
      )
      return {
        ...seat,
        isSelected
      }
    })
    
    dialogVisible.value = true
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 查看动态赛事详情
const handleDynamicViewDetails = async (event: any) => {
  try {
    // 加载赛事详情
    const eventResponse = await api.get(`/events/${event.id}`)
    eventDetails.value = eventResponse.data
    
    // 加载座位信息
    const seatsResponse = await api.get(`/seats/events/${event.id}`)
    const seats = seatsResponse.data
    
    // 解析订单中的座位信息
    const orderSeats = event.seats.split(',').map((seatStr: string) => {
      const match = seatStr.trim().match(/(\d+)排(\d+)座/)
      if (match) {
        return { row: parseInt(match[1]), col: parseInt(match[2]) }
      }
      return null
    }).filter(Boolean)
    
    // 生成座位图数据
    seatMap.value = seats.map((seat: any) => {
      const isSelected = orderSeats.some((orderSeat: any) => 
        orderSeat.row === seat.rowNum && orderSeat.col === seat.colNum
      )
      return {
        ...seat,
        isSelected
      }
    })
    
    dialogVisible.value = true
  } catch (error: any) {
    console.error('查看详情失败:', error)
    ElMessage.error('查看详情失败')
  }
}

// 处理分页当前页码变化
const handleCurrentChange = (current: number) => {
  currentPage.value = current
}

// 处理订单分页当前页码变化
const handleOrderCurrentChange = (current: number) => {
  orderCurrentPage.value = current
}

// 删除动态赛事
const handleDeleteDynamic = async (event: any) => {
  try {
    await ElMessageBox.confirm('确认删除该动态吗？', '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 从completedEvents中移除该事件
    const index = completedEvents.value.findIndex(item => item.orderId === event.orderId)
    if (index !== -1) {
      completedEvents.value.splice(index, 1)
      ElMessage.success('删除成功')
      
      // 保存到数据库
      await saveMotionsToDatabase()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 取消收藏
const handleRemoveCollection = async (collection: any) => {
  try {
    await ElMessageBox.confirm('确认取消收藏吗？', '取消收藏', {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    console.log('取消收藏参数:', { username: currentUser.username, eventId: collection.id })
    await api.delete('/users/collections', {
      data: {
        username: currentUser.username,
        eventId: collection.id
      }
    })
    ElMessage.success('取消收藏成功')
    await fetchCollections()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

// 订单列表刷新定时器
let orderRefreshInterval: number | undefined

onMounted(async () => {
  console.log('=== PersonalCenter onMounted ===')
  console.log('localStorage.user_isLoggedIn:', localStorage.getItem('user_isLoggedIn'))
  console.log('localStorage.user_user:', localStorage.getItem('user_user'))
  console.log('localStorage.user_username:', localStorage.getItem('user_username'))
  
  getCurrentUser()
  
  console.log('getCurrentUser 执行后:')
  console.log('currentUser:', currentUser)
  console.log('currentUser.username:', currentUser?.username)
  
  // 从服务器获取用户完整信息
  await fetchUserInfoFromServer()
  await fetchEvents()
  await fetchOrders()
  // 先尝试从数据库加载动态
  await loadMotionsFromDatabase()
  // 如果数据库中没有动态，则从订单中生成
  if (completedEvents.value.length === 0) {
    await fetchCompletedEvents()
  }
  if (activeMenu.value === 'favorites') {
    await fetchCollections()
  }
  
  // 启动订单列表定时刷新（每5秒刷新一次）
  orderRefreshInterval = window.setInterval(async () => {
    if (activeMenu.value === 'orders') {
      await fetchOrders()
      await fetchCompletedEvents()
    }
  }, 5000)
})

// 页面卸载时清理
onUnmounted(() => {
  if (orderRefreshInterval) {
    clearInterval(orderRefreshInterval)
  }
})

// 组件激活时重新获取用户信息和动态赛事
onActivated(async () => {
  getCurrentUser()
  await fetchCompletedEvents()
})

// 编辑个人信息
const handleEditProfile = (field: string) => {
  currentEditField.value = field
  // 获取当前值
  editValue.value = currentUser?.[field] || ''
  // 打开编辑弹窗
  editDialogVisible.value = true
}

// 获取字段名称
const getFieldName = (field: string): string => {
  const fieldNames: Record<string, string> = {
    username: '用户名',
    nickname: '昵称',
    gender: '性别',
    birthday: '生日',
    city: '所在城市',
    lifeStatus: '生活状态',
    industry: '从事行业',
    hobbies: '兴趣爱好',
    signature: '个性签名',
    phone: '手机号码'
  }
  return fieldNames[field] || field
}

// 保存个人信息
const saveProfile = async () => {
  try {
    if (currentEditField.value === 'username') {
      // 更改用户名需要特殊处理
      const oldUsername = currentUser?.username
      if (!oldUsername) {
        ElMessage.error('用户信息错误')
        return
      }
      
      // 检查新用户名是否可用
      const checkResponse = await api.get(`/users/username?username=${editValue.value}`)
      if (!checkResponse.data) {
        ElMessage.error('用户名已存在')
        return
      }
      
      // 调用API更新用户名
      await api.put(`/users/username?oldUsername=${oldUsername}&newUsername=${editValue.value}`)
      
      // 更新本地用户信息
      currentUser.username = editValue.value
      
      // 保存到localStorage
      localStorage.setItem('user', JSON.stringify(currentUser))
      localStorage.setItem('username', editValue.value)
      
      // 取消记住我功能
      localStorage.removeItem('rememberUsername')
      localStorage.removeItem('rememberPassword')
      localStorage.removeItem('rememberMe')
      
      ElMessage.success('用户名修改成功，已取消记住我功能')
    } else {
      // 其他字段的更新
      // 这里可以调用API更新用户信息
      // 示例：await api.put('/users/profile', { [currentEditField.value]: editValue.value })
      
      // 模拟更新成功
      currentUser[currentEditField.value] = editValue.value
      
      // 保存到localStorage
      localStorage.setItem('user', JSON.stringify(currentUser))
      
      ElMessage.success('保存成功')
    }
    
    editDialogVisible.value = false
  } catch (error: any) {
    console.error('保存失败:', error)
    if (error.response?.data) {
      ElMessage.error(error.response.data)
    } else {
      ElMessage.error('保存失败')
    }
  }
}

// 头像上传相关
const avatarInput = ref<HTMLInputElement>()

// 点击头像触发上传
const handleChangeAvatar = () => {
  avatarInput.value?.click()
}

// 处理头像上传
const handleAvatarUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    // 使用Canvas压缩图片
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    
    img.onload = async () => {
      // 设置压缩后的图片尺寸
      const maxWidth = 150
      const maxHeight = 150
      let width = img.width
      let height = img.height
      
      // 计算压缩后的尺寸
      if (width > height) {
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
      } else {
        if (height > maxHeight) {
          width = (width * maxHeight) / height
          height = maxHeight
        }
      }
      
      // 设置Canvas尺寸
      canvas.width = width
      canvas.height = height
      
      // 绘制压缩后的图片
      ctx?.drawImage(img, 0, 0, width, height)
      
      // 获取压缩后的DataURL
      const avatarUrl = canvas.toDataURL('image/jpeg', 0.3) // 0.3是压缩质量，进一步减少图片大小
      
      // 创建一个新对象，确保响应式更新
      const updatedUser = { ...currentUser, avatar: avatarUrl }
      updateCurrentUser(updatedUser)
      
      // 保存到localStorage
      localStorage.setItem('user_user', JSON.stringify(updatedUser))
      
      // 从localStorage中读取用户名，确保用户名存在
        const username = localStorage.getItem('user_username') || updatedUser.username
        if (!username) {
          console.error('用户名为空，无法同步头像到数据库')
          ElMessage.error('用户名为空，无法同步头像到数据库')
          return
        }
        
        try {
          console.log('准备发送头像到数据库，用户名为:', username)
          console.log('头像URL长度:', avatarUrl.length)
          
          // 发送头像信息到后端，保存到数据库
          const response = await api.put('/users/profile', {
            username: username,
            avatar: avatarUrl
          })
          console.log('头像同步到数据库成功，响应:', response)
          ElMessage.success('头像上传成功')
        } catch (error: any) {
          console.error('上传头像到数据库失败:', error)
          console.error('错误详情:', error.response)
          console.error('错误数据:', error.response?.data)
          ElMessage.error(`头像上传成功但同步到数据库失败: ${error.response?.data || error.message}`)
        }
    }
    
    // 读取文件
    img.src = URL.createObjectURL(file)
  }
}

// 从服务器获取用户信息
const fetchUserInfoFromServer = async () => {
  try {
    if (!currentUser?.username) {
      console.log('用户名为空，跳过获取用户信息')
      return
    }
    
    console.log('准备获取用户信息，用户名:', currentUser.username)
    
    const response = await api.get(`/users/info/${currentUser.username}`)
    let userInfo = response.data.data || {}
    
    // 修复可能的双层包装问题
    if (userInfo && userInfo.value) {
      userInfo = userInfo.value
    }
    
    console.log('从服务器获取的用户信息:', userInfo)
    console.log('userInfo.nickname:', userInfo.nickname)
    
    // 逐个属性赋值，确保Vue响应式系统能够检测到变化
    if (userInfo.nickname !== undefined) currentUser.nickname = userInfo.nickname
    if (userInfo.gender !== undefined) currentUser.gender = userInfo.gender
    if (userInfo.birthday !== undefined) currentUser.birthday = userInfo.birthday
    if (userInfo.city !== undefined) currentUser.city = userInfo.city
    if (userInfo.lifeStatus !== undefined) currentUser.lifeStatus = userInfo.lifeStatus
    if (userInfo.industry !== undefined) currentUser.industry = userInfo.industry
    if (userInfo.hobbies !== undefined) currentUser.hobbies = userInfo.hobbies
    if (userInfo.signature !== undefined) currentUser.signature = userInfo.signature
    if (userInfo.phone !== undefined) currentUser.phone = userInfo.phone
    if (userInfo.avatar !== undefined) currentUser.avatar = userInfo.avatar
    
    console.log('更新后的currentUser:', currentUser)
    console.log('currentUser.nickname:', currentUser?.nickname)
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 保存所有修改
const saveAllProfile = async () => {
  try {
    // 保存所有用户信息，包括头像和手机号
    const profileData = { ...currentUser }
    
    console.log('准备保存的个人信息:', profileData)
    
    // 调用API保存所有用户信息
    const saveResponse = await api.put('/users/profile', profileData)
    console.log('保存响应:', saveResponse)
    
    // 保存成功后重新获取用户信息，确保页面显示最新数据
    await fetchUserInfoFromServer()
    
    // 保存成功
    ElMessage.success('所有修改已保存')
  } catch (error: any) {
    console.error('保存失败:', error)
    if (error.response?.data) {
      ElMessage.error(error.response.data)
    } else {
      ElMessage.error('保存失败')
    }
  }
}

const activeTitle = computed(() => {
  const titles: Record<string, string> = {
    orders: '我的订单',
    favorites: '我的收藏',
    profile: '个人信息',
    settings: '安全设置'
  }
  return titles[activeMenu.value]
})

const filteredOrders = computed(() => {
  if (orderStatus.value === 'all') return orders.value
  if (orderStatus.value === 'cancelled') {
    return orders.value.filter(o => o.status === 'CANCELLED' || o.status === 'TIMEOUT')
  }
  if (orderStatus.value === 'afterSales') {
    // 显示售后中的订单
    return orders.value.filter(o => o.status === 'AFTER_SALES')
  }
  const statusMap: Record<string, string> = {
    pending: 'PENDING',
    completed: 'COMPLETED'
  }
  return orders.value.filter(o => o.status === statusMap[orderStatus.value])
})

// 计算分页后的订单列表
const paginatedOrders = computed(() => {
  const start = (orderCurrentPage.value - 1) * orderPageSize.value
  const end = start + orderPageSize.value
  return filteredOrders.value.slice(start, end)
})

// 计算订单总页数
const orderTotalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / orderPageSize.value)
})

const handleSelect = async (index: string) => {
  activeMenu.value = index
  if (index === 'favorites') {
    await fetchCollections()
  } else if (index === 'profile') {
    // 切换到个人信息页面时，重新获取用户信息，确保头像能够同步更新
    getCurrentUser()
    // 重新获取已完成订单的赛事，确保数据能够实时更新
    await fetchCompletedEvents()
  }
}

const filterOrders = async () => {
  // 切换订单状态时重置页码为第一页
  orderCurrentPage.value = 1
  
  if (orderStatus.value === 'afterSales') {
    await fetchOrders() // 刷新订单列表，以显示售后中的订单
    await fetchAfterSalesRequests()
  }
}

const getStatusTag = (status: string) => {
  const tags: Record<string, string> = {
    PENDING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    TIMEOUT: 'danger',
    AFTER_SALES: 'primary',
    REFUNDED: 'danger'
  }
  return tags[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待支付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    TIMEOUT: '超时',
    AFTER_SALES: '售后中',
    REFUNDED: '已退款'
  }
  return texts[status] || '未知状态'
}

// 计算支付倒计时
const getCountdown = (createTime: string) => {
  const orderTime = new Date(createTime).getTime()
  const now = new Date().getTime()
  const duration = 15 * 60 * 1000 // 15分钟
  const remaining = duration - (now - orderTime)
  
  if (remaining <= 0) {
    return '已超时'
  }
  
  const minutes = Math.floor(remaining / (1000 * 60))
  const seconds = Math.floor((remaining % (1000 * 60)) / 1000)
  
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 定期更新倒计时
setInterval(() => {
  // 触发重新渲染
  orders.value = [...orders.value]
}, 1000)
</script>

<style scoped>
.personal-center-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  padding: 0 2.5%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}

.header-right {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.logo-container:hover {
  opacity: 0.8;
}

.logo-img {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #1d4ed8;
  white-space: nowrap;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-layout {
  max-width: 1440px;
  width: 95%;
  margin: 20px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
  box-sizing: border-box;
}

.sidebar {
  background-color: #fff;
  border-radius: 8px;
  height: fit-content;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
}

.content-area {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  min-height: 600px;
  box-sizing: border-box;
  overflow: hidden;
}

.page-title h2 {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 22px;
  color: #2c3e50;
}

.filter-bar {
  margin-bottom: 25px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.completed-events-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.completed-events-list .order-item-card {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.completed-events-list .order-item-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.completed-events-list .order-item-body {
  width: 100%;
}

.order-item-header {
  padding: 12px 20px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #f0f2f5;
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-id {
  flex: 1;
}

.order-item-body {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  flex-wrap: wrap;
  width: 100%;
  box-sizing: border-box;
}

.order-thumb {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.order-info {
  flex: 1;
}

.order-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.order-info p {
  margin: 5px 0;
  font-size: 13px;
  color: #909399;
}

.seat-info {
  color: #606266 !important;
}

.order-price {
  margin-top: 10px;
}

.order-price .amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.countdown {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  align-items: center;
}

.countdown-label {
  font-size: 14px;
  color: #606266;
  margin-right: 5px;
}

.countdown-time {
  font-size: 14px;
  font-weight: bold;
  color: #fa8c16;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: #fff7e6;
  border: 1px solid #ffd591;
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .content-area {
    padding: 15px;
  }
}

.order-actions {
  display: flex;
  flex-direction: row;
  gap: 10px;
  align-items: center;
}

.collection-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.event-card {
  margin-bottom: 20px;
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}

.event-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
}

.event-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.event-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.event-card:hover .event-cover img {
  transform: scale(1.05);
}

.collection-time-tag {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  z-index: 1;
}

.event-info {
  padding: 18px;
  background: #ffffff;
}

.event-name {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  line-height: 1.4;
}

.event-meta {
  margin-bottom: 5px;
}

.event-meta p {
  margin: 6px 0;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.event-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}

.price small {
  font-size: 13px;
  color: #909399;
  font-weight: 400;
}

.event-actions {
  display: flex;
  gap: 10px;
}

.collection-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
  min-height: 60vh;
  align-items: center;
  justify-items: center;
}

.empty-container {
  grid-column: 1 / -1; /* 占据所有列 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}/* 订单详情弹窗样式 */
.order-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.seat-map-container {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.seat-map-container h4 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 16px;
  color: #303133;
}

.stage-container {
  text-align: center;
  margin-bottom: 20px;
  perspective: 1000px;
}

.stage-label {
  display: inline-block;
  background: #f1f5f9;
  color: #64748b;
  padding: 6px 40px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  border: 1px solid #e2e8f0;
}

.stage-line {
  height: 4px;
  background: linear-gradient(90deg, transparent, #cbd5e1, transparent);
  width: 60%;
  margin: 0 auto;
  border-radius: 2px;
  transform: rotateX(45deg);
}

.seat-grid {
  margin-bottom: 20px;
  max-height: 400px;
  overflow-y: auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.seat-layout {
  width: 100%;
  max-width: 600px;
}

.seat-header {
  display: flex;
  margin-bottom: 15px;
}

.seat-numbers-row {
  display: flex;
  align-items: center;
}

.seat-number-cell {
  width: 22px;
  height: 22px;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seat-number-label {
  margin-left: 10px;
  font-size: 12px;
  color: #94a3b8;
  align-self: center;
}

.seat-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.row-label {
  width: 60px;
  font-size: 12px;
  color: #94a3b8;
  text-align: right;
  margin-right: 15px;
  flex-shrink: 0;
}

.row-number-row {
  margin-top: 10px;
  margin-bottom: 0;
}

.row-number-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

.seat-cells {
  display: flex;
}

.seat-cell {
  width: 22px;
  height: 22px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seat {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  cursor: default;
  transition: all 0.2s ease;
  border: 1px solid rgba(0,0,0,0.05);
}

.seat.available {
  background-color: #22c55e;
}

.seat.sold {
  background-color: #ef4444;
}

.seat.locked {
  background-color: #9ca3af;
}

.seat.selected {
  background-color: #1d4ed8;
  box-shadow: 0 0 0 2px white, 0 0 0 4px #1d4ed8;
}

.no-seats {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 20px 0;
  border-top: 1px solid #f0f2f5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.color-box {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}

.color-box.available { background-color: #22c55e; }
.color-box.sold { background-color: #ef4444; }
.color-box.selected { background-color: #1d4ed8; }
.color-box.locked { background-color: #9ca3af; }

.loading {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}

/* 个人信息页面样式 */
.profile-page {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.profile-form {
  margin-top: 20px;
}

.form-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-item:hover {
  background-color: #f9fafb;
}

.form-item:active {
  background-color: #f3f4f6;
}

.form-item:last-child {
  border-bottom: none;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.form-label {
  font-size: 14px;
  color: #6b7280;
  width: 100px;
}

.form-value {
  flex: 1;
  text-align: right;
  font-size: 14px;
  color: #374151;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.form-value.with-arrow {
  gap: 8px;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  border: 2px solid #e5e7eb;
}

.avatar.uploadable {
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar.uploadable:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border-color: #165DFF;
}

.avatar-wrapper {
  position: relative;
}

/* 动态页面样式 */
.dynamic-card {
  margin-top: 20px;
}

.dynamic-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dynamic-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.dynamic-card {
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.dynamic-content {
  padding: 40px 0;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
}

.empty-dynamic {
  text-align: center;
  color: #9ca3af;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }
  
  .form-item {
    padding: 12px 0;
  }
  
  .form-label {
    width: 80px;
  }
}
</style>
