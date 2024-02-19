package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import java.util.ArrayList;
import java.util.List;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.ActorBase;
import bg.sofia.uni.fmi.mjt.dungeons.exception.InventoryException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.LevelNotHeighEnoughException;
import bg.sofia.uni.fmi.mjt.dungeons.exception.NotEnoughManaException;
import bg.sofia.uni.fmi.mjt.dungeons.map.Position;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.AttackingSource;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.spell.Spell;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.Weapon;
import bg.sofia.uni.fmi.mjt.dungeons.mechanics.attacking.weapon.initial.Shortsword;

public class Player extends ActorBase {
    private static final int EXPERIENCE_PER_LEVEL = 50;
    private static final int HP_PER_LEVEL = 10;
    private static final int MANA_PER_LEVEL = 10;

    private final char playerIndex;
    private Position position;

    private int remainingMana;
    private int totalMana;

    private int experience = 0;

    private PlayerState playerState = PlayerState.NOT_IN_FIGHT;

    private Weapon equipedWeapon; 
    private List<Spell> equipedSpells;
    private final int equipedSpellsCapacity;

    private final Inventory<AttackingSource> backpack;

    public static PlayerBuilder builder(char playerIndex, Position position) {
        return new PlayerBuilder(playerIndex, position);
    }

    public void equipSpell(int indexOfBackpack, int indexOfSpell)
            throws LevelNotHeighEnoughException, InventoryException {
        Spell itemFromBackpack = (Spell) backpack.getItem(indexOfBackpack);

        if (itemFromBackpack.getLevel() > getLevel()) {
            throw new LevelNotHeighEnoughException("Level of spell to swap with from backpack is not heigh enough");
        }

        Spell spellToBeUnequipped = equipedSpells.get(indexOfSpell);
        equipedSpells.set(indexOfSpell, (Spell) backpack.changeItem(indexOfBackpack, spellToBeUnequipped));
    }

    public void equipSpell(int indexOfBackpack) throws LevelNotHeighEnoughException, InventoryException {
        Spell itemFromBackpack = (Spell) backpack.getItem(indexOfBackpack);

        if (itemFromBackpack.getLevel() > getLevel()) {
            throw new LevelNotHeighEnoughException("Level of spell to equip is not heigh enough");
        }

        equipedSpells.add((Spell) backpack.removeItem(indexOfBackpack));
    }

    public void euqipWeapon(int indexOfBackpack) throws LevelNotHeighEnoughException, InventoryException {
        Weapon itemFromBackpack = (Weapon) backpack.getItem(indexOfBackpack);

        if (itemFromBackpack.getLevel() > getLevel()) {
            throw new LevelNotHeighEnoughException("Level not heigh enough");
        }

        equipedWeapon = (Weapon) backpack.changeItem(indexOfBackpack, equipedWeapon);
    }

    public void finishFight(int expirienceToGain) {
        gainExperience(expirienceToGain);
        setPlayerState(PlayerState.NOT_IN_FIGHT);
        enemy = null;
        remainingHp = getTotalHp();
        remainingMana = getTotalMana();

        venom = regeneration = strength = 0;
    }

    public void useManaForSpell(Spell spell) throws NotEnoughManaException {
        int manaCost = spell.getManaCost();

        if (remainingMana < manaCost) {
            throw new NotEnoughManaException("Not enogh mana to cast " + spell);
        }

        remainingMana -= manaCost;
    }

    @Override
    public boolean startTurn() {
        remainingMana = getTotalMana();

        setPlayerState(PlayerState.READY_TO_SELECT_TYPE_OF_ATTACK);

        return super.startTurn();
    }

    @Override
    public void endTurn() {
        setPlayerState(PlayerState.WAITING_DURING_OPPONENTS_TURN);

        super.endTurn();
    }

    @Override
    public int getTotalHp() {
        return totalHp;
    }

    @Override
    public String getName() {
        return "Player " + playerIndex;
    }

    @Override
    public int getLevel() {
        return experience / EXPERIENCE_PER_LEVEL + 1;
    }

