-- Seed data for AuctionPro System
-- Password for all users is 'password123' (BCrypt hashed)

-- 1. Insert Users
INSERT INTO users (username, password_hash, full_name, role, balance, active, created_at) VALUES
('admin', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'System Administrator', 'ADMIN', '0.00', 1, STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
('seller01', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'John Seller', 'SELLER', '15000.00', 1, STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
('seller02', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Sarah Art Dealer', 'SELLER', '25000.00', 1, STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
('bidder01', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Alice Bidder', 'BIDDER', '50000.00', 1, STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
('bidder02', '$2a$10$KrHGliNRYe888NJ7oVdnh.OkWKcPtkl4Vpmpr4x.L4Oxhsx12/XCm', 'Bob Collector', 'BIDDER', '75000.00', 1, STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- 2. Insert Items
INSERT INTO items (seller_id, item_type, name, description, condition_text, starting_price, created_at) VALUES
(2, 'ELECTRONICS', 'Vintage Camera X100', 'A classic rangefinder camera in excellent condition.', 'Used - Excellent', '12000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
(3, 'ART', 'Modern Abstract Painting', 'Original oil painting on canvas by local artist.', 'Brand New', '8000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
(2, 'VEHICLE', 'Classic Scooter 1985', 'Restored vintage scooter, runs perfectly.', 'Used - Good', '20000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
(3, 'ART', 'Renaissance Sculpture Replica', 'High quality marble dust replica.', 'Brand New', '5000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours')),
(2, 'ELECTRONICS', 'Quick Test Phone', 'Used for rapid testing of countdown and extension.', 'Used - Fair', '300.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- 3. Insert Auctions
-- Auction 1: Vintage Camera - Reserve $13,000 (Current $12,500) - Ends in 5 mins
INSERT INTO auctions (item_id, seller_id, current_price, highest_max_bid, reserve_price, start_time, end_time, status, created_at) VALUES
(1, 2, '12500.00', '12500.00', '13000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '-1 hour'), STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '+5 minutes'), 'RUNNING', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- Auction 2: Abstract Painting - Reserve $9,000 (Current $8,200) - Ends in 3 mins
INSERT INTO auctions (item_id, seller_id, current_price, highest_max_bid, reserve_price, start_time, end_time, status, created_at) VALUES
(2, 3, '8200.00', '8200.00', '9000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '-2 hours'), STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '+3 minutes'), 'RUNNING', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- Auction 3: Classic Scooter - Starts in 10 seconds, Ends in 10 mins
INSERT INTO auctions (item_id, seller_id, current_price, highest_max_bid, reserve_price, start_time, end_time, status, created_at) VALUES
(3, 2, '20000.00', '20000.00', '25000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '+10 seconds'), STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '+10 minutes'), 'OPEN', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- Auction 4: Sculpture Replica - Finished
INSERT INTO auctions (item_id, seller_id, current_price, highest_max_bid, reserve_price, start_time, end_time, status, created_at) VALUES
(4, 3, '5500.00', '5500.00', '5500.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '-5 hours'), STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '-1 hour'), 'FINISHED', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));

-- Auction 5: Quick Test Phone - Reserve $1000 (Current $350) - Ends in 2 mins
INSERT INTO auctions (item_id, seller_id, current_price, highest_max_bid, reserve_price, start_time, end_time, status, created_at) VALUES
(5, 2, '350.00', '350.00', '1000.00', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '-10 minutes'), STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours', '+2 minutes'), 'RUNNING', STRFTIME('%Y-%m-%dT%H:%M:%f', 'now', '+7 hours'));
