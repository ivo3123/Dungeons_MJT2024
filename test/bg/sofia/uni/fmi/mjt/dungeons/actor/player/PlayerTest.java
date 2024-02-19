package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.dungeons.exception.BackpackException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.LevelNotHeighEnoughException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NotEnoughManaException;
import bg.sofia.uni.fmi.mjt.dungeons.map.Position;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class PlayerTest {
    private static final char INDEX = '3';
    private Player player;
    private Weapon weapon;
    private List<Spell> spells;
    private Position position;
    private Backpack backpack;

    @BeforeEach
    void setUp() {
        weapon = mock(Weapon.class);
        Spell spell = mock(Spell.class);
        spells = new ArrayList<>(List.of(spell));

        position = mock(Position.class);
        when(position.column()).thenReturn(3);
        when(position.row()).thenReturn(3);

        backpack = mock(Backpack.class);

        player = Player.builder(INDEX, position)
            .setHp(100)
            .setMana(100)
            .setSpellsCapacity(2)
            .setInitialWeapon(weapon)
            .setInitialSpells(spells)
            .setBackpack(backpack)
            .build();
    }

    @Test
    void testSpellCapacityUnderZero() {
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                Player.builder('0', null)
                    .setSpellsCapacity(-3)
                    .build();
            },
            "Negative capaciy so it should throw"
        );
    }

    @Test
    void testSwapSpellWhenLevelTooHeigh() throws BackpackException {
        int indexOfBackpack = 2;
        int indexOfSpell = 3;

        Spell spell = mock(Spell.class);
        when(backpack.getItem(indexOfBackpack)).thenReturn(spell);

        when(spell.getLevel()).thenReturn(5);

        assertThrows(
            LevelNotHeighEnoughException.class,
            () -> player.equipSpell(indexOfBackpack, indexOfSpell),
            "Should throw when level of spell is too heigh"
        );
    }

    @Test
    void testEquipSpellWhenLevelTooHeigh() throws BackpackException {
        int indexOfBackpack = 2;

        Spell spell = mock(Spell.class);
        when(backpack.getItem(indexOfBackpack)).thenReturn(spell);

        when(spell.getLevel()).thenReturn(5);

        assertThrows(
            LevelNotHeighEnoughException.class,
            () -> player.equipSpell(indexOfBackpack),
            "Should throw when level of spell is too heigh"
        );
    }

    @Test
    void testEquipWeaponWhenLevelTooHeigh() throws BackpackException {
        int indexOfBackpack = 2;

        Weapon weapon = mock(Weapon.class);
        when(backpack.getItem(indexOfBackpack)).thenReturn(weapon);

        when(weapon.getLevel()).thenReturn(5);

        assertThrows(
            LevelNotHeighEnoughException.class,
            () -> player.euqipWeapon(indexOfBackpack),
            "Should throw when level of spell is too heigh"
        );
    }

    @Test
    void testFinishFight() {
        player.finishFight(170);

        assertEquals(4, player.getLevel(), "Should level up correctly");
        assertTrue(player.getRemainingHp() == player.getTotalHp(), "Should be on full hp");
        assertTrue(player.getRemainingMana() == player.getTotalMana(), "Should have full mana");
        assertNull(player.getEnemy(), "Enemy should be null after a fight");
        assertEquals(20, player.getExperience(), "Should calculate xp correctly");
        assertEquals(0, player.getVenom(), "Venom should be 0");
        assertEquals(0, player.getRegenaration(), "Regenaration should be 0");
        assertEquals(0, player.getStrength(), "Strength should be 0");
    }

    @Test
    void testUseManaWhenNotEnoughMana() {
        Spell spell = mock(Spell.class);

        when(spell.getManaCost()).thenReturn(330);

        assertThrows(
            NotEnoughManaException.class,
            () -> player.useManaForSpell(spell),
            "Should throw when the player does not have enough mana to cast the spell"
        );
    }

    @Test
    void testUseMana() throws NotEnoughManaException {
        Spell spell = mock(Spell.class);

        when(spell.getManaCost()).thenReturn(33);

        assertDoesNotThrow(
            () -> player.useManaForSpell(spell),
            "Should throw when the player does not have enough mana to cast the spell"
        );

        assertEquals(67, player.getRemainingMana(), "Should calculate mana usage correctly");
    }
}
