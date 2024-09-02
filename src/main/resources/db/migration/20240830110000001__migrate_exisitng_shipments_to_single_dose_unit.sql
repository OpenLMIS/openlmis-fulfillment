UPDATE fulfillment.shipment_line_items
SET unitoforderableid = 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2'
WHERE unitoforderableid is null;

UPDATE fulfillment.shipment_draft_line_items
SET unitoforderableid = 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2'
WHERE unitoforderableid is null;

UPDATE fulfillment.proof_of_delivery_line_items
SET unitoforderableid = 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2'
WHERE unitoforderableid is null;
