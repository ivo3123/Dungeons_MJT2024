package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;

public class Snowball extends Spell {
    private static final int MANA_COST = 10;
    
    public Snowball(int level) {
        super(level, "Snowball", createAttack(level), MANA_COST);
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 3;
        final int damageMultiplier = 1;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .build();

        return leveledAttack;
    }
}
