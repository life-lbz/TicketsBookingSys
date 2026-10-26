#!/bin/bash

# 赛事数据插入脚本 - 通过HTTP API

BASE_URL="http://localhost:8080/api/events"

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "2026 北京开山节",
    "time": "2026.03.27-03.29",
    "venue": "楠谷闪闪公园",
    "minPrice": 2280,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "综合运动",
    "city": "北京"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "武汉田径钻石联赛",
    "time": "2026.04.10-04.12",
    "venue": "武汉体育中心",
    "minPrice": 180,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "田径",
    "city": "武汉"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "成都电竞英雄联盟职业联赛",
    "time": "2026.05.01-05.05",
    "venue": "成都金融城演艺中心",
    "minPrice": 380,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "电竞",
    "city": "成都"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "上海网球大师赛",
    "time": "2026.10.08-10.15",
    "venue": "旗忠网球中心",
    "minPrice": 460,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "球类运动",
    "city": "上海"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "广州足球超级联赛",
    "time": "2026.06.15-06.20",
    "venue": "天河体育场",
    "minPrice": 120,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "广州"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "深圳游泳世界杯",
    "time": "2026.08.22-08.25",
    "venue": "深圳大运中心",
    "minPrice": 260,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "跳水/游泳",
    "city": "深圳"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "杭州电竞 DOTA2 国际邀请赛",
    "time": "2026.09.05-09.10",
    "venue": "杭州奥体中心",
    "minPrice": 580,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "电竞",
    "city": "杭州"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "南京田径世锦赛",
    "time": "2026.07.12-07.19",
    "venue": "南京奥体中心",
    "minPrice": 220,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "田径",
    "city": "南京"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "西安综合运动会",
    "time": "2026.08.01-08.10",
    "venue": "西安奥体中心",
    "minPrice": 150,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "综合运动",
    "city": "西安"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "重庆 UFC 格斗之夜",
    "time": "2026.11.02-11.03",
    "venue": "重庆华熙文体中心",
    "minPrice": 420,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "格斗搏击",
    "city": "重庆"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "天津排球联赛",
    "time": "2026.02.20-02.25",
    "venue": "天津体育馆",
    "minPrice": 90,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "天津"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "苏州羽毛球公开赛",
    "time": "2026.06.01-06.05",
    "venue": "苏州奥体中心",
    "minPrice": 160,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "苏州"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "青岛帆船公开赛",
    "time": "2026.07.20-07.23",
    "venue": "青岛奥帆中心",
    "minPrice": 320,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "综合运动",
    "city": "青岛"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "大连马拉松赛",
    "time": "2026.05.25-05.25",
    "venue": "大连城市赛道",
    "minPrice": 100,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "田径",
    "city": "大连"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "厦门马拉松赛",
    "time": "2026.01.05-01.05",
    "venue": "厦门环岛路",
    "minPrice": 120,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "田径",
    "city": "厦门"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "长沙篮球联赛",
    "time": "2026.12.05-12.10",
    "venue": "长沙贺龙体育馆",
    "minPrice": 140,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "长沙"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "郑州乒乓球世界杯",
    "time": "2026.09.20-09.24",
    "venue": "郑州奥体中心",
    "minPrice": 200,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "郑州"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "沈阳综合运动会",
    "time": "2026.08.15-08.24",
    "venue": "沈阳奥体中心",
    "minPrice": 130,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "综合运动",
    "city": "沈阳"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "济南游泳锦标赛",
    "time": "2026.07.05-07.08",
    "venue": "济南奥体中心",
    "minPrice": 180,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "跳水/游泳",
    "city": "济南"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "哈尔滨冰雪马拉松",
    "time": "2026.01.15-01.15",
    "venue": "哈尔滨冰雪大世界赛道",
    "minPrice": 220,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "田径",
    "city": "哈尔滨"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "石家庄田径锦标赛",
    "time": "2026.05.10-05.12",
    "venue": "石家庄奥体中心",
    "minPrice": 110,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "田径",
    "city": "石家庄"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "太原自由搏击赛",
    "time": "2026.10.15-10.16",
    "venue": "太原滨河体育中心",
    "minPrice": 260,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "格斗搏击",
    "city": "太原"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "合肥电竞王者荣耀职业联赛",
    "time": "2026.06.20-06.25",
    "venue": "合肥奥体中心",
    "minPrice": 320,
    "cover": "/uploads/events/default.jpg",
    "hot": true,
    "category": "电竞",
    "city": "合肥"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "福州羽毛球公开赛",
    "time": "2026.11.10-11.14",
    "venue": "福州奥体中心",
    "minPrice": 150,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "球类运动",
    "city": "福州"
  }'

echo ""

curl -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "南昌田径城市赛",
    "time": "2026.04.20-04.21",
    "venue": "南昌奥体中心",
    "minPrice": 100,
    "cover": "/uploads/events/default.jpg",
    "hot": false,
    "category": "田径",
    "city": "南昌"
  }'

echo ""
echo "所有赛事数据插入完成！"
