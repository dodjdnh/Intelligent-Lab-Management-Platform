# 数据库升级说明

如果你使用的是仓库内基础库结构，请先执行 `lab_db.sql`，再按顺序执行下面的增量脚本：

1. `lab_db_stage3.sql`
2. `lab_db_stage4.sql`
3. `lab_db_stage5.sql`
4. `lab_db_stage6.sql`

各阶段内容：

- `stage3`：告警事件表、设备注册表
- `stage4`：设备绑定表
- `stage5`：设备绑定换算规则字段
- `stage6`：库存变更日志表

推荐在 MySQL 8.0+ 环境执行，以确保 `IF NOT EXISTS` 的兼容性与字符集行为一致。
