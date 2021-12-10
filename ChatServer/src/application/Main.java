package application;
	
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.*;


public class Main extends Application {
	
	public static ExecutorService threadPool;	//Client ���ӽ� �������� thread�� ȿ�������� �����ϴ� ���̺귯��
	public static Vector<Client> clients = new Vector<Client>();	//������ Client�� ����
	
	ServerSocket serverSocket;
	

	String IP = "127.0.0.1";
	int port = 9999;		//�⺻ ��Ʈ��ȣ
	
	//Server ����, Client ���� ��ٸ��� �޼ҵ�
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
		
		//Client�� ������ ������  ��� ��ٸ��� thread
		Runnable thread = new Runnable() {
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[Ŭ���̾�Ʈ ����]" + socket.getRemoteSocketAddress()+": " + Thread.currentThread().getName());
						for(Client client : Main.clients) {
							String clientNum = Integer.toString(clients.size());
							String showClientNum = "[���� ���� ���� �ο� : "+clientNum+" �� ]\n";
							client.send(showClientNum);
						}
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
	
	//Server �۵� ���� �޼ҵ�, ��ü �ڿ� �Ҵ� ����
	public void stopServer() {
		try {
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//serverSocket �ݱ�
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//threadPool �ݱ�
			if (threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//UI����, ���������� ���α׷� ���۽�Ű�� �޼ҵ�
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		BorderPane root2 = new BorderPane();
		root2.setPadding(new Insets(5));
		root.setCenter(root2);
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));
		root2.setCenter(textArea);
		
		TextField portNum = new TextField();
		portNum.setPrefWidth(150);
		portNum.setPromptText("���� ��Ʈ ��ȣ�� �Է��ϼ���: ");
		root2.setBottom(portNum);
		//HBox.setHgrow(portNum, Priority.ALWAYS);
		
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		
		toggleButton.setOnAction(event -> {
			if (toggleButton.getText().equals("�����ϱ�")) {
				if (portNum.getText() != null ) {	//������ ���� ���� �ٲ��
					try {
						port = Integer.parseInt(portNum.getText());
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {	//port�� ������ ���� ��-�˸�â����
					
					}

				startServer(IP,port);
				Platform.runLater(() -> {
					String msg = String.format("[���� ����-port: "+port+"]\n", IP, port);
					textArea.appendText(msg);
					toggleButton.setText("�����ϱ�");
				});
				
			} else {
				stopServer();
				Platform.runLater(() -> {
					String msg = String.format("[���� ����-port: "+port+"]\n", IP, port);
					textArea.appendText(msg);
					portNum.setText("");
					toggleButton.setText("�����ϱ�");
					
				});
			}
		});
		
		Scene scene = new Scene(root, 400,350);
		primaryStage.setTitle("[��ê(Sochat)_����ON/OFF]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//���α׷� ������
	public static void main(String[] args) {
		launch(args);
	}
}
