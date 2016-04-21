package Network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static final String ADDRESS = "localhost";
	public static final int PORT_NUMBER = 4001;
	private static ServerSocket serverSocket;
	private static Socket socket;
	
	public static void main(String[] args){
		
		boolean isRunning = true;
		serverSocket = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		
		try {
			serverSocket = new ServerSocket(PORT_NUMBER);
			while(isRunning){
				socket = serverSocket.accept();
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
				
				Request request = (Request)ois.readObject();
				
				if(request.getCode() == RequestCode.CONNECT){
					oos.writeObject(request.getName() + "Has connected");
				}else if(request.getCode() == RequestCode.SEND_MESSAGE){
				}
				
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			isRunning = false;
		}
	}
}