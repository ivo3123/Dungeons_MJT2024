package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;

public interface Actor {
    boolean attack(Attack attack, Actor enemy);

    boolean recieveAttack(Attack attack);

    void endTurn();

    boolean startTurn();

    int getTotalHp();

    int getRemainingHp();

    boolean isAlive();

    int getLevel();

    int getStrength();

    int getVenom();

    int getRegenaration();

    String getName();

    int getExpirienceUponDeath();
}
