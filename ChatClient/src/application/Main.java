package application;
	
import java.io.*;
import java.net.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class Main extends Application {
	//클라이언트 프로그램에서는 쓰레드 풀을 사용할 필요가 없기 때문에 기본적으로 Thread 클래스를 이용해서 쓰레드 모듈을 처리합니다.
	
	Socket socket;
	TextArea textArea;
	
	Integer clientNum=0;
	
	//Client 프로그램 동작 메소드
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					//clientNum = clientNum+1;
					socket = new Socket(IP, port);
					receive();
				} catch(Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	//Client 프로그램 종료 메소드
	public void stopClient() {
		try {
			if (socket != null && !socket.isClosed()) {
				clientNum = clientNum-1;
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Server로부터 메세지를 전달받는 메소드
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
	
	//Server로 메세지 전송하는 메소드
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
	
	//실제로 프로그램을 동작시키는 메소드
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("닉네임을 입력하세요: ");
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
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
		
		Button sendButton = new Button("보내기");
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText()+": "+input.getText()+"\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if (connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				startClient(IPText.getText(), port);
				Platform.runLater(() -> {
					String enter = "[ "+userName.getText()+" 님이 채팅방에 입장하셨습니다.]\n"
							+ "[현재 접속 인원 : "+clientNum+" 명 ]\n";
					textArea.appendText(enter);
					//send(enter);
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					String exit = "[ "+userName.getText()+" 님이 채팅방을 퇴장하셨습니다.]\n===================================\n"
							+ "[현재 접속 인원 : "+clientNum+" 명 ]\n";
					textArea.appendText(exit);
					//send(exit);
					//send(exit);
				});
				
				connectionButton.setText("접속하기");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[소챗(Sochat)ChatRoom1]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}
	
	
	//프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