    @Override
    public int getExpirienceUponDeath() {
        final int experienceUponDeath = 70;
        return experienceUponDeath;
    }

    public char getPlayerIndex() {
        return playerIndex;
    }

    public Position getPosition() {
        return position;
    }

    public int getRemainingMana() {
        return remainingMana;
    }

    public int getTotalMana() {
        return totalMana;
    }

    public Inventory<AttackingSource> getBackpack() {
        return backpack;
    }

    public Weapon getWeapon() {
        return equipedWeapon;
    }

    public List<Spell> getSpells() {
        return equipedSpells;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public boolean isInFight() {
        return !playerState.equals(PlayerState.NOT_IN_FIGHT);
    }

    public Actor getEnemy() {
        return enemy;
    }

    public void setEnemy(Actor enemy) {
        this.enemy = enemy;
    }

    public int getExperience() {
        return experience % EXPERIENCE_PER_LEVEL;
    }

    public int getExperiencePerLevel() {
        return EXPERIENCE_PER_LEVEL;
    }

    public boolean areSpellsFull() {
        return equipedSpells.size() == equipedSpellsCapacity;
    }

    public void setPosition(Position newPosition) {
        position = newPosition;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int gainExperience(int experience) {
        int levelBeforeXp = getLevel();
        this.experience += experience;
        int levelAfterXp = getLevel();

        if (levelAfterXp != levelBeforeXp) {
            levelUp(levelAfterXp, levelBeforeXp);
        }

        return getLevel();
    }

    private void levelUp(int newLevel, int oldLevel) {
        while (newLevel > oldLevel) {
            totalHp += HP_PER_LEVEL;
            totalMana += MANA_PER_LEVEL;

            --newLevel;
        }

        remainingHp = totalHp;
        remainingMana = totalMana;
    }

    public void enterInFight() {
        this.remainingHp = getTotalHp();
        this.remainingMana = getTotalMana();
    }  

    private Player(PlayerBuilder builder) {
        super(builder.hp);
    
        this.playerIndex = builder.playerIndex;
        this.position = builder.position;
        this.totalMana = builder.mana;
        this.remainingMana = builder.mana;
        this.equipedWeapon = builder.equipedWeapon;
        this.equipedSpells = builder.equipedSpells;
        this.equipedSpellsCapacity = builder.equipedSpellsCapacity;
        this.backpack = builder.backpack;
    }

    // builder
    public static class PlayerBuilder {
        private static final int DEFAULT_HP = 50;
        private static final int DEFAULT_MANA = 50;
        private static final int DEFAULT_SPELLS_CAPACITY = 2;

        private char playerIndex;
        private Position position;

        private int hp = DEFAULT_HP;
        private int mana = DEFAULT_MANA;
    
        private Weapon equipedWeapon = new Shortsword(1); 
        private List<Spell> equipedSpells = new ArrayList<>();
        private int equipedSpellsCapacity = DEFAULT_SPELLS_CAPACITY;
    
        private Inventory<AttackingSource> backpack = new Backpack();

        public Player build() {
            return new Player(this);
        }

        public PlayerBuilder setHp(int hp) {
            this.hp = hp;
            return this;
        }

        public PlayerBuilder setMana(int mana) {
            this.mana = mana;
            return this;
        }

        public PlayerBuilder setInitialWeapon(Weapon weapon) {
            this.equipedWeapon = weapon;
            return this;
        }

        public PlayerBuilder setInitialSpells(List<Spell> spells) {
            this.equipedSpells = spells;
            return this;
        }

        public PlayerBuilder setSpellsCapacity(int capacity) {
            if (capacity < 0) {
                throw new IllegalArgumentException("Capacity should be a positive number");
            }

            this.equipedSpellsCapacity = capacity;
            return this;
        }

        public PlayerBuilder setBackpack(Inventory<AttackingSource> backpack) {
            this.backpack = backpack;
            return this;
        }

        private PlayerBuilder(char playerIndex, Position position) {
            this.playerIndex = playerIndex;
            this.position = position;
        }
    }
}
