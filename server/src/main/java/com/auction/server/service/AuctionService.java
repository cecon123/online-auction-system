package com.auction.server.service;

import com.auction.common.dto.auction.AuctionDetailDto;
import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.server.factory.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing auction lifecycle and data retrieval.
 */
public class AuctionService {
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionDao auctionDao;
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final ItemFactory itemFactory;

    public AuctionService(AuctionDao auctionDao, ItemDao itemDao, UserDao userDao) {
        this.auctionDao = auctionDao;
        this.itemDao = itemDao;
        this.userDao = userDao;
        this.itemFactory = new ItemFactory();
    }

    /**
     * Gets a summary list of all auctions.
     */
    public List<AuctionSummaryDto> getAllAuctions() {
        List<Auction> auctions = auctionDao.findAll();
        List<AuctionSummaryDto> summaries = new ArrayList<>();

        for (Auction auction : auctions) {
            Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
            if (itemOpt.isPresent()) {
                Item item = itemOpt.get();
                summaries.add(new AuctionSummaryDto(
                    auction.getId(),
                    item.getName(),
                    item.getItemType(),
                    item.getStartingPrice(),
                    auction.getCurrentPrice(),
                    auction.getStartTime(),
                    auction.getEndTime(),
                    auction.getStatus(),
                    item.getImagePath()
                ));
            }
        }
        return summaries;
    }

    /**
     * Gets detailed information for a specific auction.
     */
    public AuctionDetailDto getAuctionDetail(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("Auction not found."));

        Item item = itemDao.findById(auction.getItemId())
            .orElseThrow(() -> new IllegalStateException("Item not found for auction."));

        String sellerName = userDao.findById(auction.getSellerId())
            .map(UserDao.UserRecord::username)
            .orElse("Unknown");

        String highestBidderName = null;
        if (auction.getHighestBidderId() > 0) {
            highestBidderName = userDao.findById(auction.getHighestBidderId())
                .map(UserDao.UserRecord::username)
                .orElse("Unknown");
        }

        return new AuctionDetailDto(
            auction.getId(),
            item.getId(),
            auction.getSellerId(),
            sellerName,
            item.getName(),
            item.getItemType(),
            item.getCondition(),
            item.getDescription(),
            item.getStartingPrice(),
            auction.getCurrentPrice(),
            highestBidderName,
            auction.getStartTime(),
            auction.getEndTime(),
            auction.getStatus(),
            item.getImagePath()
        );
    }

    /**
     * Creates a new item and auction.
     */
    public AuctionSummaryDto createAuction(long sellerId, CreateAuctionRequest request) {
        // 1. Create the domain Item via Factory
        ItemFactory.CreateItemData itemData = new ItemFactory.CreateItemData(
            0L,
            sellerId,
            request.itemType(),
            request.itemName(),
            request.description(),
            request.condition(),
            request.startingPrice(),
            request.imagePath(),
            null, null, null, null, null, 0, // Mock specific fields for now
            LocalDateTime.now()
        );
        Item item = itemFactory.create(itemData);

        // 2. Persist Item
        long itemId = itemDao.create(item);
        item.setId(itemId);

        // 3. Create domain Auction
        Auction auction = new Auction(
            0L,
            itemId,
            sellerId,
            request.startingPrice(),
            0L,
            request.startTime(),
            request.endTime(),
            AuctionStatus.OPEN,
            0,
            LocalDateTime.now()
        );

        // 4. Persist Auction
        long auctionId = auctionDao.create(auction);
        auction.setId(auctionId);

        logger.info("New auction created: {} by Seller ID: {}", auction.getId(), sellerId);

        return new AuctionSummaryDto(
            auction.getId(),
            item.getName(),
            item.getItemType(),
            item.getStartingPrice(),
            auction.getCurrentPrice(),
            auction.getStartTime(),
            auction.getEndTime(),
            auction.getStatus(),
            item.getImagePath()
        );
    }
}
