package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

public enum PlayerState {
    NOT_IN_FIGHT,
    READY_TO_SELECT_TYPE_OF_ATTACK,
    READY_TO_ATTACK_WITH_SPELLS,
    READY_TO_ATTACK_WITH_WEAPON,
    ALREADY_ATTACKED_WITH_WEAPON,
    WAITING_DURING_OPPONENTS_TURN;

    public String getState() {
        return switch (this) {
            case NOT_IN_FIGHT -> "You are not in a fight";
            case READY_TO_SELECT_TYPE_OF_ATTACK -> "You can select how to attack by selecting weapon OR spells";
            case READY_TO_ATTACK_WITH_SPELLS -> "You have selected spells. Cast spells until you no longer have mana";
            case READY_TO_ATTACK_WITH_WEAPON -> "You have selected weapon. You can attack once with your weapon";
            case ALREADY_ATTACKED_WITH_WEAPON -> "You have already attacked with your weapon";
            case WAITING_DURING_OPPONENTS_TURN -> "It's the opponent's turn";
        };
    }
}
