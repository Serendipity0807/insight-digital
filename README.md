# Insight Digital(慧眼数码)

> 大型电商学习项目:Java 微服务 + AI Agent

## 技术栈

- Spring Boot 3.3.x · Spring Cloud 2023.0.x · Spring Cloud Alibaba 2023.0.3.x · JDK 21
- MyBatis-Plus · MySQL 8 · Redis 7 · RocketMQ 5 · Elasticsearch 8 · Nacos 2
- Vue 3(前端)· Python + LangChain + LangGraph(Agent)

## 结构

- `services/` 11 个微服务(三波次建设)
- `common/` 通用模块
- `deploy/` 中间件部署
- `frontend/` Vue 前端

## Swagger UI 接口测试

使用方法、登录授权和交易测试顺序见 [Swagger 测试指南](docs/swagger-testing.md)。
给网关及需要测试的业务服务激活 `swagger` Spring profile 后，访问
<http://localhost:8080/swagger-ui.html>，全部可用服务的接口会按中文业务分类显示，无需切换服务。

