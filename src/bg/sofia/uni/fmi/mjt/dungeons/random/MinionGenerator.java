package bg.sofia.uni.fmi.mjt.dungeons.random;

import bg.sofia.uni.fmi.mjt.dungeons.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeons.actor.minion.Minion;

public interface MinionGenerator {
    Minion generateMinion(Actor enemy);
}
