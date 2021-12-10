package application;
	
import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class Main extends Application {
	//Ŭ���̾�Ʈ ���α׷������� ������ Ǯ�� ����� �ʿ䰡 ���� ������ �⺻������ Thread Ŭ������ �̿��ؼ� ������ ����� ó���մϴ�.
	
	Socket socket;
	TextArea textArea;
	
	int ClientNum = 0;
	int port;
	
	//Client ���α׷� ���� �޼ҵ�
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				} catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[���� ���� ����]");
						Platform.exit();
					}
				}
			}
		};
		ClientNum+=1;
		thread.start();
	}
	
	//Client ���α׷� ���� �޼ҵ�
	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		ClientNum-=1;
	}
	
	//Server�κ��� �޼����� ���޹޴� �޼ҵ�
	public void receive() {
		while(true) {
			try {
				InputStream istream = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = istream.read(buffer);
				if (length == -1) throw new IOException();
				String msg = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(msg);
				});
			} catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	//Server�� �޼��� �����ϴ� �޼ҵ�
	public void send(String msg) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream ostream = socket.getOutputStream();
					byte[] buffer = msg.getBytes("UTF-8");
					ostream.write(buffer);
					ostream.flush();
				} catch(Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	//������ ���α׷��� ���۽�Ű�� �޼ҵ�
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("�г����� �Է��ϼ���: ");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9999");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName, IPText, portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
	
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText()+": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button sendButton = new Button("������");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText()+": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton = new Button("�����ϱ�");
		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("�����ϱ�")) {
				//�г��� �ִ��� Ȯ��
				
				//���� �õ�
				try {
					port = Integer.parseInt(portText.getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					String enter = "[ "+userName.getText()+" ���� ä�ù濡 �����ϼ̽��ϴ�.]\n";
					//String enter = "[ "+userName.getText()+" ���� ä�ù濡 �����ϼ̽��ϴ�.]\n"
					//		+ "[���� ���� �ο� : "+0+" �� ]\n";
					textArea.appendText(enter);
					//send(enter);
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					String exit = "[ "+userName.getText()+" ���� ä�ù��� �����ϼ̽��ϴ�.]\n===================================\n";
					//String exit = "[ "+userName.getText()+" ���� ä�ù��� �����ϼ̽��ϴ�.]\n===================================\n"
					//		+ "[���� ���� �ο� : "+0+" �� ]\n";
					textArea.appendText(exit);
					//send(exit);
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root, 350, 400);
		primaryStage.setTitle("[��ê(Sochat)ChatRoom]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}
	
	//���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}
