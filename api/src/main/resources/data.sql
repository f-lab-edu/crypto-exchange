INSERT INTO users(user_id, email)
VALUES (42, 'test@example.com');

INSERT INTO orders(order_id, order_status, user_id, quantity)
VALUES (1234, 'FILLED', 42, 100);
