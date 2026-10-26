<template>
  <div class="seat-selection-container">
    <el-header class="header">
      <div class="header-left">
        <el-page-header @back="$router.back()" content="选择座位" />
        <el-button link @click="$router.push('/')" class="home-btn" style="margin-left: 20px;">首页</el-button>
      </div>
      <div class="event-brief">
        <div class="event-info">
          <h3>{{ event.name }}</h3>
          <p>{{ event.time }} | {{ event.venue }}</p>
        </div>
        <el-button 
          :icon="isCollected ? 'StarFilled' : 'Star'" 
          :type="isCollected ? 'warning' : 'info'"
          circle
          @click="toggleCollection"
          :disabled="!getCurrentUsername()"
          class="collection-btn"
          :title="!getCurrentUsername() ? '请先登录' : (isCollected ? '取消收藏' : '收藏')"
        />
      </div>
    </el-header>

    <div class="main-layout">
      <!-- 座位图区域 -->
      <div class="seat-chart-area">
        <div class="stage-container">
          <div class="stage-label">赛场中央</div>
          <div class="stage-line"></div>
        </div>
        <div class="seat-chart-wrapper" ref="chartRef"></div>
        <div class="seat-legend">
          <div class="legend-item"><span class="color-box available"></span> 可选</div>
          <div class="legend-item"><span class="color-box sold"></span> 已售</div>
          <div class="legend-item"><span class="color-box selected"></span> 已选</div>
          <div class="legend-item"><span class="color-box locked"></span> 锁定</div>
        </div>
      </div>

      <!-- 订单预览区 -->
      <div class="order-preview-sidebar">
        <el-card class="order-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="header-title">订单详情</span>
              <span v-if="selectedSeats.length > 0" class="selection-count">
                ({{ selectedSeats.length }}/{{ MAX_SEATS }})
              </span>
            </div>
          </template>
          
          <div v-if="selectedSeats.length > 0" class="selected-seats-list">
            <transition-group name="list">
              <el-tag 
                v-for="seat in selectedSeats" 
                :key="seat.id" 
                class="seat-tag"
                closable
                @close="deselectSeat(seat)"
              >
                {{ seat.row }}排{{ seat.col }}座 (¥{{ seat.price }})
              </el-tag>
            </transition-group>
          </div>
          <el-empty v-else description="请在左侧选择座位" :image-size="60" />

          <div class="order-summary">
            <div class="summary-row">
              <span>已选座位：</span>
              <span>{{ selectedSeats.length }} 个</span>
            </div>
            <div class="summary-row total">
              <span>合计金额：</span>
              <span class="total-price" :class="{ 'pulse': isPriceChanging }">¥{{ totalPrice }}</span>
            </div>
            <el-button 
              type="primary" 
              class="confirm-button" 
              :disabled="selectedSeats.length === 0 || !canSelectSeat()"
              @click="confirmOrder"
            >
              {{ !getCurrentUserId() ? '请先登录' : (isAdmin() ? '管理员不可购票' : '确认选座') }}
            </el-button>
          </div>
        </el-card>

        <!-- 登录状态提示 -->
        <el-alert
          v-if="!getCurrentUserId()"
          title="请先登录"
          type="warning"
          description="登录后才可进行选座购票"
          show-icon
          :closable="false"
          class="tip-alert"
        />
        <el-alert
          v-else-if="isAdmin()"
          title="管理员账号"
          type="info"
          description="管理员仅可查看座位图，不能选座购票"
          show-icon
          :closable="false"
          class="tip-alert"
        />
        <el-alert
          v-else
          title="温馨提示"
          type="info"
          description="请在15分钟内完成支付，否则订单将被自动取消。"
          show-icon
          :closable="false"
          class="tip-alert"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import api from '../../api'

const router = useRouter()
const route = useRoute()
const chartRef = ref<HTMLElement | null>(null)
let myChart: echarts.ECharts | null = null

const event = ref<any>({
  id: route.params.id,
  name: '',
  time: '',
  venue: '',
  status: ''
})

const isCollected = ref(false)

interface Seat {
  id: string;
  row: number;
  col: number;
  price: number;
  status: 'available' | 'sold' | 'locked';
  isSelected?: boolean; // 前端选中状态
}

const rows = ref(10);
const cols = ref(15);
const MAX_SEATS = 6; // 最多选择6个座位
const seatData = ref<Seat[]>([]);
const isPriceChanging = ref(false);
const bcChannel = ref<BroadcastChannel | null>(null); // 广播频道

