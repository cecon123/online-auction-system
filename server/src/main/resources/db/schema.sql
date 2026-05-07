PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('BIDDER', 'SELLER', 'ADMIN')),
    balance TEXT NOT NULL DEFAULT '0.00',
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    seller_id INTEGER NOT NULL,
    item_type TEXT NOT NULL CHECK (item_type IN ('ELECTRONICS', 'ART', 'VEHICLE')),
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    condition_text TEXT NOT NULL DEFAULT 'Brand New',
    starting_price TEXT NOT NULL,
    image_path TEXT,

    -- Optional subtype fields.
    brand TEXT,
    model TEXT,
    artist TEXT,
    material TEXT,
    manufacturer TEXT,
    vehicle_year INTEGER,

    created_at TEXT NOT NULL,

    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auctions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL UNIQUE,
    seller_id INTEGER NOT NULL,
    current_price TEXT NOT NULL,
    highest_bidder_id INTEGER,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED')),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,

    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    amount TEXT NOT NULL,
    created_at TEXT NOT NULL,

    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auto_bids (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    max_bid TEXT NOT NULL,
    increment TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL,

    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (auction_id, bidder_id)
);

CREATE INDEX IF NOT EXISTS idx_items_seller_id
ON items(seller_id);

CREATE INDEX IF NOT EXISTS idx_auctions_status
ON auctions(status);

CREATE INDEX IF NOT EXISTS idx_auctions_time
ON auctions(start_time, end_time);

CREATE INDEX IF NOT EXISTS idx_bids_auction_id
ON bids(auction_id);

CREATE INDEX IF NOT EXISTS idx_bids_bidder_id
ON bids(bidder_id);

CREATE INDEX IF NOT EXISTS idx_auto_bids_auction_id
ON auto_bids(auction_id);
