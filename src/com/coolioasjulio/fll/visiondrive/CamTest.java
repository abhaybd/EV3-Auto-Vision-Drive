package com.coolioasjulio.fll.visiondrive;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import lejos.hardware.lcd.LCD;

public class CamTest {
	public static void main(String[] args){
		//CanvasFrame canvas = new CanvasFrame("Web Cam");
		//canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		try (FrameGrabber grabber = new VideoInputFrameGrabber(0)){
        	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        	IplImage img;
            grabber.start();
            Frame frame = grabber.grab();
            
            img = converter.convert(frame);
            LCD.drawString(img.width() + "x" + img.height(), 0, 0);
            //System.out.println(img.width() + "x" + img.height());
            
            //canvas.showImage(converter.convert(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
