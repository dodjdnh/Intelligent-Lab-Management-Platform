package com.lab.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.management.entity.Consumable;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsumableMapper extends BaseMapper<Consumable> {
    // MyBatis-Plus 已经帮你写好了所有增删改查，这里留空即可
}