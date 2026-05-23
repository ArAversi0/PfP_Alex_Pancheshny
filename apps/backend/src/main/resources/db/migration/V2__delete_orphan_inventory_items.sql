CREATE FUNCTION delete_inventory_slot_item() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.item_id IS NOT NULL THEN
        DELETE FROM items WHERE id = OLD.item_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_inventory_slot_item
AFTER DELETE ON inventory_slots
FOR EACH ROW
EXECUTE FUNCTION delete_inventory_slot_item();

