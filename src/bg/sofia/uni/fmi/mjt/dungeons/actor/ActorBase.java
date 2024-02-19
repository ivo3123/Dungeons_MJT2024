package bg.sofia.uni.fmi.mjt.dungeons.actor;

import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.Attack;

public abstract class ActorBase implements Actor {
    private int level = 1;

    protected int totalHp;
    protected int remainingHp;

    protected int venom = 0;
    protected int regeneration = 0;

    protected int strength = 0;

    protected Actor enemy = null;

    public ActorBase(int hp, int level) {
        this.totalHp = hp;
        this.remainingHp = hp;

        this.level = level;
    }

    public ActorBase(int hp) {
        this.totalHp = hp;
        this.remainingHp = hp;
    }

    @Override
    public boolean attack(Attack attack, Actor enemy) {
        this.strength += attack.getStrength();
        this.regeneration += attack.getRegenaration();

        return enemy.recieveAttack(attack);
    }

    @Override
    public boolean recieveAttack(Attack attack) {
        this.remainingHp -= attack.calculateDamage();
        this.venom += attack.getVenom();

        return isAlive();
    }

    @Override
    public void endTurn() {
        if (regeneration > 0) {
            remainingHp += regeneration;
            regeneration--;

            if (remainingHp > totalHp) {
                remainingHp = totalHp;
            }
        }
    }

    @Override
    public boolean startTurn() {
        if (venom > 0) {
            remainingHp -= venom;
            venom--;
        }

        return isAlive();
    }

    @Override
    public int getTotalHp() {
        return totalHp;
    }

    @Override
    public int getRemainingHp() {
        return remainingHp;
    }

    @Override
    public boolean isAlive() {
        return remainingHp > 0;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public int getVenom() {
        return venom;
    }

    @Override
    public int getRegenaration() {
        return regeneration;
    }
}
