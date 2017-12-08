package com.coolioasjulio.fll.visiondrive;

import java.io.IOException;

import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.video.Video;

public class ImageCapture {
	private Video video;
	private byte[] frame;
	
	/**
	 * Open webcam with specified resolution
	 * @param width with of image
	 * @param height height of image
	 * @throws IOException
	 */
	public ImageCapture(int width, int height) throws IOException{
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		video = ev3.getVideo();
		video.open(width, height);
		frame = video.createFrame();
	}
	
	/**
	 * Open webcam with 1280x720 resolution
	 * @throws IOException
	 */
	public ImageCapture() throws IOException{
		this(1280, 720);
	}
	
	/**
	 * Return captured image width
	 * @return image width
	 */
	public int imageWidth(){
		return video.getWidth();
	}
	
	/**
	 * Return captured image height
	 * @return image height
	 */
	public int imageHeight(){
		return video.getHeight();
	}
	
	/**
	 * Capture image from webcam and return it as byte array. I have no idea how it's wrapped.
	 * @return captured image in yuyv format as byte array
	 * @throws IOException
	 */
	public byte[] captureImage() throws IOException{
		video.grabFrame(frame); // TODO: Figure out how the image is wrapped
		return frame;
	}
}
