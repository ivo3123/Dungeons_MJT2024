package bg.sofia.uni.fmi.mjt.dungeons.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bg.sofia.uni.fmi.mjt.dungeons.Dungeon;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.Inventory;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.Player;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.PlayerState;
import bg.sofia.uni.fmi.mjt.dungeons.exception.ExceptionHandler;
import bg.sofia.uni.fmi.mjt.dungeons.exception.InventoryException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.LevelNotHeighEnoughException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.MovementIntoObstacleException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NotEnoughManaException;
import bg.sofia.uni.fmi.mjt.dungeons.map.GameMap;
import bg.sofia.uni.fmi.mjt.dungeons.map.Position;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class CommandExecutorTest {
    private static final char INDEX = '3';

    private Player player;
    private Dungeon dungeon;
    private CommandExecutor commandExecutor;

    @BeforeEach
    public void setUp() {
        player = mock(Player.class);
        when(player.getPlayerIndex()).thenReturn(INDEX);
        dungeon = mock(Dungeon.class);
        when(dungeon.getPlayer(INDEX)).thenReturn(player);
        ExceptionHandler exceptionHandler = mock(ExceptionHandler.class);
        commandExecutor = new CommandExecutor(dungeon, exceptionHandler);
    }

    @Test
    void testExecuteWhenPlayerIsNull() {
        when(dungeon.getPlayer(INDEX)).thenReturn(null);

        String expected = "It appears you died...";
        String actual = commandExecutor.execute(new Command("self", INDEX, List.of()));
        actual = CommandDecoder.decodeRequestToLeave(actual);

        assertEquals(expected, actual, "When player is null it should be treated as dead");
    }

    @Test
    void testShowSelf() {
        when(player.getName()).thenReturn("Player 3");
        when(player.getLevel()).thenReturn(7);
        when(player.getRemainingHp()).thenReturn(98);
        when(player.getTotalHp()).thenReturn(110);
        when(player.getRemainingMana()).thenReturn(110);
        when(player.getTotalMana()).thenReturn(110);
        when(player.getExperience()).thenReturn(35);
        when(player.getExperiencePerLevel()).thenReturn(50);
        PlayerState playerState = mock(PlayerState.class);
        when(playerState.getState()).thenReturn("You are not in a fight");
        when(player.getPlayerState()).thenReturn(playerState);

        String expected = """
                - Player 3
                - level: 7
                - health: 98/110
                - mana: 110/110
                - experience: 35/50
                - You are not in a fight""";
        String actual = commandExecutor.execute(new Command("self", INDEX, List.of()));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'self'");

        verify(player, times(1)).getLevel();
    }

    @Test
    void testShowSelfWithVenomAndRegenaration() {
        when(player.getName()).thenReturn("Player 3");
        when(player.getLevel()).thenReturn(7);
        when(player.getRemainingHp()).thenReturn(98);
        when(player.getTotalHp()).thenReturn(110);
        when(player.getRemainingMana()).thenReturn(110);
        when(player.getTotalMana()).thenReturn(110);
        when(player.getExperience()).thenReturn(35);
        when(player.getExperiencePerLevel()).thenReturn(50);
        when(player.getVenom()).thenReturn(10);
        when(player.getRegenaration()).thenReturn(4);
        PlayerState playerState = mock(PlayerState.class);
        when(playerState.getState()).thenReturn("You are not in a fight");
        when(player.getPlayerState()).thenReturn(playerState);

        String expected = """
                - Player 3
                - level: 7
                - health: 98/110
                - venom: 10
                - regenaration: 4
                - mana: 110/110
                - experience: 35/50
                - You are not in a fight""";
        String actual = commandExecutor.execute(new Command("self", INDEX, List.of()));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'self'");
    }

    @Test
    void testShowSelfWithInvalidArguments() {
        String expected = "Self command requires no other arguments";
        String actual = commandExecutor.execute(new Command("self", INDEX, List.of("aaa")));

        assertEquals(expected, actual, "unexpected output for 'self' with invalid arguments");
    }

    @Test
    void testShowEnemyWithNoEnemy() {
        when(player.getEnemy()).thenReturn(null);

        String expected = "You are not in a fight so you do not have an enemy";
        String actual = commandExecutor.execute(new Command("enemy", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'enemy'");
    }

    @Test
    void testShowEnemyWithEnemyPlayer() {
        Player enemy = mock(Player.class);
        when(enemy.getName()).thenReturn("Player 98");
        when(enemy.getLevel()).thenReturn(3);
        when(enemy.getRemainingHp()).thenReturn(10);
        when(enemy.getTotalHp()).thenReturn(20);
        when(enemy.getRemainingMana()).thenReturn(30);
        when(enemy.getTotalMana()).thenReturn(34);
        when(enemy.getVenom()).thenReturn(4);

        when(player.getEnemy()).thenReturn(enemy);
        
        String expected = """
            - Player 98
            - level: 3
            - health: 10/20
            - venom: 4
            - mana: 30/34""";
        String actual = commandExecutor.execute(new Command("enemy", INDEX, List.of()));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'enemy'");
    }

    @Test
    void testShowEnemyWithEnemyMinion() {
        Minion enemy = mock(Minion.class);
        when(enemy.getName()).thenReturn("Minion 98");
        when(enemy.getLevel()).thenReturn(3);
        when(enemy.getRemainingHp()).thenReturn(10);
        when(enemy.getTotalHp()).thenReturn(20);
        when(enemy.getVenom()).thenReturn(4);

        Attack mockedAttack = mock(Attack.class);
        when(mockedAttack.toString()).thenReturn("Gain 3 strength. Deal 2 damage.");

        when(enemy.getNextAttack()).thenReturn(mockedAttack);

        when(player.getEnemy()).thenReturn(enemy);
        
        String expected = """
            - Minion 98
            - level: 3
            - health: 10/20
            - venom: 4
            - Gain 3 strength. Deal 2 damage.
            """.trim();
        String actual = commandExecutor.execute(new Command("enemy", INDEX, List.of()));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'enemy'");
    }

    @Test
    void testShowEnemyWithInvalidArguments() {
        String expected = "Show enemy command requires no other arguments";
        String actual = commandExecutor.execute(new Command("enemy", INDEX, List.of("sami", "emocije")));

        assertEquals(expected, actual, "unexpected output for 'enemy' with invalid arguments");
    }

    @Test
    void testShowMapWithInvalidArguments() {
        String expected = "Map command requires no other arguments";
        String actual = commandExecutor.execute(new Command("map", INDEX, List.of("aniol")));

        assertEquals(expected, actual, "unexpected output for 'map' with invalid arguments");
    }

    @Test
    void testShowWeapon() {
        Weapon weapon = mock(Weapon.class);
        when(weapon.toString()).thenReturn("Mocked weapon 3");
        when(player.getWeapon()).thenReturn(weapon);

        String expected = "Mocked weapon 3";
        String actual = commandExecutor.execute(new Command("weapon", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'weapon");
    }

    @Test
    void testShowWeaponWithInvalidArguments() {
        String expected = "Weapon command requires no other arguments";
        String actual = commandExecutor.execute(new Command("weapon", INDEX, List.of("barcelona")));

        assertEquals(expected, actual, "unexpected output for 'weapon' with invalid arguments");
    }

    @Test
    void testShowSepllsWithNoSpells() {
        when(player.getSpells()).thenReturn(List.of());

        String expected = "[]";
        String actual = commandExecutor.execute(new Command("spells", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'spells' when there are no spells");
    }

    @Test
    void testShowSepllsWithOneSpell() {
        Spell spell = mock(Spell.class);
        when(spell.toString()).thenReturn("Snowball 5");
        when(player.getSpells()).thenReturn(List.of(spell));

        String expected = "[Snowball 5]";
        String actual = commandExecutor.execute(new Command("spells", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'spells' when there is one spell");
    }

    @Test
    void testShowSepllsWithTwoSpells() {
        Spell spell1 = mock(Spell.class);
        when(spell1.toString()).thenReturn("Snowball 5");
        Spell spell2 = mock(Spell.class);
        when(spell2.toString()).thenReturn("Fireball 1");

        when(player.getSpells()).thenReturn(List.of(spell1, spell2));

        String expected = "[Snowball 5, Fireball 1]";
        String actual = commandExecutor.execute(new Command("spells", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'spells' when there are two spells");

        verify(player, times(1)).getSpells();
    }

    @Test
    void testShowSpellsWithInvalidArguments() {
        String expected = "Spells command requires no other arguments";
        String actual = commandExecutor.execute(new Command("spells", INDEX, List.of("zemno")));

        assertEquals(expected, actual, "unexpected output for 'spells' with invalid arguments");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testShowBackpack() {
        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(backpack.toString()).thenReturn("This is the content of the bakpack");

        when(player.getBackpack()).thenReturn(backpack);
        String expected = "This is the content of the bakpack";
        String actual = commandExecutor.execute(new Command("backpack", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'backpack'");
    }

    @Test
    void testShowBackpackWithInvalidArguments() {
        String expected = "Backpack command requires no other arguments";
        String actual = commandExecutor.execute(new Command("backpack", INDEX, List.of("jestem")));

        assertEquals(expected, actual, "unexpected output for 'backpack' with invalid arguments");
    }

    @Test
    void testShowInfoWithZeroArguments() {
        String expected = "Info command requires 1 or 2 arguments";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'info' with zero arguments");
    }

    @Test
    void testShowInfoWithThreeArguments() {
        String expected = "Info command requires 1 or 2 arguments";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("one", "two", "three")));

        assertEquals(expected, actual, "unexpected output for 'info' with three arguments");
    }

    @Test
    void testShowInfoWithWrongFirstArgument() {
        String expected = "First argument should be weapon, spell or backpack";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("wrong", "anything")));

        assertEquals(expected, actual, "unexpected output for 'info' with wrong first argument");
    }

    @Test
    void testShowInfoSpellWithIncorrectNumber() {
        String expected = "Second argument of the info spell command should be a valid index";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("spell", "definitely not a number")));

        assertEquals(expected, actual, "unexpected output for 'info sepll' with second argument not a number");
    }

    @Test
    void testShowInfoSpellWithNumberNotValidIndex() {
        when(player.getSpells()).thenReturn(List.of());
        
        String expected = "Second argument of the info spell command should be a valid index";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("spell", "123")));

        assertEquals(expected, actual, "unexpected output for 'info sepll' with second argument not a valid index");
    }

    @Test
    void testShowInfoSpellWithOnlyOneArgument() {
        String expected = "Info spell command requires exactly one more argument which is the index of the spell";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("spell")));

        assertEquals(expected, actual, "unexpected output for 'info sepll' with no second argument");
    }

    @Test
    void testShowInfoSpell() {
        Spell spell = mock(Spell.class);
        when(spell.getName()).thenReturn("Snowball 3");
        when(spell.getLevel()).thenReturn(3);
        when(spell.getManaCost()).thenReturn(10);

        Attack attack = mock(Attack.class);
        when(attack.toString()).thenReturn("Win the game");

        when(spell.getAttack()).thenReturn(attack);

        when(player.getSpells()).thenReturn(List.of(spell));
        
        String expected = """
            - Snowball 3
            - level: 3
            - effect: Win the game
            - mana cost: 10
            """.trim();
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("spell", "1")));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'info sepll' every argument is correct");
    }

    @Test
    void testShowInfoWeaponWithTwoArguments() {
        String expected = "Info weapon command requires exactly one argument";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("weapon", "second???")));

        assertEquals(expected, actual, "unexpected output for 'info weapon' with provided second argument");
    }

    @Test
    void testShowInfoWeapon() {
        Weapon weapon = mock(Weapon.class);
        when(weapon.getName()).thenReturn("My weapon");
        when(weapon.getLevel()).thenReturn(4);

        Attack attack = mock(Attack.class);
        when(attack.toString()).thenReturn("Underwhelm");

        when(weapon.getAttack()).thenReturn(attack);
        when(player.getWeapon()).thenReturn(weapon);

        String expected = """
            - My weapon
            - level: 4
            - effect: Underwhelm
            """.trim();
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("weapon")));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'info weapon' with correct arguments");
    }

    @Test
    void testShowInfoBackpackWithIncorrectNumber() {
        String expected = "Second argument of the info backpack command should be a valid index";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("backpack", "definitely not a number")));

        assertEquals(expected, actual, "unexpected output for 'info weapon' with second argument not a number");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testShowInfoBackpackWithNumberNotValidIndex() throws InventoryException {
        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(backpack.getItem(122)).thenThrow(InventoryException.class);

        when(player.getBackpack()).thenReturn(backpack);
        
        String expected = "Second argument of the info backpack command should be a valid index";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("backpack", "123")));

        assertEquals(expected, actual, "unexpected output for 'info weapon' with second argument not valid index");
    }

    @Test
    void testShowInfoBackpackWithOnlyOneArgument() {
        String expected = "Info backpack command requires exactly one more argument which is the index of the spell";
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("backpack")));

        assertEquals(expected, actual, "unexpected output for 'info weapon' with no second argument");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testShowInfoBackpackWithItemInBackpackThatIsNotWeaponOrSpell() throws InventoryException {
        Inventory<AttackingSource> backpack = mock(Inventory.class);
        AttackingSource attackingSource = mock(AttackingSource.class);
        when(backpack.getItem(0)).thenReturn(attackingSource);

        when(player.getBackpack()).thenReturn(backpack);

        assertThrows(
            IllegalCallerException.class, 
            () -> commandExecutor.execute(new Command("info", INDEX, List.of("backpack", "1"))),
            "Should throw when item in backpack is neither spell nor weapon"
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void testShowInfoBackpack() throws InventoryException {
        Inventory<AttackingSource> backpack = mock(Inventory.class);
        Spell spell = mock(Spell.class);
        when(spell.getName()).thenReturn("Snowball 3");
        when(spell.getLevel()).thenReturn(3);
        when(spell.getManaCost()).thenReturn(10);

        Attack attack = mock(Attack.class);
        when(attack.toString()).thenReturn("Win the game");

        when(spell.getAttack()).thenReturn(attack);

        when(player.getSpells()).thenReturn(List.of(spell));
        
        when(backpack.getItem(5)).thenReturn(spell);

        when(player.getBackpack()).thenReturn(backpack);
        
        String expected = """
            - Snowball 3
            - level: 3
            - effect: Win the game
            - mana cost: 10
            """.trim();
        String actual = commandExecutor.execute(new Command("info", INDEX, List.of("backpack", "6")));
        actual = CommandDecoder.decodeMultiLineResponse(actual);

        assertEquals(expected, actual, "unexpected output for 'info weapon'");
    }

    @Test
    void testDropItemWhileInFight() {
        when(player.isInFight()).thenReturn(true);

        String expected = "Cannot execute command drop item while in fight";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("does not matter")));

        assertEquals(expected, actual, "Should not be able to drop item while in fight");
    }

    @Test
    void testDropItemWithLessArguments() {
        String expected = "Drop command requires exactly 3 arguments - backpack {index of item in backpack} {direction}";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("not", "enough")));

        assertEquals(expected, actual, "unexpected output for 'drop' with less arguments");
    }

    @Test
    void testDropItemWithMoreArguments() {
        String expected = "Drop command requires exactly 3 arguments - backpack {index of item in backpack} {direction}";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("more", "than", "actually", "needed")));

        assertEquals(expected, actual, "unexpected output for 'drop' with more arguments");
    }

    @Test
    void testDropItemWithIncorrectFirstArgument() {
        String expected = "The first argument of drop command should be backpack";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("first", "is", "incorrect")));

        assertEquals(expected, actual, "unexpected output for 'drop' when first argument is incorrect");
    }

    @Test
    void testDropItemWithIncorrectThirdArgument() {
        String expected = "The third argument of drop command should be a direction - up, down, left, right";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "any", "not direction")));

        assertEquals(expected, actual, "unexpected output for 'drop' when third argument is incorrect");
    }

    @Test
    void testDropItemWithSecondArgumentNotANumber() {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('_');

        String expected = "The second argument of drop command should be a valid index";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "not a number", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' when second argument is not a number");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDropItemWithSecondArgumentNotValidIndex() throws InventoryException {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('_');
        when(map.isFree('_')).thenReturn(true);

        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(backpack);
        when(backpack.removeItem(4)).thenThrow(InventoryException.class);
        
        String expected = "The second argument of drop command should be a valid index";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "5", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' when second argument is not a valid index");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDropItemOnAFreeCell() throws InventoryException {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('_');
        when(map.isFree('_')).thenReturn(true);

        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(backpack);

        AttackingSource attackingSource = mock(AttackingSource.class);
        when(backpack.removeItem(4)).thenReturn(attackingSource);
        
        String expected = "Item dropped successfully";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "5", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' on a free cell");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDropItemToAnotherPlayer() throws InventoryException {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('2');
        when(map.isFree('2')).thenReturn(false);
        when(map.isPlayer('2')).thenReturn(true);

        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(backpack);

        AttackingSource attackingSource = mock(AttackingSource.class);
        when(attackingSource.toString()).thenReturn("MockedThing");
        when(backpack.removeItem(4)).thenReturn(attackingSource);

        Player otherPlayer = mock(Player.class);
        when(otherPlayer.toString()).thenReturn("MockedPlayer");
        when(dungeon.getPlayer('2')).thenReturn(otherPlayer);
        Inventory<AttackingSource> otherBackpack = mock(Inventory.class);
        when(otherPlayer.getBackpack()).thenReturn(otherBackpack);
        when(otherBackpack.addItem(attackingSource)).thenReturn(true);
        
        String expected = "You successfully gave MockedThing to MockedPlayer";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "5", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' when giving successfully to another player");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDropItemToAnotherPlayerWhenTheirInventoryIsFull() throws InventoryException {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('2');
        when(map.isFree('2')).thenReturn(false);
        when(map.isPlayer('2')).thenReturn(true);

        Inventory<AttackingSource> backpack = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(backpack);

        AttackingSource attackingSource = mock(AttackingSource.class);
        when(attackingSource.toString()).thenReturn("MockedThing");
        when(backpack.removeItem(4)).thenReturn(attackingSource);

        Player otherPlayer = mock(Player.class);
        when(otherPlayer.toString()).thenReturn("MockedPlayer");
        when(dungeon.getPlayer('2')).thenReturn(otherPlayer);
        Inventory<AttackingSource> otherBackpack = mock(Inventory.class);
        when(otherPlayer.getBackpack()).thenReturn(otherBackpack);
        when(otherBackpack.addItem(attackingSource)).thenReturn(false);
        
        String expected = "The backpack of MockedPlayer is full";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "5", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' when item another player when their inventory is full");
    }

    @Test
    void testDropItemWhenNotAllowed() {
        Position oldPosotion = mock(Position.class);
        GameMap map = mock(GameMap.class);
        Position newPosition = mock(Position.class);
        when(dungeon.getMap()).thenReturn(map);
        when(player.getPosition()).thenReturn(oldPosotion);
        when(oldPosotion.getNeighborPosition("up")).thenReturn(newPosition);
        when(map.getCell(newPosition)).thenReturn('2');
        when(map.isFree('2')).thenReturn(false);
        when(map.isPlayer('2')).thenReturn(false);
        
        String expected = "Cannot drop item on a cell that is neither free nor a player";
        String actual = commandExecutor.execute(new Command("drop", INDEX, List.of("backpack", "5", "up")));

        assertEquals(expected, actual, "unexpected output for 'drop' when trying to drop to a cell that is not free nor a player");
    }

    @Test
    void testDeleteItemWhenInFight() {
        when(player.isInFight()).thenReturn(true);
        String expected = "Cannot execute command delete item while in fight";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("backpack", "5")));

        assertEquals(expected, actual, "unexpected output for 'delete' when in fight");
    }

    @Test
    void testDeleteItemWhenInvalidNumberOfArguments() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Delete item command requires 2 arguments - backpack {index in backpack}";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("backpack")));

        assertEquals(expected, actual, "unexpected output for 'delete' with invalid number of arguments");
    }

    @Test
    void testDeleteItemWhenInvalidFirstArgument() {
        when(player.isInFight()).thenReturn(false);

        String expected = "First argument of drop item command should be backpack";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("invalid", "any")));

        assertEquals(expected, actual, "unexpected output for 'delete' with invalid frist argument");
    }

    @Test
    void testDeleteItemWithInvalidNumber() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Second argument should be a valid index of the backpack";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("backpack", "not a num")));

        assertEquals(expected, actual, "unexpected output for 'delete' when second argument is not a valid number");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeleteItemWithInvalidIndex() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        when(inventory.removeItem(3)).thenThrow(InventoryException.class);

        String expected = "Second argument should be a valid index of the backpack";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("backpack", "4")));

        assertEquals(expected, actual, "unexpected output for 'delete' when second argument is not a valid index");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeleteItem() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        AttackingSource attackingSource = mock(AttackingSource.class);
        when(attackingSource.toString()).thenReturn("name");
        when(inventory.removeItem(3)).thenReturn(attackingSource);

        String expected = "Successfully removed name";
        String actual = commandExecutor.execute(new Command("delete", INDEX, List.of("backpack", "4")));

        assertEquals(expected, actual, "unexpected output for 'delete' when arguments are correct");
    }

    @Test
    void testEquipItemWhileInFight() {
        when(player.isInFight()).thenReturn(true);

        String expected = "Cannot execute command equip item while in fight";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "4")));

        assertEquals(expected, actual, "unexpected output for 'equip item' while in fight");
    }

    @Test
    void testEquipItemWithInvalidNumberOfArguments() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Equip command requires 3 or 4 arguments";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack")));

        assertEquals(expected, actual, "unexpected output for 'equip item' with less arguments");
    }

    @Test
    void testEquipItemWithInvalidFirstArgument() {
        when(player.isInFight()).thenReturn(false);

        String expected = "The first argument of equip command should be backpack";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("invalid", "any")));

        assertEquals(expected, actual, "unexpected output for 'equip item' with invalid first argument");
    }

    @Test
    void testEquipItemWhenSecondArgumentNotValidNumber() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Second argument should be an index of a item in the backpack";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "invalid", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip item' when second argument is not a number");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipItemWhenSecondArgumentNotValidIndex() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        when(inventory.getItem(1)).thenThrow(InventoryException.class);

        String expected = "Second argument should be an index of a item in the backpack";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip item' when second argument is not a valid index");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipWeaponWithThreeArguments() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Weapon weapon = mock(Weapon.class);
        when(inventory.getItem(1)).thenReturn(weapon);

        String expected = "When having selected a weapon a third argument should not be passed";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip weapon' when given 3 arguments");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipWeaponWhenLevelOfWeaponIsTooHeigh() throws LevelNotHeighEnoughException, InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Weapon weapon = mock(Weapon.class);
        when(inventory.getItem(1)).thenReturn(weapon);

        doThrow(new LevelNotHeighEnoughException("")).when(player).euqipWeapon(1);

        String expected = "Level of weapon is heigher than the level of your hero so it cannot be equipped";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip weapon' when level of weapon is too heigh");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipWeapon() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Weapon weapon = mock(Weapon.class);
        when(inventory.getItem(1)).thenReturn(weapon);

        String expected = "Equipped weapon successfully";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip weapon' successfully");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipSpellWhenSpellsAreFull() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        when(player.areSpellsFull()).thenReturn(true);

        String expected = "Cannot equip a spell when all spell slots are full. Try swapping a spell instead";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip spell' when spells are full");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipSpellWhenLevelOfSpellIsTooHeigh() throws LevelNotHeighEnoughException, InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        when(player.areSpellsFull()).thenReturn(false);

        doThrow(new LevelNotHeighEnoughException("")).when(player).equipSpell(1);

        String expected = "Level of spell to equip is heigher than the level of your hero so it cannot be equipped";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip spell' when level of spell is too heigh");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEquipSpellSuccessfully() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        when(player.areSpellsFull()).thenReturn(false);

        String expected = "Equipped sepll successfully";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2")));

        assertEquals(expected, actual, "unexpected output for 'equip spell' successfully");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSwapSpellWhenThirdArgumentIsNotANumber() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        String expected = "Third argument should be an index of an equipped spell";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "not a num")));

        assertEquals(expected, actual, "unexpected output for 'swap spell' when third argument is not a number");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSwapSpellWhenThirdArgumentIsNotAValidIndex() throws LevelNotHeighEnoughException, InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        doThrow(new IndexOutOfBoundsException()).when(player).equipSpell(1, 7);

        String expected = "Third argument should be an index of an equipped spell";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "8")));

        assertEquals(expected, actual, "unexpected output for 'swap spell' when third argument is not a valid index");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSwapSpellWhenLevelOfSpellIsTooHeigh() throws LevelNotHeighEnoughException, InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        doThrow(new LevelNotHeighEnoughException("")).when(player).equipSpell(1, 1);

        String expected = "Level of spell to swap with is heigher than your hero so it cannot be equipped";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "2")));

        assertEquals(expected, actual, "unexpected output for 'swap spell' when level of spell is too heigh");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSwapSpellSuccessfully() throws InventoryException {
        when(player.isInFight()).thenReturn(false);

        Inventory<AttackingSource> inventory = mock(Inventory.class);
        when(player.getBackpack()).thenReturn(inventory);
        Spell spell = mock(Spell.class);
        when(inventory.getItem(1)).thenReturn(spell);

        String expected = "Swapped spell successfully";
        String actual = commandExecutor.execute(new Command("equip", INDEX, List.of("backpack", "2", "2")));

        assertEquals(expected, actual, "unexpected output for 'swap spell' successfully");
    }

    @Test
    void testMoveWhileInFight() {
        when(player.isInFight()).thenReturn(true);

        String expected = "Cannot execute command move while in fight";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'move' while in fight");
    }

    @Test
    void testMoveWithLessArguments() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Map command requires only 1 argument - up, down, left, right";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'move' with less arguments");
    }

    @Test
    void testMoveWithMoreArguments() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Map command requires only 1 argument - up, down, left, right";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of("one", "two")));

        assertEquals(expected, actual, "unexpected output for 'move' with more arguments");
    }

    @Test
    void testMoveWithInvalidFirstArgument() {
        when(player.isInFight()).thenReturn(false);

        String expected = "Direction must one of the following - up, down, left, right";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of("not a direction")));

        assertEquals(expected, actual, "unexpected output for 'move' with with invalid argument");
    }

    @Test
    void testMoveIntoObstacle() throws MovementIntoObstacleException {
        when(player.isInFight()).thenReturn(false);

        when(dungeon.movePlayer("down", player)).thenThrow(MovementIntoObstacleException.class);

        String expected = "Cannot move down because there is an obstacle there";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of("down")));

        assertEquals(expected, actual, "unexpected output for 'move' when moving into an obstacle");
    }

    @Test
    void testMoveSuccessfully() throws MovementIntoObstacleException {
        when(player.isInFight()).thenReturn(false);

        when(dungeon.movePlayer("down", player)).thenReturn("res");

        String expected = "res";
        String actual = commandExecutor.execute(new Command("move", INDEX, List.of("down")));

        assertEquals(expected, actual, "unexpected output for 'move' successfully");
    }

    @Test
    void testSelectAttackWithLessArguments() {
        String expected = "Select command requires only 1 argument - spells or weapon";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'slect' with less arguments");
    }

    @Test
    void testSelectAttackWithMoreArguments() {
        String expected = "Select command requires only 1 argument - spells or weapon";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of("one", "two")));

        assertEquals(expected, actual, "unexpected output for 'slect' with more arguments");
    }

    @Test
    void testSelectAttackWithInvalidState() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        String expected = "You are not allowed to select how to attack now";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'slect' with invalid state");
    }

    @Test
    void testSelectAttackWithInvalidArgument() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);
        
        String expected = "You should select weapon or spells";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of("invalid")));

        assertEquals(expected, actual, "unexpected output for 'slect' with invalid argument");
    }

    @Test
    void testSelectWeapon() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);
        
        String expected = "You chose to use your weapon. You can attack once with your weapon";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of("weapon")));

        assertEquals(expected, actual, "unexpected output for 'slect weapon'");

        verify(player, times(1)).setPlayerState(PlayerState.READY_TO_ATTACK_WITH_WEAPON);
    }

    @Test
    void testSelectSpells() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);
        
        String expected = "You chose to cast spells. You can cast spells until your mana runs out";
        String actual = commandExecutor.execute(new Command("select", INDEX, List.of("spells")));

        assertEquals(expected, actual, "unexpected output for 'slect spells'");

        verify(player, times(1)).setPlayerState(PlayerState.READY_TO_ATTACK_WITH_SPELLS);
    }

    @Test
    void testAttackWithMoreArguments() {
        String expected = "Attack command requires 0 or 1 arguments";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("one", "two")));

        assertEquals(expected, actual, "unexpected output for 'attack' with more arguments");
    }

    @Test
    void testAttackWhenPlayerStateIsNotInFight() {
        when(player.getPlayerState()).thenReturn(PlayerState.NOT_IN_FIGHT);

        String expected = "You are not in a fight";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'attack' when player state is not in fight");
    }

    @Test
    void testAttackWhenPlayerStateIsReadyToSelectTypeOfAttack() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);

        String expected = "You need to select how to attack first - type: select {spells/weapon}";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'attack' when player state is ready to select type of atatck");
    }

    @Test
    void testAttackWhenPlayerStateIsAlreadyAttackedWithWeapon() {
        when(player.getPlayerState()).thenReturn(PlayerState.ALREADY_ATTACKED_WITH_WEAPON);

        String expected = "You chose weapon and already attacked with it. You may end your turn";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'attack' when player state is already attacked with weapon");
    }

    @Test
    void testAttackWhenPlayerStateIsWaitingDuringOpponentsTurn() {
        when(player.getPlayerState()).thenReturn(PlayerState.WAITING_DURING_OPPONENTS_TURN);

        String expected = "It's the opponent's turn";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'attack' when player state is waiting during opponent's turn");
    }

    @Test
    void testAttackWithWeaponWhenStateIsToAttackWithSpells() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        String expected = "To attack with a spell you need to specify its index";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'attack with weapon' when player state is to attack with spells");
    }

    @Test
    void testAttackWithWeaponWithoutKilling() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_WEAPON);

        Weapon weapon = mock(Weapon.class);
        when(player.getWeapon()).thenReturn(weapon);
        Attack attack = mock(Attack.class);
        when(weapon.getAttack()).thenReturn(attack);

        Actor enemy = mock(Actor.class);
        when(player.getEnemy()).thenReturn(enemy);

        when(player.attack(attack, enemy)).thenReturn(true);

        String expected = "You attacked successfully with your weapon";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'attack with weapon' without killing");

        verify(player, times(1)).setPlayerState(PlayerState.ALREADY_ATTACKED_WITH_WEAPON);
    }

    @Test
    void testAttackWithWeaponAndKillingMinion() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_WEAPON);

        Weapon weapon = mock(Weapon.class);
        when(player.getWeapon()).thenReturn(weapon);
        Attack attack = mock(Attack.class);
        when(weapon.getAttack()).thenReturn(attack);

        Minion enemy = mock(Minion.class);
        when(player.getEnemy()).thenReturn(enemy);

        Position position = new Position(2, 2);
        when(player.getPosition()).thenReturn(position);

        when(player.attack(attack, enemy)).thenReturn(false);

        when(enemy.getExpirienceUponDeath()).thenReturn(40);

        GameMap map = mock(GameMap.class);
        when(dungeon.getMap()).thenReturn(map);
        when(map.getRandomFreePosition()).thenReturn(position);

        String expected = "You killed your enemy";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'attack with weapon' and killing minion");
        
        verify(map, times(1)).setPlayer(position, INDEX);
        verify(player, times(1)).finishFight(40);
        verify(map, times(2)).getRandomFreePosition();
        verify(map, times(1)).setTreasure(position);
        verify(map, times(1)).setMinion(position);
    }

    @Test
    void testAttackWithWeaponAndKillingPlayer() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_WEAPON);

        Weapon weapon = mock(Weapon.class);
        when(player.getWeapon()).thenReturn(weapon);
        Attack attack = mock(Attack.class);
        when(weapon.getAttack()).thenReturn(attack);

        Player enemy = mock(Player.class);
        when(player.getEnemy()).thenReturn(enemy);

        Position position = new Position(2, 2);
        when(player.getPosition()).thenReturn(position);

        when(player.attack(attack, enemy)).thenReturn(false);

        when(enemy.getExpirienceUponDeath()).thenReturn(40);

        GameMap map = mock(GameMap.class);
        when(dungeon.getMap()).thenReturn(map);

        String expected = "You killed your enemy";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'attack with weapon' and killing a player");
        
        verify(map, times(1)).setPlayer(position, INDEX);
        verify(player, times(1)).finishFight(40);
    }

    @Test
    void testAttackWithSpellWithArgumentNotANumber() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        String expected = "The second argument should be a valid index of a spell";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("not a numm")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' with argument that is not a number");
    }

    @Test
    void testAttackWithSpellWhenPlayerStateIsToAttackWithWeapon() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_WEAPON);

        String expected = "To attack with your weapon you should not provide another argument";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("3")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' when player state is to attack with a weapon");
    }

    @Test
    void testAttackWithSpellWithArgumentInvalidIndex() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        when(player.getSpells()).thenReturn(List.of());

        String expected = "The second argument should be a valid index of a spell";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("3")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' with argument that is not a valid index");
    }

    @Test
    void testAttackWithSpellWithoutEnoughMana() throws NotEnoughManaException {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        Spell spell = mock(Spell.class);
        when(player.getSpells()).thenReturn(List.of(spell));

        doThrow(new NotEnoughManaException("")).when(player).useManaForSpell(spell);

        String expected = "Not enough mana to cast this spell";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("1")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' without having enough mana");
    }

    @Test
    void testAttackWithSpellWithoutKillingEnemy() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        Spell spell = mock(Spell.class);
        when(player.getSpells()).thenReturn(List.of(spell));

        Attack attack = mock(Attack.class);
        when(spell.getAttack()).thenReturn(attack);

        Actor enemy = mock(Actor.class);
        when(player.getEnemy()).thenReturn(enemy);

        when(player.attack(attack, enemy)).thenReturn(true);

        String expected = "You attacked successfully with your spell";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("1")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' without killing the enemy");
    }

    @Test
    void testAttackWithSpellAndKillingMinion() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        Spell spell = mock(Spell.class);
        when(player.getSpells()).thenReturn(List.of(spell));

        Attack attack = mock(Attack.class);
        when(spell.getAttack()).thenReturn(attack);

        Minion enemy = mock(Minion.class);
        when(player.getEnemy()).thenReturn(enemy);

        Position position = new Position(2, 2);
        when(player.getPosition()).thenReturn(position);

        when(player.attack(attack, enemy)).thenReturn(false);

        GameMap map = mock(GameMap.class);
        when(dungeon.getMap()).thenReturn(map);
        when(map.getRandomFreePosition()).thenReturn(position);

        String expected = "You killed your enemy";
        String actual = commandExecutor.execute(new Command("attack", INDEX, List.of("1")));

        assertEquals(expected, actual, "unexpected output for 'attack with spells' and killing a minion");

        verify(map, times(1)).setPlayer(position, INDEX);
        verify(map, times(2)).getRandomFreePosition();
        verify(map, times(1)).setTreasure(position);
        verify(map, times(1)).setMinion(position);
    }

    @Test
    void testEndTurnWithInvalidArguments() {
        String expected = "End turn command requires no other arguments";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'end-turn' with invalid arguments");
    }

    @Test
    void testEndTurnWhileNotInFight() {
        when(player.getPlayerState()).thenReturn(PlayerState.NOT_IN_FIGHT);

        String expected = "You can end turn only during a fight during your turn";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'end-turn' while not in fight");
    }

    @Test
    void testEndTurnWhileWaitingDuringOpponentsTurn() {
        when(player.getPlayerState()).thenReturn(PlayerState.WAITING_DURING_OPPONENTS_TURN);

        String expected = "You can end turn only during a fight during your turn";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'end-turn' while waiting during opponent's turn");
    }

    @Test
    void testEndTurnWithoutAnyoneDying() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);

        Actor enemy = mock(Actor.class);
        when(player.getEnemy()).thenReturn(enemy);

        when(enemy.startTurn()).thenReturn(true);
        when(player.isAlive()).thenReturn(true);

        String expected = "You eneded your turn successfully";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'end-turn' when neither the player nor the enemy dies");

        verify(player, times(1)).endTurn();
    }

    @Test
    void testEndTurnAndEnemyMinionDying() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_WEAPON);

        Minion enemy = mock(Minion.class);
        when(player.getEnemy()).thenReturn(enemy);

        when(enemy.startTurn()).thenReturn(false);

        Position position = new Position(4, 5);
        when(player.getPosition()).thenReturn(position);

        GameMap map = mock(GameMap.class);
        when(dungeon.getMap()).thenReturn(map);
        when(map.getRandomFreePosition()).thenReturn(position);

        String expected = "You eneded your turn successfully and the enemy died at the start of their turn";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of()));

        assertEquals(expected, actual, "unexpected output for 'end-turn' and the enemy minion dies");

        verify(player, times(1)).endTurn();
    }

    @Test
    void testEndTurnAndPlayerDying() {
        when(player.getPlayerState()).thenReturn(PlayerState.READY_TO_ATTACK_WITH_SPELLS);

        Minion enemy = mock(Minion.class);
        when(player.getEnemy()).thenReturn(enemy);

        Position position = new Position(4, 5);
        when(player.getPosition()).thenReturn(position);

        when(enemy.startTurn()).thenReturn(true);
        when(player.isAlive()).thenReturn(false);

        GameMap map = mock(GameMap.class);
        when(dungeon.getMap()).thenReturn(map);

        String expected = "You eneded your turn successfully and died during the enemy's turn";
        String actual = commandExecutor.execute(new Command("end-turn", INDEX, List.of()));
        actual = CommandDecoder.decodeRequestToLeave(actual);

        assertEquals(expected, actual, "unexpected output for 'end-turn' and the player dies");

        verify(player, times(1)).endTurn();
        verify(dungeon, times(1)).removePlayer(INDEX);
        verify(map, times(1)).setMinion(position);
    }

    @Test
    void testDisconnectWithInvalidArguments() {
        String expected = "Disconnect command requires no other arguments";
        String actual = commandExecutor.execute(new Command("quit", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for 'disconnect' with invalid arguments");
    }

    @Test
    void testDisconnectSuccessfully() {
        String expected = "You quit successfully";
        String actual = commandExecutor.execute(new Command("quit", INDEX, List.of()));
        actual = CommandDecoder.decodeRequestToLeave(actual);

        assertEquals(expected, actual, "unexpected output for 'disconnect' successfully");

        verify(dungeon, times(1)).removePlayer(INDEX);
    }

    @Test
    void testUnknownCommand() {
        String expected = "Unknown command";
        String actual = commandExecutor.execute(new Command("unknown", INDEX, List.of("any")));

        assertEquals(expected, actual, "unexpected output for an unknown command");
    }

    @Test
    void testAddPlayer() {
        commandExecutor.addPlayer(INDEX);

        verify(dungeon, times(1)).addPlayer(INDEX);
    }
}
