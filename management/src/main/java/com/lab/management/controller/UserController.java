package com.lab.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.User;
import com.lab.management.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 1. 获取用户列表
    @GetMapping("/list")
    public Result list() {
        // 查询所有用户
        List<User> list = userMapper.selectList(null);
        return Result.success(list);
    }

    // 2. 新增或更新用户
    @PostMapping("/save")
    public Result save(@RequestBody User user) {
        if (user.getId() == null) {
            // 新增逻辑
            // 默认密码设置为 123456 (如果前端没填)
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456");
            }
            // 检查用户名是否重复
            User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (exist != null) {
                return Result.error("用户名已存在");
            }
            userMapper.insert(user);
        } else {
            // 更新逻辑 (只改角色、学号等，不改密码)
            userMapper.updateById(user);
        }
        return Result.success("保存成功");
    }

    // 3. 删除用户
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // 4. 重置密码 (管理员常用功能)
    @PostMapping("/reset-pwd")
    public Result resetPwd(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        User user = new User();
        user.setId(id);
        user.setPassword("123456"); // 强制重置为 123456
        userMapper.updateById(user);
        return Result.success("密码已重置为 123456");
    }
}