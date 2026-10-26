<template>
  <div class="home-container">
    <el-header class="header">
      <div class="header-left-group">
        <div class="logo-container" @click="$router.push('/')">
          <img src="../../assets/images/ticketsale.png" alt="Logo" class="logo-img" />
          <span class="logo-text">大型赛事门票预订与座位选择系统</span>
        </div>
        
        <!-- 城市选择器 -->
        <div class="city-selector">
          <el-popover
            placement="bottom-start"
            :width="400"
            trigger="click"
            v-model:visible="cityPopoverVisible"
          >
            <template #reference>
              <div class="current-city">
                <el-icon><Location /></el-icon>
                <span>{{ locatedCity || '全国' }}</span>
                <el-icon class="arrow-down"><ArrowDown /></el-icon>
              </div>
            </template>
            
            <div class="city-panel">
              <div class="city-panel-header">
                <span class="label">当前定位城市：</span>
                <span class="locating-city">{{ locatedCity || '定位中...' }}</span>
                <el-button link type="primary" :icon="Position" @click="handleLocate(true)">重新定位</el-button>
              </div>
              
              <div class="city-groups">
                <div class="city-group">
                  <div class="group-label">主要城市</div>
                  <div class="city-list">
                    <span 
                      v-for="city in majorCities" 
                      :key="city" 
                      class="city-item"
                      :class="{ active: filters.city === city }"
                      @click="selectCity(city)"
                    >
                      {{ city }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
        </div>

        <!-- 分类选择器 -->
        <div class="category-selector">
          <el-popover
            placement="bottom-start"
            :width="500"
            trigger="click"
            v-model:visible="categoryPopoverVisible"
          >
            <template #reference>
              <div class="current-category">
                <el-icon><Menu /></el-icon>
                <span>{{ filters.category }}</span>
                <el-icon class="arrow-down"><ArrowDown /></el-icon>
              </div>
            </template>
            
            <div class="category-panel">
              <div class="category-grid">
                <div 
                  v-for="cat in categoryGroups" 
                  :key="cat.name" 
                  class="category-item-detailed"
                  :class="{ active: filters.category === cat.name }"
                  @click="selectCategory(cat.name)"
                >
                  <div class="cat-header">
                    <span class="cat-name">{{ cat.name }}</span>
                  </div>
                  <div class="cat-desc">{{ cat.desc }}</div>
                </div>
              </div>
            </div>
          </el-popover>
        </div>
      </div>
      
      <div class="search-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索赛事名称、地点、类别"
          :prefix-icon="Search"
          class="search-input"
          clearable
          size="small"
          @keyup.enter="fetchEvents"
        >
          <template #append>
            <el-button 
              type="primary" 
              :loading="searching"
              size="small"
              @click="handleSearch"
            >
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="user-actions">
        <el-button link @click="goHome()" class="home-btn">首页</el-button>
        
        <!-- 管理员登录状态：只有管理员登录时显示，用户登录时不显示管理员登录入口 -->
        <el-dropdown 
          v-if="isAdmin" 
          trigger="click"
          @command="handleAdminCommand"
        >
          <el-button 
            type="primary" 
            link 
            class="admin-btn"
          >
            <el-icon><Setting /></el-icon>
            <span class="admin-username">{{ adminUsername }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin-dashboard">管理首页</el-dropdown-item>
              <el-dropdown-item command="admin-logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 管理员登录入口：只有用户未登录时显示 -->
        <el-dropdown 
          v-else-if="!isLoggedIn" 
          trigger="click"
          @command="handleAdminCommand"
        >
          <el-button 
            type="primary" 
            link 
            class="admin-btn"
          >
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin-login">管理员登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 用户登录状态：只有用户登录或管理员未登录时显示 -->
        <el-dropdown 
          v-if="!isAdmin" 
          trigger="click" 
          @command="handleUserCommand"
        >
          <el-button link class="user-dropdown-btn">
            <template v-if="isLoggedIn">
              <el-avatar :size="24" :src="currentUser?.avatar" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ username }}</span>
            </template>
            <template v-else>
              登录/注册
            </template>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-if="!isLoggedIn">
                <el-dropdown-item command="login">登录</el-dropdown-item>
                <el-dropdown-item command="register">注册</el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <main class="main-content">
      <!-- 轮播图/热门赛事推荐 -->
      <section class="featured-section">
        <el-carousel height="350px" indicator-position="outside">
          <el-carousel-item v-for="item in featuredEvents" :key="item.id">
            <div class="carousel-item-content">
              <img :src="getCoverUrl(item.cover)" :alt="item.name" />
              <div class="carousel-info">
                <h2>{{ item.name }}</h2>
                <p>{{ item.time }} | {{ item.venue }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 搜索结果展示 -->
      <section class="search-results-section" v-if="currentSearchQuery.trim() && searchResults.length > 0">
        <div class="search-results-header">
          <h3>搜索结果</h3>
          <span class="search-results-count">找到 {{ searchResults.length }} 个相关赛事</span>
          <el-button link type="primary" @click="clearSearch">清除搜索</el-button>
        </div>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="event in searchResults" :key="event.id">
            <el-card class="event-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToSeatSelection(event.id)">
              <div class="event-cover">
                <img :src="getCoverUrl(event.cover)" class="image" :alt="event.name" />
                <div v-if="event.hot" class="hot-tag">热门</div>
              </div>
              <div class="event-info">
                <h4 class="event-name">{{ event.name }}</h4>
                <div class="event-meta">
                  <p><el-icon><Calendar /></el-icon> {{ event.time }}</p>
                  <p><el-icon><Location /></el-icon> {{ event.venue }}</p>
                </div>
                <div class="event-footer">
                  <div class="event-status" :class="event.status">
                    {{ event.status }}
                  </div>
                  <span class="price">¥{{ event.minPrice }} <small>起</small></span>
                  <el-button v-if="event.status === '售票中'" type="primary" size="small" @click.stop="goToSeatSelection(event.id)">立即购票</el-button>
                  <el-button v-else type="info" size="small" disabled>{{ event.status }}</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section class="search-results-section" v-else-if="currentSearchQuery.trim() && searchResults.length === 0">
        <div class="no-search-results">
          <el-icon :size="48" color="#909399"><Search /></el-icon>
          <p>未找到与 "{{ currentSearchQuery }}" 相关的赛事</p>
          <el-button type="primary" plain @click="clearSearch">清除搜索</el-button>
        </div>
      </section>

      <!-- 全部体育赛事标题 -->
      <section class="all-events-title-section">
        <h2 class="all-events-title">全部体育赛事</h2>
      </section>

      <!-- 赛事列表 - 按类别分组（预览模式） -->
      <section class="event-list-section" v-if="!expandedCategory && filters.category === '全部赛事分类'">
        <div v-for="category in categoryGroups.filter(c => c.name !== '全部赛事分类' && getEventsByCategory(c.name).length > 0)" :key="category.name" class="category-section">
          <div class="category-header">
            <h3>{{ category.name }}</h3>
            <span class="category-desc">{{ category.desc }}</span>
          </div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="event in getEventsByCategory(category.name, 4)" :key="event.id">
              <el-card class="event-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToSeatSelection(event.id)">
                <div class="event-cover">
                  <img :src="getCoverUrl(event.cover)" class="image" :alt="event.name" />
                  <div v-if="event.hot" class="hot-tag">热门</div>
                  <el-button 
                    :icon="getCollectionIcon(event.id)" 
                    :type="isCollected(event.id) ? 'warning' : 'info'"
                    circle
                    @click.stop="toggleCollection(event.id)"
                    :disabled="!isLoggedIn"
                    class="collection-btn"
                    :title="!isLoggedIn ? '请先登录' : (isCollected(event.id) ? '取消收藏' : '收藏')"
                  />
                </div>
                <div class="event-info">
                  <h4 class="event-name">{{ event.name }}</h4>
                  <div class="event-meta">
                    <p><el-icon><Calendar /></el-icon> {{ event.time }}</p>
                    <p><el-icon><Location /></el-icon> {{ event.venue }}</p>
                  </div>
                  <div class="event-footer">
                  <div class="event-status" :class="event.status">
                    {{ event.status }}
                  </div>
                  <span class="price">¥{{ event.minPrice }} <small>起</small></span>
                  <el-button v-if="event.status === '售票中'" type="primary" size="small" @click.stop="goToSeatSelection(event.id)">立即购票</el-button>
                  <el-button v-else type="info" size="small" disabled>{{ event.status }}</el-button>
                </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div class="more-events-wrapper" v-if="getEventsByCategory(category.name).length > 4">
            <el-button type="primary" plain @click="expandCategoryFn(category.name)">
              更多赛事 <el-icon><ArrowDown /></el-icon>
            </el-button>
          </div>
        </div>
      </section>

      <!-- 分类筛选结果 -->
      <section class="event-list-section" v-if="filters.category !== '全部赛事分类' && !expandedCategory">
        <div class="expanded-header">
          <el-button link @click="resetCategoryFilter">
            <el-icon><ArrowLeft /></el-icon> 返回全部赛事
          </el-button>
        </div>
        <div class="category-section">
          <div class="category-header">
            <h3>{{ filters.category }}</h3>
            <span class="category-desc">{{ categoryGroups.find(c => c.name === filters.category)?.desc }}</span>
          </div>
          <el-row :gutter="20" v-if="eventList.length > 0">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="event in eventList" :key="event.id">
              <el-card class="event-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToSeatSelection(event.id)">
                <div class="event-cover">
                  <img :src="getCoverUrl(event.cover)" class="image" :alt="event.name" />
                  <div v-if="event.hot" class="hot-tag">热门</div>
                  <el-button 
                    :icon="getCollectionIcon(event.id)" 
                    :type="isCollected(event.id) ? 'warning' : 'info'"
                    circle
                    @click.stop="toggleCollection(event.id)"
                    :disabled="!isLoggedIn"
                    class="collection-btn"
                    :title="!isLoggedIn ? '请先登录' : (isCollected(event.id) ? '取消收藏' : '收藏')"
                  />
                </div>
                <div class="event-info">
                  <h4 class="event-name">{{ event.name }}</h4>
                  <div class="event-meta">
                    <p><el-icon><Calendar /></el-icon> {{ event.time }}</p>
                    <p><el-icon><Location /></el-icon> {{ event.venue }}</p>
                  </div>
                  <div class="event-footer">
                  <div class="event-status" :class="event.status">
                    {{ event.status }}
                  </div>
                  <span class="price">¥{{ event.minPrice }} <small>起</small></span>
                  <el-button v-if="event.status === '售票中'" type="primary" size="small" @click.stop="goToSeatSelection(event.id)">立即购票</el-button>
                  <el-button v-else type="info" size="small" disabled>{{ event.status }}</el-button>
                </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div v-else class="no-events">暂无{{ filters.category }}赛事</div>
        </div>
      </section>

      <!-- 展开的类别详情 -->
      <section class="event-list-section expanded-section" v-if="expandedCategory">
        <div class="expanded-header">
          <el-button link @click="closeExpandedCategory">
            <el-icon><ArrowLeft /></el-icon> 返回全部赛事
          </el-button>
        </div>
        <div class="category-section">
          <div class="category-header">
            <h3>{{ getExpandedCategory()?.name }}</h3>
            <span class="category-desc">{{ getExpandedCategory()?.desc }}</span>
          </div>
          <el-row :gutter="20" v-if="getEventsByCategory(expandedCategory).length > 0">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="event in getEventsByCategory(expandedCategory)" :key="event.id">
              <el-card class="event-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToSeatSelection(event.id)">
                <div class="event-cover">
                  <img :src="getCoverUrl(event.cover)" class="image" :alt="event.name" />
                  <div v-if="event.hot" class="hot-tag">热门</div>
                  <el-button 
                    :icon="getCollectionIcon(event.id)" 
                    :type="isCollected(event.id) ? 'warning' : 'info'"
                    circle
                    @click.stop="toggleCollection(event.id)"
                    :disabled="!isLoggedIn"
                    class="collection-btn"
                    :title="!isLoggedIn ? '请先登录' : (isCollected(event.id) ? '取消收藏' : '收藏')"
                  />
                </div>
                <div class="event-info">
                  <h4 class="event-name">{{ event.name }}</h4>
                  <div class="event-meta">
                    <p><el-icon><Calendar /></el-icon> {{ event.time }}</p>
                    <p><el-icon><Location /></el-icon> {{ event.venue }}</p>
                  </div>
                  <div class="event-footer">
                  <div class="event-status" :class="event.status">
                    {{ event.status }}
                  </div>
                  <span class="price">¥{{ event.minPrice }} <small>起</small></span>
                  <el-button v-if="event.status === '售票中'" type="primary" size="small" @click.stop="goToSeatSelection(event.id)">立即购票</el-button>
                  <el-button v-else type="info" size="small" disabled>{{ event.status }}</el-button>
                </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div v-else class="no-events">暂无赛事</div>
        </div>
      </section>
    </main>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordVisible"
      title="修改密码"
      width="450px"
    >
      <el-form :model="changePasswordForm" :rules="changePasswordRules" ref="changePasswordFormRef" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="changePasswordForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="changePasswordForm.code" placeholder="请输入验证码" />
            <div class="captcha-container" @click="generateChangePasswordCaptcha">
              <canvas ref="changePasswordCaptchaCanvas" width="120" height="40" class="captcha-image"></canvas>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="changePasswordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="changePasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="changePasswordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePasswordSubmit" :loading="changePasswordLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Calendar, Location, ArrowDown, ArrowLeft, Position, Menu, Setting, User, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const router = useRouter()
const searchText = ref('')
const currentSearchQuery = ref('')
const searching = ref(false)
const isLoggedIn = ref(localStorage.getItem('user_isLoggedIn') === 'true')
const isAdmin = ref(localStorage.getItem('admin_isLoggedIn') === 'true')
const username = ref(localStorage.getItem('user_username') || '我的账户')
const adminUsername = ref(localStorage.getItem('admin_username') || '')
// 获取用户信息，包含头像
const savedUser = localStorage.getItem('user_user') || '{}'
let parsedUser = JSON.parse(savedUser)

// 检查是否有双层包装
if (parsedUser && parsedUser.value) {
  parsedUser = parsedUser.value
}

const currentUser = ref(parsedUser)

// 修改密码相关
const changePasswordVisible = ref(false)
const changePasswordLoading = ref(false)
const changePasswordFormRef = ref()
const changePasswordCaptchaCanvas = ref<HTMLCanvasElement | null>(null)
const currentChangePasswordCaptcha = ref('')
const changePasswordForm = reactive({
  phone: '',
  code: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const changePasswordRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^[A-Z]{4}$/, message: '请输入4个大写字母', trigger: 'blur' }
  ],
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== changePasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 从服务器获取用户信息（包括头像和昵称）
const fetchUserInfo = async () => {
  if (!currentUser.value?.username) {
    console.log('用户名为空，跳过获取用户信息')
    return
  }
  
  try {
    const response = await api.get(`/users/info/${currentUser.value.username}`)
    let userInfo = response.data.data || {}
    
    // 处理可能的双层包装
    if (userInfo && userInfo.value) {
      userInfo = userInfo.value
    }
    
    console.log('从服务器获取的用户信息:', userInfo)
    
    // 更新头像信息
    if (userInfo.avatar) {
      currentUser.value.avatar = userInfo.avatar
    }
    
    // 更新昵称信息
    if (userInfo.nickname) {
      currentUser.value.nickname = userInfo.nickname
    }
    
    // 更新localStorage
    localStorage.setItem('user_user', JSON.stringify(currentUser.value))
    console.log('用户信息已更新到localStorage')
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 刷新登录状态
const refreshLoginStatus = () => {
  isLoggedIn.value = localStorage.getItem('user_isLoggedIn') === 'true'
  username.value = localStorage.getItem('user_username') || '我的账户'
  
  // 刷新用户信息
  const userData = JSON.parse(localStorage.getItem('user_user') || '{}')
  if (userData && userData.value) {
    currentUser.value = userData.value
  } else {
    currentUser.value = userData
  }
}

// 收藏相关
const collectedEvents = ref<Set<number>>(new Set())

const filters = reactive({
  city: '全国',
  category: '全部赛事分类'
})

const cityPopoverVisible = ref(false)
const categoryPopoverVisible = ref(false)
const locatedCity = ref('')
const majorCities = ['全国', '北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '南京', '西安', '重庆', '天津', '苏州', '青岛', '大连', '厦门', '长沙', '郑州', '沈阳', '济南', '哈尔滨', '石家庄', '太原', '合肥', '福州', '南昌', '南宁', '昆明', '贵阳', '兰州', '海口', '三亚']
const categoryGroups = [
  { name: '全部赛事分类', desc: '查看所有赛事' },
  { name: '球类运动', desc: '足球、篮球、网球、排球、乒乓球、羽毛球等' },
  { name: '田径', desc: '奥运会田径、世锦赛、钻石联赛' },
  { name: '跳水/游泳', desc: '游泳世锦赛、奥运会游泳、跳水世界杯' },
  { name: '格斗搏击', desc: 'UFC、拳击、自由搏击' },
  { name: '电竞', desc: 'LOL、DOTA2、CSGO、王者荣耀等职业赛事' },
  { name: '综合运动', desc: '奥运会、亚运会、全运会' }
]

const cityNameMap: Record<string, string> = {
  'Beijing': '北京',
  'Shanghai': '上海',
  'Guangzhou': '广州',
  'Shenzhen': '深圳',
  'Hangzhou': '杭州',
  'Chengdu': '成都',
  'Wuhan': '武汉',
  'Nanjing': '南京',
  'Xian': '西安',
  'Xi\'an': '西安',
  'Chongqing': '重庆',
  'Tianjin': '天津',
  'Suzhou': '苏州',
  'Qingdao': '青岛',
  'Dalian': '大连',
  'Xiamen': '厦门',
  'Changsha': '长沙',
  'Zhengzhou': '郑州',
  'Shenyang': '沈阳',
  'Jinan': '济南',
  'Harbin': '哈尔滨',
  'Shijiazhuang': '石家庄',
  'Taiyuan': '太原',
  'Hefei': '合肥',
  'Fuzhou': '福州',
  'Nanchang': '南昌',
  'Nanning': '南宁',
  'Kunming': '昆明',
  'Guiyang': '贵阳',
  'Lanzhou': '兰州',
  'Yinchuan': '银川',
  'Xining': '西宁',
  'Urumqi': '乌鲁木齐',
  'Lhasa': '拉萨',
  'Haikou': '海口',
  'Sanya': '三亚',
  'Changchun': '长春',
  'Mudanjiang': '牡丹江',
  'Jilin': '吉林',
  'Tangshan': '唐山',
  'Qiqihar': '齐齐哈尔',
  'Baotou': '包头',
  'Hohhot': '呼和浩特',
  'Luoyang': '洛阳',
  'Weifang': '潍坊',
  'Yantai': '烟台',
  'Zibo': '淄博',
  'Ningbo': '宁波',
  'Wenzhou': '温州',
  'Jinhua': '金华',
  'Foshan': '佛山',
  'Zhongshan': '中山',
  'Zhuhai': '珠海',
  'Huizhou': '惠州',
  'Dongguan': '东莞',
  'Shantou': '汕头',
  'Xuzhou': '徐州',
  'Wuxi': '无锡',
  'Changzhou': '常州',
  'Yangzhou': '扬州',
  'Taizhou-Jiangsu': '泰州',
  'Shaoxing': '绍兴',
  'Taizhou-Zhejiang': '台州',
  'Lianyungang': '连云港',
  'Nanyang': '南阳',
  'Kaifeng': '开封',
  'Anyang': '安阳',
  'Xinxiang': '新乡',
  'Jiaozuo': '焦作',
  'Puyang': '濮阳',
  'Xuchang': '许昌',
  'Luohe': '漯河',
  'Sanmenxia': '三门峡',
  'Shangqiu': '商丘',
  'Zhumadian': '驻马店',
  'Xinyang': '信阳',
  'Zhoukou': '周口',
  'Nanchong': '南充',
  'Mianyang': '绵阳',
  'Zigong': '自贡',
  'Neijiang': '内江',
  'Leshan': '乐山',
  'Dazhou': '达州',
  'Luzhou': '泸州',
  'Yibin': '宜宾',
  'Deyang': '德阳',
  'Meishan': '眉山',
  'Ziyang': '资阳',
  'Suining': '遂宁',
  'Guang-an': '广安',
  'Bazhong': '巴中',
  'Ya-an': '雅安',
  'Panzhihua': '攀枝花',
  'Liupanshui': '六盘水',
  'Zunyi': '遵义',
  'Anshun': '安顺',
  'Kaili': '凯里',
  'Duyun': '都匀',
  'Xiangtan': '湘潭',
  'Hengyang': '衡阳',
  'Yueyang': '岳阳',
  'Zhangjiajie': '张家界',
  'Yiyang': '益阳',
  'Changde': '常德',
  'Chenzhou': '郴州',
  'Yongzhou': '永州',
  'Shaoyang': '邵阳',
  'Huaihua': '怀化',
  'Xiangxi': '湘西',
  'Jingzhou': '荆州',
  'Yichang': '宜昌',
  'Xiangyang': '襄阳',
  'Xiaogan': '孝感',
  'Huangshi': '黄石',
  'Ezhou': '鄂州',
  'Jingmen': '荆门',
  'Shiyan': '十堰',
  'Huanggang': '黄冈',
  'Xianning': '咸宁',
  'Enshi': '恩施',
  'Shennongjia': '神农架',
  'Xianyang': '咸阳',
  'Yan\'an': '延安',
  'Weinan': '渭南',
  'Ankang': '安康',
  'Shangluo': '商洛',
  'Tongchuan': '铜川',
  'Baoji': '宝鸡',
  'Hanzhong': '汉中',
  'Yulin': '榆林'
}

const convertCityName = (city: string): string => {
  if (!city) return city
  city = city.trim()
  return cityNameMap[city] || city
}

// 定位缓存
let locationCache: { city: string; timestamp: number } | null = null
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存（缩短缓存时间）

const handleLocate = async (forceRefresh: boolean = false) => {
  // 强制刷新时清除缓存
  if (forceRefresh) {
    locationCache = null
  }

  // 检查缓存
  if (locationCache && Date.now() - locationCache.timestamp < CACHE_DURATION) {
    locatedCity.value = locationCache.city
    ElMessage.success(`定位成功：${locatedCity.value}`)
    return
  }

  locatedCity.value = ''
  ElMessage.info('正在获取位置信息...')

  // 检查浏览器定位权限
  if (navigator.permissions && navigator.permissions.query) {
    try {
      const permissionStatus = await navigator.permissions.query({ name: 'geolocation' })
      console.log('定位权限状态:', permissionStatus.state)
      
      if (permissionStatus.state === 'denied') {
        ElMessage.warning('定位权限已被拒绝，请在浏览器设置中开启定位权限')
        // 直接尝试IP定位
        await tryIpLocation()
        return
      }
    } catch (error) {
      console.log('权限检查失败:', error)
    }
  }

  // 优先使用 GPS 定位
  if (navigator.geolocation) {
    try {
      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          timeout: 8000,
          enableHighAccuracy: true
        })
      })

      const { latitude, longitude } = position.coords
      console.log('GPS定位成功，经纬度:', { latitude, longitude })

      try {
        // 尝试使用百度地图API
        const baiduSuccess = await tryBaiduGeocoding(latitude, longitude)
        if (baiduSuccess) {
          return
        }
        // 百度API失败，尝试IP定位
        console.log('百度API失败，尝试IP定位')
        await tryIpLocation()
        return
      } catch (apiError) {
        console.error('百度API调用失败，尝试IP定位:', apiError)
        await tryIpLocation()
        return
      }
    } catch (gpsError) {
      console.error('GPS定位失败，尝试IP定位:', gpsError)
      // GPS定位失败，尝试IP定位
      await tryIpLocation()
      return
    }
  } else {
    console.log('浏览器不支持GPS定位，尝试IP定位')
    // 浏览器不支持GPS定位，尝试IP定位
    await tryIpLocation()
  }

  // 默认定位到北京
  setDefaultLocation()
}

