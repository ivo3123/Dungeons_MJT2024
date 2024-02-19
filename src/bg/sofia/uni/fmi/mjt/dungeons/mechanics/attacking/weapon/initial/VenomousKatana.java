package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;

public class VenomousKatana extends Weapon {
    public VenomousKatana(int level) {
        super(level, "Venomous Katana", createAttack(level));
    }

    private static Attack createAttack(int level) {
        final int initialDamage = 5;
        final int initialVenom = 3;
        final int damageMultiplier = 2;
        final int venomMultiplier = 1;

        Attack leveledAttack = Attack.builder()
            .setDamage(initialDamage + (level - 1) * damageMultiplier)
            .setVenom(initialVenom + (level - 1) * venomMultiplier)
            .build();

        return leveledAttack;
    }
}
