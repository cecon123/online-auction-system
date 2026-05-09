package com.auction.server.socket;

import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.auth.LoginRequest;
import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.auth.RegisterRequest;
import com.auction.common.dto.auth.RegisterResponse;
import com.auction.common.dto.auth.UpdateUserStatusRequest;
import com.auction.common.dto.auth.UserDto;
import com.auction.common.dto.bid.AutoBidDto;
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.dto.bid.SetAutoBidRequest;
import com.auction.common.dto.dashboard.DashboardDto;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.enums.ItemType;
import com.auction.common.enums.Role;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.AutoBidDao;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.sqlite.SQLiteAuctionDao;
import com.auction.server.dao.sqlite.SQLiteBidDao;
import com.auction.server.dao.sqlite.SQLiteItemDao;
import com.auction.server.dao.sqlite.SQLiteUserDao;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuthService;
import com.auction.server.service.BidService;
import com.auction.server.service.NotificationService;
import com.auction.server.service.SessionManager;
import com.auction.server.service.WalletService;
import com.auction.server.util.JsonMapper;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes socket requests to the correct controller/service.
 */
public class RequestRouter {

    private static final Logger logger = LoggerFactory.getLogger(
        RequestRouter.class
    );

    private final JsonMapper jsonMapper;
    private final AuthService authService;
    private final BidService bidService;
    private final WalletService walletService;
    private final AuctionService auctionService;
    private final NotificationService notificationService;
    private final SessionManager sessionManager;
    private final AuctionDao auctionDao;
    private final UserDao userDao;
    private final ItemDao itemDao;
    private final PrintWriter clientWriter;

