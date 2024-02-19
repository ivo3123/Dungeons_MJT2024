package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSourceBase;

public abstract class Weapon extends AttackingSourceBase {
    public Weapon(int level, String name, Attack attack) {
        super(level, name, attack);
    }
}
