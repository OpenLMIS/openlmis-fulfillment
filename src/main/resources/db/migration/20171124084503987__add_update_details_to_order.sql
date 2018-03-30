ALTER TABLE fulfillment.orders ADD COLUMN lastupdateddate timestamp with time zone;
ALTER TABLE fulfillment.orders ADD COLUMN lastupdaterid uuid;

UPDATE fulfillment.orders SET lastupdateddate = createddate;
UPDATE fulfillment.orders SET lastupdaterid = createdbyid;
