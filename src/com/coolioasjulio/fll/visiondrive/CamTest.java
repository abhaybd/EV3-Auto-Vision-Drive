package com.coolioasjulio.fll.visiondrive;

import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.video.Video;

import java.io.IOException;

import lejos.hardware.BrickFinder;

public class CamTest {
	public static void main(String[] args) throws IOException, InterruptedException{
		EV3 ev3 = (EV3) BrickFinder.getLocal();
		Video video = ev3.getVideo();
		video.open(1280, 720);
		byte[] frame = video.createFrame();
		video.grabFrame(frame);
		LCD.drawInt(frame.length, 0, 0);
		Thread.sleep(10000);
	}
}
