package com.coolioasjulio.fll.webcam;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

public class SingleImageTest {
	public static void main(String[] args){
		CanvasFrame canvas = new CanvasFrame("Web Cam");
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		try (FrameGrabber grabber = new VideoInputFrameGrabber(0)){
        	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        	IplImage img;
            grabber.start();
            Frame frame = grabber.grab();
            
            img = converter.convert(frame);
            
            System.out.println(img.width() + "x" + img.height());
            
            canvas.showImage(converter.convert(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}