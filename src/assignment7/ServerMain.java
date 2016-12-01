/* Chatroom ServerMain.java
 * EE422C Project 7 submission by
 * Aaron Chang
 * AAC3434
 * 16475
 * Siva Manda
 * SM48525
 * 16480
 * Slip days used: 1
 * Git URL: https://github.com/aaronachang/Project7
 * Fall 2016
 */

package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMain extends Observable {
	private AtomicBoolean password = new AtomicBoolean(false);
	private String key = "";
	
	public static void main(String[] args) {
		try {
			new ServerMain().setUpNetworking();
		} catch (Exception e) {
			//System.exit(0);
			e.printStackTrace();
		}
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
			Thread t = new Thread(new ClientHandler(clientSocket));

			t.start();
			this.addObserver(writer);
			System.out.println("got a connection");
		}
	}
	class ClientHandler implements Runnable {
		private BufferedReader reader;

		public ClientHandler(Socket clientSocket) {
			Socket sock = clientSocket;
			try {
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if (message.length() > 13 && message.substring(0, 14).contains("*adding user*")) {
						String user = message.substring(14, message.length());
						if (password.get()) {
							setChanged();
							notifyObservers("Please enter the password.:" + user);
						} else if (!userExists(user)) {
							saveUser(user);
							if (users.size() == 1) {
								setChanged();
								notifyObservers("Password:" + user);
							} else {
								setChanged();
								notifyObservers("Name:" + user);
							}
						} else {
							if (userExists(user)) {
								setChanged();
								notifyObservers("User already exists, please enter a different name.");
							}
						}
						message = "";
					} else if (message.length() > 4 && message.substring(0, 4).contains("key:")) {
						System.out.println("trying");
						if (message.substring(4, message.length()).equals(key)) {
							System.out.println("unlocked");
							setChanged();
							notifyObservers("Key unlocked");
						} else {
							System.out.println("locked");
							setChanged();
							notifyObservers("Key locked");
						}
					} else if (message.length() > 8 && message.substring(0, 8).contains("Password")) {
						password.set(true);
						key = message.substring(message.indexOf(':') + 1, message.length());
					} else {
						setChanged();
						notifyObservers(message);
					}
				}
			} catch (IOException e) {
				//System.exit(0);
				e.printStackTrace();
			}
		}
	}

	private static List<String> users = Collections.synchronizedList(new ArrayList<String>());
	public static void saveUser(String user) {
		synchronized(users) {
			users.add(user);
		}
	}
	public static synchronized boolean userExists(String user) {
		return users.contains(user);
	}
}