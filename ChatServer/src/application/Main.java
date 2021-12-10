package application;
	
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;


public class Main extends Application {
	
	public static ExecutorService threadPool;	//Client 접속시 여러개의 thread를 효율적으로 관리하는 라이브러리
	public static Vector<Client> clients = new Vector<Client>();	//접속한 Client들 관리
	
	ServerSocket serverSocket;
	
	//Server 구동, Client 연결 기다리는 메소드
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
			
		} catch(Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		//Client가 접속할 때까지  계속 기다리는 thread
		Runnable thread = new Runnable() {
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속]" + socket.getRemoteSocketAddress()+": " + Thread.currentThread().getName());
						/*for(Client client : Main.clients) {
							String clientNum = Integer.toString(clients.size());
							String showClientNum = "[현재 누적 접속 인원 : "+clientNum+" 명 ]\n";
							client.send(showClientNum);
						}*/
					} catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//Server 작동 중지 메소드, 전체 자원 할당 해제
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//serverSocket 닫기
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//threadPool 닫기
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//UI생성, 실직적으로 프로그램 동작시키는 메소드
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if (toggleButton.getText().equals("시작하기")) {
				startServer(IP,port);
				Platform.runLater(() -> {
					String msg = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String msg = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("시작하기");
				});
			}
		});
		
		Scene scene = new Scene(root, 400,400);
		primaryStage.setTitle("[소챗(Sochat)_서버ON/OFF]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
