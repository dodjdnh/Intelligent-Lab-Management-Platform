package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.User;
import com.lab.management.exception.BusinessException;
import com.lab.management.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username") == null ? null : loginInfo.get("username").trim();
        String password = loginInfo.get("password") == null ? null : loginInfo.get("password").trim();
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("用户名和密码不能为空");
        }

        // 1. 去数据库查询该用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // 2. 校验账号密码
        if (user != null && passwordMatches(password, user.getPassword())) {
            StpUtil.login(user.getId()); // Sa-Token 登录

            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")
                    && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(password));
                userMapper.updateById(user);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("token", StpUtil.getTokenValue());
            map.put("role", user.getRole()); // 返回数据库里存的角色
            map.put("userName", user.getUsername());
            map.put("userNo", user.getUserNo());
            return Result.success(map);
        }

        throw new BusinessException("用户名或密码错误");
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
