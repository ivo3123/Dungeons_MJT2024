package bg.sofia.uni.fmi.mjt.dungeons.architecture;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import bg.sofia.uni.fmi.mjt.dungeons.Dungeon;
import bg.sofia.uni.fmi.mjt.dungeons.actor.player.IndexCreator;
import bg.sofia.uni.fmi.mjt.dungeons.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.dungeons.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.dungeons.exception.ExceptionHandler;
import bg.sofia.uni.fmi.mjt.dungeons.map.GameMap;
import bg.sofia.uni.fmi.mjt.dungeons.random.MinionGenerator;
import bg.sofia.uni.fmi.mjt.dungeons.random.RandomMinionGenerator;
import bg.sofia.uni.fmi.mjt.dungeons.random.RandomTreasureGenerator;
import bg.sofia.uni.fmi.mjt.dungeons.random.TreasureGenerator;

public class Server {
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private static final String CONNECTION_ERROR = "~";

    private final int port;
    private final String host;

    private final CommandExecutor commandExecutor;
    private final IndexCreator indexCreator;

    private boolean isServerWorking = true;

    private Selector selector;
    private ByteBuffer buffer;

    public Server(CommandExecutor commandExecutor, IndexCreator indexCreator) {
        this.commandExecutor = commandExecutor;
        this.indexCreator = indexCreator;

        this.port = SERVER_PORT;
        this.host = SERVER_HOST;
    }

    public static void main(String... args) {
        try (Reader reader = new FileReader("resources" + File.separator + "mapScheme.txt")) {
            final int rows = 12;
            final int columns = 12;
            GameMap map = new GameMap(reader, rows, columns);

            TreasureGenerator treasureGenerator = new RandomTreasureGenerator();
            MinionGenerator minionGenerator = new RandomMinionGenerator();

            IndexCreator indexCreator = new IndexCreator();

            Dungeon dungeon = new Dungeon(map, treasureGenerator, minionGenerator, indexCreator);
            ExceptionHandler exceptionHandler = new ExceptionHandler();

            CommandExecutor commandExecutor = new CommandExecutor(dungeon, exceptionHandler);

            Server server = new Server(commandExecutor, indexCreator);

            server.start();
        } catch (IOException e) {
            ExceptionHandler exceptionHandler = new ExceptionHandler();
            exceptionHandler.handleException(e, "server");
        }
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            initialize(serverSocketChannel);

            while (isServerWorking) {
                int readyChannels = selector.select();

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                iterateKeys(keyIterator);
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void initialize(ServerSocketChannel serverSocketChannel) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    private void iterateKeys(Iterator<SelectionKey> keyIterator) throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (key.isReadable()) {
                boolean shouldContinue = read(key);

                if (shouldContinue) {
                    continue;
                }
            } else if (key.isAcceptable()) {
                accept(key);
            }

            keyIterator.remove();
        }
    }

    private boolean read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead < 0) {
            socketChannel.close();
            return true;
        }

        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        String clientInput = new String(clientInputBytes, StandardCharsets.UTF_8);
        clientInput = clientInput.substring(0, clientInput.length() - 1);
        String output = commandExecutor.execute(CommandCreator.newCommand(clientInput)) + System.lineSeparator();
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        return false;
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);

        Character index = indexCreator.getIndexForPlayer();

        if (index == null) {
            accept.write(ByteBuffer.wrap(CONNECTION_ERROR.getBytes(StandardCharsets.UTF_8)));
            return;
        }

        accept.write(ByteBuffer.wrap(String.valueOf(index).getBytes(StandardCharsets.UTF_8)));

        accept.register(selector, SelectionKey.OP_READ);

        commandExecutor.addPlayer(index);
    }
}