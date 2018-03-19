ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN lotId uuid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN vvmStatus character varying(255);
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN useVvm boolean NOT NULL DEFAULT FALSE;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN rejectionReasonId uuid;

CREATE TABLE proof_of_delivery_line_items_new AS 
SELECT podli.id
  , podli.proofofdeliveryid
  , podli.notes
  , podli.quantityreceived::int AS quantityaccepted
  , podli.quantityreturned::int AS quantityrejected
  , oli.orderableid AS orderableid
  , podli.lotid
  , podli.vvmstatus
  , podli.usevvm
  , podli.rejectionreasonid
FROM proof_of_delivery_line_items podli
  JOIN fulfillment.order_line_items oli ON oli.id = podli.orderlineitemid
;

ALTER TABLE proof_of_delivery_line_items_new
  ADD CONSTRAINT proof_of_delivery_line_items_new_pkey PRIMARY KEY (id)
  , ALTER COLUMN proofofdeliveryid SET NOT NULL
  , ADD CONSTRAINT proof_of_delivery_line_items_proofofdeliveryid_fk FOREIGN KEY (proofofdeliveryid) REFERENCES proof_of_deliveries(id)
  , ALTER COLUMN orderableid SET NOT NULL
  , ALTER COLUMN usevvm SET NOT NULL
  , ALTER COLUMN usevvm SET DEFAULT FALSE
;

CREATE INDEX ON fulfillment.proof_of_delivery_line_items_new (proofofdeliveryid);

DROP TABLE proof_of_delivery_line_items;
ALTER TABLE proof_of_delivery_line_items_new RENAME TO proof_of_delivery_line_items;

ALTER TABLE proof_of_delivery_line_items
  RENAME CONSTRAINT proof_of_delivery_line_items_new_pkey TO proof_of_delivery_line_items_pkey;
