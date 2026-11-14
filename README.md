# 青藏高原非物质文化遗产资源数据管理系统

- 后端：Java 17 + SpringBoot 3 + Spring-Data-MongoDB + Spring Security + JWT
- 前端：Vue3 + Vite + Element-Plus + Axios + Pinia + ECharts
- 数据库：MongoDB（默认本地 `mongodb://localhost:27017/heritage_db`）
- 接口前缀 `/api`，后端端口 `8080`，前端开发端口 `5173`

## 目录结构

```
1/
├─ heritage-java-backend/   # SpringBoot3 后端
└─ heritage-frontend/       # Vue3 前端
```

## 一、启动后端

```
1. 修改 application.yml 填写 MongoDB 连接串
   # 本地：   mongodb://localhost:27017/heritage_db
   # Atlas： mongodb+srv://xxx:xxx@cluster0.xxx.mongodb.net/heritage_db
2. Maven 下载依赖（在 heritage-java-backend 目录执行：mvn clean install）
3. 运行 HeritageApplication.java
服务地址 http://127.0.0.1:8080
```

首次启动会自动初始化数据（集合为空时）：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | 管理员（可增删改） |
| user | 123456 | 普通用户（仅查询浏览） |

同时写入 7 条非遗项目演示数据（锅庄舞、藏戏、唐卡、藏医药、碉楼营造技艺、赛马会、藏历民俗节庆）及关联的传承人、民俗活动、数据来源。

## 二、启动前端

```
cd heritage-frontend
npm install
npm run dev
```

浏览器访问 `http://127.0.0.1:5173`，开发环境已配置 `/api` 代理到 `http://127.0.0.1:8080`。

## 三、接口清单

| 模块 | 方法 | 路径 | 权限 |
| --- | --- | --- | --- |
| 用户 | POST | /api/user/register | 公开 |
| 用户 | POST | /api/user/login | 公开 |
| 用户 | GET | /api/user/list | 仅 admin |
| 非遗项目 | GET | /api/project | 登录可读 |
| 非遗项目 | GET | /api/project/{id} | 登录可读 |
| 非遗项目 | GET | /api/project/stat | 登录可读（Aggregation 聚合） |
| 非遗项目 | POST / PUT / DELETE | /api/project[/{id}] | 仅 admin |
| 传承人 | GET | /api/inheritor | 登录可读 |
| 传承人 | POST / PUT / DELETE | /api/inheritor[/{id}] | 仅 admin |
| 民俗活动 | GET | /api/activity | 登录可读 |
| 民俗活动 | POST / PUT / DELETE | /api/activity[/{id}] | 仅 admin |
| 数据来源 | GET | /api/source | 登录可读 |
| 数据来源 | POST / PUT / DELETE | /api/source[/{id}] | 仅 admin |

统一返回格式：

```json
{ "code": 200, "msg": "提示信息", "data": {} }
```

普通用户调用写接口，后端 Security 直接返回 `403`。

## 四、MongoDB 集合

| 集合名 | 说明 |
| --- | --- |
| project | 非遗项目（含内嵌 `media_list` 数组，`inheritor_ref_list` / `activity_ref_list` 存 ID 数组，`source_ref` 存单个 ID） |
| inheritor | 传承人（独立文档） |
| folk_activity | 民俗活动（独立文档） |
| data_source | 数据来源（独立文档） |
| sys_user | 系统用户（密码 BCrypt 加密） |

> 多对多关系全部使用 ObjectId 字符串数组实现，未创建任何中间集合/中间表。