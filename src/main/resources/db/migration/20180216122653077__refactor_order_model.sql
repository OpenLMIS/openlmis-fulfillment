-- Move amount of ordered packs to orderedquantity column.
-- Drop packstoship and filledquantity columns

UPDATE order_line_items SET orderedquantity = packstoship;
ALTER TABLE order_line_items DROP COLUMN packstoship;
ALTER TABLE order_line_items DROP COLUMN filledquantity;