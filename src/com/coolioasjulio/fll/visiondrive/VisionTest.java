package com.coolioasjulio.fll.visiondrive;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import lejos.hardware.Button;

public class VisionTest {
	public static void main(String[] args) throws IOException{
		InetAddress server = NetworkUtils.pingAll(4445);
		Socket socket = new Socket(server, 4444);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		ImageCapture imageCapture = new ImageCapture(160, 120);
		while(Button.ENTER.isUp()){
			byte[] bytes = imageCapture.captureImage();
			out.writeInt(imageCapture.imageWidth());
			out.writeInt(imageCapture.imageHeight());
			out.flush();
			out.writeInt(bytes.length);
			out.write(bytes);
			out.flush();
		}
		socket.close();
	}
}
