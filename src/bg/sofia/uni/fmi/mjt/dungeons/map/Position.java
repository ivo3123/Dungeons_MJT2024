package bg.sofia.uni.fmi.mjt.dungeons.map;

import bg.sofia.uni.fmi.mjt.dungeons.command.Direction;

public record Position(int row, int column) {
    public static Position of(Position position, String direction) {
        return switch (Direction.getDirection(direction)) {
            case Direction.UP -> new Position(position.row() - 1, position.column());
            case Direction.DOWN -> new Position(position.row() + 1, position.column());
            case Direction.LEFT -> new Position(position.row(), position.column() - 1);
            case Direction.RIGHT -> new Position(position.row(), position.column() + 1);
        };
    }

    public Position getNeighborPosition(String direction) {
        return Position.of(this, direction);
    }
}
