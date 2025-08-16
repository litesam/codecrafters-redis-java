import com.redis.CommandHandler;
import com.redis.RespParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (clientSocket;
             var outputStream = clientSocket.getOutputStream();
             var inputStream = clientSocket.getInputStream()
        ) {
            RespParser parser = new RespParser(inputStream);
            CommandHandler commandHandler = new CommandHandler();
            while (true) {
                List<String> commandParts = parser.parseCommand();
                if (commandParts == null) {
                    System.out.println("Client disconnected.");
                    break;
                }
                System.out.println("Received command: " + commandParts);
                commandHandler.handleCommand(commandParts, outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
