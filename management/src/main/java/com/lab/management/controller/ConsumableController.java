package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;
import com.lab.management.entity.User;
import com.lab.management.mapper.ConsumableApplyMapper;
import com.lab.management.mapper.ConsumableMapper;
import com.lab.management.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consumable")
public class ConsumableController {

    @Autowired
    private ConsumableMapper consumableMapper;
    @Autowired
    private ConsumableApplyMapper applyMapper;
    @Autowired
    private UserMapper userMapper;

    // 1. 耗材库存列表 (所有人可见)
    @GetMapping("/list")
    public Result list() {
        return Result.success(consumableMapper.selectList(null));
    }

    // 2. 申请记录列表 (修改修复版)
    @GetMapping("/apply-list")
    public Result applyList() {
        LambdaQueryWrapper<ConsumableApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ConsumableApply::getId);

        // 1. 获取当前登录用户的 ID
        long currentUserId = StpUtil.getLoginIdAsLong();

        // 2. 查数据库，看看这个人到底是什么角色
        User currentUser = userMapper.selectById(currentUserId);

        // 3. 判断逻辑：如果不是 "admin"，就只能看自己的
        // 注意：这里直接对比数据库里的字符串，绝对不会错
        if (currentUser != null && !"admin".equals(currentUser.getRole())) {
            wrapper.eq(ConsumableApply::getUserId, currentUserId);
        }

        // 如果是 admin，上面的 if 不会执行，Wrapper 里没有限制，自然就查出所有人了

        return Result.success(applyMapper.selectList(wrapper));
    }

    // 3. 提交申请 (学生操作)
    @PostMapping("/apply")
    public Result apply(@RequestBody Map<String, Object> params) {
        Long consumableId = Long.valueOf(params.get("id").toString());
        Integer num = (Integer) params.get("num");

        // 校验库存是否足够 (虽然还没扣，但如果库存不够现在就提示)
        Consumable item = consumableMapper.selectById(consumableId);
        if (item == null) return Result.error("物品不存在");
        if (item.getCount() < num) return Result.error("当前库存不足，无法申请");

        // 获取当前用户信息
        User user = userMapper.selectById(StpUtil.getLoginIdAsLong());

        // 生成申请单
        ConsumableApply apply = new ConsumableApply();
        apply.setConsumableId(consumableId);
        apply.setConsumableName(item.getName());
        apply.setUserId(user.getId());
        apply.setUserName(user.getUsername());
        apply.setUserNo(user.getUserNo());
        apply.setNum(num);
        apply.setStatus("审核中");

        applyMapper.insert(apply);
        return Result.success("申请已提交，等待管理员审核");
    }

    // 4. 入库 (管理员操作 - 修改版：支持自动合并库存)
    @PostMapping("/add")
    public Result add(@RequestBody Consumable consumable) {
        // 1. 构造查询条件：检查 名称 + 规格 是否已经存在
        LambdaQueryWrapper<Consumable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consumable::getName, consumable.getName())
                .eq(Consumable::getSpecification, consumable.getSpecification());

        Consumable existingItem = consumableMapper.selectOne(wrapper);

        if (existingItem != null) {
            // === 情况 A: 已经存在 ===
            // 逻辑：取出旧库存，加上新入库的数量，然后更新回去
            int newCount = existingItem.getCount() + consumable.getCount();
            existingItem.setCount(newCount);

            // (可选) 如果你想更新单位，可以在这里 setUnit，通常不需要
            consumableMapper.updateById(existingItem);
            return Result.success("入库成功，库存已合并");
        } else {
            // === 情况 B: 不存在 ===
            // 逻辑：直接作为新记录插入
            consumableMapper.insert(consumable);
            return Result.success("新品入库成功");
        }
    }
}