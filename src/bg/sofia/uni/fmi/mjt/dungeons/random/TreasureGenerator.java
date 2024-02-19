package bg.sofia.uni.fmi.mjt.dungeons.random;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;

public interface TreasureGenerator {
    AttackingSource generateTreasure(int playerLevel);
}
