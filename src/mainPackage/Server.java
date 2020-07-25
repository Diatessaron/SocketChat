package mainPackage;

import client.BotClient;
import mainPackage.Connection;
import mainPackage.ConsoleHelper;
import mainPackage.Message;
import mainPackage.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Server has started");
            while (true){
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String, Connection> pair : connectionMap.entrySet()){
            try {
                Connection value = pair.getValue();
                value.send(message);
            } catch (IOException e){
                ConsoleHelper.writeMessage("Message was not sent");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket){
            this.socket=socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() == MessageType.USER_NAME) {
                    String name = message.getData();
                    if (name != null && !name.equals("") && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return name;
                    } else continue;
                } else continue;
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry<String, Connection> pair : connectionMap.entrySet()){
                String key = pair.getKey();
                if(!key.equals(userName)) connection.send(new Message(MessageType.USER_ADDED, key));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();
                String text = "";
                if (message.getType() == MessageType.TEXT) {
                    text = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, text));
                } else ConsoleHelper.writeMessage("Received message is not text");
            }
        }

        @Override
        public void run(){
            ConsoleHelper.writeMessage("Established a new connection: " + socket.getRemoteSocketAddress());
            String userName="";
            try (Connection connection = new Connection(socket);) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

        }
    }
}
