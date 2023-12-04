import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerWorker implements Runnable{
    private Socket clientSocket = null;

    private boolean nameSet;

    private LinkedList<ServerWorker> connectedUsers = null;

    private PrintWriter out;

    private BufferedReader in;

    private AsciiArt asciiArt;

    private String name = Thread.currentThread().getName();


    public ServerWorker(Socket clientSocket, LinkedList<ServerWorker> connectedUsers) {
        this.clientSocket = clientSocket;
        this.connectedUsers = connectedUsers;
        this.asciiArt = new AsciiArt();
    }

    public PrintWriter getOut() {
        return out;
    }

    public String getName() {
        return name;
    }

    
    public void sendAll(String input) {
        for (int i = 0; i < connectedUsers.size(); i++) {
            connectedUsers.get(i).getOut().write(input);
            connectedUsers.get(i).getOut().flush();
        }
    }

   public void setName(BufferedReader in) throws IOException{
       while (!nameSet) {
           String setNameMessage = "Welcome! Please set your name\n";
           out.write(setNameMessage);
           out.flush();
           String chosenName = in.readLine();
           Thread.currentThread().setName(chosenName);
           name = Thread.currentThread().getName();
           String welcomeMessage = name + " has joined the chat!\n";
           System.out.println(name + " set name as " + name);
           System.out.println(welcomeMessage);
           sendAll(welcomeMessage);
           nameSet = true;
       }
   }

   public void setUpStreams() throws IOException{
       System.out.println("Connected a user, created " + Thread.currentThread().getName());
       in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
       out = new PrintWriter(clientSocket.getOutputStream());
   }

   public void quit() {
       String quitMessage = name + " has quit the chat.\n";
       int listPos = -1;
       for (int i = 0; i < connectedUsers.size(); i++) {
           if (connectedUsers.get(i) == this) {
               listPos = i;
               break;
           }
       }
       sendAll(quitMessage);
       connectedUsers.remove(listPos);
       System.out.println(quitMessage);
   }

   public void whisper(String input) {
       String defaultMessage = "Invalid usage of whisper. Please use: whisper (user) Followed by your message.\n";
       if (input.toLowerCase().startsWith("/whisper (")) {
           String targetUser = input.substring(input.indexOf("(") + 1, input.indexOf(")"));
           System.out.println("target user " + targetUser);
           defaultMessage = "User not found.\n";
           for (int i = 0; i < connectedUsers.size(); i++) {
               if (connectedUsers.get(i).getName().equals(targetUser)) {
                   defaultMessage = "Successfully sent message to " + targetUser + ".\n";
                   connectedUsers.get(i).getOut().write("Whisper from " + name + ": " + input.substring(input.indexOf(")") + 1) + "\n");
                   connectedUsers.get(i).getOut().flush();
                   break;
               }
           }

       }
       out.write(defaultMessage);
       out.flush();
   }

    @Override
    public void run() {
        try{
            setUpStreams();

           setName(in);

            while (clientSocket.isConnected()) {
               //Reading the first line
                String read = in.readLine();

                //Checking if the user wants to quit
                if (read.toLowerCase().equals("/quit") || read.equals(null)) {
                  quit();
                  break;
                }
                //If the user does not want to quit, defining their text as a string that will be ready to send to all users
                String chat = name + " said: " + read + "\n";
                System.out.println(chat);

               //Looking for commands

                if (read.toLowerCase().startsWith("/whisper")) {
                   whisper(read);
                   continue;
                }



                if (read.toLowerCase().equals("/rat")) {
                   chat = asciiArt.getRat();

                }

                if (read.toLowerCase().equals("/heart")) {
                   chat = asciiArt.getHeart();

                }

                if (read.toLowerCase().contains("among us") || read.toLowerCase().contains("imposter")) {
                  chat = asciiArt.getSus();
                }

                //Sends input to all connected users
                sendAll(chat);
            }
            in.close();
            out.close();
            clientSocket.close();

        }


        catch (NullPointerException e) {
            String quitMessage = name + " has quit the chat.\n";
            int listPos = -1;
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (connectedUsers.get(i) == this) {
                    listPos = i;
                    break;
                }
            }
            sendAll(quitMessage);
            connectedUsers.remove(listPos);
            System.out.println(quitMessage + " Quit due to nullPointerException Protocol\n");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}
