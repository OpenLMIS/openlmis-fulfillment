ALTER TABLE fulfillment.proof_of_deliveries ADD COLUMN shipmentId uuid NOT NULL;
ALTER TABLE fulfillment.proof_of_deliveries ADD COLUMN status character varying(255) NOT NULL;

-- connect external orders with pod

ALTER TABLE fulfillment.proof_of_deliveries DROP COLUMN orderid;
