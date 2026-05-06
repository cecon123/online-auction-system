package com.auction.server.socket;

import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.enums.Role;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import com.auction.server.service.AuthService;
import com.auction.server.service.SessionManager;
import com.auction.server.util.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Routes socket requests to the correct controller/service.
 */
public class RequestRouter {

    private final JsonMapper jsonMapper;
    private final AuthService authService;
    private final SessionManager sessionManager;

    public RequestRouter() {
        this.jsonMapper = JsonMapper.getInstance();

        // Khởi tạo các Service thật
        UserDao userDao = new SQLiteUserDao();
        this.authService = new AuthService(userDao);
        this.sessionManager = SessionManager.getInstance();
    }

    public Response<?> route(Request<?> request) {
        if (request == null) {
            return Response.fail(null, null, "Request body is null");
        }

        if (request.getType() == null) {
            return Response.fail(
                null,
                request.getRequestId(),
                "Request type is required"
            );
        }

        MessageType type = request.getType();
        System.out.println(
            "[Request] Type: " + type + ", ID: " + request.getRequestId()
        );

        try {
            return switch (type) {
                case LOGIN -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Đăng nhập thành công",
                    authService.login(
                        requireData(
                            request,
                            LoginRequest.class,
                            "Dữ liệu đăng nhập thiếu"
                        )
                    )
                );
                case REGISTER -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Đăng ký thành công",
                    authService.register(
                        requireData(
                            request,
                            RegisterRequest.class,
                            "Dữ liệu đăng ký thiếu"
                        )
                    )
                );
                case LOGOUT -> {
                    sessionManager.invalidateSession(request.getToken());
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Đã đăng xuất",
                        null
                    );
                }
                case GET_AUCTIONS -> handleGetAuctionsMock(request);
                case GET_AUCTION_DETAIL -> handleGetAuctionDetailMock(request);
                case CREATE_AUCTION -> handleCreateAuctionMock(request);
                case PLACE_BID -> handlePlaceBidMock(request);
                case GET_BID_HISTORY -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Mock bid history route reached",
                    List.of()
                );
                case SUBSCRIBE_AUCTION -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Mock subscribe auction successful",
                    null
                );
                case UNSUBSCRIBE_AUCTION -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Mock unsubscribe auction successful",
                    null
                );
                default -> Response.fail(
                    type,
                    request.getRequestId(),
                    "Unsupported message type in mock router: " + type
                );
            };
        } catch (IllegalArgumentException e) {
            return Response.fail(type, request.getRequestId(), e.getMessage());
        } catch (RuntimeException e) {
            return Response.fail(
                type,
                request.getRequestId(),
                "Server mock router error: " + e.getMessage()
            );
        }
    }

    private Response<LoginResponse> handleLoginMock(Request<?> request) {
        LoginRequest data = requireData(
            request,
            LoginRequest.class,
            "LOGIN requires username and password"
        );

        if (isBlank(data.username()) || isBlank(data.password())) {
            throw new IllegalArgumentException(
                "Username and password are required."
            );
        }

        Role role = detectMockRole(data.username());

        LoginResponse response = new LoginResponse(
            1L,
            data.username().trim(),
            role,
            "mock-session-token-" + role.name().toLowerCase()
        );

        return Response.ok(
            MessageType.LOGIN,
            request.getRequestId(),
            "Mock login successful",
            response
        );
    }

    private Response<RegisterResponse> handleRegisterMock(Request<?> request) {
        RegisterRequest data = requireData(
            request,
            RegisterRequest.class,
            "REGISTER requires fullName, username, password and role"
        );

        if (
            isBlank(data.fullName()) ||
            isBlank(data.username()) ||
            isBlank(data.password()) ||
            data.role() == null
        ) {
            throw new IllegalArgumentException(
                "Full name, username, password and role are required."
            );
        }

        if (data.role() == Role.ADMIN) {
            throw new IllegalArgumentException(
                "Admin accounts cannot be registered from the public UI."
            );
        }

        RegisterResponse response = new RegisterResponse(
            100L,
            data.username().trim(),
            data.role()
        );

        return Response.ok(
            MessageType.REGISTER,
            request.getRequestId(),
            "Mock register successful",
            response
        );
    }

    private Response<List<AuctionSummaryDto>> handleGetAuctionsMock(
        Request<?> request
    ) {
        LocalDateTime now = LocalDateTime.now();

        List<AuctionSummaryDto> auctions = List.of(
            new AuctionSummaryDto(
                1L,
                "Vintage Camera X100",
                ItemType.ELECTRONICS,
                new BigDecimal("12000.00"),
                new BigDecimal("14500.00"),
                now.minusHours(1),
                now.plusHours(2),
                AuctionStatus.RUNNING,
                "/images/mock-camera.png"
            ),
            new AuctionSummaryDto(
                2L,
                "Modern Abstract Painting",
                ItemType.ART,
                new BigDecimal("8000.00"),
                new BigDecimal("9500.00"),
                now.minusMinutes(30),
                now.plusHours(4),
                AuctionStatus.RUNNING,
                "/images/mock-art.png"
            ),
            new AuctionSummaryDto(
                3L,
                "Classic Scooter 1985",
                ItemType.VEHICLE,
                new BigDecimal("20000.00"),
                new BigDecimal("22500.00"),
                now.plusHours(1),
                now.plusDays(1),
                AuctionStatus.OPEN,
                "/images/mock-vehicle.png"
            )
        );

        return Response.ok(
            MessageType.GET_AUCTIONS,
            request.getRequestId(),
            "Mock auctions loaded",
            auctions
        );
    }

    private Response<AuctionDetailDto> handleGetAuctionDetailMock(
        Request<?> request
    ) {
        LocalDateTime now = LocalDateTime.now();

        AuctionDetailDto detail = new AuctionDetailDto(
            1L,
            10L,
            2L,
            "seller-demo",
            "Vintage Camera X100",
            ItemType.ELECTRONICS,
            "Used - Excellent",
            "A collectible vintage camera prepared for live bidding demo.",
            new BigDecimal("12000.00"),
            new BigDecimal("14500.00"),
            "mock-user",
            now.minusHours(1),
            now.plusHours(2),
            AuctionStatus.RUNNING,
            "/images/mock-camera.png"
        );

        return Response.ok(
            MessageType.GET_AUCTION_DETAIL,
            request.getRequestId(),
            "Mock auction detail loaded",
            detail
        );
    }

    private Response<AuctionSummaryDto> handleCreateAuctionMock(
        Request<?> request
    ) {
        CreateAuctionRequest data = requireData(
            request,
            CreateAuctionRequest.class,
            "CREATE_AUCTION requires auction payload"
        );

        validateCreateAuctionRequest(data);

        AuctionSummaryDto response = new AuctionSummaryDto(
            999L,
            data.itemName().trim(),
            data.itemType(),
            data.startingPrice(),
            data.startingPrice(),
            data.startTime(),
            data.endTime(),
            AuctionStatus.OPEN,
            data.imagePath()
        );

        return Response.ok(
            MessageType.CREATE_AUCTION,
            request.getRequestId(),
            "Mock auction created successfully",
            response
        );
    }

    private Response<PlaceBidResponse> handlePlaceBidMock(Request<?> request) {
        PlaceBidRequest data = requireData(
            request,
            PlaceBidRequest.class,
            "PLACE_BID requires auctionId and amount"
        );

        if (data.auctionId() <= 0) {
            throw new IllegalArgumentException("auctionId must be positive.");
        }

        if (
            data.amount() == null ||
            data.amount().compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new IllegalArgumentException("Bid amount must be positive.");
        }

        PlaceBidResponse response = new PlaceBidResponse(
            data.auctionId(),
            data.amount(),
            "mock-user",
            LocalDateTime.now()
        );

        return Response.ok(
            MessageType.PLACE_BID,
            request.getRequestId(),
            "Mock bid accepted",
            response
        );
    }

    private void validateCreateAuctionRequest(CreateAuctionRequest data) {
        if (isBlank(data.itemName())) {
            throw new IllegalArgumentException("Item name is required.");
        }

        if (data.itemType() == null) {
            throw new IllegalArgumentException("Item type is required.");
        }

        if (isBlank(data.condition())) {
            throw new IllegalArgumentException("Condition is required.");
        }

        if (isBlank(data.description())) {
            throw new IllegalArgumentException("Description is required.");
        }

        if (
            data.startingPrice() == null ||
            data.startingPrice().compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new IllegalArgumentException(
                "Starting price must be positive."
            );
        }

        if (data.startTime() == null) {
            throw new IllegalArgumentException("Start time is required.");
        }

        if (data.endTime() == null) {
            throw new IllegalArgumentException("End time is required.");
        }

        if (!data.endTime().isAfter(data.startTime())) {
            throw new IllegalArgumentException(
                "End time must be after start time."
            );
        }
    }

    private Role detectMockRole(String username) {
        String normalized = username.toLowerCase();

        if (normalized.contains("admin")) {
            return Role.ADMIN;
        }

        if (normalized.contains("seller")) {
            return Role.SELLER;
        }

        return Role.BIDDER;
    }

    private <T> T requireData(
        Request<?> request,
        Class<T> clazz,
        String errorMessage
    ) {
        T data = jsonMapper.convertData(request.getData(), clazz);

        if (data == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return data;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
