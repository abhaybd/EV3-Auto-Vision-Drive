package com.coolioasjulio.fll.visiondrive;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lejos.hardware.lcd.LCD;

public class YUVTest {
	public static void main(String[] args) throws IOException{
		ImageCapture ic = new ImageCapture();
		byte[] bytes = ic.captureImage();
		
		LCD.drawInt(bytes.length, 0, 0);
		
		Socket socket = new Socket("192.168.1.87", 4444);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeInt(ic.imageWidth());
		out.writeInt(ic.imageHeight());
		out.flush();
		out.writeInt(bytes.length);
		out.write(bytes);
		out.flush();
		socket.close();
	}
}
