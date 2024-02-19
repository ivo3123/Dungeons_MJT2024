package bg.sofia.uni.fmi.mjt.dungeons.actor.minion;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;

public class NobleKnight extends Minion {
    private static final int HP = 45;
    private static final int HP_MULTIPLIER = 10;
    private static final int INITIAL_DAMAGE = 4;
    
    public NobleKnight(Actor enemy) {
        super(HP + (enemy.getLevel() - 1) * HP_MULTIPLIER, "Noble knight", enemy);
    }

    @Override
    public Attack getNextAttack() {
        return Attack.builder()
            .setDamage(INITIAL_DAMAGE + getLevel() - 1 + strength)
            .setStrength(2)
            .build();
    }
}
