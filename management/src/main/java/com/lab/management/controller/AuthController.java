package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.User;
import com.lab.management.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper; // 注入刚才写的数据库操作类

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        // 1. 去数据库查询该用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // 2. 校验账号密码
        if (user != null && user.getPassword().equals(password)) {
            StpUtil.login(user.getId()); // Sa-Token 登录

            Map<String, Object> map = new HashMap<>();
            map.put("token", StpUtil.getTokenValue());
            map.put("role", user.getRole()); // 返回数据库里存的角色
            return Result.success(map);
        }

        return Result.error("用户名或密码错误");
    }
}