package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_consumable") // 对应数据库表名
public class Consumable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String specification;

    // 对应数据库中的 count 列，这里会根据传感器传来的重力值自动更新
    private Integer count;

    private String unit;

    /**
     * 新增字段：RFID
     * 对应数据库 sys_consumable 表中你新加的 rfid 列
     * 如果数据库列名叫 rfid (小写)，MyBatis-Plus 默认可以自动映射
     * 为了保险，建议加上 @TableField("rfid")
     */
    @TableField("rfid")
    private String rfid;
}