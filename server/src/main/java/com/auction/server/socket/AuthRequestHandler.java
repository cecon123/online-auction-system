package com.auction.server.socket;

import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AuthRequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(AuthRequestHandler.class);

  private final RouterContext context;

  AuthRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<LoginResponse> handleLogin(Request<?> request) {
    LoginResponse response =
        context.authService.login(
            context.requireData(request, LoginRequest.class, "Missing login data"));
    context.notificationService.registerUserConnection(response.userId(), context.clientWriter);
    return Response.ok(MessageType.LOGIN, request.getRequestId(), "Login successful", response);
  }

  Response<RegisterResponse> handleRegister(Request<?> request) {
    RegisterResponse response =
        context.authService.register(
            context.requireData(request, RegisterRequest.class, "Missing registration data"));
    context.notificationService.broadcastToAllUsers(MessageType.USER_LIST_UPDATED, null);
    return Response.ok(
        MessageType.REGISTER, request.getRequestId(), "Registration successful", response);
  }

  Response<Void> handleLogout(Request<?> request) {
    context.sessionManager.invalidateSession(request.getToken());
    context.notificationService.unregisterUserConnection(context.clientWriter);
    logger.info("Logout successful.");
    return Response.ok(MessageType.LOGOUT, request.getRequestId(), "Logged out successfully", null);
  }
}
