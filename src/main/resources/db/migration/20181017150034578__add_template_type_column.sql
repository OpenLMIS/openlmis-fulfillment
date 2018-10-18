ALTER TABLE transfer_properties
    ADD transferType VARCHAR(50);

UPDATE transfer_properties
    SET transferType = 'ORDER';

ALTER TABLE transfer_properties
  ALTER COLUMN transferType SET NOT NULL;

