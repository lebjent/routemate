-- Kept separate from the already-applied V22 migration to preserve its Flyway checksum.
CREATE INDEX IX_PRODUCT_PUBLIC_APPROVAL
    ON TB_TRAVEL_PRODUCT (USE_YN, APPROVAL_STATUS, SORT_ORDER, CREATE_DT DESC);
