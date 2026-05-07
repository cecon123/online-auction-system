-- Seed data for AuctionPro System
-- Password for all users is 'password123' (BCrypt hashed)

-- 1. Insert Users
-- Roles: 'BIDDER', 'SELLER', 'ADMIN'
INSERT INTO users (username, password_hash, full_name, role, balance, active, created_at) VALUES
('admin', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'System Administrator', 'ADMIN', '0.00', 1, REPLACE(DATETIME('now'), ' ', 'T')),
('seller01', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'John Seller', 'SELLER', '15000.00', 1, REPLACE(DATETIME('now'), ' ', 'T')),
('seller02', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Sarah Art Dealer', 'SELLER', '25000.00', 1, REPLACE(DATETIME('now'), ' ', 'T')),
('bidder01', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Alice Bidder', 'BIDDER', '50000.00', 1, REPLACE(DATETIME('now'), ' ', 'T')),
('bidder02', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Bob Collector', 'BIDDER', '75000.00', 1, REPLACE(DATETIME('now'), ' ', 'T'));
-- 2. Insert Items
-- Item Types: 'ELECTRONICS', 'ART', 'VEHICLE'
INSERT INTO items (seller_id, item_type, name, description, condition_text, starting_price, created_at) VALUES
(2, 'ELECTRONICS', 'Vintage Camera X100', 'A classic rangefinder camera in excellent condition.', 'Used - Excellent', '12000.00', REPLACE(DATETIME('now'), ' ', 'T')),
(3, 'ART', 'Modern Abstract Painting', 'Original oil painting on canvas by local artist.', 'Brand New', '8000.00', REPLACE(DATETIME('now'), ' ', 'T')),
(2, 'VEHICLE', 'Classic Scooter 1985', 'Restored vintage scooter, runs perfectly.', 'Used - Good', '20000.00', REPLACE(DATETIME('now'), ' ', 'T')),
(3, 'ART', 'Renaissance Sculpture Replica', 'High quality marble dust replica.', 'Brand New', '5000.00', REPLACE(DATETIME('now'), ' ', 'T'));

-- 3. Insert Auctions
-- Status: 'OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED'
-- Note: 'RUNNING' status means the auction is active and can receive bids.

-- Auction 1: Vintage Camera (Running)
INSERT INTO auctions (item_id, seller_id, current_price, start_time, end_time, status, created_at) VALUES
(1, 2, '12000.00', REPLACE(DATETIME('now', '-1 hour'), ' ', 'T'), REPLACE(DATETIME('now', '+2 hours'), ' ', 'T'), 'RUNNING', REPLACE(DATETIME('now'), ' ', 'T'));

-- Auction 2: Abstract Painting (Running)
INSERT INTO auctions (item_id, seller_id, current_price, start_time, end_time, status, created_at) VALUES
(2, 3, '8000.00', REPLACE(DATETIME('now', '-30 minutes'), ' ', 'T'), REPLACE(DATETIME('now', '+5 hours'), ' ', 'T'), 'RUNNING', REPLACE(DATETIME('now'), ' ', 'T'));

-- Auction 3: Classic Scooter (Upcoming - OPEN)
INSERT INTO auctions (item_id, seller_id, current_price, start_time, end_time, status, created_at) VALUES
(3, 2, '20000.00', REPLACE(DATETIME('now', '+1 day'), ' ', 'T'), REPLACE(DATETIME('now', '+2 days'), ' ', 'T'), 'OPEN', REPLACE(DATETIME('now'), ' ', 'T'));

-- Auction 4: Sculpture Replica (Running)
INSERT INTO auctions (item_id, seller_id, current_price, start_time, end_time, status, created_at) VALUES
(4, 3, '5000.00', REPLACE(DATETIME('now', '-2 hours'), ' ', 'T'), REPLACE(DATETIME('now', '+1 hour'), ' ', 'T'), 'RUNNING', REPLACE(DATETIME('now'), ' ', 'T'));
