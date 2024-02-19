package bg.sofia.uni.fmi.mjt.dungeons.random;

import java.util.Random;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.Minion;
import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.NobleKnight;

public class RandomMinionGenerator implements MinionGenerator {
    private static final int NOBLE_KNIGHT = 0;

    private Random random;

    public RandomMinionGenerator() {
        this.random = new Random();
    }

    @Override
    public Minion generateMinion(Actor enemy) {
        final int minionsCount = 1;

        int randomNumberForItem = random.nextInt(0, minionsCount);
        
        return switch (randomNumberForItem) {
            case NOBLE_KNIGHT -> new NobleKnight(enemy);
            default -> throw new RuntimeException("Number of minions exceeds the minions provided");
        };
    }
}
