import com.redis.CommandHandler;
import com.redis.DataStore;
import com.redis.RespParser;
import com.redis.config.ConfigStore;
import com.redis.config.InfoStore;
import com.redis.replication.ReplicaHandshake;
import com.redis.util.ArgsParser;
import com.redis.util.RdbParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        Optional<String> portArg = ArgsParser.ArgsConstants.PORT.apply(args);

        int port = Integer.parseInt(portArg.orElse("6379"));
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            Optional<String> dir = ArgsParser.ArgsConstants.DIR.apply(args);
            Optional<String> db = ArgsParser.ArgsConstants.DBFILENAME.apply(args);
            Optional<String> replicaOf = ArgsParser.ArgsConstants.REPLICAOF.apply(args);
            String dirValue = dir.orElse("/tmp/redis-files");
            String dbfileName = db.orElse("dump.rdb");

            ConfigStore configStore = new ConfigStore();
            configStore.set(ArgsParser.DIR.substring(2), dirValue);
            configStore.set(ArgsParser.DBFILENAME.substring(2), dbfileName);
            InfoStore infoStore = new InfoStore();
            if (replicaOf.isPresent() && !replicaOf.get().isBlank()) {
                infoStore.set("replication", "role", "slave");
                // Start replica handshake to the master in a virtual thread
                String replicaOfVal = replicaOf.get().trim();
                String[] parts = replicaOfVal.split("\\s+");
                if (parts.length >= 2) {
                    String masterHost = parts[0];
                    int masterPort = Integer.parseInt(parts[1]);
                    Thread.ofVirtual().start(() -> ReplicaHandshake.start(masterHost, masterPort, port));
                }
            } else {
                infoStore.set("replication", "role", "master");
            }            DataStore dataStore = new DataStore();
            RdbParser rdbParser;
            final var dbPath = Paths.get(dirValue, dbfileName);
            if (Files.exists(dbPath)) {
                try (final InputStream fis = new FileInputStream(dbPath.toFile())) {
                    rdbParser = new RdbParser(fis, dataStore);
                    rdbParser.load();
                }
            }
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                Thread.ofVirtual().start(() -> handleClient(clientSocket, dataStore, configStore, infoStore));
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket, DataStore dataStore, ConfigStore configStore, InfoStore infoStore) {
        try (clientSocket;
             var outputStream = clientSocket.getOutputStream();
             var inputStream = clientSocket.getInputStream()
        ) {
            RespParser parser = new RespParser(inputStream);
            CommandHandler commandHandler = new CommandHandler(dataStore, configStore, infoStore);
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
