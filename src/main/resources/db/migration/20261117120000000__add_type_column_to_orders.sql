ALTER TABLE fulfillment.orders ADD COLUMN type VARCHAR(255);

UPDATE fulfillment.orders
SET type = CASE WHEN externalid IS NULL THEN 'REQUISITION_LESS' ELSE 'REQUISITION' END;

ALTER TABLE fulfillment.orders ALTER COLUMN type SET NOT NULL;
