ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN orderableId uuid;

-- update existing rows for orderableid, then enforce NOT NULL
UPDATE fulfillment.proof_of_delivery_line_items podli
  SET orderableid = oli.orderableid
  FROM fulfillment.order_line_items oli
  WHERE oli.id = podli.orderlineitemid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ALTER COLUMN orderableId SET NOT NULL;

ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN orderlineitemid;
