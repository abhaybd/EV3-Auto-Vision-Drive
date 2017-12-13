package com.coolioasjulio.fll.test;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ConvertTest {
	public static final int WIDTH = 176;
	public static final int HEIGHT = 144;
	public static void main(String[] args) throws IOException{
		//FileInputStream in = new FileInputStream("img.yuv");
		DataInputStream in = new DataInputStream(new FileInputStream("img.yuv"));
		byte[] bytes = new byte[50688];
		in.readFully(bytes);
		
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		for(int i = 0; i < bytes.length; i+=4){
			int y1 = bytes[i] & 0xFF;
            int y2 = bytes[i+2] & 0xFF;
            int u = bytes[i+1] & 0xFF;
            int v = bytes[i+3] & 0xFF;
            int rgb1 = convertYUVtoARGB(y1,u,v);
            int rgb2 = convertYUVtoARGB(y2,u,v);
            image.setRGB((i % (WIDTH * 2)) / 2, i / (WIDTH * 2), rgb1);
            image.setRGB((i % (WIDTH * 2)) / 2 + 1, i / (WIDTH * 2), rgb2);
		}
		
		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(image.getScaledInstance(WIDTH*5, HEIGHT*5, BufferedImage.SCALE_FAST)));
		frame.add(label);
		frame.pack();
		frame.setVisible(true);
		
		
	}
	
    static int convertYUVtoARGB(int y, int u, int v) {
        int c = y - 16;
        int d = u - 128;
        int e = v - 128;
        int r = (298*c+409*e+128)/256;
        int g = (298*c-100*d-208*e+128)/256;
        int b = (298*c+516*d+128)/256;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }
 
}
