package bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking;

public class Attack {
    // attacks towards the enemies
    private int damage;
    private final int hitsCount;
    private final int venom;

    // buffs towards self
    private final int strength;
    private final int regenaration;

    public static AttackBuilder builder() {
        return new AttackBuilder();
    }

    public int calculateDamage() {
        return (damage + strength) * hitsCount;
    }

    public int getDamage() {
        return damage;
    }

    public int getHitsCount() {
        return hitsCount;
    }

    public int getVenom() {
        return venom;
    }

    public int getStrength() {
        return strength;
    }

    public int getRegenaration() {
        return regenaration;
    }

    public Attack setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        if (strength > 0) {
            stringBuilder.append("Gain ").append(strength).append(" strength. ");
        }

        if (damage > 0) {
            stringBuilder.append("Deal ").append(damage);

            if (hitsCount > 1) {
                stringBuilder.append('x').append(hitsCount);
            }

            stringBuilder.append(" damage. ");
        }

        if (venom > 0) {
            stringBuilder.append("Apply ").append(venom).append(" venom");
        }

        if (regenaration > 0) {
            stringBuilder.append("Regenarate ").append(regenaration).append(" health");
        }

        return stringBuilder.toString();
    }

    private Attack(AttackBuilder builder) {
        this.damage = builder.damage;
        this.hitsCount = builder.hitsCount;
        this.strength = builder.strength;
        this.venom = builder.venom;
        this.regenaration = builder.regenaration;
    }

    // builder
    public static class AttackBuilder {
        private int damage = 0;
        private int hitsCount = 1;
        private int venom = 0;
        private int strength = 0;
        private int regenaration = 0;

        public AttackBuilder setDamage(int damage) {
            if (damage <= 0) {
                throw new IllegalArgumentException("Damage should be >= 1");
            }

            this.damage = damage;
            return this;
        }

        public AttackBuilder setHitsCount(int hitsCount) {
            if (hitsCount <= 0) {
                throw new IllegalArgumentException("Hits count should be >= 1");
            }

            this.hitsCount = hitsCount;
            return this;
        }

        public AttackBuilder setVenom(int venom) {
            if (venom <= 0) {
                throw new IllegalArgumentException("Venom should be >= 1");
            }

            this.venom = venom;
            return this;
        }

        public AttackBuilder setStrength(int strength) {
            if (strength <= 0) {
                throw new IllegalArgumentException("Strength should be >= 1");
            }

            this.strength = strength;
            return this;
        }

        public AttackBuilder setRegenaration(int regenaration) {
            if (regenaration <= 0) {
                throw new IllegalArgumentException("Regenaration should be >= 1");
            }

            this.regenaration = regenaration;
            return this;
        }

        public Attack build() {
            return new Attack(this);
        }
    }
}
