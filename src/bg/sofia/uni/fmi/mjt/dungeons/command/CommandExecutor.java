package bg.sofia.uni.fmi.mjt.dungeons.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.dungeons.Dungeon;
import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.Player;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.PlayerState;
import bg.sofia.uni.fmi.mjt.dungeons.exception.ExceptionHandler;
import bg.sofia.uni.fmi.mjt.dungeons.exception.InventoryException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.LevelNotHeighEnoughException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.MovementIntoObstacleException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NotEnoughManaException;
import bg.sofia.uni.fmi.mjt.dungeons.map.GameMap;
import bg.sofia.uni.fmi.mjt.dungeons.map.Position;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class CommandExecutor {
    static final String CLIENT_CAN_LEAVE = "CAN_LEAVE";
    static final String JSON_RESPONSE = "JSON_RESPONSE";
    static final String MULTI_LINE_STRING = "MULTI_LINE_STRING";
    static final String NEW_LINE_SEPARATOR = "NEW_LINE_SEPARATOR";

    static final String LINE_BEGINNING = "- ";

    private static final int THREE = 3;

    private static final String MAP = "map";
    private static final String SELF = "self";
    private static final String ENEMY = "enemy";
    private static final String WEAPON = "weapon";
    private static final String SPELLS = "spells";
    private static final String SPELL = "spell";
    private static final String BACKPACK = "backpack";
    private static final String INFO = "info";
    private static final String DROP = "drop";
    private static final String DELETE = "delete";
    private static final String EQUIP = "equip";
    private static final String MOVE = "move";
    private static final String SELECT = "select";
    private static final String ATTACK = "attack";
    private static final String END_TURN = "end-turn";
    private static final String QUIT = "quit";

    private final Dungeon dungeon;
    private final ExceptionHandler exceptionHandler;

    public CommandExecutor(Dungeon dungeon, ExceptionHandler exceptionHandler) {
        this.dungeon = dungeon;
        this.exceptionHandler = exceptionHandler;
    }

    public void addPlayer(char playerIndex) {
        dungeon.addPlayer(playerIndex);
    }

    public String execute(Command command) {
        if (getPlayer(command) == null) {
            dungeon.removePlayer(command.playerIndex());
            return formatAsCommandToLeave("It appears you died...");
        }

        return switch (command.command()) {
            case MAP -> showMap(command);
            case SELF -> showSelf(command);
            case ENEMY -> showEnemy(command);
            case WEAPON -> showWeapon(command);
            case SPELLS -> showSpells(command);
            case BACKPACK -> showBackpack(command);
            case INFO -> showInfo(command);
            case DROP -> dropItem(command);
            case DELETE -> deleteItem(command);
            case EQUIP -> equipItem(command);
            case MOVE -> move(command);
            case SELECT -> selectTypeOfAttack(command);
            case ATTACK -> attack(command);
            case END_TURN -> endTurn(command);
            case QUIT -> disconnectClient(command);
            default -> "Unknown command";
        };
    }

    private String showMap(Command command) {
        if (command.arguments().size() != 0) {
            return "Map command requires no other arguments";
        }

        return dungeon.getMap().toString();
    }

    private String showSelf(Command command) {
        if (command.arguments().size() != 0) {
            return "Self command requires no other arguments";
        }

        Player player = getPlayer(command);

        String lineOfMana = LINE_BEGINNING + "mana: " + player.getRemainingMana() + "/" + player.getTotalMana();
        String lineOfPlayerState = LINE_BEGINNING + player.getPlayerState().getState();
        String lineOfExperience = 
            LINE_BEGINNING + "experience: " + player.getExperience() + "/" + player.getExperiencePerLevel();

        List<String> lines = getActorInfo(player);
        lines.add(lineOfMana);
        lines.add(lineOfExperience);
        lines.add(lineOfPlayerState);

        return formatAsMultiLineString(lines);
    }

    private String showEnemy(Command command) {
        if (command.arguments().size() != 0) {
            return "Show enemy command requires no other arguments";
        }

        Actor enemy = getPlayer(command).getEnemy();

        if (enemy == null) {
            return "You are not in a fight so you do not have an enemy";
        }

        List<String> lines = getActorInfo(enemy);

        if (enemy instanceof Minion minion) {
            String lineOfEnemyNextAttack = LINE_BEGINNING + minion.getNextAttack();
            lines.add(lineOfEnemyNextAttack);
        } else if (enemy instanceof Player player) {
            String lineOfMana = LINE_BEGINNING + "mana: " + player.getRemainingMana() + "/" + player.getTotalMana();
            lines.add(lineOfMana);
        }

        return formatAsMultiLineString(lines);
    }

    private String showWeapon(Command command) {
        if (command.arguments().size() != 0) {
            return "Weapon command requires no other arguments";
        }

        return getPlayer(command).getWeapon().toString();
    }

    private String showSpells(Command command) {
        if (command.arguments().size() != 0) {
            return "Spells command requires no other arguments";
        }

        return getPlayer(command).getSpells().toString();
    }

    private String showBackpack(Command command) {
        if (command.arguments().size() != 0) {
            return "Backpack command requires no other arguments";
        }

        return getPlayer(command).getBackpack().toString();
    }

    private String showInfo(Command command) {
        if (command.arguments().size() == 0 || command.arguments().size() > 2) {
            return "Info command requires 1 or 2 arguments";
        }

        String firstArgument = command.arguments().getFirst();

        return switch (firstArgument) {
            case WEAPON -> {
                if (command.arguments().size() != 1) {
                    yield "Info weapon command requires exactly one argument";
                }
                yield handleShowInfoWapon(command);
            }
            case SPELL -> {
                if (command.arguments().size() != 2) {
                    yield "Info spell command requires exactly one more argument which is the index of the spell";
                }
                yield handleShowInfoSpell(command);
            }
            case BACKPACK -> {
                if (command.arguments().size() != 2) {
                    yield "Info backpack command requires exactly one more argument which is the index of the spell";
                }
                yield handleShowInfoBackpack(command);
            }
            default -> "First argument should be weapon, spell or backpack";
        };
    }

    private String dropItem(Command command) {
        if (getPlayer(command).isInFight()) {
            return getCannotExecuteCommandMessage("drop item");
        }

        if (command.arguments().size() != THREE) {
            return "Drop command requires exactly 3 arguments - backpack {index of item in backpack} {direction}";
        }
        if (!command.arguments().getFirst().equals(BACKPACK)) {
            return "The first argument of drop command should be backpack";
        }
        if (!Direction.isValid(command.arguments().get(2))) {
            return "The third argument of drop command should be a direction - up, down, left, right";
        }

        return handleDropItem(command);
    }

    private String deleteItem(Command command) {
        if (getPlayer(command).isInFight()) {
            return getCannotExecuteCommandMessage("delete item");
        }

        if (command.arguments().size() != 2) {
            return "Delete item command requires 2 arguments - backpack {index in backpack}";
        }
        if (!command.arguments().getFirst().equals(BACKPACK)) {
            return "First argument of drop item command should be " + BACKPACK;
        }

        try {
            int indexOfBackpack = getIndex(command, 1);
            AttackingSource item = getPlayer(command).getBackpack().removeItem(indexOfBackpack);

            return "Successfully removed " + item;
        } catch (NumberFormatException | InventoryException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Second argument should be a valid index of the backpack";
        }
    }

    private String equipItem(Command command) {
        if (getPlayer(command).isInFight()) {
            return getCannotExecuteCommandMessage("equip item");
        }

        if (command.arguments().size() != 2 && command.arguments().size() != THREE) {
            return "Equip command requires 3 or 4 arguments";
        } else if (!command.arguments().getFirst().equals(BACKPACK)) {
            return "The first argument of equip command should be " + BACKPACK;
        }
        try {
            Player player = getPlayer(command);
            int indexOfBackpack = getIndex(command, 1);
            AttackingSource item = player.getBackpack().getItem(indexOfBackpack);
            if (item instanceof Weapon) {
                return command.arguments().size() == THREE
                    ? "When having selected a weapon a third argument should not be passed"
                    : handleEquipWeapon(command, player, indexOfBackpack);
            } else {
                return command.arguments().size() == 2
                    ? handleEquipSpell(command, player, indexOfBackpack)
                    : handleSwapSpell(command, player, indexOfBackpack);
            }
        } catch (NumberFormatException | InventoryException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Second argument should be an index of a item in the backpack";
        }
    }

    private String move(Command command) {
        if (getPlayer(command).isInFight()) {
            return getCannotExecuteCommandMessage("move");
        }

        if (command.arguments().size() != 1) {
            return "Map command requires only 1 argument - up, down, left, right";
        }

        String direction = command.arguments().getFirst();

        if (!Direction.isValid(direction)) {
            return "Direction must one of the following - up, down, left, right";
        }

        try {
            return dungeon.movePlayer(direction, getPlayer(command));
        } catch (MovementIntoObstacleException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Cannot move " + direction + " because there is an obstacle there";
        }
    }

    private String selectTypeOfAttack(Command command) {
        if (command.arguments().size() != 1) {
            return "Select command requires only 1 argument - " + SPELLS + " or " + WEAPON;
        }

        Player player = getPlayer(command);

        if (!player.getPlayerState().equals(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK)) {
            return "You are not allowed to select how to attack now";
        }

        if (command.arguments().getFirst().equals(SPELLS)) {
            player.setPlayerState(PlayerState.READY_TO_ATTACK_WITH_SPELLS);
            return "You chose to cast spells. You can cast spells until your mana runs out";
        } else if (command.arguments().getFirst().equals(WEAPON)) {
            player.setPlayerState(PlayerState.READY_TO_ATTACK_WITH_WEAPON);
            return "You chose to use your weapon. You can attack once with your weapon";
        } else {
            return "You should select weapon or spells";
        }
    }

    private String attack(Command command) {
        if (command.arguments().size() > 1) {
            return "Attack command requires 0 or 1 arguments";
        }

        Player player = getPlayer(command);
        PlayerState playerState = player.getPlayerState();

        Optional<String> invalidState = getInvalidState(playerState);

        if (invalidState.isPresent()) {
            return invalidState.get();
        }

        if (command.arguments().size() == 0) {
            return handleAttackWithWeapon(player);
        } else {
            return handleAttackWithSpell(command, player);
        }
    }

    private String endTurn(Command command) {
        if (command.arguments().size() != 0) {
            return "End turn command requires no other arguments";
        }

        Player player = getPlayer(command);

        if (
            player.getPlayerState().equals(PlayerState.NOT_IN_FIGHT) ||
            player.getPlayerState().equals(PlayerState.WAITING_DURING_OPPONENTS_TURN)
        ) {
            return "You can end turn only during a fight during your turn";
        }

        String turnEnded = "You eneded your turn successfully";
        player.endTurn();
        Actor enemy = player.getEnemy();
        boolean enemySurvived = enemy.startTurn();

        if (!enemySurvived) {
            resolveFight(player, enemy, player.getPosition());
            return turnEnded + " and the enemy died at the start of their turn";
        }
        if (!player.isAlive()) {
            resolveFight(enemy, player, player.getPosition());
            return formatAsCommandToLeave(turnEnded + " and died during the enemy's turn");
        }

        return turnEnded;
    }

    private String disconnectClient(Command command) {
        if (command.arguments().size() != 0) {
            return "Disconnect command requires no other arguments";
        }

        Player player = getPlayer(command);

        if (player.isInFight()) {
            resolveFight(player.getEnemy(), player, player.getPosition());
        }

        dungeon.removePlayer(command.playerIndex());
        return formatAsCommandToLeave("You quit successfully");
    }

    private List<String> getActorInfo(Actor actor) {
        String lineOfActor = LINE_BEGINNING + actor.getName();
        String lineOfLevel = LINE_BEGINNING + "level: " + actor.getLevel();
        String lineOfHp = LINE_BEGINNING + "health: " + actor.getRemainingHp() + "/" + actor.getTotalHp();

        List<String> lines = new ArrayList<>(List.of(lineOfActor, lineOfLevel, lineOfHp));

        if (actor.getStrength() > 0) {
            String lineOfStrength = LINE_BEGINNING + "strength: " + actor.getStrength();
            lines.add(lineOfStrength);
        }

        if (actor.getVenom() > 0) {
            String lineOfVenom = LINE_BEGINNING + "venom: " + actor.getVenom();
            lines.add(lineOfVenom);
        }

        if (actor.getRegenaration() > 0) {
            String lineOfRegenaration = LINE_BEGINNING + "regenaration: " + actor.getRegenaration();
            lines.add(lineOfRegenaration);
        }

        return lines;
    }

    private String handleShowInfoWapon(Command command) {
        Weapon weapon = getPlayer(command).getWeapon();

        return formatAsMultiLineString(getWeaponInfo(weapon));
    }

    private String handleShowInfoSpell(Command command) {
        try {
            int index = getIndex(command, 1);
            Spell spell = getPlayer(command).getSpells().get(index);

            return formatAsMultiLineString(getSpellInfo(spell));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Second argument of the info spell command should be a valid index";
        }
    }

    private String handleShowInfoBackpack(Command command) {
        try {
            int index = getIndex(command, 1);
            AttackingSource item = getPlayer(command).getBackpack().getItem(index);

            if (item instanceof Weapon weapon) {
                return formatAsMultiLineString(getWeaponInfo(weapon));
            } else if (item instanceof Spell spell) {
                return formatAsMultiLineString(getSpellInfo(spell));
            }

            throw new IllegalCallerException("Unexpected type of item");
        } catch (NumberFormatException | InventoryException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Second argument of the info backpack command should be a valid index";
        }
    }

    private List<String> getAttackingSourceInfo(AttackingSource attackingSource) {
        String lineOfName = LINE_BEGINNING + attackingSource.getName();
        String lineOfLevel = LINE_BEGINNING + "level: " + attackingSource.getLevel();
        String lineOfAttack = LINE_BEGINNING + "effect: " + attackingSource.getAttack();

        List<String> lines = new ArrayList<>();

        lines.addAll(List.of(lineOfName, lineOfLevel, lineOfAttack));

        return lines;
    }

    private List<String> getSpellInfo(Spell spell) {
        List<String> lines = getAttackingSourceInfo(spell);

        String lineOfMana = LINE_BEGINNING + "mana cost: " + spell.getManaCost();
        lines.add(lineOfMana);

        return lines;
    }

    private List<String> getWeaponInfo(Weapon weapon) {
        List<String> lines = getAttackingSourceInfo(weapon);

        return lines;
    }

    private String handleDropItem(Command command) {
        try {
            Position playerPosition = getPlayer(command).getPosition();
            String directon = command.arguments().getLast();
            Position positionToDrop = playerPosition.getNeighborPosition(directon);
            char cellToDropState = dungeon.getMap().getCell(positionToDrop);
            int index = getIndex(command, 1);

            if (dungeon.getMap().isFree(cellToDropState)) {
                AttackingSource itemToDrop = getPlayer(command).getBackpack().removeItem(index);
                
                dungeon.addDroppedItem(positionToDrop, itemToDrop);
                return "Item dropped successfully";
            } else if (dungeon.getMap().isPlayer(cellToDropState)) {
                AttackingSource itemToDrop = getPlayer(command).getBackpack().removeItem(index);
                Player otherPlayer = dungeon.getPlayer(cellToDropState);

                boolean addedSuccessfully = otherPlayer.getBackpack().addItem(itemToDrop);

                return addedSuccessfully
                    ? "You successfully gave " +  itemToDrop + " to " + otherPlayer
                    : "The backpack of " + otherPlayer + " is full";
            } else {
                return "Cannot drop item on a cell that is neither free nor a player";
            }
        } catch (NumberFormatException | InventoryException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "The second argument of drop command should be a valid index";
        }
    }

    private String handleEquipWeapon(Command command, Player player, int indexOfBackpack) throws InventoryException {
        try {
            player.euqipWeapon(indexOfBackpack);
            return "Equipped weapon successfully";
        } catch (LevelNotHeighEnoughException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Level of weapon is heigher than the level of your hero so it cannot be equipped";
        }
    }

    private String handleEquipSpell(Command command, Player player, int indexOfBackpack) throws InventoryException {
        if (player.areSpellsFull()) {
            return "Cannot equip a spell when all spell slots are full. Try swapping a spell instead";
        } else {
            try {
                player.equipSpell(indexOfBackpack);
                return "Equipped sepll successfully";
            } catch (LevelNotHeighEnoughException e) {
                exceptionHandler.handleException(e, getPlayer(command).getName());
                return "Level of spell to equip is heigher than the level of your hero so it cannot be equipped";
            }
        }
    }

    private String handleSwapSpell(Command command, Player player, int indexOfBackpack) {
        try {
            int indexOfSpell = getIndex(command, 2);
            player.equipSpell(indexOfBackpack, indexOfSpell);
            
            return "Swapped spell successfully";
        } catch (NumberFormatException | InventoryException | IndexOutOfBoundsException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Third argument should be an index of an equipped spell";
        } catch (LevelNotHeighEnoughException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Level of spell to swap with is heigher than your hero so it cannot be equipped";
        }
    }

    private Optional<String> getInvalidState(PlayerState playerState) {
        Optional<String> invalidState = switch (playerState) {
            case PlayerState.WAITING_DURING_OPPONENTS_TURN -> Optional.of("It's the opponent's turn");
            case PlayerState.NOT_IN_FIGHT -> Optional.of("You are not in a fight");
            case PlayerState.ALREADY_ATTACKED_WITH_WEAPON -> Optional.of(
                "You chose weapon and already attacked with it. You may end your turn"
            );
            case PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK -> Optional.of(
                "You need to select how to attack first - type: select {spells/weapon}"
            );
            default -> Optional.empty();
        };

        return invalidState;
    }

    private String handleAttackWithWeapon(Player player) {
        return switch (player.getPlayerState()) {
            case PlayerState.READY_TO_ATTACK_WITH_WEAPON -> {
                boolean enemySurvived = player.attack(player.getWeapon().getAttack(), player.getEnemy());

                if (!enemySurvived) {
                    resolveFight(player, player.getEnemy(), player.getPosition());
                    yield "You killed your enemy";
                }

                player.setPlayerState(PlayerState.ALREADY_ATTACKED_WITH_WEAPON);
                yield "You attacked successfully with your weapon";
            }
            default -> "To attack with a spell you need to specify its index";
        };
    }

    private String handleAttackWithSpell(Command command, Player player) {
        try {
            int index = getIndex(command, 0);

            return switch (player.getPlayerState()) {
                case PlayerState.READY_TO_ATTACK_WITH_SPELLS -> {
                    Spell spell = player.getSpells().get(index);
                    player.useManaForSpell(spell);
                    boolean enemySurvived = player.attack(spell.getAttack(), player.getEnemy());

                    if (!enemySurvived) {
                        resolveFight(player, player.getEnemy(), player.getPosition());
                        yield "You killed your enemy";
                    }

                    yield "You attacked successfully with your spell";
                }
                default -> "To attack with your weapon you should not provide another argument";
            };
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "The second argument should be a valid index of a spell";
        } catch (NotEnoughManaException e) {
            exceptionHandler.handleException(e, getPlayer(command).getName());
            return "Not enough mana to cast this spell";
        }
    }

    private void resolveFight(Actor winner, Actor loser, Position position) {
        GameMap map = dungeon.getMap();

        if (loser instanceof Player playerToRemove) {
            dungeon.removePlayer(playerToRemove.getPlayerIndex());
            if (winner instanceof Player player) {
                map.setPlayer(position, player.getPlayerIndex());
                player.finishFight(loser.getExpirienceUponDeath());
            } else {
                dungeon.getMap().setMinion(position);
            }
        } else if (loser instanceof Minion) {
            Player player = (Player) winner;

            map.setPlayer(position, player.getPlayerIndex());
            player.finishFight(loser.getExpirienceUponDeath());

            map.setTreasure(map.getRandomFreePosition());
            map.setMinion(map.getRandomFreePosition());
        }
    }

    private Player getPlayer(Command command) {
        return dungeon.getPlayer(command.playerIndex());
    }

    private static int getIndex(Command command, int indexOfArgument) {
        return Integer.parseInt(command.arguments().get(indexOfArgument)) - 1;
    }

    private static String getCannotExecuteCommandMessage(String command) {
        return "Cannot execute command " + command + " while in fight";
    }

    private static String formatAsMultiLineString(String... lines) {
        return MULTI_LINE_STRING + Arrays.stream(lines).collect(Collectors.joining(NEW_LINE_SEPARATOR));
    }

    private static String formatAsMultiLineString(List<String> lines) {
        return formatAsMultiLineString(lines.toArray(new String[0]));
    }

    private static String formatAsCommandToLeave(String message) {
        return CLIENT_CAN_LEAVE + message;
    }
}
