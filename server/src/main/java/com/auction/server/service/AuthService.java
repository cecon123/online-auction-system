package com.auction.server.service;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.server.dao.UserDao;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for authentication-related operations.
 * Uses BCrypt for password security.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(
        AuthService.class
    );

    private final UserDao userDao;
    private final SessionManager sessionManager;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Registers a new user.
     * Hashes password using BCrypt before storing in DB.
     */
    public RegisterResponse register(RegisterRequest request) {
        // 1. Check if username exists
        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // 2. Hash password
        String salt = BCrypt.gensalt(12);
        String hashed = BCrypt.hashpw(request.password(), salt);

        // 3. Store in DB
        long id = userDao.create(
            request.username(),
            hashed,
            request.fullName(),
            request.role()
        );

        logger.info(
            "Successfully registered new user: {} with ID: {}",
            request.username(),
            id
        );

        return new RegisterResponse(id, request.username(), request.role());
    }

    /**
     * Authenticates a user.
     * Verifies password hash and issues a session token.
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Find user
        Optional<UserDao.UserRecord> userOpt = userDao.findByUsername(
            request.username()
        );

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        UserDao.UserRecord user = userOpt.get();

        // 2. Check active status
        if (!user.active()) {
            throw new IllegalStateException("Your account has been suspended.");
        }

        // 3. Verify password
        if (!BCrypt.checkpw(request.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // 4. Issue token
        String token = sessionManager.createSession(user.id());
        logger.info(
            "User {} logged in successfully. Issued token: {}",
            user.username(),
            token
        );

        return new LoginResponse(
            user.id(),
            user.username(),
            user.role(),
            token
        );
    }
}