const fetchEventDetails = async () => {
  try {
    const response = await api.get(`/events/${route.params.id}`)
    // 后端统一返回 { code, message, data }，优先取 data 字段
    const eventData = response.data?.data ?? response.data ?? response
    event.value = {
      id: route.params.id,
      name: eventData.name,
      time: eventData.time,
      venue: eventData.venue,
      status: eventData.status || ''
    }
  } catch (error: any) {
    console.error('获取赛事详情失败:', error)
    ElMessage.error('获取赛事详情失败')
  }
}

const fetchSeats = async (isInitial = false) => {
  try {
    const response = await api.get(`/seats/events/${route.params.id}`)
    const seatsData = response.data.data || response.data || []
    
    if (seatsData && seatsData.length > 0) {
      // 计算行列数
      rows.value = Math.max(...seatsData.map((s: any) => s.rowNum))
      cols.value = Math.max(...seatsData.map((s: any) => s.colNum))
      
      // 保存当前用户选中的座位ID
      const selectedSeatIds = new Set(seatData.value.filter(s => s.isSelected).map(s => s.id))
      
      // 转换数据格式，保留用户选中状态
      seatData.value = seatsData.map((s: any) => {
        const seatId = String(s.id)
        const newStatus = s.status === 'AVAILABLE' ? 'available' : 
                         s.status === 'SOLD' ? 'sold' : 
                         s.status === 'LOCKED' ? 'locked' : 'available'
        
        // 只清除已被售出的座位选中状态；已被自己锁定(locked)的座位需保留选中状态
        const isSelected = selectedSeatIds.has(seatId) && newStatus !== 'sold'
        
        return {
          id: seatId,
          row: s.rowNum,
          col: s.colNum,
          price: s.price,
          status: newStatus,
          isSelected: isSelected
        }
      });
      
      // 首次加载渲染图表，后续刷新更新图表
      if (isInitial || !myChart) {
        renderChart()
      } else {
        updateChart()
      }
    } else {
      seatData.value = []
    }
  } catch (error: any) {
    console.error('获取座位数据失败:', error)
    ElMessage.error('获取座位数据失败')
  }
}

const selectedSeats = computed(() => seatData.value.filter(s => s.isSelected))
const totalPrice = computed(() => selectedSeats.value.reduce((sum, s) => sum + s.price, 0))

// 监听总价变化，触发动画反馈
watch(totalPrice, () => {
  isPriceChanging.value = true
  setTimeout(() => {
    isPriceChanging.value = false
  }, 300)
})

// 获取当前用户ID（从localStorage）
const getCurrentUserId = () => {
  const userId = localStorage.getItem('user_userId');
  if (userId) {
    return parseInt(userId);
  }
  return null;
}

// 获取当前用户名（从localStorage）
const getCurrentUsername = () => {
  return localStorage.getItem('user_username');
}

// 检查是否是管理员
const isAdmin = () => {
  return localStorage.getItem('admin_isLoggedIn') === 'true';
}

// 检查是否可以选座
const canSelectSeat = () => {
  const userId = getCurrentUserId();
  const admin = isAdmin();
  const isUserLoggedIn = localStorage.getItem('user_isLoggedIn') === 'true';
  console.log('canSelectSeat - userId:', userId, ', isAdmin:', admin, ', isUserLoggedIn:', isUserLoggedIn);
  // 只有普通用户登录且不是管理员才能选座
  return isUserLoggedIn && !admin;
}

// 锁定座位
const lockSeat = async (seat: Seat) => {
  // 检查是否可以选座
  if (!canSelectSeat()) {
    if (isAdmin()) {
      ElMessage.warning('管理员账号不能选座购票，请使用普通用户账号登录');
    } else {
      ElMessage.warning('请先登录后购票');
    }
    return false;
  }

  const userId = getCurrentUserId();
  if (!userId) {
    ElMessage.warning('请先登录后购票');
    return false;
  }

  try {
    console.log('正在锁定座位:', seat.id, '用户:', userId);
    const response = await api.post(`/seats/${seat.id}/lock`, { userId });
    console.log('锁定成功:', response.data);
    if (response.data) {
      // 标记为前端选中状态
      seat.isSelected = true;
      updateChart();
      ElMessage.success(`已选择 ${seat.row}排${seat.col}座`);
      return true;
    }
  } catch (error: any) {
    console.error('锁定座位失败:', error);
    if (error.response?.data) {
      ElMessage.error(error.response.data);
    } else {
      ElMessage.error('座位锁定失败，请重试');
    }
    // 刷新座位状态
    await fetchSeats();
    return false;
  }
  return false;
}

