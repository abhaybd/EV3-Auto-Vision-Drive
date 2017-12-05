package com.coolioasjulio.fll.visiondrive;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

public class ImageUtils {
	/**
	 * Capture image from specified webcam
	 * @param device Webcam index to use
	 * @return IplImage captured from specified webcam
	 * @throws IOException If error occurs accessing webcam
	 */
	public static IplImage captureImage(int device) throws IOException{
		try (FrameGrabber grabber = new VideoInputFrameGrabber(device)){
        	OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        	IplImage img;
            grabber.start();
            Frame frame = grabber.grab();
            img = converter.convert(frame);
            return img;
        } catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Capture image from webcam, using default webcam. (index 0)
	 * @return IplImage captured from webcam 0
	 * @throws IOException If error occurs accessing webcam
	 */
	public static IplImage captureImage() throws IOException{
		return captureImage(0);
	}
	
	public static byte[] getBytes(IplImage image){
		ByteBuffer buff = image.asByteBuffer();
		byte[] bytes = new byte[buff.remaining()];
		buff.get(bytes);
		return bytes;
	}
}
