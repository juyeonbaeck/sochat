package application;

import java.io.*;
import java.net.Socket;

//하나의 Client와 통신하도록 해주는 클래스
public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	//Client로부터 메세지 전달받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {
			
			public void run() {
				try {
					while(true) {
						//변수 바꿈
						InputStream istream = socket.getInputStream();
						//byte 수 바꿀까?
						byte[] buffer = new byte[512];
						int length = istream.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[메세지 수신 성공]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
						String msg = new String(buffer, 0, length, "UTF-8");
						
						for(Client client : Main.clients) {
							client.send(msg);
						}
					}
					
				} catch(Exception e) {
					try {
						System.out.println("[메세지 수신 오류]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
					} catch(Exception e2) {

						e2.printStackTrace();
					}
				}
				
			}
		};
		Main.threadPool.submit(thread);
		
	}

	//Client에게 메세지 전송하는 메소드
	public void send(String msg) {
		Runnable thread = new Runnable() {
			public void run() {
				try {
					OutputStream ostream = socket.getOutputStream();
					byte[] buffer = msg.getBytes("UTF-8");
					ostream.write(buffer);
					ostream.flush();
				} catch(Exception e) {
					try {
						System.out.println("[메세지 송신 오류]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);
	}
	
}
