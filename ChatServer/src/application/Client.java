package application;

import java.io.*;
import java.net.Socket;

//�ϳ��� Client�� ����ϵ��� ���ִ� Ŭ����
public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}

	//Client�κ��� �޼��� ���޹޴� �޼ҵ�
	public void receive() {
		Runnable thread = new Runnable() {
			
			public void run() {
				try {
					while(true) {
						//���� �ٲ�
						InputStream istream = socket.getInputStream();
						//byte �� �ٲܱ�?
						byte[] buffer = new byte[512];
						int length = istream.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[�޼��� ���� ����]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
						String msg = new String(buffer, 0, length, "UTF-8");
						
						for(Client client : Main.clients) {
							client.send(msg);
						}
					}
					
				} catch(Exception e) {
					try {
						System.out.println("[�޼��� ���� ����]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
					} catch(Exception e2) {

						e2.printStackTrace();
					}
				}
				
			}
		};
		Main.threadPool.submit(thread);
		
	}

	//Client���� �޼��� �����ϴ� �޼ҵ�
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
						System.out.println("[�޼��� �۽� ����]"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName());
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
