package com.video.util;


import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;

public class Recorder {
	private static final double FRAME_RATE = 25.0;
	private static final double MOTION_FACTOR = 1;
	private static final String OUTPUTFILE = "rtmp://49.235.177.235:1935/myapp/test";
	//private static final String OUTPUTFILE = "C:\\Users\\AS US\\Desktop\\视频\\111.mp4";
	/**
	 * 创建视频帧录制器
	 */
	public FFmpegFrameRecorder createRecorder(int width, int height, String channelId) {
		// 帧录制器  audioChannels 0无音频 1单声道 2立体声
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(OUTPUTFILE, width, height, 2);
		/*
		 * https://cloud.tencent.com/developer/article/1119657 videoBitRate这个参数很重要，
		 * 越大，越清晰，但最终的生成的视频也越大。
		 * 查看一个资料，说均衡考虑建议设为videoWidth*videoHeight*frameRate*0.07*运动因子，
		 * 运动因子则与视频中画面活动频繁程度有关，如果很频繁就设为4，不频繁则设为1
		 */
		recorder.setVideoBitrate((int) ((width * height * FRAME_RATE) * MOTION_FACTOR * 0.07));
		recorder.setInterleaved(true);
		recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        
		recorder.setVideoOption("crf", "25");
		/*权衡quality(视频质量)和encode speed(编码速度) values(值)： ultrafast(终极快),superfast(超级快),
		veryfast(非常快), faster(很快), fast(快), medium(中等), slow(慢), slower(很慢),
		veryslow(非常慢)
		ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；而veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
		参考：https://trac.ffmpeg.org/wiki/Encode/H.264
		*/		
		//recorder.setVideoOption("preset", "slow");
		// 2000 kb/s, 720P视频的合理比特率范围
		recorder.setVideoBitrate(2000000);
		// h264编/解码器
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		// 封装格式flv
		recorder.setFormat("flv");
		/*recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);//mp4用
		recorder.setFormat("mp4");*/
		// 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
		recorder.setFrameRate(FRAME_RATE);
		// 不可变(固定)音频比特率
		recorder.setAudioOption("crf", "0");
		// 最高质量
		recorder.setAudioQuality(0);
		// 音频比特率
		recorder.setAudioBitrate(192000);
		// 音频采样率
		recorder.setSampleRate(44100);
		// 双通道(立体声)
		recorder.setAudioChannels(2);
		// 音频编/解码器
		recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
		return recorder;
	}

	public FFmpegFrameRecorder startRecorder(int width, int height, String channelId) {
		System.out.println("视频宽"+width+"视频高"+height);
		Boolean exceptionSign=false;
		FFmpegFrameRecorder recorder = createRecorder(width, height, channelId);
		try {
			if (recorder != null) {
				System.out.println("开启录制器");
				recorder.start();
			}
		} catch (Exception e) {
			exceptionSign=true;
			e.printStackTrace();
		}finally {
			try {
				if(exceptionSign&&recorder!=null) {
				recorder.stop();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return recorder;
	}
	
}
