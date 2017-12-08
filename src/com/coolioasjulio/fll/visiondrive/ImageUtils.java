package com.coolioasjulio.fll.visiondrive;

import java.io.IOException;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.video.Video;

public class ImageUtils {
	private static boolean setup = false;
	private static Video video;
	
	/**
	 * Open webcam with specified resolution
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public static void setup(int width, int height) throws IOException{
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		video = ev3.getVideo();
		video.open(width, height);
	}
	
	/**
	 * Open webcam with 1280x720 resolution
	 * @throws IOException
	 */
	public static void setup() throws IOException{
		setup(1280, 720);
	}
	
	public static int imageWidth(){
		return video.getWidth();
	}
	
	public static int imageHeight(){
		return video.getHeight();
	}
	
	public static byte[] captureImage() throws IOException{
		setupIfNot();
		byte[] frame = video.createFrame();
		video.grabFrame(frame);
		return frame;
	}
	
	private static void setupIfNot() throws IOException{
		if(!setup){
			setup();
		}
		setup = true;
	}
}
