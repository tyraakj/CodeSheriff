-- ============================================================================
-- CodeSheriff Production Database Schema
-- Supabase Postgres with RLS
-- Run this in Supabase SQL Editor
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- TABLE: users
-- Extends Supabase Auth with application-specific profile data
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    plan_tier TEXT NOT NULL DEFAULT 'free' CHECK (plan_tier IN ('free', 'pro', 'enterprise')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_plan_tier ON users(plan_tier);
CREATE INDEX idx_users_created_at ON users(created_at);

COMMENT ON TABLE users IS 'Extended user profiles linked to Supabase Auth';
COMMENT ON COLUMN users.plan_tier IS 'Subscription tier: free (5 analyses/month), pro (unlimited), enterprise (audit trail export)';

-- ============================================================================
-- TABLE: analyses
-- One record per ZIP upload, tracks overall analysis lifecycle
-- ============================================================================
CREATE TABLE analyses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    zip_filename TEXT NOT NULL,
    upload_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'complete', 'failed', 'blocked')),
    total_classes_found INTEGER DEFAULT 0,
    total_methods_found INTEGER DEFAULT 0,
    processing_time_ms INTEGER,
    codebase_brief TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analyses_user_id ON analyses(user_id);
CREATE INDEX idx_analyses_status ON analyses(status);
CREATE INDEX idx_analyses_upload_timestamp ON analyses(upload_timestamp DESC);
CREATE INDEX idx_analyses_user_status ON analyses(user_id, status);

COMMENT ON TABLE analyses IS 'Top-level analysis record for each ZIP upload';
COMMENT ON COLUMN analyses.status IS 'pending: queued, processing: in progress, complete: success, failed: error, blocked: security violation';

