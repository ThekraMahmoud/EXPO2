-- Back up the database first and verify the index name before running.
-- The old cart model had a unique (user_id, seller_id) constraint, which prevents
-- a user from creating a new cart for the same restaurant after checkout.
-- Run only if this index exists in your current database:
ALTER TABLE carts DROP INDEX uk_cart_user_seller;

-- Hibernate ddl-auto=update should add the nullable order_id column and unique
-- constraint for the new Invoices.order relation. Verify the generated schema.