// 解锁座位
const unlockSeat = async (seat: Seat) => {
  const userId = getCurrentUserId();
  if (!userId) return;

  try {
    await api.post(`/seats/${seat.id}/unlock`, { userId });
    // 取消前端选中状态
    seat.isSelected = false;
    updateChart();
  } catch (error: any) {
    console.error('解锁座位失败:', error);
    // 刷新座位状态
    await fetchSeats();
  }
}

const renderChart = () => {
  if (!chartRef.value) {
    console.log('chartRef 为空');
    return;
  }
  
  console.log('renderChart 被调用，座位数据:', seatData.value);
  
  // 如果图表不存在，初始化并绑定事件
  if (!myChart) {
    console.log('初始化 ECharts');
    myChart = echarts.init(chartRef.value);
    
    // 绑定点击事件
    myChart.on('click', (params: any) => {
      console.log('点击事件触发:', params);
      console.log('params.data:', params.data);
      
      if (params.componentType === 'series') {
        // params.data 是 {value: [...], itemStyle: {...}} 结构
        const dataArray = params.data && params.data.value ? params.data.value : params.data;
        console.log('数据数组:', dataArray);
        
        const seatId = Array.isArray(dataArray) && dataArray.length > 3 ? String(dataArray[3]) : null;
        console.log('查找座位ID:', seatId);
        
        if (!seatId || seatId === 'undefined') {
          console.error('座位ID无效:', seatId);
          return;
        }
        
        const seat = seatData.value.find(s => s.id === seatId);
        if (!seat) {
          console.error('未找到座位:', seatId);
          return;
        }
        
        console.log('点击座位:', seat);
        
        // 检查是否可以选座
        const canSelect = canSelectSeat();
        console.log('canSelectSeat 返回:', canSelect);
        console.log('isAdmin:', isAdmin());
        console.log('getCurrentUserId:', getCurrentUserId());
        console.log('user_isLoggedIn:', localStorage.getItem('user_isLoggedIn'));
        console.log('admin_isLoggedIn:', localStorage.getItem('admin_isLoggedIn'));
        
        // 检查赛事状态，停售的赛事不允许选座
        if (event.value.status === '停售') {
          ElMessage.warning('该赛事已停止售票，无法选择座位');
          return;
        }
        
        if (!canSelect) {
          if (isAdmin()) {
            ElMessage.warning('管理员账号不能选座购票，请使用普通用户账号登录');
          } else {
            ElMessage.warning('请先登录后购票');
          }
          return;
        }
        
        if (seat.status === 'available') {
          if (selectedSeats.value.length >= MAX_SEATS) {
            ElMessage.warning(`每单最多选择 ${MAX_SEATS} 个座位`);
            return;
          }
          // 调用锁定API
          lockSeat(seat);
        } else if (seat.isSelected) {
          // 调用解锁API
          unlockSeat(seat);
        } else if (seat.status === 'sold') {
          ElMessage.info('该座位已售出');
        } else if (seat.status === 'locked') {
          ElMessage.info('该座位已被其他用户锁定');
        }
      }
    });
    
    window.addEventListener('resize', () => {
      myChart?.resize();
    });
  }

  const option: echarts.EChartsOption = {
    animation: false,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#1d4ed8',
      borderWidth: 1,
      padding: [10, 15],
      textStyle: { color: '#333', fontSize: 13 },
      extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15); border-radius: 8px;',
      formatter: (params: any) => {
        const seatId = params.data[3];
        const seat = seatData.value.find(s => s.id === seatId);
        if (!seat) return '';

        let statusText = '可选';
        let statusColor = '#22c55e';
        if (seat.status === 'sold') {
          statusText = '已售';
          statusColor = '#ef4444';
        } else if (seat.isSelected) {
          statusText = '已选';
          statusColor = '#1d4ed8';
        } else if (seat.status === 'locked') {
          statusText = '锁定';
          statusColor = '#9ca3af';
        }

        return `
          <div style="line-height: 1.8">
            <div style="font-weight: bold; font-size: 15px; border-bottom: 1px solid #eee; margin-bottom: 8px; padding-bottom: 4px;">
              ${seat.row}排 ${seat.col}号
            </div>
            价格：<span style="color: #ef4444; font-weight: bold">¥${seat.price}</span><br/>
            状态：<span style="color: ${statusColor}">${statusText}</span>
          </div>
        `;
      }
    },
    grid: {
      top: '15%',
      bottom: '10%',
      left: '8%',
      right: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: cols.value }, (_, i) => (i + 1).toString()),
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        show: true,
        color: '#94a3b8',
        interval: 0
      },
      position: 'top',
      name: '座号',
      nameTextStyle: { color: '#94a3b8', padding: [0, 0, 0, 10] }
    },
    yAxis: {
      type: 'category',
      data: Array.from({ length: rows.value }, (_, i) => `第${i + 1}排`),
      inverse: true,
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        show: true,
        color: '#94a3b8'
      },
      name: '排号',
      nameTextStyle: { color: '#94a3b8', padding: [0, 10, 0, 0] }
    },
    series: [
      {
        type: 'scatter',
        symbol: 'circle',
        symbolSize: 22,
        itemStyle: {
          borderWidth: 1,
          borderColor: 'rgba(0,0,0,0.05)'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          },
          scale: 1.3
        },
        data: seatData.value.map(s => {
          let color = '#22c55e';
          if (s.status === 'sold') color = '#ef4444';
          if (s.isSelected) color = '#1d4ed8';
          if (s.status === 'locked') color = '#9ca3af';

          return {
            value: [s.col - 1, s.row - 1, s.status, s.id, s.price],
            itemStyle: { color }
          };
        })
      }
    ]
  };
  myChart.setOption(option);
}

