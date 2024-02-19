package bg.sofia.uni.fmi.mjt.dungeons.architecture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import bg.sofia.uni.fmi.mjt.dungeons.actor.player.IndexCreator;
import bg.sofia.uni.fmi.mjt.dungeons.command.CommandDecoder;

public class Client {
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_HOST = "localhost";

    private char playerIndex;

    private final int port;
    private final String host;

    public static void main(String... args) {
        Client client = new Client();
        client.start();
    }

    public Client() {
        this.port = SERVER_PORT;
        this.host = SERVER_HOST;
    }

    public void start() {
        try (
            SocketChannel socketChannel = SocketChannel.open();
            BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in);
        ) {
            socketChannel.connect(new InetSocketAddress(host, port));

            recieveUniqueIndex(socketChannel);

            if (IndexCreator.isValidIndex(this.playerIndex)) {
                System.out.println("You entered the game as player " + this.playerIndex);
            } else {
                System.out.println("The server is full. Please try again later!");
                return;
            }

            executeEventLoop(scanner, writer, reader);
        } catch (IOException e) {
            System.err.println("Looks like the server is down. Please try again later!");
        }
    }

    private void recieveUniqueIndex(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);

        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        char playerIndex = (char) byteBuffer.get();

        this.playerIndex = playerIndex;
    }

    private void executeEventLoop(Scanner scanner, PrintWriter writer, BufferedReader reader) throws IOException {
        while (true) {
            System.out.print("=> ");
            String commandOfClient = scanner.nextLine();  // read a line from the console

            writer.println(this.playerIndex + commandOfClient);
                // sends the command to the server
                // and the index of the player who made the command

            String replyFromServer = reader.readLine();  // read the response from the server

            boolean shouldExitLoop = printResponse(replyFromServer);

            if (shouldExitLoop) {
                break;
            }
        }
    }

    private boolean printResponse(String replyFromServer) {
        if (CommandDecoder.isEncodedMap(replyFromServer)) {
            System.out.println(CommandDecoder.decodeEncodedMap(replyFromServer));
        } else if (CommandDecoder.isRequestToLeave(replyFromServer)) {
            System.out.println(CommandDecoder.decodeRequestToLeave(replyFromServer));
            return true;
        } else if (CommandDecoder.isMultiLineResponse(replyFromServer)) {
            System.out.println(CommandDecoder.decodeMultiLineResponse(replyFromServer));
        } else {
            System.out.println(replyFromServer);
        }

        return false;
    }
}