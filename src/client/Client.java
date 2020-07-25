package client;

import mainPackage.Connection;
import mainPackage.ConsoleHelper;
import mainPackage.Message;
import mainPackage.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " has joined the chat");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " has left the chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST) connection.send(new Message(MessageType.USER_NAME, getUserName()));
                else if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType()==MessageType.TEXT) processIncomingMessage(message.getData());
                else if (message.getType()==MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                else if(message.getType()==MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        @Override
        public void run(){
            try (Socket socket = new Socket(getServerAddress(), getServerPort());) {
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Please, enter your address");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Please, enter your port");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("Please, enter your username");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e){
            clientConnected=false;
            ConsoleHelper.writeMessage("Message sending failed");
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e){
                ConsoleHelper.writeMessage("This thread was interrupted");
            }
            if(clientConnected) ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            while(clientConnected){
                String text = ConsoleHelper.readString();
                if(shouldSendTextFromConsole()) sendTextMessage(text);
                if(text.equals("exit")) break;
            }
        }
    }
}