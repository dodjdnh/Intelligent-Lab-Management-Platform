# Contributing to Intelligent Lab Management Platform

感谢你对 **智慧实验室管理系统 (SLMS)** 的关注与支持！无论是提交 Bug 报告、提出功能建议还是直接贡献代码，我们都非常欢迎。

---

## 🛠 提交流程

1. **Fork 本仓库** 到你自己的 GitHub 账号下。
2. **克隆代码到本地** 并基于 `main` 分支创建特性分支：
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **编写代码并进行本地测试**：
   - 后端：确保 Spring Boot 服务可正常启动并通过已有单元测试。
   - 前端：运行 `npm run dev` 确保 Vue 3 页面无语法错误与渲染异常。
4. **提交代码**：
   - 遵循规范的 Commit 格式，如 `feat: add ...`、`fix: resolve ...`、`docs: update ...`。
5. **推送分支并发起 Pull Request**：
   - 详细描述你的修改内容与关联的 Issue 编号。

---

## 规范与准则

- 保持代码整洁，遵循 Spring Boot 与 Vue 3 社区编码最佳实践。
- 敏感配置（如数据库密码、私钥）严禁直接提交至公共仓库。
- 欢迎在 Issues 和 Discussions 中交流讨论！
