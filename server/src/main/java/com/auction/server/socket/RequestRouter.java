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
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.AuctionDao;
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
import com.auction.server.service.SessionManager;
import com.auction.server.util.JsonMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes socket requests to the correct controller/service.
 */
public class RequestRouter {

    private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);

    private final JsonMapper jsonMapper;
    private final AuthService authService;
    private final BidService bidService;
    private final AuctionService auctionService;
    private final SessionManager sessionManager;

    public RequestRouter() {
        this.jsonMapper = JsonMapper.getInstance();

        // Initialize real DAOs
        UserDao userDao = new SQLiteUserDao();
        AuctionDao auctionDao = new SQLiteAuctionDao();
        BidDao bidDao = new SQLiteBidDao();
        ItemDao itemDao = new SQLiteItemDao();

        // Initialize real services
        this.authService = new AuthService(userDao);
        this.bidService = new BidService(auctionDao, bidDao);
        this.auctionService = new AuctionService(auctionDao, itemDao, userDao);
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
                        requireData(request, LoginRequest.class, "Missing login data")
                    )
                );
                case REGISTER -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Registration successful",
                    authService.register(
                        requireData(request, RegisterRequest.class, "Missing registration data")
                    )
                );
                case LOGOUT -> {
                    sessionManager.invalidateSession(request.getToken());
                    logger.info("Logout successful for token: {}", request.getToken());
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
                        yield Response.fail(type, request.getRequestId(), "Unauthorized. Please login.");
                    }
                    PlaceBidRequest bidData = requireData(request, PlaceBidRequest.class, "Missing bid data");
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Bid accepted",
                        bidService.placeBid(userId, bidData)
                    );
                }
                case GET_AUCTIONS -> Response.ok(
                    type,
                    request.getRequestId(),
                    "Auctions loaded",
                    auctionService.getAllAuctions()
                );
                case GET_AUCTION_DETAIL -> {
                    Long auctionId = requireData(request, Long.class, "Missing auctionId");
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Auction detail loaded",
                        auctionService.getAuctionDetail(auctionId)
                    );
                }
                case CREATE_AUCTION -> {
                    Long userId = sessionManager.getUserId(request.getToken());
                    if (userId == null) {
                        yield Response.fail(type, request.getRequestId(), "Unauthorized. Please login.");
                    }
                    CreateAuctionRequest data = requireData(request, CreateAuctionRequest.class, "Missing auction data");
                    yield Response.ok(
                        type,
                        request.getRequestId(),
                        "Auction created successfully",
                        auctionService.createAuction(userId, data)
                    );
                }
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
                    "Unsupported message type: " + type
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
}
