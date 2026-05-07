package com.auction.server.service;

import com.auction.common.dto.auction.AuctionSummaryDto;
import com.auction.common.dto.auction.CreateAuctionRequest;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.model.Auction;
import com.auction.common.model.Item;
import com.auction.server.dao.AuctionDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.factory.ItemFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing auction lifecycle and creation.
 */
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(
        AuctionService.class
    );

    private final AuctionDao auctionDao;
    private final ItemDao itemDao;
    private final ItemFactory itemFactory;

    public AuctionService(AuctionDao auctionDao, ItemDao itemDao) {
        this.auctionDao = auctionDao;
        this.itemDao = itemDao;
        this.itemFactory = new ItemFactory();
    }

    /**
     * Creates a new item and its associated auction.
     *
     * @param sellerId The ID of the user creating the auction.
     * @param request  The request containing item and auction details.
     * @return A summary DTO of the newly created auction.
     */
    public AuctionSummaryDto createAuction(
        long sellerId,
        CreateAuctionRequest request
    ) {
        logger.info(
            "Creating new auction for seller ID: {}, Item: {}",
            sellerId,
            request.itemName()
        );

        // 1. Create and persist Item
        ItemFactory.CreateItemData itemData = new ItemFactory.CreateItemData(
            0L,
            sellerId,
            request.itemType(),
            request.itemName(),
            request.description(),
            request.condition(),
            request.startingPrice(),
            request.imagePath(),
            null, // brand
            null, // model
            null, // artist
            null, // material
            null, // manufacturer
            0,    // year
            LocalDateTime.now()
        );

        Item item = itemFactory.create(itemData);

        long itemId = itemDao.create(item);
        logger.debug("Item created with ID: {}", itemId);

        // 2. Create and persist Auction
        Auction auction = new Auction(
            0L, // ID will be generated
            itemId,
            sellerId,
            request.startingPrice(),
            null, // No highest bidder yet
            request.startTime(),
            request.endTime(),
            AuctionStatus.OPEN,
            0L, // Initial version
            LocalDateTime.now()
        );

        long auctionId = auctionDao.create(auction);
        logger.info(
            "Auction created with ID: {} for Item ID: {}",
            auctionId,
            itemId
        );

        return new AuctionSummaryDto(
            auctionId,
            request.itemName(),
            request.itemType(),
            request.startingPrice(),
            request.startingPrice(),
            request.startTime(),
            request.endTime(),
            AuctionStatus.OPEN,
            request.imagePath()
        );
    }
}
