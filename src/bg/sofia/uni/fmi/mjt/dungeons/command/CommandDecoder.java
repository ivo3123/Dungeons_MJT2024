package bg.sofia.uni.fmi.mjt.dungeons.command;

import bg.sofia.uni.fmi.mjt.dungeons.map.GameMap;

public final class CommandDecoder {
    private CommandDecoder() {

    }

    public static boolean isEncodedMap(String reply) {
        return reply.startsWith(GameMap.ENCODED_PREFIX);
    }

    public static String decodeEncodedMap(String encodedMap) {
        encodedMap = encodedMap.substring(GameMap.ENCODED_PREFIX.length());
        String[] tokens = encodedMap.split(GameMap.SEPARATOR);
        int columns = Integer.parseInt(tokens[1]);
        String mapString = tokens[2];

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < mapString.length(); i++) {
            if ((i + 1) % columns == 0) {
                stringBuilder.append(mapString.charAt(i)).append(System.lineSeparator());
            } else {
                stringBuilder.append(mapString.charAt(i)).append(' ');
            }
        }

        String mapWithLastSpace = stringBuilder.toString();

        return mapWithLastSpace.substring(0, mapWithLastSpace.length() - 1);
    }

    public static boolean isRequestToLeave(String reply) {
        return reply.startsWith(CommandExecutor.CLIENT_CAN_LEAVE);
    }

    public static String decodeRequestToLeave(String reply) {
        reply = reply.substring(CommandExecutor.CLIENT_CAN_LEAVE.length());

        return reply;
    }

    public static boolean isMultiLineResponse(String reply) {
        return reply.startsWith(CommandExecutor.MULTI_LINE_STRING);
    }

    public static String decodeMultiLineResponse(String reply) {
        reply = reply
            .substring(CommandExecutor.MULTI_LINE_STRING.length())
            .replace(CommandExecutor.NEW_LINE_SEPARATOR, System.lineSeparator());

        return reply;
    }
}
