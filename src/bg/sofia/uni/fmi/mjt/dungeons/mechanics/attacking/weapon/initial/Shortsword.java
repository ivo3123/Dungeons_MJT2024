package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class Shortsword extends Weapon {
    public Shortsword(int level) {
        super(level, "Shortsword", createAttack(level));
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 10;
        final int damageMultiplier = 3;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .build();

        return leveledAttack;
    }
}
