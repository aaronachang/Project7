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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMain extends Observable {
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
						if (!userExists(user)) {
							saveUser(user);
							setChanged();
							notifyObservers("Name:" + user);
						} else {
							if (userExists(user)) {
								setChanged();
								notifyObservers("User already exists, please enter a different name.");
							}
						}
						message = "";
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
	private static AtomicInteger currentClient = new AtomicInteger(0);
	public static int getClient() {
		return currentClient.get();
	}
	public static void setClient(int client) {
		currentClient.set(client);
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