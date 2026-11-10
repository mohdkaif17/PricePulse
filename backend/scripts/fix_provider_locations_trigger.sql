CREATE OR REPLACE FUNCTION trg_provider_locations_set_geom()
RETURNS trigger AS $$
BEGIN
    IF NEW.location IS NULL AND NEW.longitude IS NOT NULL AND NEW.latitude IS NOT NULL THEN
        NEW.location := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_provider_locations_geom ON provider_locations;
CREATE TRIGGER trg_set_provider_locations_geom
BEFORE INSERT OR UPDATE ON provider_locations
FOR EACH ROW
EXECUTE FUNCTION trg_provider_locations_set_geom();