    public RequestRouter(PrintWriter clientWriter) {
        this.jsonMapper = JsonMapper.getInstance();
        this.clientWriter = clientWriter;

        // Initialize real DAOs
        this.userDao = new SQLiteUserDao();
        this.auctionDao = new SQLiteAuctionDao();
        this.itemDao = new SQLiteItemDao();
        BidDao bidDao = new SQLiteBidDao();
        AutoBidDao autoBidDao = new com.auction.server.dao.sqlite.SQLiteAutoBidDao();

        // Initialize real services
        this.authService = new AuthService(userDao);
        this.bidService = new BidService(auctionDao, bidDao, userDao, autoBidDao);
        this.walletService = new WalletService(userDao);
        this.auctionService = new AuctionService(auctionDao, itemDao);
        this.notificationService = NotificationService.getInstance();
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
        logger.info("[Request] Type: {}, ID: {}", type, request.getRequestId());

        try {
            return switch (type) {
                case LOGIN -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Login successful",
                    authService.login(
                        requireData(
                            request,
                            LoginRequest.class,
                            "Missing login data"
                        )
                    )
                );
                case REGISTER -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Registration successful",
                    authService.register(
                        requireData(
                            request,
                            RegisterRequest.class,
                            "Missing registration data"
                        )
                    )
                );
                case LOGOUT -> {
                    sessionManager.invalidateSession(request.getToken());
                    logger.info(
                        "Logout successful for token: {}",
                        request.getToken()
                    );
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Logged out successfully",
                        null
                    );
                }
                case PLACE_BID -> {
                    Long userId = sessionManager.getUserId(request.getToken());
                    if (userId == null) {
                        yield Response.fail(
                            type,
                            request.getRequestId(),
                            "Unauthorized. Please login."
                        );
                    }
                    PlaceBidRequest bidData = requireData(
                        request,
                        PlaceBidRequest.class,
                        "Missing bid data"
                    );
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Bid accepted",
                        bidService.placeBid(userId, bidData)
                    );
                }
                case GET_AUCTIONS -> handleGetAuctions(request);
                case GET_AUCTION_DETAIL -> handleGetAuctionDetail(request);
                case CREATE_AUCTION -> handleCreateAuction(request);
                case DEPOSIT -> handleDeposit(request);
                case WITHDRAW -> handleWithdraw(request);
                case GET_BID_HISTORY -> {
                    Long auctionId = jsonMapper.convertData(
                        request.getData(),
                        Long.class
                    );
                    if (auctionId == null) {
                        yield Response.fail(
                            type,
                            request.getRequestId(),
                            "Missing auction ID"
                        );
                    }
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Bid history loaded",
                        bidService.getBidHistory(auctionId)
                    );
                }
                case SUBSCRIBE_AUCTION -> {
                    Long auctionId = jsonMapper.convertData(
                        request.getData(),
                        Long.class
                    );
                    if (auctionId == null) {
                        yield Response.fail(
                            type,
                            request.getRequestId(),
                            "Missing auction ID"
                        );
                    }
                    notificationService.subscribe(auctionId, clientWriter);
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Subscribed to auction " + auctionId,
                        null
                    );
                }
                case UNSUBSCRIBE_AUCTION -> {
                    Long auctionId = jsonMapper.convertData(
                        request.getData(),
                        Long.class
                    );
                    if (auctionId == null) {
                        yield Response.fail(
                            type,
                            request.getRequestId(),
                            "Missing auction ID"
                        );
                    }
                    notificationService.unsubscribe(auctionId, clientWriter);
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Unsubscribed from auction " + auctionId,
                        null
                    );
                }
                case GET_MY_BIDS -> handleGetMyBids(request);
                case GET_DASHBOARD -> handleGetDashboard(request);
                case SET_AUTO_BID -> handleSetAutoBid(request);
                case GET_AUTO_BID -> handleGetAutoBid(request);
                case ADMIN_GET_USERS -> {
                    requireAdmin(request);
                    yield handleAdminGetUsers(request);
                }
                case ADMIN_UPDATE_USER_STATUS -> {
                    requireAdmin(request);
                    yield handleAdminUpdateUserStatus(request);
                }
                case ADMIN_GET_AUCTIONS -> {
                    requireAdmin(request);
                    yield handleGetAuctions(request); // Admin uses same summary for now
                }
                default -> Response.fail(
                    type,
                    request.getRequestId(),
                    "Unsupported message type in router: " + type
                );
            };
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.fail(type, request.getRequestId(), e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error processing request: {}", type, e);
            return Response.fail(
                type,
                request.getRequestId(),
                "Server internal error: " + e.getMessage()
            );
        }
    }

    private Response<DashboardDto> handleGetDashboard(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        UserDao.UserRecord user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

        BigDecimal balance = user.balance();
        int participatingAuctionsCount = 0;
        int winningAuctionsCount = 0;
        int activeAuctionsCount = 0;
        int totalAuctionsCount = 0;
        int totalUsersCount = 0;

        // Role-specific stats
        if (user.role() == Role.BIDDER) {
            participatingAuctionsCount = bidService.getMyBids(userId).size();
            winningAuctionsCount = auctionDao.findByBidderId(userId).size();
        } else if (user.role() == Role.SELLER) {
            List<Auction> sellerAuctions = auctionDao.findBySellerId(userId);
            totalAuctionsCount = sellerAuctions.size();
            activeAuctionsCount = (int) sellerAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                .count();
        } else if (user.role() == Role.ADMIN) {
            List<Auction> allAuctions = auctionDao.findAll();
            totalAuctionsCount = allAuctions.size();
            activeAuctionsCount = (int) allAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                .count();
            totalUsersCount = userDao.findAll().size();
        }

        DashboardDto dashboard = new DashboardDto(
            balance,
            participatingAuctionsCount,
            winningAuctionsCount,
            activeAuctionsCount,
            totalAuctionsCount,
            totalUsersCount
        );

        return Response.ok(
            MessageType.GET_DASHBOARD,
            request.getRequestId(),
            "Dashboard data loaded",
            dashboard
        );
    }

    private Response<Void> handleSetAutoBid(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        SetAutoBidRequest data = requireData(
            request,
            SetAutoBidRequest.class,
            "SET_AUTO_BID requires payload"
        );

        bidService.setAutoBid(userId, data);

        return Response.ok(
            MessageType.SET_AUTO_BID,
            request.getRequestId(),
            "Auto-bid rule saved",
            null
        );
    }

    private Response<AutoBidDto> handleGetAutoBid(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        Long auctionId = jsonMapper.convertData(request.getData(), Long.class);
        if (auctionId == null) {
            throw new IllegalArgumentException("Missing auction ID");
        }

        return Response.ok(
            MessageType.GET_AUTO_BID,
            request.getRequestId(),
            "Auto-bid rule loaded",
            bidService.getAutoBid(userId, auctionId).orElse(null)
        );
    }

    private Response<List<AuctionSummaryDto>> handleGetAuctions(
        Request<?> request
    ) {
        List<Auction> auctionList = auctionDao.findAll();
        List<Item> itemList = itemDao.findAll();

        java.util.Map<Long, Item> itemMap = itemList
            .stream()
            .collect(java.util.stream.Collectors.toMap(Item::getId, i -> i));

        List<AuctionSummaryDto> auctions = auctionList
            .stream()
            .map(a -> {
                Item item = itemMap.get(a.getItemId());
                String title = (item != null)
                    ? item.getName()
                    : "Unknown Item";
                ItemType type = (item != null)
                    ? item.getItemType()
                    : ItemType.ELECTRONICS;
                String image = (item != null) ? item.getImagePath() : null;

                return new AuctionSummaryDto(
                    a.getId(),
                    title,
                    type,
                    a.getCurrentPrice(),
                    a.getCurrentPrice(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus(),
                    image
                );
            })
            .toList();

        return Response.ok(
            MessageType.GET_AUCTIONS,
            request.getRequestId(),
            "Auctions loaded",
            auctions
        );
    }

    private Response<AuctionDetailDto> handleGetAuctionDetail(
        Request<?> request
    ) {
        Long auctionId = jsonMapper.convertData(request.getData(), Long.class);
        if (auctionId == null) {
            throw new IllegalArgumentException("Missing auction ID");
        }

        Auction auction = auctionDao
            .findById(auctionId)
            .orElseThrow(() ->
                new IllegalArgumentException("Auction not found: " + auctionId)
            );

        Item item = itemDao
            .findById(auction.getItemId())
            .orElseThrow(() ->
                new IllegalStateException(
                    "Item not found for auction: " + auction.getItemId()
                )
            );

        String sellerName = userDao
            .findById(auction.getSellerId())
            .map(UserDao.UserRecord::username)
            .orElse("Unknown Seller");

        String highestBidder = null;
        if (auction.getHighestBidderId() != null) {
            highestBidder = userDao
                .findById(auction.getHighestBidderId())
                .map(UserDao.UserRecord::username)
                .orElse("Unknown");
        }

        AuctionDetailDto detail = new AuctionDetailDto(
            auction.getId(),
            auction.getItemId(),
            auction.getSellerId(),
            sellerName,
            item.getName(),
            item.getItemType(),
            item.getCondition(),
            item.getDescription(),
            auction.getCurrentPrice(),
            auction.getCurrentPrice(),
            highestBidder,
            auction.getStartTime(),
            auction.getEndTime(),
            auction.getStatus(),
            item.getImagePath()
        );

        return Response.ok(
            MessageType.GET_AUCTION_DETAIL,
            request.getRequestId(),
            "Auction detail loaded",
            detail
        );
    }

    private Response<BigDecimal> handleDeposit(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        BigDecimal amount = jsonMapper.convertData(
            request.getData(),
            BigDecimal.class
        );
        if (amount == null) {
            throw new IllegalArgumentException("Missing deposit amount");
        }

        BigDecimal newBalance = walletService.deposit(userId, amount);

        return Response.ok(
            MessageType.DEPOSIT,
            request.getRequestId(),
            "Deposit successful",
            newBalance
        );
    }

    private Response<BigDecimal> handleWithdraw(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        BigDecimal amount = jsonMapper.convertData(
            request.getData(),
            BigDecimal.class
        );
        if (amount == null) {
            throw new IllegalArgumentException("Missing withdraw amount");
        }

        BigDecimal newBalance = walletService.withdraw(userId, amount);

        return Response.ok(
            MessageType.WITHDRAW,
            request.getRequestId(),
            "Withdraw successful",
            newBalance
        );
    }

    private Response<AuctionSummaryDto> handleCreateAuction(
        Request<?> request
    ) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        CreateAuctionRequest data = requireData(
            request,
            CreateAuctionRequest.class,
            "CREATE_AUCTION requires auction payload"
        );

        validateCreateAuctionRequest(data);

        AuctionSummaryDto response = auctionService.createAuction(userId, data);

        return Response.ok(
            MessageType.CREATE_AUCTION,
            request.getRequestId(),
            "Auction created successfully",
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

    private Response<List<AuctionSummaryDto>> handleGetMyBids(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        // Find all auctions where the user has placed at least one bid
        List<Long> auctionIds = bidService.getMyBids(userId);
        
        List<AuctionSummaryDto> auctions = auctionIds.stream()
            .map(auctionDao::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(a -> {
                Item item = itemDao.findById(a.getItemId()).orElse(null);
                String title = (item != null) ? item.getName() : "Unknown Item";
                ItemType type = (item != null) ? item.getItemType() : ItemType.ELECTRONICS;
                String image = (item != null) ? item.getImagePath() : null;

                return new AuctionSummaryDto(
                    a.getId(),
                    title,
                    type,
                    a.getCurrentPrice(),
                    a.getCurrentPrice(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus(),
                    image
                );
            })
            .toList();

        return Response.ok(
            MessageType.GET_MY_BIDS,
            request.getRequestId(),
            "My bids loaded",
            auctions
        );
    }

    private void requireAdmin(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        UserDao.UserRecord user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

        if (user.role() != Role.ADMIN) {
            throw new IllegalStateException("Access denied. Admin role required.");
        }
    }

    private Response<List<UserDto>> handleAdminGetUsers(Request<?> request) {
        List<UserDto> users = userDao.findAll().stream()
            .map(u -> new UserDto(
                u.id(),
                u.username(),
                u.fullName(),
                u.role(),
                u.balance(),
                u.active(),
                u.createdAt()
            ))
            .toList();

        return Response.ok(
            MessageType.ADMIN_GET_USERS,
            request.getRequestId(),
            "User list loaded",
            users
        );
    }

    private Response<Void> handleAdminUpdateUserStatus(Request<?> request) {
        UpdateUserStatusRequest data = requireData(
            request,
            UpdateUserStatusRequest.class,
            "ADMIN_UPDATE_USER_STATUS requires user status payload"
        );

        userDao.updateActiveStatus(data.userId(), data.active());
        logger.info("Admin updated user {} active status to {}", data.userId(), data.active());

        return Response.ok(
            MessageType.ADMIN_UPDATE_USER_STATUS,
            request.getRequestId(),
            "User status updated successfully",
            null
        );
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
