package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import bg.sofia.uni.fmi.mjt.dungeons.exception.InventoryException;

public interface Inventory<Item> {
    boolean addItem(Item item);

    Item getItem(int index) throws InventoryException;

    Item removeItem(int index) throws InventoryException;

    Item changeItem(int index, Item newItem) throws InventoryException;
}
