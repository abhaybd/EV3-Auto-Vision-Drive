package com.coolioasjulio.fll.visiondrive;

import java.io.IOException;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.video.Video;

public class ImageCapture {
	private Video video;
	
	/**
	 * Open webcam with specified resolution
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public ImageCapture(int width, int height) throws IOException{
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		video = ev3.getVideo();
		video.open(width, height);
	}
	
	/**
	 * Open webcam with 1280x720 resolution
	 * @throws IOException
	 */
	public ImageCapture() throws IOException{
		this(1280, 720);
	}
	
	public int imageWidth(){
		return video.getWidth();
	}
	
	public int imageHeight(){
		return video.getHeight();
	}
	
	public byte[] captureImage() throws IOException{
		byte[] frame = video.createFrame();
		video.grabFrame(frame);
		return frame;
	}
}