const updateChart = () => {
  if (!myChart) return;
  const data = seatData.value.map(s => {
    let color = '#22c55e';
    if (s.status === 'sold') color = '#ef4444';
    if (s.isSelected) color = '#1d4ed8';
    if (s.status === 'locked') color = '#9ca3af';

    return {
      value: [s.col - 1, s.row - 1, s.status, s.id, s.price],
      itemStyle: { color }
    };
  });
  myChart.setOption({
    series: [{ data }]
  });
}

const deselectSeat = async (seat: Seat) => {
  await unlockSeat(seat);
}

const checkCollectionStatus = async () => {
  const username = getCurrentUsername();
  if (!username) return;
  
  try {
    const eventIdNum = typeof event.value.id === 'string' ? parseInt(event.value.id) : event.value.id;
    const response = await api.get(`/users/collections/check?username=${username}&eventId=${eventIdNum}`);
    isCollected.value = response.data?.data?.isCollected ?? response.data?.isCollected ?? false;
  } catch (error: any) {
    console.error('检查收藏状态失败:', error);
  }
}

const toggleCollection = async () => {
  const username = getCurrentUsername();
  if (!username) {
    ElMessage.warning('请先登录后收藏');
    return;
  }
  
  try {
    const eventIdNum = typeof event.value.id === 'string' ? parseInt(event.value.id) : event.value.id;
    
    if (isCollected.value) {
      // 取消收藏
      await api.delete('/users/collections', {
        data: {
          username,
          eventId: eventIdNum
        }
      });
      ElMessage.success('取消收藏成功');
      isCollected.value = false;
    } else {
      // 添加收藏
      await api.post('/users/collections', {
        username,
        eventId: eventIdNum
      });
      ElMessage.success('收藏成功');
      isCollected.value = true;
    }
  } catch (error: any) {
    console.error('操作收藏失败:', error);
    if (error.response?.data) {
      ElMessage.error(error.response.data);
    } else {
      ElMessage.error('操作收藏失败');
    }
  }
}

const confirmOrder = async () => {
  // 检查是否可以选座
  if (!canSelectSeat()) {
    if (isAdmin()) {
      ElMessage.warning('管理员账号不能选座购票，请使用普通用户账号登录');
    } else {
      ElMessage.warning('请先登录后购票');
    }
    return;
  }

  const userId = getCurrentUserId();
  if (!userId) {
    ElMessage.warning('请先登录后购票');
    return;
  }
  
  if (selectedSeats.value.length === 0) {
    ElMessage.warning('请至少选择一个座位');
    return;
  }
  
  try {
    const seatIds = selectedSeats.value.map(s => parseInt(s.id));
    const eventIdNum = typeof event.value.id === 'string' ? parseInt(event.value.id) : event.value.id;
    console.log('确认选座参数:', { userId, eventId: eventIdNum, seatIds });
    const response = await api.post('/orders/confirm', {
      userId,
      eventId: eventIdNum,
      seatIds
    });
    
    if (response.data) {
      ElMessage.success('选座成功，已创建订单');
      // 立即刷新座位状态（同步到管理端）
      await fetchSeats(false);
      // 跳转到个人中心我的订单页面
      await router.push('/personal-center');
    }
  } catch (error: any) {
    if (error.response?.data) {
      ElMessage.error(error.response.data);
    } else {
      ElMessage.error('确认选座失败');
    }
    // 刷新座位状态
    await fetchSeats();
  }
}

