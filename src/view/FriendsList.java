package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import network.Request;
import network.RequestCode;
import network.Response;
import network.ResponseCode;
import network.Server;

/**
 * 
 * @author Joshua Riccio
 *
 */
public class FriendsList extends JFrame{

	private static final long serialVersionUID = -2585106853637231791L;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private DefaultListModel<String> listmodel;
	private JList<String> list;
	private JScrollPane scrollpane;
	
	private ChatWindow chatwindow;
	private String username;
	
	/**
	 * Constructor for FriendsList
	 */
	public FriendsList(){
		setupLogin();
	}

	private void setupLogin() {
		this.setTitle("Net Chat");
		this.setSize(400, 300);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout());
		
		JTextField usernamefield = new JTextField();
		usernamefield.setPreferredSize(new Dimension(200, 25));
		this.add(usernamefield);
		
		JButton loginbtn = new JButton("Connect");
		this.add(loginbtn);
		loginbtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				FriendsList.this.remove(usernamefield);
				FriendsList.this.remove(loginbtn);
				setupWindow(usernamefield.getText());
				setupChatService();
				connectToServer();
			}
			
		});
	}

	private void setupChatService() {
		this.chatwindow = new ChatWindow(this.username, this.oos);
	}

	private void connectToServer() {
		Request request = new Request(RequestCode.CONNECT, this.username);
		try {
			socket = new Socket(Server.ADDRESS, Server.PORT_NUMBER);
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			oos.writeObject(request);
			Response response = (Response) ois.readObject();
			if (response.getCode() == ResponseCode.SUCCESS) {
				ServerListener serverlistener = new ServerListener();
				serverlistener.start();
				request = new Request(RequestCode.REQUEST_USERS_ONLINE);
				oos.writeObject(request);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private void setupWindow(String username) {
		this.setTitle("Net Chat");
		this.setSize(300, 700);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.username = username;
		
		this.listmodel = new DefaultListModel<String>();
		this.list = new JList<String>(listmodel);
		this.scrollpane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setPreferredSize(new Dimension(200, 600));
		setLayout(new BorderLayout());
		this.add(scrollpane, BorderLayout.CENTER);
		
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				Request request = new Request(RequestCode.EXITING);
				try {
					oos.writeObject(request);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
	}
	
	private class ServerListener extends Thread {
		private boolean isRunning = true;

		@Override
		public void run() {
			while (isRunning) {
				Response response;
				try {
					response = (Response) ois.readObject();
					if (response.getCode() == ResponseCode.USERS_LIST_SENT) {
						processUpdateUserList(response);
					}else if (response.getCode() == ResponseCode.NEW_USER_CONNECTED) {
						processNewUserConnected(response);
					}else if (response.getCode() == ResponseCode.NEW_MESSAGE) {
						processNewMessageRecieved(response);
					} else if (response.getCode() == ResponseCode.USER_DISCONNECTED) {
						processUserDisconnected(response);
					}
				} catch (ClassNotFoundException | IOException e) {
					isRunning = false;
					e.printStackTrace();
				}
			}
		}

		private void processUserDisconnected(Response response) {
			chatwindow.updateConversation(response.getName(), " has disconnected.");
			if(FriendsList.this.listmodel.contains(response.getName()))
				FriendsList.this.listmodel.removeElement(response.getName());
		}

		private void processNewMessageRecieved(Response response) {
			if(!chatwindow.isVisible())
				chatwindow.setVisible(true);
			chatwindow.updateConversation(response.getName(), ": " + response.getMessage());
		}

		private void processNewUserConnected(Response response) {
			FriendsList.this.listmodel.addElement(response.getName());
		}

		private void processUpdateUserList(Response response) {
			Vector<String> users = response.getUserList();
			for(String user : users){
				if(!FriendsList.this.listmodel.contains(user))
					FriendsList.this.listmodel.addElement(user);
			}
		}
	}

}