-- ============================================================================
-- TABLE: classes
-- Every class extracted by JavaParser
-- ============================================================================
CREATE TABLE classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    class_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    annotation_list TEXT[],
    line_count INTEGER NOT NULL DEFAULT 0,
    dependency_list TEXT[],
    risk_level TEXT NOT NULL DEFAULT 'low' CHECK (risk_level IN ('low', 'medium', 'high', 'critical')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_classes_analysis_id ON classes(analysis_id);
CREATE INDEX idx_classes_risk_level ON classes(risk_level);
CREATE INDEX idx_classes_class_name ON classes(class_name);

COMMENT ON TABLE classes IS 'Parsed Java classes from uploaded codebase';
COMMENT ON COLUMN classes.risk_level IS 'Assigned by Layer 2 based on credential detection and suspicious patterns';

-- ============================================================================
-- TABLE: methods
-- Every method extracted by JavaParser
-- ============================================================================
CREATE TABLE methods (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    method_name TEXT NOT NULL,
    return_type TEXT NOT NULL,
    parameter_list TEXT NOT NULL,
    line_number INTEGER NOT NULL,
    line_count INTEGER NOT NULL DEFAULT 0,
    body_hash TEXT NOT NULL,
    has_javadoc BOOLEAN NOT NULL DEFAULT FALSE,
    has_tests BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_methods_class_id ON methods(class_id);
CREATE INDEX idx_methods_analysis_id ON methods(analysis_id);
CREATE INDEX idx_methods_method_name ON methods(method_name);
CREATE INDEX idx_methods_body_hash ON methods(body_hash);

COMMENT ON TABLE methods IS 'Parsed methods from Java classes';
COMMENT ON COLUMN methods.body_hash IS 'SHA-256 hash for detecting duplicate methods across codebase';

-- ============================================================================
-- TABLE: bob_outputs
-- Every IBM WatsonX (Bob) API response
-- ============================================================================
CREATE TABLE bob_outputs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    method_id UUID REFERENCES methods(id) ON DELETE SET NULL,
    output_type TEXT NOT NULL CHECK (output_type IN ('codebase_brief', 'method_explanation', 'javadoc', 'junit_test')),
    prompt_sent TEXT NOT NULL,
    response_received TEXT NOT NULL,
    tokens_used INTEGER NOT NULL,
    latency_ms INTEGER NOT NULL,
    hallucination_confidence_score NUMERIC(5,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bob_outputs_analysis_id ON bob_outputs(analysis_id);
CREATE INDEX idx_bob_outputs_method_id ON bob_outputs(method_id);
CREATE INDEX idx_bob_outputs_output_type ON bob_outputs(output_type);
CREATE INDEX idx_bob_outputs_created_at ON bob_outputs(created_at DESC);

COMMENT ON TABLE bob_outputs IS 'All IBM WatsonX API interactions with hallucination validation';
COMMENT ON COLUMN bob_outputs.hallucination_confidence_score IS 'Percentage of Bob-mentioned identifiers verified in AST baseline (Layer 3)';

-- ============================================================================
-- TABLE: security_scans
-- One record per analysis, tracks 4-layer security pipeline results
-- ============================================================================
CREATE TABLE security_scans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    analysis_id UUID NOT NULL UNIQUE REFERENCES analyses(id) ON DELETE CASCADE,
    scan_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    layer_1_passed BOOLEAN NOT NULL DEFAULT FALSE,
    layer_2_passed BOOLEAN NOT NULL DEFAULT FALSE,
    layer_3_passed BOOLEAN NOT NULL DEFAULT FALSE,
    layer_4_passed BOOLEAN NOT NULL DEFAULT FALSE,
    overall_status TEXT NOT NULL CHECK (overall_status IN ('clean', 'flagged', 'blocked')),
    ast_baseline JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_security_scans_analysis_id ON security_scans(analysis_id);
CREATE INDEX idx_security_scans_overall_status ON security_scans(overall_status);
CREATE INDEX idx_security_scans_scan_timestamp ON security_scans(scan_timestamp DESC);

COMMENT ON TABLE security_scans IS '4-layer security pipeline results per analysis';
COMMENT ON COLUMN security_scans.ast_baseline IS 'JSON snapshot of all class/method names and signatures for hallucination detection';

-- ============================================================================
-- TABLE: security_flags
-- Individual security violations detected by Layers 1-3
-- ============================================================================
CREATE TABLE security_flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scan_id UUID NOT NULL REFERENCES security_scans(id) ON DELETE CASCADE,
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    flag_type TEXT NOT NULL CHECK (flag_type IN ('asi01_injection', 'credential_leak', 'hallucination', 'audit')),
    file_name TEXT NOT NULL,
    line_number INTEGER,
    flagged_content TEXT NOT NULL,
    severity TEXT NOT NULL CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    remediation_suggestion TEXT,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMPTZ,
    resolved_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_security_flags_scan_id ON security_flags(scan_id);
CREATE INDEX idx_security_flags_analysis_id ON security_flags(analysis_id);
CREATE INDEX idx_security_flags_flag_type ON security_flags(flag_type);
CREATE INDEX idx_security_flags_severity ON security_flags(severity);
CREATE INDEX idx_security_flags_resolved ON security_flags(resolved);

COMMENT ON TABLE security_flags IS 'Individual security violations with remediation tracking';
COMMENT ON COLUMN security_flags.flagged_content IS 'NEVER contains actual credentials - only sanitized/redacted references';

-- ============================================================================
-- TABLE: audit_trail
-- Append-only enterprise audit log
-- ============================================================================
CREATE TABLE audit_trail (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    analysis_id UUID REFERENCES analyses(id) ON DELETE SET NULL,
    event_type TEXT NOT NULL CHECK (event_type IN (
        'upload', 'scan_layer1_start', 'scan_layer1_complete', 
        'scan_layer2_start', 'scan_layer2_complete',
        'scan_layer3_baseline', 'scan_layer3_validate',
        'bob_call', 'export', 'flag_raised', 'flag_resolved', 'analysis_blocked'
    )),
    event_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    event_detail JSONB NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_trail_user_id ON audit_trail(user_id);
CREATE INDEX idx_audit_trail_analysis_id ON audit_trail(analysis_id);
CREATE INDEX idx_audit_trail_event_type ON audit_trail(event_type);
CREATE INDEX idx_audit_trail_event_timestamp ON audit_trail(event_timestamp DESC);
CREATE INDEX idx_audit_trail_user_timestamp ON audit_trail(user_id, event_timestamp DESC);

COMMENT ON TABLE audit_trail IS 'Append-only audit log for compliance and forensics';
COMMENT ON COLUMN audit_trail.event_detail IS 'JSON structure varies by event_type, includes flag counts, token usage, etc.';

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE analyses ENABLE ROW LEVEL SECURITY;
ALTER TABLE classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE bob_outputs ENABLE ROW LEVEL SECURITY;
ALTER TABLE security_scans ENABLE ROW LEVEL SECURITY;
ALTER TABLE security_flags ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_trail ENABLE ROW LEVEL SECURITY;

-- USERS
CREATE POLICY users_select_own ON users FOR SELECT USING (auth.uid() = id);
CREATE POLICY users_update_own ON users FOR UPDATE USING (auth.uid() = id);

-- ANALYSES
CREATE POLICY analyses_select_own ON analyses FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY analyses_insert_own ON analyses FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY analyses_update_own ON analyses FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY analyses_delete_own ON analyses FOR DELETE USING (auth.uid() = user_id);

-- CLASSES
CREATE POLICY classes_select_own ON classes FOR SELECT USING (
    EXISTS (SELECT 1 FROM analyses WHERE analyses.id = classes.analysis_id AND analyses.user_id = auth.uid())
);

-- METHODS
CREATE POLICY methods_select_own ON methods FOR SELECT USING (
    EXISTS (SELECT 1 FROM analyses WHERE analyses.id = methods.analysis_id AND analyses.user_id = auth.uid())
);

-- BOB_OUTPUTS
CREATE POLICY bob_outputs_select_own ON bob_outputs FOR SELECT USING (
    EXISTS (SELECT 1 FROM analyses WHERE analyses.id = bob_outputs.analysis_id AND analyses.user_id = auth.uid())
);

-- SECURITY_SCANS
CREATE POLICY security_scans_select_own ON security_scans FOR SELECT USING (
    EXISTS (SELECT 1 FROM analyses WHERE analyses.id = security_scans.analysis_id AND analyses.user_id = auth.uid())
);

-- SECURITY_FLAGS
CREATE POLICY security_flags_select_own ON security_flags FOR SELECT USING (
    EXISTS (SELECT 1 FROM analyses WHERE analyses.id = security_flags.analysis_id AND analyses.user_id = auth.uid())
);

-- AUDIT_TRAIL
CREATE POLICY audit_trail_select_own ON audit_trail FOR SELECT USING (auth.uid() = user_id);

-- ============================================================================
-- FUNCTIONS AND TRIGGERS
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_analyses_updated_at BEFORE UPDATE ON analyses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- AUTO-CREATE USER PROFILE
-- ============================================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, display_name, plan_tier)
    VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'display_name', 'User'), 'free');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- GRANTS
-- ============================================================================

GRANT ALL ON ALL TABLES IN SCHEMA public TO service_role;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO service_role;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO service_role;

-- Made with Bob
