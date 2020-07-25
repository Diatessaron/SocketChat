package client;

import mainPackage.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client{

    public class BotSocketThread extends Client.SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            if(message == null) return;
            if(!message.contains(": ")) return;

            String [] array = message.split(": ");
            String name = array[0];
            String content = array[1];

            SimpleDateFormat simpleDateFormat = null;

            if(content.equals("дата")){
                simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
            }
            else if(content.equals("день")){
                simpleDateFormat= new SimpleDateFormat("d");
            }
            else if(content.equals("месяц")){
                simpleDateFormat = new SimpleDateFormat("MMMM");
            }
            else if(content.equals("год")){
                simpleDateFormat = new SimpleDateFormat("YYYY");
            }
            else if(content.equals("время")){
                simpleDateFormat = new SimpleDateFormat("H:mm:ss");
            }
            else if(content.equals("час")){
                simpleDateFormat = new SimpleDateFormat("H");
            }
            else if(content.equals("минуты")){
                simpleDateFormat = new SimpleDateFormat("m");
            }
            else if(content.equals("секунды")){
                simpleDateFormat = new SimpleDateFormat("s");
            }
            if (simpleDateFormat != null) {
                String result = "Информация для " + name + ": " + simpleDateFormat.format(Calendar.getInstance().getTime());
                sendTextMessage(result);
            }
        }
    }

    @Override
    protected SocketThread getSocketThread(){
        BotSocketThread botSocketThread = new BotSocketThread();
        return botSocketThread;
    }

    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        return "date_bot_" + (int) (Math.random()*100);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
