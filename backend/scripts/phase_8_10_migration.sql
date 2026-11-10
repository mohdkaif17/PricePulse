-- ============================================================================
-- PRICEPULSE PHASES 8, 9, 10 DATABASE UPGRADE
-- Implements listing status, verification_records, listing_reports, and audit_logs
-- ============================================================================

-- 1. Upgrade users with password_hash
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NULL;

-- Set default demo passwords (hashed for bcrypt 'password123')
-- $2b$12$e868N4vJ8v4XzK6mGjCGe.q1Z3R1X2Y4Z5W6V7U8T9S0R1Q2P3O4N is demo hash
UPDATE users SET password_hash = '$2b$12$EQo9lVbKkW6F7o6rE3Kx5O8w2yG0vK4hZ9X3tB8bX3T5h5e7yK0qK' WHERE password_hash IS NULL;

-- 2. Upgrade listings with status and rejection_reason
ALTER TABLE listings ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'active';
ALTER TABLE listings DROP CONSTRAINT IF EXISTS chk_listings_status;
ALTER TABLE listings ADD CONSTRAINT chk_listings_status CHECK (
    status IN ('active', 'pending', 'rejected', 'archived', 'suspended')
);

ALTER TABLE listings ADD COLUMN IF NOT EXISTS rejection_reason TEXT NULL;

-- Synchronize status from moderation_status if existing
UPDATE listings SET status = 'active' WHERE moderation_status = 'approved' AND is_active = TRUE;
UPDATE listings SET status = 'pending' WHERE moderation_status = 'pending';
UPDATE listings SET status = 'rejected' WHERE moderation_status = 'rejected';
UPDATE listings SET status = 'archived' WHERE is_active = FALSE;

CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);

-- 3. Verification Records Table
CREATE TABLE IF NOT EXISTS verification_records (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    verifier_user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    verification_type VARCHAR(50) NOT NULL DEFAULT 'FIELD_VISIT',
    status VARCHAR(20) NOT NULL DEFAULT 'verified' CHECK (status IN ('verified', 'rejected', 'inconclusive')),
    notes TEXT NULL,
    evidence_url VARCHAR(500) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_verification_records_listing ON verification_records(listing_id);

-- 4. Listing Reports Table (Phase 8.6)
CREATE TABLE IF NOT EXISTS listing_reports (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    reporter_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason_category VARCHAR(50) NOT NULL CHECK (
        reason_category IN ('incorrect_info', 'duplicate_listing', 'misleading_price', 'outdated_info', 'fake_provider', 'inappropriate_content', 'other')
    ),
    details TEXT NOT NULL,
    review_status VARCHAR(30) NOT NULL DEFAULT 'pending_review' CHECK (
        review_status IN ('pending_review', 'reviewed', 'action_taken', 'dismissed')
    ),
    admin_notes TEXT NULL,
    resolved_by BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    resolved_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_listing_reports_listing ON listing_reports(listing_id);
CREATE INDEX IF NOT EXISTS idx_listing_reports_status ON listing_reports(review_status);

-- 5. Immutable Audit Logs Table (Phase 8 & 9)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    old_values JSONB NULL,
    new_values JSONB NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Reset sequence IDs
SELECT setval('verification_records_id_seq', (SELECT COALESCE(MAX(id), 1) FROM verification_records));
SELECT setval('listing_reports_id_seq', (SELECT COALESCE(MAX(id), 1) FROM listing_reports));
SELECT setval('audit_logs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM audit_logs));

