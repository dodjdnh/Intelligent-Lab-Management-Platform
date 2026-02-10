package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {
    // 显式指定 ID 为数据库自增，防止 MP 尝试生成随机的长 ID 导致匹配失败
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String role;

    // === 新增字段 ===
    // 对应数据库里的 user_no 字段
    @TableField("user_no")
    private String userNo;
}