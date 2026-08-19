<div align="center">
  <h1>🧪 智慧实验室管理系统</h1>
  <p><strong>Smart Lab Management System</strong></p>
  <p>IoT 感知 · 库存同步 · 预约审批 · Vue 3 可视化</p>
  <p>
    <img src="https://img.shields.io/badge/Backend-Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/Frontend-Vue%203-42B883?style=flat-square&logo=vuedotjs&logoColor=white" alt="Vue 3" />
    <img src="https://img.shields.io/badge/Database-MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" />
    <img src="https://img.shields.io/badge/IoT-RFID%20%2B%20HX711-8B5CF6?style=flat-square" alt="IoT" />
    <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License" />
    <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square" alt="PRs Welcome" />
  </p>
</div>

> 面向高校实验室的数字化管理平台。系统把预约、审批、耗材库存和硬件上报串成一条可追溯的业务链，解决物资流转不透明和库存数据滞后的问题。

## 项目亮点

| 能力 | 说明 |
| --- | --- |
| IoT 库存同步 | RFID 识别耗材身份，HX711 采集重量，后端实时更新库存 |
| 业务闭环 | 学生申请 → 管理员审核 → 库存扣减 / 时段占用 |
| 权限控制 | Sa-Token 区分管理员与学生角色，并为硬件上报提供白名单 |
| 可视化工作台 | Vue 3 + Element Plus 展示预约、库存和设备数据 |
| 公网联调 | 可通过 Ngrok 把本地服务暴露给远程硬件设备 |

## 数据链路

~~~mermaid
flowchart LR
    H[RFID / HX711] -->|HTTP JSON| N[Ngrok 隧道]
    N --> A[Spring Boot API]
    A --> L[设备日志]
    A --> S[库存与业务数据]
    S --> V[Vue 3 管理界面]
~~~

硬件上报示例：

~~~json
{"gravity": 120.54, "RFID": "24fac1c7"}
~~~

后端根据 RFID 匹配耗材，并把原始数据记录到 device_data_logs，再更新 sys_consumable.count。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3、MyBatis-Plus、MySQL 8、Sa-Token |
| 前端 | Vue 3、Element Plus、Axios、ECharts 规划中 |
| 硬件 | Arduino / ESP32、HX711、RC522 |
| 通信 | HTTP POST、JSON、Ngrok |

## 快速开始

### 1. 初始化数据库

创建 lab_db 数据库，并执行：

~~~text
management/sql/lab_db.sql
~~~

### 2. 启动后端

1. 使用 IDEA 打开 management 目录。
2. 修改 management/src/main/resources/application.yml 中的数据库账号和密码。
3. 运行 ManagementApplication.java。
4. 如需联调硬件，可执行 ngrok http 8080。

### 3. 启动前端

~~~powershell
cd lab-system
npm install
npm run dev
~~~

## 演示账号

<details>
<summary>展开查看本地演示账号</summary>

| 角色 | 用户名 | 密码 | 主要权限 |
| --- | --- | --- | --- |
| 管理员 | Anno | 20100908 | 审批、人员管理、设备绑定、库存监控 |
| 学生 | Tomori | 20101122 | 实验室预约、耗材申领、查看进度 |

这些账号仅用于本地演示，生产环境必须替换并关闭默认凭据。
</details>

## 资料入口

- [功能介绍](./功能介绍.md)
- [后端工程](./management/)
- [前端工程](./lab-system/)

## 后续方向

- [x] RFID 与重力数据上报及库存联动
- [ ] 基于 ECharts 的实验室环境与库存大屏
- [ ] AI 自然语言查询助手
- [ ] 传感器异常告警
