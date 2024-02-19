package bg.sofia.uni.fmi.mjt.dungeons.command;

import java.util.List;

public record Command(String command, char playerIndex, List<String> arguments) {

}
