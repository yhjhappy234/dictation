package com.yhj.dictation.service;

import com.yhj.dictation.entity.User;
import com.yhj.dictation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 内置头像列表
    private static final String[] AVATARS = {
            "avatar1.png", "avatar2.png", "avatar3.png", "avatar4.png", "avatar5.png",
            "avatar6.png", "avatar7.png", "avatar8.png", "avatar9.png", "avatar10.png",
            "avatar11.png", "avatar12.png", "avatar13.png", "avatar14.png", "avatar15.png",
            "avatar16.png", "avatar17.png", "avatar18.png", "avatar19.png", "avatar20.png"
    };

    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回 null
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("用户不存在: {}", username);
            return null;
        }

        User user = userOpt.get();
        if (user.getStatus() == User.UserStatus.DISABLED) {
            log.warn("用户已禁用: {}", username);
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码错误: {}", username);
            return null;
        }

        log.info("用户登录成功: {}", username);
        return user;
    }

    /**
     * 创建用户（默认普通用户角色）
     * @param username 用户名
     * @param password 原始密码（会被加密）
     * @return 创建的用户
     */
    @Transactional
    public User createUser(String username, String password) {
        return createUser(username, password, User.UserRole.USER);
    }

    /**
     * 创建用户
     * @param username 用户名
     * @param password 原始密码（会被加密）
     * @param role 用户角色
     * @return 创建的用户
     */
    @Transactional
    public User createUser(String username, String password, User.UserRole role) {
        if (userRepository.existsByUsername(username)) {
            log.warn("用户名已存在: {}", username);
            throw new IllegalArgumentException("用户名已存在: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setAvatar(getRandomAvatar());
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("创建用户成功: {}, 角色: {}, 头像: {}", username, role, user.getAvatar());
        return user;
    }

    /**
     * 初始化默认用户（如果不存在）
     * @param username 用户名
     * @param password 原始密码
     */
    @Transactional
    public void initDefaultUser(String username, String password) {
        if (!userRepository.existsByUsername(username)) {
            createUser(username, password, User.UserRole.ADMIN);
            log.info("初始化默认管理员用户: {}", username);
        }
    }

    /**
     * 获取随机头像
     */
    public String getRandomAvatar() {
        Random random = new Random();
        return AVATARS[random.nextInt(AVATARS.length)];
    }

    /**
     * 获取所有内置头像列表
     */
    public List<String> getAllAvatars() {
        return List.of(AVATARS);
    }

    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 更新用户密码（需验证旧密码）
     */
    @Transactional
    public User updatePassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("更新用户密码: {}", user.getUsername());
        return user;
    }

    /**
     * 更新用户密码（需验证旧密码）
     */
    @Transactional
    public User updatePasswordWithVerify(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("更新用户密码: {}", user.getUsername());
        return user;
    }

    /**
     * 更新用户头像
     */
    @Transactional
    public User updateAvatar(Long userId, String avatar) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        // 验证头像是否有效
        if (!getAllAvatars().contains(avatar)) {
            throw new IllegalArgumentException("无效的头像: " + avatar);
        }

        User user = userOpt.get();
        user.setAvatar(avatar);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("更新用户头像: {} -> {}", user.getUsername(), avatar);
        return user;
    }

    /**
     * 更新用户角色
     */
    @Transactional
    public User updateRole(Long userId, User.UserRole role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("更新用户角色: {} -> {}", user.getUsername(), role);
        return user;
    }

    /**
     * 禁用用户
     */
    @Transactional
    public User disableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        user.setStatus(User.UserStatus.DISABLED);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("禁用用户: {}", user.getUsername());
        return user;
    }

    /**
     * 启用用户
     */
    @Transactional
    public User enableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        user.setStatus(User.UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("启用用户: {}", user.getUsername());
        return user;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        userRepository.deleteById(userId);
        log.info("删除用户: {}", user.getUsername());
    }

    /**
     * 检查用户是否是管理员
     */
    public boolean isAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN;
    }
}