// 尝试百度地图逆地理编码
const tryBaiduGeocoding = async (latitude: number, longitude: number): Promise<boolean> => {
  try {
    const response = await fetch(
      `/api/baidu/reverse_geocoding/v3/?ak=E4805d16520de693a3fe707cdc962045&output=json&coordtype=wgs84ll&location=${latitude},${longitude}`
    )
    console.log('百度API响应状态:', response.status)
    
    if (!response.ok) {
      console.log('百度API请求失败，尝试其他定位方式')
      return false
    }
    
    const data = await response.json()
    console.log('百度API响应数据:', data)

    if (data.status === 0 && data.result) {
      let city = data.result.addressComponent?.city || data.result.addressComponent?.province || ''
      console.log('百度API返回城市:', city)

      if (!city || city === '全国') {
        city = '北京'
      }

      city = convertCityName(city)
      city = city.replace('市', '').replace('省', '')
      locatedCity.value = city
      // 缓存定位结果（不更新筛选城市，保持默认"全国"）
      locationCache = { city, timestamp: Date.now() }
      ElMessage.success(`定位成功：${locatedCity.value}`)
      return true
    } else {
      console.log('百度API返回错误:', data.status, data.message)
      return false
    }
  } catch (error) {
    console.error('百度API调用失败:', error)
    return false
  }
}

