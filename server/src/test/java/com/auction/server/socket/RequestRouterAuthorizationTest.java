package com.auction.server.socket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.dto.bid.PlaceBidRequest;
import com.auction.common.enums.ItemType;
import com.auction.common.protocol.MessageType;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.server.dao.SchemaInitializer;
import com.auction.server.service.SessionManager;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestRouterAuthorizationTest {

  private static final String TEST_DB_FILE = "router_authorization_test.db";
  private static final String TEST_DB_URL = "jdbc:sqlite:" + TEST_DB_FILE;

  private RequestRouter router;

  @BeforeAll
  static void initDb() {
    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      dbFile.delete();
    }
    System.setProperty("auction.db.url", TEST_DB_URL);
    System.setProperty("auction.skip.seed", "false");
    SchemaInitializer.initialize();
  }

  @AfterAll
  static void cleanup() {
    System.clearProperty("auction.db.url");
    System.clearProperty("auction.skip.seed");
    File dbFile = new File(TEST_DB_FILE);
    if (dbFile.exists()) {
      dbFile.delete();
    }
  }

  @BeforeEach
  void setUp() {
    router = new RequestRouter(new PrintWriter(new StringWriter(), true));
  }

  @Test
  void sellerCannotPlaceBid() {
    String sellerToken = SessionManager.getInstance().createSession(2L);
    Request<PlaceBidRequest> request =
        new Request<>(
            MessageType.PLACE_BID,
            "seller-bid",
            sellerToken,
            new PlaceBidRequest(1L, new BigDecimal("13000.00")));

    Response<?> response = router.route(request);

    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("BIDDER role required"));
  }

  @Test
  void bidderCannotCreateAuction() {
    String bidderToken = SessionManager.getInstance().createSession(4L);
    Request<CreateAuctionRequest> request =
        new Request<>(
            MessageType.CREATE_AUCTION,
            "bidder-create",
            bidderToken,
            new CreateAuctionRequest(
                "Unauthorized lot",
                ItemType.ELECTRONICS,
                "New",
                "Should not be created",
                new BigDecimal("100.00"),
                null,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusMinutes(5),
                null,
                null));

    Response<?> response = router.route(request);

    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("SELLER role required"));
  }

  @Test
  void unsupportedItemMessageRemainsUnsupported() {
    Request<Void> request = new Request<>(MessageType.CREATE_ITEM, "legacy-item-create", null, null);

    Response<?> response = router.route(request);

    assertFalse(response.isSuccess());
    assertTrue(response.getMessage().contains("Unsupported message type in router: CREATE_ITEM"));
  }

  @Test
  void logoutInvalidatesTokenForFutureRequests() {
    String bidderToken = SessionManager.getInstance().createSession(4L);
    Request<Void> logoutRequest =
        new Request<>(MessageType.LOGOUT, "logout-invalidates-token", bidderToken, null);

    Response<?> logoutResponse = router.route(logoutRequest);
    Response<?> dashboardResponse =
        router.route(
            new Request<>(
                MessageType.GET_DASHBOARD, "after-logout-dashboard", bidderToken, null));

    assertTrue(logoutResponse.isSuccess());
    assertFalse(dashboardResponse.isSuccess());
    assertTrue(dashboardResponse.getMessage().contains("Unauthorized"));
  }
}
