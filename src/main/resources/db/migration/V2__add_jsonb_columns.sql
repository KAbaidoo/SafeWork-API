-- Convert JSON columns to JSONB for better performance and advanced querying
-- JSONB provides binary storage, indexing support, and advanced operators

-- Convert custom_attributes from JSON to JSONB in assets table
ALTER TABLE assets ALTER COLUMN custom_attributes TYPE JSONB USING custom_attributes::JSONB;

-- Convert report_data from JSON to JSONB in inspections table  
ALTER TABLE inspections ALTER COLUMN report_data TYPE JSONB USING report_data::JSONB;

-- Convert template_data from JSON to JSONB in checklists table
ALTER TABLE checklists ALTER COLUMN template_data TYPE JSONB USING template_data::JSONB;

-- Add comments to document JSONB usage
COMMENT ON COLUMN assets.custom_attributes IS 'JSONB column for flexible asset attributes and metadata';
COMMENT ON COLUMN inspections.report_data IS 'JSONB column for structured inspection reports and findings';
COMMENT ON COLUMN checklists.template_data IS 'JSONB column for dynamic checklist templates and configurations';