package com.video.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.springframework.stereotype.Component;

import com.video.util.Grabber;
import com.video.util.Recorder;
import com.video.util.RecorderAudioThread;
import com.video.util.RecorderVideoThread;
@Component
public class VideoService {
	public static ConcurrentHashMap<String, RecorderVideoThread> recorderVideoMap = new ConcurrentHashMap<String,RecorderVideoThread>();
	public static ConcurrentHashMap<String, RecorderAudioThread> recorderAudioMap = new ConcurrentHashMap<String,RecorderAudioThread>();
	private static Thread videoThread;
	private static Thread audioThread;
	/*public static void main(String[] args) {
		doService("666",true);
	}*/
	public static void doService(String channelId,Boolean flag){
		if(flag) {//开启录制线程
			Frame grabFrame= null;
			IplImage grabImage = null;
			//每个用户一个抓取器
			OpenCVFrameGrabber grabber = new Grabber().startGrabber(channelId);
			try{
				if((grabFrame=grabber.grab()) !=null) {//视频的宽度必须是32的倍数，高度必须是2的倍数
					System.out.println("取到第一帧");
					grabImage = Grabber.converter.convert(grabFrame);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			//每个用户一个录制器
			FFmpegFrameRecorder recorder = new Recorder().startRecorder(grabImage.width(), grabImage.height(), channelId);
			//控制器
			CountDownLatch countDownLatch = new CountDownLatch(1);
			//启动一个线程录制视频
			RecorderVideoThread recorderVideo = new RecorderVideoThread(grabber,recorder,countDownLatch);
			recorderVideoMap.put(channelId, recorderVideo);
			videoThread = new Thread(recorderVideo,"RecorderVideo"+channelId);
			videoThread.start();
			//启动一个线程录制音频
			RecorderAudioThread recorderAudio = new RecorderAudioThread(recorder,countDownLatch);
			recorderAudioMap.put(channelId, recorderAudio);
			audioThread=new Thread(recorderAudio,"RecorderAudio"+channelId);
			audioThread.start();
		}else {//关闭录制线程
			RecorderVideoThread recorderVideo = recorderVideoMap.get(channelId);
			RecorderAudioThread recorderAudio = recorderAudioMap.get(channelId);
			recorderVideo.sign=true;
			recorderAudio.sign=true;
			recorderVideoMap.remove(channelId);
			recorderAudioMap.remove(channelId);
			try {
				videoThread.join();
				audioThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally {
				System.out.println("置空");
				recorderVideo=null;
				recorderAudio=null;
			}
		}
	}
}
