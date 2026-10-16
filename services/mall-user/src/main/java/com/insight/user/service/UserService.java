package com.insight.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insight.common.exception.BusinessException;
import com.insight.common.exception.ErrorCode;
import com.insight.user.dto.RegisterRequest;
import com.insight.user.entity.User;
import com.insight.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /** BCrypt 编码器:线程安全,整个服务用一个实例即可 */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Long register(RegisterRequest req) {
        // 1. 查重:给用户友好提示(真正的最终防线是第 3 步的唯一索引)
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 2. 组装 + BCrypt 加密:明文密码只在这一行出现,之后库里只有哈希
        User user = new User();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname()
                : "用户" + req.getPhone().substring(7));
        user.setLevel(1);
        user.setStatus(1);

        // 3. 落库。并发下两个请求同时注册同一手机号,唯一索引会抛 DuplicateKeyException,
        //    这里转成友好业务异常(而不是"系统繁忙")
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        log.info("用户注册成功, userId={}", user.getId());
        return user.getId();
    }
}
