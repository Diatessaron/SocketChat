package client;

public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    public ClientGuiController(){

    }

    public class GuiSocketThread extends SocketThread{
        @Override
        public void processIncomingMessage(String message){
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        public void informAboutAddingNewUser(String userName){
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        public void informAboutDeletingNewUser(String userName){
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        public void notifyConnectionStatusChanged(boolean clientConnected){
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

    public ClientGuiModel getModel(){
        return model;
    }
    public String getServerAddress(){
        return view.getServerAddress();
    }

    public int getServerPort(){
        return view.getServerPort();
    }

    public String getUserName(){
        return view.getUserName();
    }

    protected SocketThread getSocketThread(){
        return new GuiSocketThread();
    }

    @Override
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    public static void main(String[] args) {
        ClientGuiController clientGuiController = new ClientGuiController();
        clientGuiController.run();
    }
}