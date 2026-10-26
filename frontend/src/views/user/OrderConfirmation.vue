<template>
  <div class="order-confirmation-container">
    <el-header class="header">
      <el-page-header @back="$router.back()" content="订单确认" />
      <el-button link @click="$router.push('/')" class="home-btn" style="margin-left: 20px;">首页</el-button>
    </el-header>

    <div class="main-layout">
      <!-- 订单详情 (左侧) -->
      <div class="order-details-area">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>赛事信息</span>
            </div>
          </template>
          <div class="event-info-display">
            <img :src="event.cover" class="event-thumb" :alt="event.name" />
            <div class="event-text">
              <h2>{{ event.name }}</h2>
              <p><el-icon><Calendar /></el-icon> {{ event.time }}</p>
              <p><el-icon><Location /></el-icon> {{ event.venue }}</p>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="mt-20">
          <template #header>
            <div class="card-header">
              <span>座位信息</span>
            </div>
          </template>
          <el-table :data="selectedSeats" style="width: 100%">
            <el-table-column prop="row" label="排" width="100" />
            <el-table-column prop="col" label="座" width="100" />
            <el-table-column label="票种" width="150">
              <template #default>普通票</template>
            </el-table-column>
            <el-table-column prop="price" label="单价">
              <template #default="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
          </el-table>
          <div class="order-id-display">
            <span>订单编号：{{ orderId }}</span>
          </div>
        </el-card>

        <!-- 底部政策 -->
        <div class="policy-section">
          <h3>购票须知</h3>
          <p>1. 本项目支持“条件退”，具体以票务页面展示为准。</p>
          <p>2. 儿童购票：1.2米以上凭成人票入场，1.2米以下谢绝入场。</p>
          <p>3. 观演须知：请提前 60 分钟入场，保持安静，禁止携带违禁品。</p>
        </div>
      </div>

      <!-- 支付方式 (右侧) -->
      <div class="payment-area">
        <el-card shadow="never" class="payment-card">
          <template #header>
            <div class="card-header payment-header">
              <span>支付详情</span>
              <div class="countdown">
                支付倒计时: <span class="time">{{ formatCountdown }}</span>
              </div>
            </div>
          </template>

          <div class="total-amount">
            <span>应付总额</span>
            <span class="price">¥{{ totalPrice }}</span>
          </div>

          <div class="payment-methods">
            <p class="section-title">选择支付方式</p>
            <el-radio-group v-model="paymentMethod" class="method-group">
              <el-radio border label="alipay" class="method-radio">
                <img src="https://img.alicdn.com/tfs/TB1p_4Yv_tYBeNjy1XdXXXXnvXa-100-100.png" width="20" alt="支付宝图标" /> 支付宝
              </el-radio>
              <el-radio border label="wechat" class="method-radio">
                <img src="https://img.alicdn.com/tfs/TB1.P_Yv_tYBeNjy1XdXXXXnvXa-100-100.png" width="20" alt="微信支付图标" /> 微信支付
              </el-radio>
            </el-radio-group>
          </div>

          <div class="qr-code-area" v-if="paymentMethod">
            <div class="qr-placeholder">
              <el-icon :size="100" color="#909399"><Picture /></el-icon>
              <p>请使用{{ paymentMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</p>
            </div>
          </div>

          <el-button 
            type="primary" 
            class="pay-button" 
            size="large"
            @click="handlePay"
            :loading="paying"
          >
            立即支付
          </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, Location, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'


const route = useRoute()
const router = useRouter()

const event = ref<any>({
  id: route.query.eventId,
  name: '',
  time: '',
  venue: '',
  cover: ''
})

const selectedSeats = ref(JSON.parse(route.query.seats as string || '[]'))
const orderId = ref('ORD' + Date.now().toString().slice(-8))
const paymentMethod = ref('alipay')
const paying = ref(false)
const countdown = ref(900) // 15 minutes in seconds

let timer: number | null = null

const totalPrice = computed(() => selectedSeats.value.reduce((sum: number, s: any) => sum + s.price, 0))

const formatCountdown = computed(() => {
  const m = Math.floor(countdown.value / 60)
  const s = countdown.value % 60
  return `${m}:${s < 10 ? '0' + s : s}`
})

const startTimer = () => {
  timer = window.setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      stopTimer()
      ElMessage.error('支付已超时，订单已取消')
      router.push('/')
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const fetchEventDetails = async () => {
  // Mock data
  event.value = {
    id: route.query.eventId,
    name: '2026 开山节 (北京)',
    time: '2026.03.27 19:30',
    venue: '楠谷闪闪公园',
    cover: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512"%3E%3Crect fill="%23f5f5f5" width="512" height="512"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="24" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3E%E8%B5%9B%E4%BA%8B%E5%9B%BE%E7%89%87%3C/text%3E%3C/svg%3E'
  }
}

const handlePay = async () => {
  paying.value = true
  setTimeout(() => {
    paying.value = false
    ElMessage.success('支付成功！')
    router.push('/personal-center')
  }, 1000)
}

onMounted(() => {
  if (selectedSeats.value.length === 0) {
    router.push('/')
    return
  }
  fetchEventDetails()
  startTimer()
})

onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped>
.order-confirmation-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 50px;
}

.header {
  background-color: #fff;
  padding: 10px 2.5%;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.main-layout {
  max-width: 1440px;
  width: 95%;
  margin: 0 auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
}

.order-details-area {
  flex: 1;
}

.event-info-display {
  display: flex;
  gap: 20px;
}

.event-thumb {
  width: 120px;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
}

.event-text h2 {
  margin: 0 0 15px 0;
  font-size: 20px;
}

.event-text p {
  margin: 8px 0;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mt-20 { margin-top: 20px; }

.order-id-display {
  margin-top: 20px;
  font-size: 13px;
  color: #909399;
}

.policy-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.policy-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
}

.policy-section p {
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
  margin: 5px 0;
}

.payment-area {
  width: 400px;
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
  }
  
  .payment-area {
    width: 100%;
  }
  
  .payment-card {
    position: static;
  }
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.countdown {
  font-size: 14px;
  color: #606266;
}

.countdown .time {
  color: #ef4444;
  font-weight: bold;
}

.total-amount {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 20px 0;
  border-bottom: 1px solid #f0f2f5;
}

.total-amount .price {
  font-size: 28px;
  font-weight: bold;
  color: #ef4444;
}

.payment-methods {
  padding: 20px 0;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 15px;
}

.method-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.method-radio {
  margin: 0 !important;
  display: flex;
  align-items: center;
  height: 50px;
}

.qr-code-area {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 20px;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.qr-placeholder p {
  font-size: 12px;
  color: #909399;
}

.pay-button {
  width: 100%;
  height: 50px;
}
</style>
