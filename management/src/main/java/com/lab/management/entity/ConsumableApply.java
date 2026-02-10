package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 必须引入这个
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_consumable_apply")
public class ConsumableApply {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 重点修改：手动指定数据库里的下划线列名
    @TableField("consumable_id")
    private Long consumableId;

    @TableField("consumable_name")
    private String consumableName;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("user_no")
    private String userNo;

    private Integer num; // 这个单词也是数据库列名，不用加注解
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;
}