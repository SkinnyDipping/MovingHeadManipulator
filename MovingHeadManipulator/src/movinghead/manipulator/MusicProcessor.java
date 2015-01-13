package movinghead.manipulator;

import org.jtransforms.fft.FloatFFT_1D;

import android.util.Log;

public class MusicProcessor implements Runnable {

	private static final String TAG = "MusicProcessor";
//	private final float SHORT2BYTE = (int)Byte.MAX_VALUE / (int)Short.MAX_VALUE;


	private MicrophoneHandler mMicrophone;
	private FloatFFT_1D fft = new FloatFFT_1D(MicrophoneHandler.RECORDER_SAMPLE_SIZE);

	private short[] audioData;
	private boolean isRunning = false;

	public MusicProcessor() {
		mMicrophone = new MicrophoneHandler();
	}

	@Override
	public void run() {
		isRunning = true;
		int seq = 0;
		while (isRunning) {
			if (mMicrophone.isNewSampleAvailable()) {
				this.audioData = mMicrophone.getAudioData();
				// ControlActivity.controlData[0] = (byte) (audioData[0] *
				// SHORT2BYTE);
//				ControlActivity.controlData[5] = 
				Log.d(TAG, "Sample "+seq+++": " + audioData[0]);
				fft.realForward(mMicrophone.getFloatAudioData());
				
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	public void startProcessing() {
		mMicrophone.start();
		new Thread(this).start();
	}

	public void stopProcessing() {
		mMicrophone.stop();
		isRunning = false;
	}

}
