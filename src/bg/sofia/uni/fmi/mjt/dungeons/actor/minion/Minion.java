package bg.sofia.uni.fmi.mjt.dungeons.actor.minion;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.ActorBase;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;

public abstract class Minion extends ActorBase {
    private final String name;

    public Minion(int hp, String name, Actor enemy) {
        super(hp, enemy.getLevel());

        this.name = name;
        this.enemy = enemy;
    }

    public abstract Attack getNextAttack();

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean startTurn() {
        boolean survived = super.startTurn();

        if (!survived) {
            return false;
        }

        attack(getNextAttack(), enemy);

        endTurn();

        enemy.startTurn();

        return true;
    }

    @Override
    public int getExpirienceUponDeath() {
        final int experienceUponDeath = 70;
        return experienceUponDeath;
    }
}
