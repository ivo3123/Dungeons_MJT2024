package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.InventoryException;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;

public class BackpackTest {
    private Backpack backpack = new Backpack();

    @Test
    void testAddItemOneItem() {
        AttackingSource attackingSource = mock(AttackingSource.class);
    
        assertTrue(backpack.addItem(attackingSource), "Should be able to add one item");
    }

    @Test
    void testAddItemFiveItems() {
        AttackingSource attackingSource = mock(AttackingSource.class);
    
        assertTrue(backpack.addItem(attackingSource), "Should be able to add one item");
        assertTrue(backpack.addItem(attackingSource), "Should be able to add two item");
        assertTrue(backpack.addItem(attackingSource), "Should be able to add three item");
        assertTrue(backpack.addItem(attackingSource), "Should be able to add four item");
        assertFalse(backpack.addItem(attackingSource), "Should not be able to add five item");
    }

    @Test
    void testGetItemWithNoItems() {
        assertThrows(
            InventoryException.class,
            () -> backpack.getItem(3),
            "Should throw when there is nothing to get"
        );
    }

    @Test
    void testGetItemWitSomeItems() throws BackpackException {
        AttackingSource attackingSource = mock(AttackingSource.class);
        backpack.addItem(attackingSource);

        assertEquals(attackingSource, backpack.getItem(0), "Should return correctly when there is only 1 item in backpack and getting index 0");
        assertEquals(attackingSource, backpack.getItem(0), "The item in the backpack should remain in the backpack after getting it");
    }

    @Test
    void testRemoveItemWithNoItems() {
        assertThrows(
            BackpackException.class,
            () -> backpack.removeItem(3),
            "Should throw when there is nothing to remove"
        );
    }

    @Test
    void testRemoveItemWitSomeItems() throws BackpackException {
        AttackingSource attackingSource = mock(AttackingSource.class);
        backpack.addItem(attackingSource);

        assertEquals(attackingSource, backpack.removeItem(0), "Should return correctly when there is only 1 item in backpack and removing index 0");
        
        assertThrowsExactly(
            BackpackException.class,
            () -> backpack.getItem(0),
            "Item should not remain in the backpack after removing it"
        );
    }

    @Test
    void testChanegItem() throws BackpackException {
        AttackingSource attackingSource = mock(AttackingSource.class);
        backpack.addItem(attackingSource);

        AttackingSource attackingSource2 = mock(AttackingSource.class);

        backpack.changeItem(0, attackingSource2);

        assertSame(attackingSource2, backpack.getItem(0), "Should be same after changing");
    }

    @Test
    void testToString() {
        AttackingSource attackingSource = mock(AttackingSource.class);
        AttackingSource attackingSource2 = mock(AttackingSource.class);
        when(attackingSource.toString()).thenReturn("me1");
        when(attackingSource2.toString()).thenReturn("me2");
        backpack.addItem(attackingSource);
        backpack.addItem(attackingSource2);

        assertEquals("[me1, me2]", backpack.toString(), "Should format the string correctly");
    }
}
