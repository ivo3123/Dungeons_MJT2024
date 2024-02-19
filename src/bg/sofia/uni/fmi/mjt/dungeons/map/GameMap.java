package bg.sofia.uni.fmi.mjt.dungeons.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bg.sofia.uni.fmi.mjt.dungeons.command.Direction;

public class GameMap {
    public static final String ENCODED_PREFIX = "COMMAND";
    public static final String SEPARATOR = "SPLIT";

    private static final char WALL = '#';
    private static final char FREE = '_';
    private static final char TREASURE = 'T';
    private static final char MINION = 'M';
    private static final char COMBAT = 'C';
    private static final char BATTLE = 'B';

    private List<List<Character>> data;
    private final int rows;
    private final int columns;

    public GameMap(Reader reader, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        initializeData();

        applyMapScheme(reader);
    }

    public int getRowsCount() {
        return rows;
    }

    public int getColumnsCount() {
        return columns;
    }

    public boolean isFree(Position position) {
        return getCell(position) == FREE;
    }

    public boolean isFree(char ch) {
        return ch == FREE;
    }

    public boolean isPlayer(Position position) {
        return Character.isDigit(getCell(position));
    }

    public boolean isPlayer(char ch) {
        return Character.isDigit(ch);
    }

    public boolean isWall(Position position) {
        return getCell(position) == WALL;
    }

    public boolean isTreasure(Position position) {
        return getCell(position) == TREASURE;
    }

    public boolean isMinion(Position position) {
        return getCell(position) == MINION;
    }

    public boolean isCombat(Position position) {
        return getCell(position) == COMBAT;
    }

    public boolean isBattle(Position position) {
        return getCell(position) == BATTLE;
    }

    public void setTreasure(Position position) {
        setCell(position, TREASURE);
    }

    public void setBattle(Position position) {
        setCell(position, BATTLE);
    }

    public void setPlayer(Position position, char playerIndex) {
        setCell(position, playerIndex);
    }

    public void setMinion(Position position) {
        setCell(position, MINION);
    }

    public Position addPlayer(char playerIndex) {
        Position position = getRandomFreePosition();
        setCell(position, playerIndex);

        return position;
    }

    public void removePlayer(char playerIndex) {
        for (int i = 0; i < getRowsCount(); i++) {
            for (int j = 0; j < getColumnsCount(); j++) {
                Position current = new Position(i, j);

                if (getCell(current) == playerIndex) {
                    setCell(current, FREE);
                    return;
                }
            }
        }
    }

    public void updatePlayerMovement(char playerIndex, Position newPosition, Position oldPosition) {
        setCell(newPosition, playerIndex);
        setCell(oldPosition, FREE);
    }

    public void updatePlayerMovementIntoBattle(Position newPosition, Position oldPosition) {
        setCell(newPosition, BATTLE);
        setCell(oldPosition, FREE);
    }

    public void updatePlayerMovementIntoCombat(Position newPosition, Position oldPosition) {
        setCell(newPosition, COMBAT);
        setCell(oldPosition, FREE);
    }

    public char getCellAfterMovement(Position position, String direction) {
        if (!Direction.isValid(direction)) {
            throw new IllegalArgumentException("Invalid direction");
        }

        return getCell(Position.of(position, direction));
    }

    public Position getRandomFreePosition() {
        List<Position> freePositions = new ArrayList<>();

        for (int row = 0; row < getRowsCount(); row++) {
            for (int column = 0; column < getColumnsCount(); column++) {
                Position position = new Position(row, column);

                if (isFree(position)) {
                    freePositions.add(position);
                }
            }
        }

        Collections.shuffle(freePositions);

        return freePositions.getFirst();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(ENCODED_PREFIX)
            .append(rows)
            .append(SEPARATOR)
            .append(columns)
            .append(SEPARATOR);

        for (List<Character> line : data) {
            for (char ch : line) {
                stringBuilder.append(ch);
            }
        }
        
        return stringBuilder.toString();
    }

    public char getCell(Position position) {
        return data.get(position.row()).get(position.column());
    }

    private void setCell(Position position, char element) {
        data.get(position.row()).set(position.column(), element);
    }

    private void applyMapScheme(Reader mapSchemeReader) {
        try (BufferedReader reader = new BufferedReader(mapSchemeReader)) {
            for (int counter = 0; true; counter++) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                data.set(counter, convertToList(line.toCharArray()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error when opening map scheme file", e);
        }
    }

    private List<Character> convertToList(char[] arr) {
        List<Character> list = new ArrayList<>();

        for (char ch : arr) {
            list.add(ch);
        }

        return list;
    }

    private void initializeData() {
        data = new ArrayList<>();

        for (int row = 0; row < getRowsCount(); row++) {
            List<Character> rowList = new ArrayList<>();

            for (int column = 0; column < getColumnsCount(); column++) {
                rowList.add(FREE);
            }

            data.add(rowList);
        }
    }
}