INSERT INTO orders (customer_id, status, total_price)
VALUES
    ('cust_001', 'PENDING', 150.50);

INSERT INTO orders_items (order_id, product_id, quantity)
VALUES
    (1, 101, 2)