# 大型赛事门票预订与座位选择系统

一个前后端分离的赛事票务平台：观众可在线浏览赛事、可视化选座、下单支付；管理员可统一管理赛事、票务、订单、售后与账号。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3.5 + TypeScript + Vite + Element Plus + ECharts + Axios + Vue Router |
| 后端 | Spring Boot 3.2.3 + Spring Security + Spring Data JPA + JWT (jjwt 0.11.5) |
| 数据库 | MySQL 8.0 |
| 构建 | Maven 3.9 / JDK 17 |

## 功能模块

### 用户端

- **赛事大厅**：按名称 / 地点 / 类别搜索，热门标签、分页浏览、封面图展示
- **账号体系**：注册 / 登录（JWT + BCrypt），个人资料（头像、昵称、城市、兴趣等）
- **可视化选座**：ECharts 绘制座位图，实时区分「可选 / 已售 / 已选 / 锁定」四种状态
- **座位锁定**：选座即调用锁座接口，15 分钟支付倒计时，超时自动释放
- **下单支付**：确认选座生成订单 → 选择支付宝 / 微信 → 支付完成
- **订单管理**：全部 / 待支付 / 已完成 / 已取消 分类，支持取消、删除、售后申请
- **赛事收藏**：一键收藏 / 取消收藏

### 管理端

- **数据看板**：赛事总数、票务总数、订单总数、总销售额统计
- **赛事管理**：增删改查、封面上传、售票状态（售票中 / 停售 / 未发售）切换
- **票务管理**：按赛事批量生成座位、查看座位售出情况
- **订单管理**：多维度查看全量订单
- **售后处理**：处理用户退票 / 售后申请
- **账号管理**：用户账号、管理员账号（含超级管理员权限分级）

## 核心设计

1. **座位级并发控制**：座位表带 `status`（AVAILABLE / SOLD / LOCKED）+ `lock_time` + `lock_expire_time` + `locked_by`，配合定时任务扫描超时座位与超时订单，自动回滚库存。
2. **多页面实时同步**：通过 `BroadcastChannel` 广播 `SEAT_UPDATED` 事件，选座页与管理端票务页状态即时联动。
3. **数据一致性**：支付成功后同步更新订单状态、座位状态，并触发 `TotalSalesService` 重算赛事销售数据（订单数、已售座位、销售额、售出率）。
4. **停售策略**：赛事开始超过 2 天自动拒绝售票，防止脏数据下单。
5. **统一响应封装**：后端接口统一返回 `ApiResponse{code, message, data}`，前端 axios 拦截器统一处理 token 注入与 401 登出。

## 目录结构

```
TicketsBookingSys/
├── backend/                      Spring Boot 服务（端口 8080）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ticket/system/
│       │   ├── controller/       Event / Seat / Ticket / Order / User / Admin / AfterSales / Collection / TotalSales
│       │   ├── model/            Event, Seat, Order, User, UserInfo, Admin, AfterSales, Collection, TotalSales
│       │   ├── service/          业务逻辑与定时任务
│       │   ├── repository/       JPA 数据访问
│       │   ├── config/           安全、CORS、调度、数据初始化
│       │   ├── filter/           JWT 过滤器
│       │   ├── common/           ApiResponse、全局异常处理
│       │   └── utils/            工具类
│       └── resources/
│           ├── application.properties
│           └── data/             初始化 SQL 脚本
├── frontend/                     Vue 3 前端（端口 5174）
│   ├── vite.config.ts
│   └── src/
│       ├── api/                  axios 封装
│       ├── router/               路由与守卫
│       └── views/
│           ├── user/             Home, Login, SeatSelection, OrderConfirmation, PersonalCenter
│           └── admin/            AdminDashboard, AdminEvents, AdminTickets, AdminOrders, AdminManagement, UserManagement
└── uploads/                      赛事封面等运行时上传文件（未纳入版本控制）
```

## 快速启动

### 1. 准备数据库

```sql
CREATE DATABASE ticket_system DEFAULT CHARSET utf8mb4;
```

后端使用 `spring.jpa.hibernate.ddl-auto=update`，首次启动会自动建表。
按需执行 `backend/src/main/resources/data/` 与根目录下的补充 SQL（售后表、用户信息表、收藏表等）。

修改 `backend/src/main/resources/application.properties` 中的数据库连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ticket_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 2. 启动后端

```bash
cd backend
mvn -DskipTests package
java -jar target/TicketsBookingSys-backend-0.0.1-SNAPSHOT.jar
```

服务地址：<http://localhost:8080>

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：<http://localhost:5174>（若 5173 端口被占用，Vite 会自动递增；后端 CORS 已放开 `http://localhost:*`）

### 4. 访问

| 入口 | 地址 |
| --- | --- |
| 用户端 | <http://localhost:5174> |
| 管理端 | <http://localhost:5174/admin> |

## 说明

- `uploads/` 目录用于存放赛事封面等上传文件，仓库中未包含。首次运行时把图片放入 `uploads/events/images/` 并保证数据库中 `events.cover` 字段指向 `/uploads/events/images/xxx.png` 即可；也可通过管理端「赛事管理 → 上传封面」重新上传。
- 前端 `main.ts` 会在每次页面加载时清除登录标记（`user_isLoggedIn` / `admin_isLoggedIn`），因此刷新页面后需要重新登录，这是原设计行为。
- `application.properties` 中 `spring.jpa.show-sql=true` 会输出大量 SQL 日志，如需安静可改为 `false`。
