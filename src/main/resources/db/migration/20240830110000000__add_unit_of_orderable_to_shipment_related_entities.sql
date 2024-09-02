ALTER TABLE fulfillment.shipment_line_items ADD COLUMN unitoforderableid uuid;
ALTER TABLE fulfillment.shipment_draft_line_items ADD COLUMN unitoforderableid uuid;
ALTER TABLE fulfillment.proof_of_delivery_line_items ADD COLUMN unitoforderableid uuid;
