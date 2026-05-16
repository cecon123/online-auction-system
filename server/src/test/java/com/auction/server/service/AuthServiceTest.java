package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.enums.Role;
import com.auction.server.dao.UserDao;
import com.auction.server.exception.AuthenticationException;
import com.auction.server.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserDao userDao;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userDao);
  }

  @Test
  void shouldRegisterSuccessfully() {
    // Arrange
    RegisterRequest request =
        new RegisterRequest("Test User", "testuser", "password123", Role.BIDDER);

    when(userDao.findByUsername("testuser")).thenReturn(Optional.empty());
    when(userDao.create(anyString(), anyString(), anyString(), any(Role.class), any(), any()))
        .thenReturn(1L);

    // Act
    var response = authService.register(request);

    // Assert
    assertNotNull(response);
    assertEquals("testuser", response.username());
    verify(userDao)
        .create(eq("testuser"), anyString(), eq("Test User"), eq(Role.BIDDER), any(), any());
  }

  @Test
  void shouldFailRegisterWhenUsernameExists() {
    // Arrange
    RegisterRequest request =
        new RegisterRequest("Test User", "testuser", "password123", Role.BIDDER);
    UserDao.UserRecord existingUser = createMockUser(1L, "testuser", BigDecimal.ZERO);

    when(userDao.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThrows(ValidationException.class, () -> authService.register(request));
  }

  @Test
  void shouldRejectPublicAdminRegistration() {
    RegisterRequest request =
        new RegisterRequest("Admin User", "admin_user", "password123", Role.ADMIN);

    ValidationException exception =
        assertThrows(ValidationException.class, () -> authService.register(request));

    assertTrue(exception.getMessage().contains("Admin accounts cannot be created"));
    verify(userDao, never()).create(anyString(), anyString(), anyString(), any(), any(), any());
  }

  @Test
  void shouldLoginSuccessfully() {
    // Arrange
    String password = "password123";
    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
    UserDao.UserRecord user = createMockUser(1L, "testuser", BigDecimal.ZERO);
    // Since we can't easily set the hashed password in mock without reflection if it's a record,
    // I'll update createMockUser to accept it.
    user =
        new UserDao.UserRecord(
            1L,
            "testuser",
            hashed,
            "Test User",
            Role.BIDDER,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            true,
            LocalDateTime.now());

    when(userDao.findByUsername("testuser")).thenReturn(Optional.of(user));

    LoginRequest request = new LoginRequest("testuser", password);

    // Act
    var response = authService.login(request);

    // Assert
    assertNotNull(response);
    assertEquals("testuser", response.username());
    assertNotNull(response.token());
  }

  @Test
  void shouldFailLoginWithWrongPassword() {
    // Arrange
    String hashed = BCrypt.hashpw("correct_password", BCrypt.gensalt());
    UserDao.UserRecord user =
        new UserDao.UserRecord(
            1L,
            "testuser",
            hashed,
            "Test User",
            Role.BIDDER,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            true,
            LocalDateTime.now());

    when(userDao.findByUsername("testuser")).thenReturn(Optional.of(user));

    LoginRequest request = new LoginRequest("testuser", "wrong_password");

    // Act & Assert
    assertThrows(AuthenticationException.class, () -> authService.login(request));
  }

  private UserDao.UserRecord createMockUser(long id, String username, BigDecimal balance) {
    return new UserDao.UserRecord(
        id,
        username,
        "hashed_password",
        "Full Name",
        Role.BIDDER,
        balance,
        BigDecimal.ZERO,
        true,
        LocalDateTime.now());
  }
}
