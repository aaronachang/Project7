/* Chatroom ClientMain.java
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

import java.io.*; 
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application; 
import javafx.geometry.Insets; 
import javafx.geometry.Pos; 
import javafx.scene.Scene; 
import javafx.scene.control.Label; 
import javafx.scene.control.ScrollPane; 
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField; 
import javafx.scene.layout.BorderPane; 
import javafx.stage.Stage; 

public class ClientMain extends Application {
	private final String IP_ADDRESS = "172.16.25.224";
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	private String sentString = "";
	private String name = "";
	private AtomicBoolean verified = new AtomicBoolean(false);
	private AtomicBoolean setUpPassword = new AtomicBoolean(false);
	private AtomicBoolean password = new AtomicBoolean(false);
	private AtomicBoolean key = new AtomicBoolean(false);
	
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if (!verified.get()) {
						synchronized(this){
							if (message.equals("Key locked")) {
								incoming.appendText("Incorrect password, please try again.");
								getUserInputs();
							} else if (message.equals("Key unlocked")) {
								writer.println(name + " has just joined the chat.");
								writer.flush();
								//outgoing.setText("");
								//outgoing.requestFocus();
								verified.set(true);
								key.set(false);
							} else if (message.length() > 27 && message.contains("Please enter the password.:")) {
								incoming.appendText(message.substring(0, message.indexOf(':')) + "\n");
								name = message.substring(message.indexOf(':') + 1, message.length());
								key.set(true);
								getUserInputs();
							} else if (message.length() > 8 && message.contains("Password:")) {
								name = message.substring(message.indexOf(':') + 1, message.length());
								setUpPassword.set(true);
								setUpPassword();
								verified.set(true);
							} else if (message.substring(0, 5).contains("Name:")){
								name = message.substring(5, message.length());
								writer.println(name + " has just joined the chat.");
								writer.flush();
//								outgoing.setText("");
//								outgoing.requestFocus();
								verified.set(true);
							} else if (message.equals("User already exists, please enter a different name.")){
								incoming.appendText(message + "\n");
								getUserInputs();
							}
						}
					} else {
						if (message.length() > 5 && (message.substring(0, 5).contains("Name:") 
								|| message.equals("Key locked")
								|| message.equals("key:")
								|| message.equals("Key unlocked")
								|| message.equals("Please enter the password.:")
								|| message.equals("Password:"))
								|| (message.length() > 27 && message.contains("Please enter the password.:"))) {} 
						else if (message.length() > 6 && message.charAt(message.indexOf(']') + 3) == '@') {
							synchronized(this) {
								if (message.substring(message.indexOf(']') + 4, message.indexOf(']') + 4 + name.length()).equals(name)) {
									String sender = message.substring(message.indexOf('['), message.indexOf(']') + 1);
									String pm = message.substring(message.indexOf(']') + 4 + name.length(), message.length());
									incoming.appendText("From " + sender + ":" + pm + "\n");
								} else if (message.equals(sentString)) {
									incoming.appendText(message + "\n");
								}
							} 
						} else {
							synchronized(this){
								incoming.appendText(message + "\n");
							}
						}
					 }
					sentString = "";
				}
			} catch (IOException ex) {
				//System.exit(0);
				ex.printStackTrace();
			}
		}
	}
	
	public void setUpPassword() {
		incoming.appendText("Please enter a password. (Enter \"N\" to not set up a password)\n");
		getUserInputs();
	}

	@Override // Override the start method in the Application class 
	public void start(Stage stage) throws Exception { 
		// Panel p to hold the label and text field 
		@SuppressWarnings("resource")
		Socket sock = new Socket(IP_ADDRESS, 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Chat: ")); 

		outgoing = new TextField(); 
		outgoing.setAlignment(Pos.BOTTOM_RIGHT); 
		paneForTextField.setCenter(outgoing); 

		BorderPane mainPane = new BorderPane(); 
		// Text area to display contents 
		incoming = new TextArea(); 
		mainPane.setCenter(new ScrollPane(incoming)); 
		mainPane.setTop(paneForTextField); 

		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 450, 200); 
		stage.setTitle("Client"); // Set the stage title 
		stage.setScene(scene); // Place the scene in the stage
		//this.stage = stage;
		stage.show(); // Display the stage 
		
		incoming.setText("Enter a username in the chat line above.\n");

		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		
		getUserInputs();
	}
	
	private void getUserInputs() {
		outgoing.setOnAction(e -> { 
			if (key.get()) {
				writer.println("key:" + outgoing.getText());
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			} else if (!verified.get()) {
				addUser();
			} else if (setUpPassword.get()) {
				String decision = outgoing.getText();
				if (decision.equals("N")) {
					writer.println(name + " has just joined the chat.");
				} else {
					password.set(true);
					writer.println("Password:" + outgoing.getText());
				}
				
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
				setUpPassword.set(false);
			}
			else {
				sentString = "[" + name + "]: " + outgoing.getText();
				writer.println(sentString);
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			}
		});
	}
	
	private synchronized void addUser() {
		String unverified = outgoing.getText();
		writer.println("*adding user* " + unverified);
		writer.flush();
		outgoing.setText("");
		outgoing.requestFocus();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
