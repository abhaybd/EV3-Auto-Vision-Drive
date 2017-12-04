package com.coolioasjulio.webcam;
import org.bytedeco.javacv.*;

import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;



public class WebcamTest implements Runnable {
    final int INTERVAL = 100;///you may use interval
    CanvasFrame canvas = new CanvasFrame("Web Cam");

    public WebcamTest() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    public void run() {

        try (FrameGrabber grabber = new VideoInputFrameGrabber(0)){
        	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        	IplImage img;
            grabber.start();
            while (!Thread.interrupted()) {
                Frame frame = grabber.grab();

                img = converter.convert(frame);

                //the grabbed frame will be flipped, re-flip to make it right
                // cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise

                canvas.showImage(converter.convert(img));

                Thread.sleep(INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebcamTest gs = new WebcamTest();
        Thread th = new Thread(gs);
        th.start();
    }
}