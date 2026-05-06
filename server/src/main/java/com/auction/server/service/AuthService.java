package com.auction.server.service;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.enums.Role;
import com.auction.server.dao.UserDao;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service xử lý các nghiệp vụ liên quan đến xác thực (Authentication).
 * Sử dụng BCrypt để bảo mật mật khẩu.
 */
public class AuthService {

    private final UserDao userDao;
    private final SessionManager sessionManager;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Đăng ký người dùng mới.
     * Băm mật khẩu bằng BCrypt trước khi lưu vào DB.
     */
    public RegisterResponse register(RegisterRequest request) {
        // 1. Kiểm tra username tồn tại
        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }

        // 2. Băm mật khẩu
        String salt = BCrypt.gensalt(12);
        String hashed = BCrypt.hashpw(request.password(), salt);

        // 3. Lưu vào DB
        long id = userDao.create(
            request.username(),
            hashed,
            request.fullName(),
            request.role()
        );

        System.out.println(
            "[AuthService] Đã tạo người dùng mới: " +
                request.username() +
                " với ID: " +
                id
        );

        return new RegisterResponse(id, request.username(), request.role());
    }

    /**
     * Đăng nhập người dùng.
     * Kiểm tra hash mật khẩu và cấp token.
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user
        Optional<UserDao.UserRecord> userOpt = userDao.findByUsername(
            request.username()
        );

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "Tên đăng nhập hoặc mật khẩu không chính xác."
            );
        }

        UserDao.UserRecord user = userOpt.get();

        // 2. Kiểm tra trạng thái active
        if (!user.active()) {
            throw new IllegalStateException("Tài khoản của bạn đã bị khóa.");
        }

        // 3. Kiểm tra mật khẩu
        if (!BCrypt.checkpw(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException(
                "Tên đăng nhập hoặc mật khẩu không chính xác."
            );
        }

        // 4. Cấp token
        String token = sessionManager.createSession(user.id());

        return new LoginResponse(
            user.id(),
            user.username(),
            user.role(),
            token
        );
    }
}
