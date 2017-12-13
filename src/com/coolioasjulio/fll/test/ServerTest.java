package com.coolioasjulio.fll.test;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest {
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = new ServerSocket(4444);
		System.out.println("Waiting...");
		Socket socket = serverSocket.accept();
		System.out.println("Connected!");
		DataInputStream in = new DataInputStream(socket.getInputStream());
		int width = in.readInt();
		int height = in.readInt();
		System.out.println("Image is " + width + "x" + height);
		int length = in.readInt();
		System.out.println("Recieving " + length + " bytes");
		byte[] bytes = new byte[length];
		in.readFully(bytes);
		serverSocket.close();
		FileOutputStream file = new FileOutputStream("img.yuv");
		file.write(bytes);
		file.close();
	}
}
