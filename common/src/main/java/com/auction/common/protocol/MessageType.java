package com.auction.common.protocol;

/**
 * Defines all message types exchanged between JavaFX clients and the socket server.
 *
 * <p>Request types are sent from client to server. Event types are sent from server to subscribed
 * clients.
 */
public enum MessageType {
  // Authentication
  REGISTER,
  LOGIN,
  LOGOUT,

  // Dashboard
  GET_DASHBOARD,

  // Auction browsing
  GET_AUCTIONS,
  GET_AUCTION_DETAIL,

  // Seller item and auction management
  CREATE_ITEM,
  UPDATE_ITEM,
  DELETE_ITEM,
  CREATE_AUCTION,
  GET_SELLER_AUCTIONS,
  GET_SELLER_STATS,
  SYSTEM_NOTIFICATION,
  UPDATE_AUCTION,
  CANCEL_AUCTION,

  // Bidding
  PLACE_BID,
  GET_BID_HISTORY,
  GET_USER_BID_HISTORY,
  GET_MY_BIDS,

  // Realtime subscription
  SUBSCRIBE_AUCTION,
  UNSUBSCRIBE_AUCTION,

  // Server pushed realtime events
  BID_UPDATE,
  AUCTION_CLOSED,
  TIME_EXTENDED,
  AUCTION_LIST_UPDATED,
  USER_LIST_UPDATED,

  // Wallet
  DEPOSIT,
  WITHDRAW,

  // Auto-bid
  SET_AUTO_BID,
  GET_AUTO_BID,

  // Admin
  ADMIN_GET_USERS,
  ADMIN_UPDATE_USER_STATUS,
  ADMIN_GET_AUCTIONS,
  ADMIN_CANCEL_AUCTION,
  AUCTION_CANCELED,
}
