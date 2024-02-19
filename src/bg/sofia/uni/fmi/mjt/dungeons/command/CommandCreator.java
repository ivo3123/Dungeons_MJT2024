package bg.sofia.uni.fmi.mjt.dungeons.command;

import java.util.Arrays;
import java.util.List;

import bg.sofia.uni.fmi.mjt.dungeons.actor.player.IndexCreator;

public class CommandCreator {
    public static Command newCommand(String clientInput) {
        if (clientInput.isEmpty()) {
            throw new IllegalArgumentException("Should not be empty");
        }

        char playerIndex = clientInput.charAt(0);

        String clientInputWithoutIndex = clientInput.substring(1);

        if (!IndexCreator.isValidIndex(playerIndex)) {
            throw new IllegalArgumentException("Should be valid index");
        }

        String[] tokens = clientInputWithoutIndex.split(" ");
        String command = tokens.length > 0 ? tokens[0] : "";
        List<String> arguments = Arrays.stream(tokens)
            .skip(1)
            .toList();

        return new Command(command, playerIndex, arguments);
    }
}