onMounted(() => {
  // 加载赛事和座位信息（不需要登录即可查看）
  fetchEventDetails();
  fetchSeats(true); // 首次加载

  // 检查登录状态并给出提示
  const userId = getCurrentUserId();
  const admin = isAdmin();
  if (!userId) {
    ElMessage.info('请先登录后购票');
  } else if (admin) {
    ElMessage.info('管理员账号仅可查看座位图，不能选座购票');
  }
  
  // 检查收藏状态（所有登录用户都检查）
  if (getCurrentUsername()) {
    checkCollectionStatus();
  }

  window.addEventListener('resize', () => myChart?.resize());

  // 监听座位更新广播
  bcChannel.value = new BroadcastChannel('seat_updates');
  bcChannel.value.onmessage = (msgEvent) => {
    const data = msgEvent.data;
    if (data.type === 'SEAT_UPDATED' && data.eventId === event.value.id) {
      console.log('收到座位更新通知:', data);
      // 刷新座位状态
      fetchSeats(false);
      ElMessage.info('座位状态已更新');
    }
  };
});

onUnmounted(async () => {
  window.removeEventListener('resize', () => myChart?.resize());
  // 关闭广播频道
  if (bcChannel.value) {
    bcChannel.value.close();
    bcChannel.value = null;
  }
  
  // 释放用户选中的座位
  const selectedSeats = seatData.value.filter(s => s.isSelected);
  for (const seat of selectedSeats) {
    await unlockSeat(seat);
  }
});
</script>

<style scoped>
.seat-selection-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  padding: 10px 2.5%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  align-items: center;
}

.event-brief {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.event-info h3 {
  margin: 0;
  font-size: 18px;
}

.event-info p {
  margin: 5px 0 0;
  font-size: 13px;
  color: #909399;
}

.collection-btn {
  margin-top: 5px;
  transition: all 0.3s ease;
}

.collection-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

.collection-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.main-layout {
  flex: 1;
  display: flex;
  padding: 20px 2.5%;
  gap: 20px;
  overflow: hidden;
}

.seat-chart-area {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  padding: 20px;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
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

.seat-chart-wrapper {
  flex: 1;
  width: 100%;
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

.order-preview-sidebar {
  width: 350px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.order-card .header-title {
  font-weight: bold;
  font-size: 16px;
  color: #1e293b;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selection-count {
  font-size: 13px;
  color: #1d4ed8;
  font-weight: bold;
  background: #eff6ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.selected-seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 15px 0;
  max-height: 200px;
  overflow-y: auto;
}

.seat-tag {
  padding: 8px 12px;
  height: auto;
  border-radius: 6px;
  background-color: #eff6ff;
  border-color: #dbeafe;
  color: #1d4ed8;
  font-weight: 500;
}

.order-summary {
  margin-top: auto;
  border-top: 1px dashed #e2e8f0;
  padding-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  color: #64748b;
}

.summary-row.total {
  margin: 15px 0 20px;
  font-size: 20px;
  font-weight: bold;
  color: #1e293b;
}

.total-price {
  color: #ef4444;
  display: inline-block;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.total-price.pulse {
  transform: scale(1.1);
  color: #dc2626;
}

.confirm-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(29, 78, 216, 0.2);
  transition: all 0.3s ease;
}

.confirm-button:not(:disabled):active {
  transform: scale(0.98);
}

.tip-alert {
  border-radius: 8px;
}

@media (max-width: 1024px) {
  .order-preview-sidebar {
    width: 100%;
  }
  
  .seat-chart-area {
    /* flex 纵向布局下需取消 flex:1，否则 flex-basis:0 会让图表容器高度塌陷 */
    flex: 0 0 auto;
    height: 500px;
    min-height: 380px;
  }
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
    overflow: auto;
  }
  
  .seat-chart-area {
    flex: 0 0 auto;
    height: 600px;
    min-height: 420px;
  }
  
  .order-preview-sidebar {
    width: 100%;
  }
}
</style>
