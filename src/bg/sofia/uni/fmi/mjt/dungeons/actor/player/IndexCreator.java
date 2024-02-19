package bg.sofia.uni.fmi.mjt.dungeons.actor.player;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class IndexCreator {
    private static final Set<Character> INDEXES;

    private Queue<Character> playersToAdd;

    static {
        INDEXES = new HashSet<>();

        final char from = '1';
        final char to = '9';

        for (char i = from; i <= to; i += 1) {
            INDEXES.add(i);
        }
    }

    public static boolean isValidIndex(char index) {
        return INDEXES.contains(index);
    }

    public IndexCreator() {
        playersToAdd = new PriorityQueue<>();
        
        for (char index : INDEXES) {
            playersToAdd.add(index);
        }
    }

    public Character getIndexForPlayer() {
        return playersToAdd.poll();
    }

    public void addIndexForPlayer(char index) {
        playersToAdd.add(index);
    }
}
