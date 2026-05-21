package com.auction.server.socket;

import com.auction.common.dto.auth.UpdateUserStatusRequest;
import com.auction.common.dto.auth.UserDto;
import com.auction.common.enums.Role;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.UserDao;
import com.auction.server.exception.BusinessRuleException;
import com.auction.server.exception.ResourceNotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AdminRequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(AdminRequestHandler.class);

  private final RouterContext context;

  AdminRequestHandler(RouterContext context) {
    this.context = context;
  }

  Response<List<UserDto>> handleGetUsers(Request<?> request) {
    context.requireAdmin(request);
    List<UserDto> users =
        context.userDao.findAll().stream()
            .map(
                user ->
                    new UserDto(
                        user.id(),
                        user.username(),
                        user.fullName(),
                        user.role(),
                        user.balance(),
                        user.lockedBalance(),
                        user.active(),
                        user.createdAt()))
            .toList();

    return Response.ok(
        MessageType.ADMIN_GET_USERS, request.getRequestId(), "User list loaded", users);
  }

  Response<Void> handleUpdateUserStatus(Request<?> request) {
    context.requireAdmin(request);
    UpdateUserStatusRequest data =
        context.requireData(
            request,
            UpdateUserStatusRequest.class,
            "ADMIN_UPDATE_USER_STATUS requires user status payload");

    UserDao.UserRecord targetUser =
        context.userDao
            .findById(data.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + data.userId()));

    if (targetUser.role() == Role.ADMIN && !data.active()) {
      throw new BusinessRuleException("Administrative accounts cannot be deactivated.");
    }

    context.userDao.updateActiveStatus(data.userId(), data.active());
    logger.info("Admin updated user {} active status to {}", data.userId(), data.active());

    context.notificationService.broadcastToAllUsers(MessageType.USER_LIST_UPDATED, null);

    return Response.ok(
        MessageType.ADMIN_UPDATE_USER_STATUS,
        request.getRequestId(),
        "User status updated successfully",
        null);
  }
}
