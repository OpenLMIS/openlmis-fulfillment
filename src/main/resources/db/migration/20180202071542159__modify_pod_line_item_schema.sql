-- add new columns
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN orderableId uuid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN lotId uuid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN quantityAccepted integer;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN vvmStatus character varying(255);
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN useVvm boolean NOT NULL DEFAULT FALSE;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN quantityRejected integer;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN rejectionReasonId uuid;

-- update existing rows for orderableid, then enforce NOT NULL
UPDATE fulfillment.proof_of_delivery_line_items podli
  SET orderableid = oli.orderableid
  FROM fulfillment.order_line_items oli
  WHERE oli.id = podli.orderlineitemid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ALTER COLUMN orderableId SET NOT NULL;

-- update existing rows
UPDATE fulfillment.proof_of_delivery_line_items SET quantityAccepted = quantityreceived;
UPDATE fulfillment.proof_of_delivery_line_items SET quantityRejected = quantityreturned;

-- remove unnecessary columns
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityreceived;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityreturned;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityshipped;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN replacedproductcode;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN orderlineitemid;
