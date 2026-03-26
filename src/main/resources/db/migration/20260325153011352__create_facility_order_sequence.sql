-- Table to track order sequence per facility
CREATE TABLE IF NOT EXISTS fulfillment.facility_order_sequence (
    supplyingfacilityid UUID PRIMARY KEY,
    lastsequencevalue INTEGER NOT NULL DEFAULT 0
);
