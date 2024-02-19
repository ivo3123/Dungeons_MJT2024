package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking;

public interface AttackingSource {
    Attack getAttack();

    String getName();

    int getLevel();
}
