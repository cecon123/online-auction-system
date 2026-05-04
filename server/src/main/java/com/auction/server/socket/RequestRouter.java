package com.auction.server.socket;

import com.auction.common.dto.auth.LoginResponse;
import com.auction.common.dto.bid.PlaceBidResponse;
import com.auction.common.enums.Role;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Routes socket requests to the correct controller/service.
 *
 * Current version is a mock router used to verify that the protocol works.
 * In the next backend step, these mock responses will be replaced by:
 *
 * LOGIN -> AuthController
 * REGISTER -> AuthController
 * GET_AUCTIONS -> AuctionController
 * PLACE_BID -> BidController
 */
public class RequestRouter {

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

        return switch (type) {
            case LOGIN -> handleLoginMock(request);
            case REGISTER -> Response.ok(
                type,
                request.getRequestId(),
                "Register route reached",
                null
            );
            case GET_AUCTIONS -> Response.ok(
                type,
                request.getRequestId(),
                "Get auctions route reached",
                null
            );
            case GET_AUCTION_DETAIL -> Response.ok(
                type,
                request.getRequestId(),
                "Get auction detail route reached",
                null
            );
            case PLACE_BID -> handlePlaceBidMock(request);
            case GET_BID_HISTORY -> Response.ok(
                type,
                request.getRequestId(),
                "Get bid history route reached",
                null
            );
            case SUBSCRIBE_AUCTION -> Response.ok(
                type,
                request.getRequestId(),
                "Subscribe auction route reached",
                null
            );
            case UNSUBSCRIBE_AUCTION -> Response.ok(
                type,
                request.getRequestId(),
                "Unsubscribe auction route reached",
                null
            );
            default -> Response.fail(
                type,
                request.getRequestId(),
                "Unsupported message type: " + type
            );
        };
    }

    private Response<LoginResponse> handleLoginMock(Request<?> request) {
        LoginResponse response = new LoginResponse(
            1L,
            "mock-user",
            Role.BIDDER,
            "mock-session-token"
        );

        return Response.ok(
            MessageType.LOGIN,
            request.getRequestId(),
            "Mock login successful",
            response
        );
    }

    private Response<PlaceBidResponse> handlePlaceBidMock(Request<?> request) {
        PlaceBidResponse response = new PlaceBidResponse(
            1L,
            new BigDecimal("15000"),
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
}
