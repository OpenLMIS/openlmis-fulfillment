ALTER TABLE transfer_properties
    ADD transferType VARCHAR(50);

UPDATE transfer_properties
    SET transferType = 'ORDER';

ALTER TABLE transfer_properties
  ALTER COLUMN transferType SET NOT NULL;

ALTER TABLE transfer_properties
  DROP CONSTRAINT IF EXISTS uk_sprkvmtubsjd58jc0afdycmiy;

CREATE UNIQUE INDEX
  uk_tp_by_facility_and_transfer_type
  ON transfer_properties (transferType, facilityId);

