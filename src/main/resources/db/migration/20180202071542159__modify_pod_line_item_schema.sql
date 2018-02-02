ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityreceived;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityreturned;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN quantityshipped;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN replacedproductcode;
ALTER TABLE fulfillment.proof_of_delivery_line_items DROP COLUMN orderlineitemid;

ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN orderableId uuid NOT NULL;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN lotId uuid NOT NULL;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN quantityAccepted integer;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN vvmStatus character varying(255);
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN useVvm boolean NOT NULL;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN quantityRejected integer;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN rejectionReasonId uuid;

