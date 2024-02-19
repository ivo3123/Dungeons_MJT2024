package bg.sofia.uni.fmi.mjt.dungeons.random;

import java.util.Random;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.initial.Fireball;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.initial.Snowball;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial.Dagger;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial.Greatsword;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial.VenomousKatana;

public class RandomTreasureGenerator implements TreasureGenerator {
    private static final int GREATSWORD = 0;
    private static final int VENOMOUS_KATANA = 1;
    private static final int DAGGER = 2;
    private static final int FIREBALL = 3;
    private static final int SNOWBALL = 4;

    private Random random;
    
    public RandomTreasureGenerator() {
        this.random = new Random();
    }

    @Override
    public AttackingSource generateTreasure(int playerLevel) {
        final int treasuresCount = 5;

        int randomNumberForItem = random.nextInt(0, treasuresCount);

        final int variance = 1;
        int randomNumberForLevel = random.nextInt(playerLevel, playerLevel + variance + 1);
        
        return switch (randomNumberForItem) {
            case GREATSWORD -> new Greatsword(randomNumberForLevel);
            case VENOMOUS_KATANA -> new VenomousKatana(randomNumberForLevel);
            case DAGGER -> new Dagger(randomNumberForLevel);
            case FIREBALL -> new Fireball(randomNumberForLevel);
            case SNOWBALL -> new Snowball(randomNumberForLevel);
            default -> throw new RuntimeException("Number of treasures exceeds the treasures provided");
        };
    }
}