// 尝试IP定位
const tryIpLocation = async () => {
  // 首先尝试使用 ipapi.co（更稳定的IP定位服务）
  try {
    const response = await fetch('https://ipapi.co/json/')
    console.log('IPAPI.CO响应状态:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('IPAPI.CO响应数据:', data)
      if (data.city) {
        let city = data.city
        console.log('IPAPI.CO返回城市:', city)
        city = convertCityName(city)
        city = city.replace('市', '').replace('省', '')
        locatedCity.value = city
        locationCache = { city, timestamp: Date.now() }
        ElMessage.success(`定位成功：${locatedCity.value}`)
        return
      }
    }
  } catch (_error) {
    console.error('IPAPI.CO定位失败')
  }

  // 尝试使用 freegeoip.app
  try {
    const response = await fetch('https://freegeoip.app/json/')
    console.log('FreeGeoIP响应状态:', response.status)
    if (response.ok) {
      const data = await response.json()
      console.log('FreeGeoIP响应数据:', data)
      if (data.city) {
        let city = data.city
        console.log('FreeGeoIP返回城市:', city)
        city = convertCityName(city)
        city = city.replace('市', '').replace('省', '')
        locatedCity.value = city
        locationCache = { city, timestamp: Date.now() }
        ElMessage.success(`定位成功：${locatedCity.value}`)
        return
      }
    }
  } catch (_error) {
    console.error('FreeGeoIP定位失败')
  }

  // IP定位失败，使用默认位置
  setDefaultLocation()
}

