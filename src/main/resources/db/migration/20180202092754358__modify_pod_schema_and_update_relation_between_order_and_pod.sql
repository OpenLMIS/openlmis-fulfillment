-- add new columns
ALTER TABLE fulfillment.proof_of_deliveries ADD COLUMN shipmentId uuid;
ALTER TABLE fulfillment.proof_of_deliveries ADD COLUMN status character varying(255) DEFAULT 'INITIATED';

-- connect external orders with pod
INSERT INTO fulfillment.shipments (id, orderid, shippedbyid, shippeddate, notes, extradata)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), o.id, o.createdById, o.createdDate, NULL, '{"external": true}'::jsonb
FROM fulfillment.proof_of_deliveries AS p
INNER JOIN fulfillment.orders AS o ON o.id = p.orderId;

INSERT INTO fulfillment.shipment_line_items (id, orderableid, lotid, quantityshipped, shipmentid)
SELECT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), oli.orderableId, NULL, oli.orderedQuantity, s.id
FROM fulfillment.proof_of_deliveries AS p
INNER JOIN fulfillment.orders AS o ON o.id = p.orderId
INNER JOIN fulfillment.order_line_items AS oli ON o.id = oli.orderId
INNER JOIN fulfillment.shipments AS s ON o.id = s.orderId;

UPDATE fulfillment.proof_of_deliveries AS p
SET shipmentId = s.id
FROM fulfillment.orders AS o, fulfillment.shipments AS s
WHERE p.orderId = o.id AND o.id = s.orderId;

-- update default POD status
UPDATE fulfillment.proof_of_deliveries AS p
SET status = 'CONFIRMED'
FROM fulfillment.orders AS o
WHERE o.status = 'RECEIVED'
  AND p.orderid = o.id;

-- set not null flag for shipmentId column
ALTER TABLE fulfillment.proof_of_deliveries ALTER COLUMN shipmentId SET NOT NULL;

-- remove unnecessary columns
ALTER TABLE fulfillment.proof_of_deliveries DROP COLUMN orderid;
