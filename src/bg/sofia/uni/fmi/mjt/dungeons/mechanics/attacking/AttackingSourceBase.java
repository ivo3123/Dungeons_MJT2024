package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking;

public abstract class AttackingSourceBase implements AttackingSource {
    private int level;
    private String name;
    private Attack attack;

    public AttackingSourceBase(int level, String name, Attack attack) {
        this.level = level;
        this.name = name;
        this.attack = attack;
    }

    @Override
    public String toString() {
        return name + " " + level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Attack getAttack() {
        return attack;
    }
}