// 设置默认位置
const setDefaultLocation = () => {
  locatedCity.value = '北京'
  locationCache = { city: '北京', timestamp: Date.now() }
  ElMessage.warning('定位失败，已默认定位到北京')
}

const selectCity = async (city: string) => {
  filters.city = city
  cityPopoverVisible.value = false
  currentPage.value = 1
  await fetchEvents()
}

const selectCategory = async (category: string) => {
  filters.category = category
  categoryPopoverVisible.value = false
  currentPage.value = 1
  await fetchEvents()
}

const resetCategoryFilter = async () => {
  filters.category = '全部赛事分类'
  currentPage.value = 1
  await fetchEvents()
}

const featuredEvents = ref<any[]>([])
const eventList = ref<any[]>([])
const allEvents = ref<any[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 展开的类别
const expandedCategory = ref<string>('')

const fetchEvents = async () => {
  searching.value = true

  try {
    // 从后端 API 获取赛事数据
    const response = await api.get('/events')
    allEvents.value = response.data.data || []

    // Filter based on selected city and category
    let filteredEventsList = [...allEvents.value]

    // 1. Filter by search text (matches name, venue, city, category, or category descriptions)
    if (currentSearchQuery.value.trim()) {
      const query = currentSearchQuery.value.trim().toLowerCase()

      // Find category names whose name or description matches the search text
      const matchedCategoryNames = categoryGroups
        .filter(cat => cat.name.toLowerCase().includes(query) || cat.desc.toLowerCase().includes(query))
        .map(cat => cat.name)

      filteredEventsList = filteredEventsList.filter((e: any) => {
        const nameMatch = e.name?.toLowerCase().includes(query)
        const venueMatch = e.venue?.toLowerCase().includes(query)
        const cityMatch = e.city?.toLowerCase().includes(query)
        const categoryMatch = matchedCategoryNames.includes(e.category) || e.category?.toLowerCase().includes(query)
        const statusMatch = e.status === '售票中' || e.status === '售罄' || e.status === '停售'
        return (nameMatch || venueMatch || cityMatch || categoryMatch) && statusMatch
      })
    } else {
      // 显示售票中、售罄和停售状态的赛事
      filteredEventsList = filteredEventsList.filter((e: any) => e.status === '售票中' || e.status === '售罄' || e.status === '停售')
    }

    // 2. Filter by city
    if (filters.city !== '全国') {
      filteredEventsList = filteredEventsList.filter((e: any) => e.city === filters.city)
    }

    // 3. Filter by category
    if (filters.category !== '全部赛事分类') {
      filteredEventsList = filteredEventsList.filter((e: any) => e.category === filters.category)
    }

    // 更新总数
    total.value = filteredEventsList.length

    // 分页处理
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    eventList.value = filteredEventsList.slice(start, end)

    featuredEvents.value = allEvents.value.filter((e: any) => e.hot && (e.status === '售票中' || e.status === '售罄' || e.status === '停售')).slice(0, 10)
  } catch (error: any) {
    console.error('获取赛事列表失败:', error)
    ElMessage.error('获取赛事列表失败：' + (error.response?.data || error.message))
  }

  searching.value = false
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

// 根据类别获取赛事（热门赛事优先）
const getEventsByCategory = (category: string, limit?: number) => {
  const filtered = allEvents.value.filter(event => {
    const matchCity = filters.city === '全国' || event.city === filters.city
    const matchCategory = event.category === category
    const matchStatus = event.status === '售票中' || event.status === '售罄' || event.status === '停售'
    return matchCity && matchCategory && matchStatus
  })
  // 热门赛事排在前面
  const sorted = filtered.sort((a, b) => {
    if (a.hot === b.hot) return 0
    return a.hot ? -1 : 1
  })
  // 如果有限制数量，返回前N个
  return limit ? sorted.slice(0, limit) : sorted
}

// 获取展开的类别信息
const getExpandedCategory = () => {
  return categoryGroups.find(c => c.name === expandedCategory.value)
}

// 展开类别
const expandCategoryFn = (categoryName: string) => {
  expandedCategory.value = categoryName
}

// 关闭展开的类别
const closeExpandedCategory = () => {
  expandedCategory.value = ''
}

// 点击搜索按钮执行搜索
const handleSearch = async () => {
  currentSearchQuery.value = searchText.value.trim()
  currentPage.value = 1
  await fetchEvents()
}

// 搜索结果
const searchResults = computed(() => {
  if (!currentSearchQuery.value.trim()) return []
  
  const query = currentSearchQuery.value.trim().toLowerCase()
  
  // Find category names whose name or description matches the search text
  const matchedCategoryNames = categoryGroups
    .filter(cat => cat.name.toLowerCase().includes(query) || cat.desc.toLowerCase().includes(query))
    .map(cat => cat.name)
  
  return allEvents.value.filter((e: any) => {
    const nameMatch = e.name?.toLowerCase().includes(query)
    const venueMatch = e.venue?.toLowerCase().includes(query)
    const cityMatch = e.city?.toLowerCase().includes(query)
    const categoryMatch = matchedCategoryNames.includes(e.category) || e.category?.toLowerCase().includes(query)
    const statusMatch = e.status === '售票中' || e.status === '售罄' || e.status === '停售'
    return (nameMatch || venueMatch || cityMatch || categoryMatch) && statusMatch
  })
})

// 清除搜索
const clearSearch = () => {
  searchText.value = ''
  currentSearchQuery.value = ''
}

// Watch city and category filter
watch([() => filters.city, () => filters.category], async () => {
  await fetchEvents()
})

onMounted(async () => {
  // 刷新登录状态（确保从localStorage正确读取）
  refreshLoginStatus()
  
  await fetchEvents() // 加载赛事数据
  await handleLocate() // 自动定位
  await fetchCollectedEvents() // 获取收藏列表
  
  // 获取用户信息（包括头像）
  if (isLoggedIn.value) {
    await fetchUserInfo()
  }
  
  // 监听localStorage变化，确保用户信息能够同步更新
  window.addEventListener('storage', (e) => {
    if (e.key === 'user_user' || e.key === 'user_isLoggedIn') {
      refreshLoginStatus()
    }
  })
})

const handleUserCommand = (command: string) => {
  // 刷新登录状态，确保信息同步
  refreshLoginStatus()
  
  if (command === 'login') {
    router.push('/login')
  } else if (command === 'register') {
    router.push('/login?tab=register')
  } else if (command === 'profile') {
    router.push('/personal-center')
  } else if (command === 'change-password') {
    changePasswordForm.phone = ''
    changePasswordForm.code = ''
    changePasswordForm.oldPassword = ''
    changePasswordForm.newPassword = ''
    changePasswordForm.confirmPassword = ''
    changePasswordVisible.value = true
    setTimeout(() => {
      generateChangePasswordCaptcha()
    }, 100)
  } else if (command === 'admin') {
    router.push('/admin/dashboard')
  } else if (command === 'logout') {
    localStorage.removeItem('user_isLoggedIn')
    localStorage.removeItem('user_isAdmin')
    localStorage.removeItem('user_username')
    localStorage.removeItem('user_userId')
    localStorage.removeItem('user_user')
    isLoggedIn.value = false
    username.value = '我的账户'
    ElMessage.success('已退出登录')
    router.push('/')
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

// 生成修改密码页面的图形验证码
const generateChangePasswordCaptcha = () => {
  const canvas = changePasswordCaptchaCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  // 清除画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 生成验证码
  const code = generateRandomCode()
  currentChangePasswordCaptcha.value = code
  
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
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.3)`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }
  
  // 绘制验证码文字
  const fontSizes = [18, 20, 22, 24]
  for (let i = 0; i < code.length; i++) {
    const char = code[i]
    ctx.font = `${fontSizes[Math.floor(Math.random() * fontSizes.length)]}px Arial`
    ctx.fillStyle = `rgba(${Math.floor(Math.random() * 100 + 50)}, ${Math.floor(Math.random() * 100 + 50)}, ${Math.floor(Math.random() * 100 + 50)}, 0.8)`
    ctx.textAlign = 'center'
    ctx.save()
    ctx.translate(20 + i * 28, canvas.height / 2 + 8)
    ctx.rotate((Math.random() - 0.5) * 0.3)
    ctx.fillText(char, 0, 0)
    ctx.restore()
  }
}

// 提交修改密码
const handleChangePasswordSubmit = async () => {
  // 验证图形验证码
  if (changePasswordForm.code.toUpperCase() !== currentChangePasswordCaptcha.value) {
    ElMessage.error('验证码错误')
    generateChangePasswordCaptcha()
    return
  }

  const valid = await changePasswordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changePasswordLoading.value = true
  try {
    const userId = localStorage.getItem('user_userId')
    if (!userId) {
      ElMessage.error('用户ID不存在')
      return
    }
    
    await api.put(`/users/${userId}/change-password`, {
      phone: changePasswordForm.phone,
      oldPassword: changePasswordForm.oldPassword,
      newPassword: changePasswordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    changePasswordVisible.value = false
    
    // 修改密码后强制重新登录
    localStorage.removeItem('user_isLoggedIn')
    localStorage.removeItem('user_isAdmin')
    localStorage.removeItem('user_username')
    localStorage.removeItem('user_userId')
    localStorage.removeItem('user_user')
    isLoggedIn.value = false
    username.value = '我的账户'
    ElMessage.info('密码已修改，请重新登录')
    await router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || error.response?.data || '密码修改失败')
    generateChangePasswordCaptcha()
  } finally {
    changePasswordLoading.value = false
  }
}

// 管理员命令处理
const handleAdminCommand = (command: string) => {
  if (command === 'admin-login') {
    router.push('/admin')
  } else if (command === 'admin-dashboard') {
    router.push('/admin/dashboard')
  } else if (command === 'admin-logout') {
    localStorage.removeItem('admin_isLoggedIn')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_id')
    localStorage.removeItem('admin_role')
    isAdmin.value = false
    adminUsername.value = ''
    ElMessage.success('管理员已退出登录')
    router.push('/')
  }
}

const goToSeatSelection = (id: number) => {
  router.push(`/seat-selection/${id}`)
}

// 点击首页按钮返回顶部
const goHome = () => {
  // 如果已经在首页，直接滚动到顶部
  if (window.location.pathname === '/') {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  } else {
    // 跳转到首页
    router.push('/')
  }
}

// 获取当前用户名
const getCurrentUsername = (): string | null => {
  return localStorage.getItem('user_username')
}

// 检查赛事是否已收藏
const isCollected = (eventId: number): boolean => {
  return collectedEvents.value.has(eventId)
}

// 获取收藏图标
const getCollectionIcon = (eventId: number) => {
  return isCollected(eventId) ? StarFilled : Star
}

// 切换收藏状态
const toggleCollection = async (eventId: number) => {
  const username = getCurrentUsername()
  if (!username) {
    ElMessage.warning('请先登录后收藏')
    return
  }
  
  try {
    if (isCollected(eventId)) {
      // 取消收藏
      await api.delete('/users/collections', {
        data: {
          username,
          eventId
        }
      })
      collectedEvents.value.delete(eventId)
      ElMessage.success('取消收藏成功')
    } else {
      // 添加收藏
      await api.post('/users/collections', {
        username,
        eventId
      })
      collectedEvents.value.add(eventId)
      ElMessage.success('收藏成功')
    }
  } catch (error: any) {
    console.error('操作收藏失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.response?.data) {
      ElMessage.error(error.response.data)
    } else {
      ElMessage.error('操作收藏失败')
    }
  }
}

// 获取用户的收藏列表
const fetchCollectedEvents = async () => {
  const username = getCurrentUsername()
  if (!username) return
  
  try {
    const response = await api.get(`/users/collections/${username}`)
    const collections = response.data.data || []
    const collectedIds = new Set(collections.map((item: any) => item.id))
    collectedEvents.value = collectedIds as Set<number>
  } catch (error: any) {
    console.error('获取收藏列表失败:', error)
  }
}
</script>

<style scoped>
.home-container {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2.5%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left-group {
  display: flex;
  align-items: center;
  gap: 30px;
}

.city-selector, .category-selector {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.current-city, .current-category {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.current-city:hover, .current-category:hover {
  background-color: #f5f7fa;
}

.arrow-down {
  font-size: 12px;
  color: #909399;
}

.city-panel, .category-panel {
  padding: 10px;
}

.category-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.category-item-detailed {
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.category-item-detailed:hover {
  background-color: #f8fafc;
  border-color: #3b82f6;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(59, 130, 246, 0.15);
}

.category-item-detailed.active {
  background: linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%);
  color: #fff;
  border-color: #1d4ed8;
  box-shadow: 0 3px 8px rgba(29, 78, 216, 0.3);
}

.cat-header {
  margin-bottom: 4px;
}

.cat-name {
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0;
}

.cat-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.category-item-detailed.active .cat-desc {
  color: rgba(255, 255, 255, 0.85);
}

.city-panel-header {
  display: flex;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
  margin-bottom: 15px;
}

.city-panel-header .label {
  font-size: 13px;
  color: #909399;
}

.city-panel-header .locating-city {
  font-size: 14px;
  font-weight: bold;
  color: #1d4ed8;
  margin: 0 10px;
}

.city-group {
  margin-bottom: 15px;
}

.group-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.city-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.city-item {
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.city-item:hover {
  color: #1d4ed8;
  background-color: #eff6ff;
}

.city-item.active {
  color: #fff;
  background-color: #1d4ed8;
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

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-dropdown-btn {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  margin-right: 4px;
}

.admin-username {
  color: #f59e0b;
  font-weight: 600;
  font-size: 14px;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    padding: 10px;
    height: auto;
  }
  
  .header-left-group {
    width: 100%;
    justify-content: space-between;
    margin-bottom: 10px;
  }
  
  .search-bar {
    width: 100%;
    margin: 10px 0;
  }
  
  .user-actions {
    width: 100%;
    justify-content: space-around;
  }
}

.search-bar {
  flex: 0 1 400px;
  margin: 0 20px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-btn {
  margin-right: 10px;
}

.main-content {
  max-width: 1440px;
  width: 95%;
  margin: 20px auto;
  padding: 0 20px;
}

.carousel-item-content {
  position: relative;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-item-content img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: white;
}

.search-results-section {
  margin: 30px 0;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);
  border: 1px solid #e6f2ff;
}

.search-results-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.search-results-header h3 {
  font-size: 22px;
  font-weight: 700;
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

.no-search-results {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.no-search-results p {
  margin: 20px 0;
  font-size: 16px;
}

.all-events-title-section {
  margin: 40px 0 30px 0;
  text-align: center;
}

.all-events-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  position: relative;
  display: inline-block;
}

.all-events-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: #409eff;
  border-radius: 2px;
}

.category-section {
  margin-bottom: 50px;
  background: #ffffff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.more-events-wrapper {
  text-align: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.expanded-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.expanded-header {
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.category-header {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 3px solid #409eff;
  position: relative;
}

.category-header::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 100px;
  height: 6px;
  background: #409eff;
  border-radius: 3px;
  clip-path: polygon(
    0% 50%,
    5% 0%,
    10% 50%,
    15% 100%,
    20% 50%,
    25% 0%,
    30% 50%,
    35% 100%,
    40% 50%,
    45% 0%,
    50% 50%,
    55% 100%,
    60% 50%,
    65% 0%,
    70% 50%,
    75% 100%,
    80% 50%,
    85% 0%,
    90% 50%,
    95% 100%,
    100% 50%
  );
}

.category-header h3 {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  letter-spacing: 1px;
}

.category-desc {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
}

.no-events {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 15px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
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

.hot-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(238, 90, 90, 0.3);
  z-index: 1;
}

.collection-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1;
  transition: all 0.3s ease;
}

.collection-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 200, 0, 0.4);
}

.collection-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.event-status {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}

/* 验证码相关样式 */
.code-input {
  display: flex;
  align-items: center;
  gap: 10px;
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

.price small {
  font-size: 12px;
  font-weight: normal;
}
</style>
