package com.video.util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.WindowConstants;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import com.video.service.VideoService;

public class RecorderVideoThread implements Runnable {
	public volatile Boolean sign=false;
	private OpenCVFrameGrabber grabber;
	private FFmpegFrameRecorder recorder;
	private CountDownLatch countDownLatch;
	public RecorderVideoThread(OpenCVFrameGrabber grabber,FFmpegFrameRecorder recorder,CountDownLatch countDownLatch) {
		this.grabber=grabber;
		this.recorder=recorder;
		this.countDownLatch=countDownLatch;
	}
	public void setSign(Boolean sign) {
		this.sign = sign;
	}
	@Override
	public void run() {
		try {
			IplImage grabbedImage;
			Frame frame;
			//CanvasFrame canvas = createCanvasFrame();
			System.out.println("视频录制中");
			long startTime=0L;
			long ts=0L;
			while ((grabbedImage = Grabber.converter.convert(grabber.grab())) != null) {
				if(sign) {
					break;
				}
				frame = Grabber.converter.convert(grabbedImage);
				if(frame!=null) {
					//canvas.showImage(frame);//获取摄像头图像并放到窗口上显示
					if (startTime == 0) { startTime = System.currentTimeMillis();}
					ts = 1000 * (System.currentTimeMillis() - startTime);
					if (ts > recorder.getTimestamp()) {
						//解决mp4视频播放快速
						recorder.setTimestamp(ts);
					}
					recorder.record(frame);
				}
			}
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				
				System.out.println("准备关闭录制器");
				countDownLatch.await();
				if(recorder!=null) recorder.stop();
				System.out.println("已关闭录制器");
				if(grabber!=null) grabber.stop();
				System.out.println("已关闭抓取器和录制器");
			} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static CanvasFrame createCanvasFrame() {
		 CanvasFrame canvas = new CanvasFrame("");
	     canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	     canvas.setAlwaysOnTop(true);
	     canvas.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				RecorderVideoThread recorderVideo = VideoService.recorderVideoMap.get("666");
				RecorderAudioThread recorderAudio = VideoService.recorderAudioMap.get("666");
				recorderVideo.sign=true;
				recorderAudio.sign=true;
				VideoService.recorderVideoMap.remove("666");
				VideoService.recorderAudioMap.remove("666");
				/*recorderVideo=null;
				recorderAudio=null;*/
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	     
	    return canvas;
	}

}
