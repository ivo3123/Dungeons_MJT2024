package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class Greatsword extends Weapon {
    public Greatsword(int level) {
        super(level, "Greatsword", createAttack(level));
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 16;
        final int damageMultiplier = 4;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .build();

        return leveledAttack;
    }
}
