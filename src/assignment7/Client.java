package assignment7;

import java.io.*; 
import java.net.*;
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

public class Client extends Application { 
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;	
	
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					incoming.appendText(message + "\n");
				}
			} catch (IOException ex) {
				System.exit(0);
				//ex.printStackTrace();
			}
		}
	}

	@Override // Override the start method in the Application class 
	public void start(Stage primaryStage) throws Exception { 
		// Panel p to hold the label and text field 
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
		primaryStage.setTitle("Client"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 
		
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		outgoing.setOnAction(e -> { 
			String input = outgoing.getText().trim(); 
			writer.println(input);
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus(); 
		}); 
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
