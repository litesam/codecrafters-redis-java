import com.redis.CommandHandler;
import com.redis.DataStore;
import com.redis.RespParser;
import com.redis.config.ConfigStore;
import com.redis.util.ArgsParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            Optional<String> dir = ArgsParser.ArgsConstants.DIR.apply(args);
            Optional<String> db = ArgsParser.ArgsConstants.DBFILENAME.apply(args);
            String dirValue = dir.orElse("/tmp/redis-files");
            String dbfileName = db.orElse("dump.rdb");
            ConfigStore configStore = new ConfigStore();
            configStore.set(ArgsParser.DIR.substring(2), dirValue);
            configStore.set(ArgsParser.DBFILENAME.substring(2), dbfileName);
            DataStore dataStore = new DataStore();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                Thread.ofVirtual().start(() -> handleClient(clientSocket, dataStore, configStore));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket, DataStore dataStore, ConfigStore configStore) {
        try (clientSocket;
             var outputStream = clientSocket.getOutputStream();
             var inputStream = clientSocket.getInputStream()
        ) {
            RespParser parser = new RespParser(inputStream);
            CommandHandler commandHandler = new CommandHandler(dataStore, configStore);
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
