PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA busy_timeout = 5000;

CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT NOT NULL UNIQUE,
                                     password_hash TEXT NOT NULL,
                                     full_name TEXT,
                                     role TEXT NOT NULL,
                                     active INTEGER NOT NULL DEFAULT 1,
                                     created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     seller_id INTEGER NOT NULL,
                                     type TEXT NOT NULL,
                                     name TEXT NOT NULL,
                                     description TEXT,
                                     starting_price REAL NOT NULL,
                                     image_path TEXT,
                                     created_at TEXT NOT NULL,
                                     FOREIGN KEY (seller_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS auctions (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        item_id INTEGER NOT NULL,
                                        seller_id INTEGER NOT NULL,
                                        current_price REAL NOT NULL,
                                        highest_bidder_id INTEGER,
                                        start_time TEXT NOT NULL,
                                        end_time TEXT NOT NULL,
                                        status TEXT NOT NULL,
                                        version INTEGER NOT NULL DEFAULT 0,
                                        FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS bids (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    auction_id INTEGER NOT NULL,
                                    bidder_id INTEGER NOT NULL,
                                    amount REAL NOT NULL,
                                    created_at TEXT NOT NULL,
                                    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS auto_bids (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         auction_id INTEGER NOT NULL,
                                         bidder_id INTEGER NOT NULL,
                                         max_bid REAL NOT NULL,
                                         increment REAL NOT NULL,
                                         active INTEGER NOT NULL DEFAULT 1,
                                         created_at TEXT NOT NULL,
                                         FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
    );

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_auctions_status ON auctions(status);
CREATE INDEX IF NOT EXISTS idx_auctions_end_time ON auctions(end_time);
CREATE INDEX IF NOT EXISTS idx_bids_auction_id ON bids(auction_id);
