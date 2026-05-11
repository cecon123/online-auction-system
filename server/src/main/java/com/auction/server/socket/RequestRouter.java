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
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.dto.bid.PlaceBidResponse;
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
import com.auction.server.concurrency.IdempotencyManager;
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
    private final IdempotencyManager idempotencyManager;
    private final AuctionDao auctionDao;
    private final UserDao userDao;
    private final ItemDao itemDao;
    private final PrintWriter clientWriter;

    public RequestRouter(PrintWriter clientWriter) {
        this.jsonMapper = JsonMapper.getInstance();
        this.clientWriter = clientWriter;
        this.idempotencyManager = IdempotencyManager.getInstance();

        // Initialize real DAOs
        this.userDao = new SQLiteUserDao();
        this.auctionDao = new SQLiteAuctionDao();
        this.itemDao = new SQLiteItemDao();
        BidDao bidDao = new SQLiteBidDao();
        AutoBidDao autoBidDao = new com.auction.server.dao.sqlite.SQLiteAutoBidDao();

        // Initialize real services
        this.authService = new AuthService(userDao);
        this.walletService = new WalletService(userDao);
        this.bidService = new BidService(auctionDao, bidDao, userDao, autoBidDao, walletService);
        this.auctionService = new AuctionService(auctionDao, itemDao, bidDao, walletService);
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

        // 1. Idempotency Check (Anti-Replay)
        if (!idempotencyManager.isNewRequest(request.getRequestId())) {
            logger.warn("Replay attack detected or duplicate request: {}", request.getRequestId());
            return Response.fail(
                request.getType(),
                request.getRequestId(),
                "Duplicate request detected (Replay protection)"
            );
        }

        MessageType type = request.getType();
        logger.info("[Request] Type: {}, ID: {}", type, request.getRequestId());

        try {
            return switch (type) {
                case LOGIN -> {
                    com.auction.common.dto.auth.LoginResponse authRes = authService.login(
                        requireData(request, LoginRequest.class, "Missing login data")
                    );
                    NotificationService.getInstance().registerUserConnection(authRes.userId(), clientWriter);
                    yield Response.ok(type, request.getRequestId(), "Login successful", authRes);
                }
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
                    NotificationService.getInstance().unregisterUserConnection(clientWriter);
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
                    Long userId = requireActiveUser(request);
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
                case GET_SELLER_AUCTIONS -> handleGetSellerAuctions(request);
                case GET_SELLER_STATS -> handleGetSellerStats(request);
                case GET_AUCTION_DETAIL -> handleGetAuctionDetail(request);
                case CREATE_AUCTION -> handleCreateAuction(request);
                case UPDATE_AUCTION -> handleUpdateAuction(request);
                case CANCEL_AUCTION -> handleCancelAuction(request);
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
                case GET_USER_BID_HISTORY -> handleGetUserBidHistory(request);
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
                case ADMIN_CANCEL_AUCTION -> {
                    requireAdmin(request);
                    yield handleAdminCancelAuction(request);
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
        Long userId = requireActiveUser(request);

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
            List<Long> joinedAuctionIds = bidService.getMyBids(userId);
            List<Auction> joinedAuctions = joinedAuctionIds.stream()
                .map(auctionDao::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

            participatingAuctionsCount = (int) joinedAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING || a.getStatus() == AuctionStatus.OPEN)
                .count();
            
            winningAuctionsCount = (int) joinedAuctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.RUNNING && userId.equals(a.getHighestBidderId()))
                .count();
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
            user.lockedBalance(),
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
        Long userId = requireActiveUser(request);

        com.auction.common.dto.bid.SetAutoBidRequest data = requireData(
            request,
            com.auction.common.dto.bid.SetAutoBidRequest.class,
            "SET_AUTO_BID requires payload"
        );

        bidService.setAutoBid(userId, data);

        return Response.ok(
            MessageType.SET_AUTO_BID,
            request.getRequestId(),
            "Auto-bid limit saved",
            null
        );
    }

    private Response<com.auction.common.dto.bid.AutoBidDto> handleGetAutoBid(Request<?> request) {
        Long userId = requireActiveUser(request);

        Long auctionId = jsonMapper.convertData(request.getData(), Long.class);
        if (auctionId == null) {
            throw new IllegalArgumentException("Missing auction ID");
        }

        return Response.ok(
            MessageType.GET_AUTO_BID,
            request.getRequestId(),
            "Auto-bid limit loaded",
            bidService.getAutoBid(userId, auctionId).orElse(null)
        );
    }

    private Response<List<AuctionSummaryDto>> handleGetAuctions(
        Request<?> request
    ) {
        List<Auction> auctionList = auctionDao.findAll();
        return mapToAuctionSummaries(auctionList, request.getRequestId());
    }

    private Response<List<AuctionSummaryDto>> handleGetSellerAuctions(
        Request<?> request
    ) {
        Long userId = requireActiveUser(request);

        List<Auction> auctionList = auctionDao.findBySellerId(userId);
        return mapToAuctionSummaries(auctionList, MessageType.GET_SELLER_AUCTIONS, "Seller auctions loaded", request.getRequestId());
    }

    private Response<com.auction.common.dto.dashboard.SellerStatsDto> handleGetSellerStats(
        Request<?> request
    ) {
        Long userId = requireActiveUser(request);

        List<Auction> sellerAuctions = auctionDao.findBySellerId(userId);
        
        java.math.BigDecimal expectedRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        int activeAuctionsCount = 0;
        int finishedAuctionsCount = 0;
        int successfulAuctionsCount = 0;
        int totalAuctionsCount = sellerAuctions.size();
        
        // Let's iterate and calculate
        for (Auction a : sellerAuctions) {
            if (a.getStatus() == AuctionStatus.RUNNING || a.getStatus() == AuctionStatus.OPEN) {
                activeAuctionsCount++;
                if (a.getCurrentPrice() != null) {
                    expectedRevenue = expectedRevenue.add(a.getCurrentPrice());
                }
            } else if (a.getStatus() == AuctionStatus.FINISHED) {
                finishedAuctionsCount++;
                // If there's a highest bidder and price >= reserve price (handled elsewhere usually, but let's assume if it has a highest bidder it's successful for now, or check reserve price)
                if (a.getHighestBidderId() != null && (a.getReservePrice() == null || a.getCurrentPrice().compareTo(a.getReservePrice()) >= 0)) {
                    successfulAuctionsCount++;
                    if (a.getCurrentPrice() != null) {
                        totalRevenue = totalRevenue.add(a.getCurrentPrice());
                    }
                }
            }
        }
        
        int successRate = 0;
        if (finishedAuctionsCount > 0) {
            successRate = (int) Math.round((double) successfulAuctionsCount / finishedAuctionsCount * 100);
        }

        int totalBidsReceived = 0;
        for (Auction a : sellerAuctions) {
            List<?> history = bidService.getBidHistory(a.getId());
            if (history != null) {
                totalBidsReceived += history.size();
            }
        }

        com.auction.common.dto.dashboard.SellerStatsDto stats = new com.auction.common.dto.dashboard.SellerStatsDto(
            expectedRevenue,
            totalRevenue,
            totalBidsReceived,
            successRate,
            activeAuctionsCount,
            totalAuctionsCount
        );

        return Response.ok(
            MessageType.GET_SELLER_STATS,
            request.getRequestId(),
            "Seller stats loaded",
            stats
        );
    }

    private Response<List<AuctionSummaryDto>> mapToAuctionSummaries(List<Auction> auctionList, String requestId) {
        return mapToAuctionSummaries(auctionList, MessageType.GET_AUCTIONS, "Auctions loaded", requestId);
    }

    private Response<List<AuctionSummaryDto>> mapToAuctionSummaries(List<Auction> auctionList, MessageType type, String message, String requestId) {
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
                ItemType itemType = (item != null)
                    ? item.getItemType()
                    : ItemType.ELECTRONICS;
                String image = (item != null) ? item.getImagePath() : null;

                BigDecimal startingPrice = (item != null) ? item.getStartingPrice() : a.getCurrentPrice();

                return new AuctionSummaryDto(
                    a.getId(),
                    title,
                    itemType,
                    startingPrice,
                    a.getCurrentPrice(),
                    a.getHighestBidderId(),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus(),
                    image
                );
            })
            .toList();

        return Response.ok(
            type,
            requestId,
            message,
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
            auction.getCurrentPrice(), // startingPrice (should ideally be from auction too if tracked)
            auction.getCurrentPrice(),
            auction.getReservePrice(),
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
        Long userId = requireActiveUser(request);

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
        Long userId = requireActiveUser(request);

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
        Long userId = requireActiveUser(request);

        CreateAuctionRequest data = requireData(
            request,
            CreateAuctionRequest.class,
            "CREATE_AUCTION requires auction payload"
        );

        validateCreateAuctionRequest(data);

        // Process image upload
        String savedFileName = com.auction.server.util.ImageUtil.saveBase64Image(
            data.imageBase64(),
            com.auction.server.config.AppProperties.getInstance().getAssetDir()
        );

        CreateAuctionRequest finalRequest = data;
        if (savedFileName != null) {
            finalRequest = new CreateAuctionRequest(
                data.itemName(),
                data.itemType(),
                data.condition(),
                data.description(),
                data.startingPrice(),
                data.reservePrice(),
                data.startTime(),
                data.endTime(),
                savedFileName, // Store only the filename (uuid.png)
                null
            );
        }

        AuctionSummaryDto response = auctionService.createAuction(userId, finalRequest);

        NotificationService.getInstance().broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

        return Response.ok(
            MessageType.CREATE_AUCTION,
            request.getRequestId(),
            "Auction created successfully",
            response
        );
    }

    private Response<?> handleUpdateAuction(Request<?> request) {
        Long userId = requireActiveUser(request);

        com.auction.common.dto.auction.UpdateAuctionRequest data = requireData(
            request,
            com.auction.common.dto.auction.UpdateAuctionRequest.class,
            "UPDATE_AUCTION requires auction payload"
        );

        validateUpdateAuctionRequest(data);

        // Process image upload
        String savedFileName = com.auction.server.util.ImageUtil.saveBase64Image(
            data.imageBase64(),
            com.auction.server.config.AppProperties.getInstance().getAssetDir()
        );

        com.auction.common.dto.auction.UpdateAuctionRequest finalRequest = data;
        if (savedFileName != null) {
            finalRequest = new com.auction.common.dto.auction.UpdateAuctionRequest(
                data.auctionId(),
                data.itemName(),
                data.itemType(),
                data.condition(),
                data.description(),
                data.startingPrice(),
                data.reservePrice(),
                data.startTime(),
                data.endTime(),
                savedFileName,
                null
            );
        }

        auctionService.updateAuction(userId, finalRequest);

        NotificationService.getInstance().broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

        return Response.ok(
            MessageType.UPDATE_AUCTION,
            request.getRequestId(),
            "Auction updated successfully",
            null
        );
    }

    private Response<?> handleCancelAuction(Request<?> request) {
        Long userId = requireActiveUser(request);

        Long auctionId = jsonMapper.convertData(request.getData(), Long.class);
        if (auctionId == null) {
            throw new IllegalArgumentException("Missing auction ID");
        }

        auctionService.cancelAuction(userId, auctionId);
        
        NotificationService.getInstance().broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

        return Response.ok(
            MessageType.CANCEL_AUCTION,
            request.getRequestId(),
            "Auction canceled successfully",
            null
        );
    }

    private Response<?> handleAdminCancelAuction(Request<?> request) {
        Long adminId = sessionManager.getUserId(request.getToken());
        if (adminId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        Long auctionId = jsonMapper.convertData(request.getData(), Long.class);
        if (auctionId == null) {
            throw new IllegalArgumentException("Missing auction ID");
        }

        auctionService.adminCancelAuction(adminId, auctionId);
        
        NotificationService.getInstance().broadcastToAllUsers(MessageType.AUCTION_LIST_UPDATED, null);

        return Response.ok(
            MessageType.ADMIN_CANCEL_AUCTION,
            request.getRequestId(),
            "Auction canceled successfully by Admin",
            null
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

    private void validateUpdateAuctionRequest(com.auction.common.dto.auction.UpdateAuctionRequest data) {
        if (data.auctionId() <= 0) {
            throw new IllegalArgumentException("Invalid auction ID.");
        }
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
        Long userId = requireActiveUser(request);

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

                BigDecimal startingPrice = (item != null) ? item.getStartingPrice() : a.getCurrentPrice();

                return new AuctionSummaryDto(
                    a.getId(),
                    title,
                    type,
                    startingPrice,
                    a.getCurrentPrice(),
                    a.getHighestBidderId(),
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

    private Response<List<com.auction.common.dto.bid.BidHistoryDto>> handleGetUserBidHistory(Request<?> request) {
        Long userId = requireActiveUser(request);

        List<com.auction.common.dto.bid.BidHistoryDto> history = bidService.getUserBidHistory(userId);
        
        // Enrich with item names
        List<com.auction.common.dto.bid.BidHistoryDto> enrichedHistory = history.stream()
            .map(h -> {
                String title = auctionDao.findById(h.auctionId())
                    .flatMap(a -> itemDao.findById(a.getItemId()))
                    .map(Item::getName)
                    .orElse(h.auctionTitle());
                
                return new com.auction.common.dto.bid.BidHistoryDto(
                    h.bidId(),
                    h.auctionId(),
                    title,
                    h.amount(),
                    h.timestamp(),
                    h.result()
                );
            })
            .toList();

        return Response.ok(
            MessageType.GET_USER_BID_HISTORY,
            request.getRequestId(),
            "User bid history loaded",
            enrichedHistory
        );
    }

    private Long requireActiveUser(Request<?> request) {
        Long userId = sessionManager.getUserId(request.getToken());
        if (userId == null) {
            throw new IllegalStateException("Unauthorized. Please login.");
        }

        UserDao.UserRecord user = userDao.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found."));

        if (!user.active()) {
            throw new IllegalStateException("Your account has been suspended.");
        }

        return userId;
    }

    private void requireAdmin(Request<?> request) {
        Long userId = requireActiveUser(request);
        UserDao.UserRecord user = userDao.findById(userId).get();

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
                u.lockedBalance(),
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

        UserDao.UserRecord targetUser = userDao.findById(data.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + data.userId()));

        if (targetUser.role() == Role.ADMIN && !data.active()) {
            throw new IllegalStateException("Administrative accounts cannot be deactivated.");
        }

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
