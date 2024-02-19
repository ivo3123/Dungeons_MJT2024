package bg.sofia.uni.fmi.mjt.dungeons;

import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.IndexCreator;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.Player;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.PlayerState;
import bg.sofia.uni.fmi.mjt.dungeons.exception.MovementIntoObstacleException;
import bg.sofia.uni.fmi.mjt.dungeons.map.GameMap;
import bg.sofia.uni.fmi.mjt.dungeons.map.Position;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;
import bg.sofia.uni.fmi.mjt.dungeons.random.MinionGenerator;
import bg.sofia.uni.fmi.mjt.dungeons.random.TreasureGenerator;

public class Dungeon {
    private final GameMap map;
    private final Map<Character, Player> players;
    private final TreasureGenerator treasureGenerator;
    private final MinionGenerator minionGenerator;
    private final IndexCreator indexCreator;
    private final Map<Position, AttackingSource> droppedItems;
    
    public Dungeon(
        GameMap map,
        TreasureGenerator treasureGenerator,
        MinionGenerator minionGenerator,
        IndexCreator indexCreator
    ) {
        this.map = map;

        this.treasureGenerator = treasureGenerator;
        this.minionGenerator = minionGenerator;

        this.indexCreator = indexCreator;

        this.players = new HashMap<>();

        this.droppedItems = new HashMap<>();
    }

    public GameMap getMap() {
        return map;
    }

    public void addPlayer(char playerIndex) {
        Position position = map.addPlayer(playerIndex);

        Player newPlayer = Player.builder(playerIndex, position).build();

        players.put(playerIndex, newPlayer);
    }

    public void removePlayer(char playerIndex) {
        players.remove(playerIndex);
        map.removePlayer(playerIndex);
        indexCreator.addIndexForPlayer(playerIndex);
    }

    public Player getPlayer(char index) {
        return players.get(index);
    }

    public Map<Position, AttackingSource> getDroppedItems() {
        return droppedItems;
    }

    public void addDroppedItem(Position position, AttackingSource item) {
        droppedItems.put(position, item);
        map.setTreasure(position);
    }

    public String movePlayer(String direction, Player player) throws MovementIntoObstacleException {
        Position oldPosition = player.getPosition();
        Position newPosition = oldPosition.getNeighborPosition(direction);

        if (map.isWall(newPosition)) {
            throw new MovementIntoObstacleException("Cannot make a move into an obstacle");
        }
        StringBuilder result = new StringBuilder("You moved successfully");
        if (map.isFree(newPosition)) {
            map.updatePlayerMovement(player.getPlayerIndex(), newPosition, oldPosition);
        } else if (map.isTreasure(newPosition)) {
            String treasureMessage = playerObtainsTreasure(newPosition, player);
            result.append(treasureMessage);
            map.updatePlayerMovement(player.getPlayerIndex(), newPosition, oldPosition);
        } else if (map.isMinion(newPosition)) {
            String battleEnteredMessage = playerEntersBattle(player);
            result.append(battleEnteredMessage);
            map.updatePlayerMovementIntoBattle(newPosition, oldPosition);
        } else if (map.isPlayer(newPosition)) {
            String combatEnteredMessage = playerEntersCombat(newPosition, player);
            result.append(combatEnteredMessage);
            map.updatePlayerMovementIntoCombat(newPosition, oldPosition);
        } else {
            return "Cannot move there";
        }
        player.setPosition(newPosition);
        return result.toString();
    }

    private String playerObtainsTreasure(Position position, Player player) {
        AttackingSource droppedItem = this.droppedItems.get(position);

        AttackingSource result = droppedItem == null 
            ? this.treasureGenerator.generateTreasure(player.getLevel())
            : this.droppedItems.remove(position);
        
        boolean couldAddSuccessfully = player.getBackpack().addItem(result);

        if (couldAddSuccessfully) {
            final int experienceForNonDroppedTreasure = 15;
            final int experienceGained = droppedItem != null ? 0 : experienceForNonDroppedTreasure;
            int levelBeforeExperienceGain = player.getLevel();
            int levelAfterExperienceGain = player.gainExperience(experienceGained);

            String levelUp = levelBeforeExperienceGain == levelAfterExperienceGain
                ? ""
                : " and leveled to level " + levelAfterExperienceGain;

            String experienceGainedMessage = experienceGained == 0 
                ? ""
                : " and gained " + experienceGained + " experience" + levelUp;

            return " and found  a treasure - " + result.toString() + experienceGainedMessage;
        } else {
            return " but cannot obtain the treasure because your backpack is full";
        }
    }

    private String playerEntersBattle(Player player) {
        Minion minion = this.minionGenerator.generateMinion(player);

        player.setPlayerState(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);
        player.setEnemy(minion);
        player.enterInFight();

        return " and entered in a battle with " + minion + ". It's your turn";
    }

    private String playerEntersCombat(Position newPosition, Player player) {
        char otherPlayerIndex = this.map.getCell(newPosition);
        Player otherPlayer = this.getPlayer(otherPlayerIndex);

        player.setPlayerState(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);
        player.setEnemy(otherPlayer);
        player.enterInFight();

        otherPlayer.setPlayerState(PlayerState.WAITING_DURING_OPPONENTS_TURN);
        otherPlayer.setEnemy(player);
        otherPlayer.enterInFight();

        return " and entered in a combat with " + otherPlayer.getName() + ". It's your turn";
    }
}
