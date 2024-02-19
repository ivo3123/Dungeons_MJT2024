package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSourceBase;

public abstract class Spell extends AttackingSourceBase {
    private int manaToCast;

    public Spell(int level, String name, Attack attack, int manaToCast) {
        super(level, name, attack);

        this.manaToCast = manaToCast;
    }

    public int getManaCost() {
        return manaToCast;
    }
}
