package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import java.util.ArrayList;
import java.util.List;

import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackException;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;

public class Backpack implements Inventory<AttackingSource> {
    private static final int DEFAULT_CAPACITY = 4;

    private List<AttackingSource> items;
    private int capacity; 

    public Backpack() {
        this.items = new ArrayList<>();
        capacity = DEFAULT_CAPACITY;
    }

    public boolean addItem(AttackingSource attackingSource) {
        if (items.size() < capacity) {
            items.add(attackingSource);
            
            return true;
        }

        return false;
    }

    public AttackingSource getItem(int index) throws BackpackException {
        if (index < 0 || index >= items.size()) {
            throw new BackpackException("Invalid index");
        }
        
        return items.get(index);
    }

    public AttackingSource removeItem(int index) throws BackpackException {
        if (index < 0 || index >= items.size()) {
            throw new BackpackException("Invalid index");
        }
        
        return items.remove(index);
    }

    public AttackingSource changeItem(int index, AttackingSource newContent) throws BackpackException {
        if (index < 0 || index >= items.size()) {
            throw new BackpackException("Invalid index");
        }
        
        AttackingSource result = items.get(index);
        items.set(index, newContent);
        return result;
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
