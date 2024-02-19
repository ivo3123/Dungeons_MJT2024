package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;

public class Fireball extends Spell {
    private static final int MANA_COST = 25;
    
    public Fireball(int level) {
        super(level, "Fireball", createAttack(level), MANA_COST);
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 8;
        final int damageMultiplier = 2;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .build();

        return leveledAttack;
    }
}
