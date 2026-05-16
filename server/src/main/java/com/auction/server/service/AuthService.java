package com.auction.server.service;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.enums.Role;
import com.auction.server.dao.UserDao;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.exception.ValidationException;
import java.math.BigDecimal;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Service for authentication-related operations. Uses BCrypt for password security. */
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserDao userDao;
  private final SessionManager sessionManager;

  public AuthService(UserDao userDao) {
    this.userDao = userDao;
    this.sessionManager = SessionManager.getInstance();
  }

  /**
   * Registers a new user. Hashes password using BCrypt before storing in DB.
   *
   * @param request The registration request details.
   * @return A response containing the newly created user's info.
   * @throws ValidationException if the username already exists or role is invalid.
   */
  public RegisterResponse register(RegisterRequest request) {
    if (request.role() == Role.ADMIN) {
      throw new ValidationException("Admin accounts cannot be created from public register.");
    }

    // 1. Check if username exists
    if (userDao.findByUsername(request.username()).isPresent()) {
      throw new ValidationException("Username already exists.");
    }

    // 2. Hash password
    String salt = BCrypt.gensalt(12);
    String hashed = BCrypt.hashpw(request.password(), salt);

    // 3. Store in DB
    BigDecimal initialBalance = BigDecimal.ZERO;
    BigDecimal initialLocked = BigDecimal.ZERO;
    long id =
        userDao.create(
            request.username(),
            hashed,
            request.fullName(),
            request.role(),
            initialBalance,
            initialLocked);

    logger.info("Successfully registered new user: {} with ID: {}", request.username(), id);

    return new RegisterResponse(
        id, request.username(), request.role(), initialBalance, initialLocked);
  }

  /**
   * Authenticates a user. Verifies password hash and issues a session token.
   *
   * @param request The login credentials.
   * @return A response containing user details and a session token.
   * @throws AuthenticationException if credentials are invalid or account is suspended.
   */
  public LoginResponse login(LoginRequest request) {
    // 1. Find user
    Optional<UserDao.UserRecord> userOpt = userDao.findByUsername(request.username());

    if (userOpt.isEmpty()) {
      throw new AuthenticationException("Invalid username or password.");
    }

    UserDao.UserRecord user = userOpt.get();

    // 2. Check active status
    if (!user.active()) {
      throw new AuthenticationException("Your account has been suspended.");
    }

    // 3. Verify password
    if (!BCrypt.checkpw(request.password(), user.passwordHash())) {
      throw new AuthenticationException("Invalid username or password.");
    }

    // 4. Issue token
    String token = sessionManager.createSession(user.id());
    logger.info("User {} logged in successfully.", user.username());

    return new LoginResponse(
        user.id(),
        user.username(),
        user.fullName(),
        user.role(),
        user.balance(),
        user.lockedBalance(),
        token);
  }
}
