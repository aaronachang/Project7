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
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	private String sentString = "";
	private String name = "";
	private AtomicBoolean initial = new AtomicBoolean(true);
	
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					if (message.charAt(message.indexOf('>') + 2) == '@') {
						synchronized(this) {
							if (message.substring(message.indexOf('>') + 3, message.indexOf('>') + 3 + name.length()).equals(name)) {
								String sender = message.substring(message.indexOf('<'), message.indexOf('>') + 1);
								String pm = message.substring(message.indexOf('>') + 2, message.length());
								incoming.appendText("From " + sender + ": " + pm + "\n");
							} else if (message.equals(sentString)) {
								incoming.appendText(message + "\n");
							}
						}
					} else {
						synchronized(this){
							incoming.appendText(message + "\n");
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

	@Override // Override the start method in the Application class 
	public void start(Stage stage) throws Exception { 
		// Panel p to hold the label and text field 
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		
		/*
		 * Implementation of other UI Features here
		 * Possibilities: Login with Username Password
		 * Maybe leave button to exit chat room 
		 * Multicoloring of different chat text
		 * 
		 * Pseudocode for user login:
		 * Ideally want a login window to display first before chat window
		 * Prompt Username, Passwoord to create account (skip if already have a login)
		 * sign in then to chat and chat window displayed
		 * 
		 * Make grid or another pane to have text boxes
		 * Implement similar code below but logic should be 
		 * Make sign in
		 * if (signed in = true)
		 * 		then display chat window is crednetials correct
		 * 
		 * Code to get login:
		 * Label userName = new Label("User Name:");
		 * grid.add(userName);
		 * Array to store username for each client
		 * 
		 * TextField userTextField = new TextField();
		 * grid.add(userTextField);
		 * 
		 * Label pass_word = new Label("Password");
		 * grid.add(pass_word);
		 * Another array to store password for each client
		 * index should be client number (so username and password match)
		 * 
		 * PasswordField pwBox = new PasswordField();
		 * grid.add(pwBox);
		 * 
		 * Have sign in button
		 * Button btn - new Button("Sign in");
		 * adjust button position to preferred location
		 * 
		 * action to verify login and allow access to chat
		 */
		
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
		
		incoming.setText("Enter a Username in the chat line above.\n");

		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		outgoing.setOnAction(e -> { 
			if (initial.getAndSet(false)) {
				name = outgoing.getText();
				sentString = name + " just joined the chat room.";
			} else {
				sentString = "[" + name + "]: "+ outgoing.getText();
			}
			writer.println(sentString);
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		}); 
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
