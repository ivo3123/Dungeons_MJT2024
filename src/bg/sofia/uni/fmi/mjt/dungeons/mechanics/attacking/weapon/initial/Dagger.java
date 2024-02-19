package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class Dagger extends Weapon {
    public Dagger(int level) {
        super(level, "Dagger", createAttack(level));
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 3;
        final int hitsCount = 3;
        final int damageMultiplier = 2;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .setHitsCount(hitsCount)
            .build();

        return leveledAttack;
    }
}
