package movinghead.manipulator;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;
import movinghead.manipulator.utils.ArtNet;

public class MicrophoneHandler {

	private static final String TAG = "MicrophoneHandler";
	public static final int RECORDER_SAMPLERATE = 44100;
	public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_DEFAULT;
	public static final int RECORDER_SAMPLE_SIZE = 1024; // want to play 2048
															// (2K) since 2
															// bytes we use only
															// 1024
	private final int bufferSize = AudioRecord.getMinBufferSize(
			RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

	private static volatile short[] audioData = new short[RECORDER_SAMPLE_SIZE];
	private static volatile float[] fAudioData = new float[RECORDER_SAMPLE_SIZE];
	private static volatile boolean newSampleAvailable = false;

	private AudioRecord recorder;

	private boolean RECORDING = true;

	public MicrophoneHandler() {
	}

	public void start() {
		this.RECORDING = true;
		recorder = findAudioRecord();
		Log.d(TAG, "Recorder bound");
		recorder.startRecording();
		Log.d(TAG, "Recorder started");
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (RECORDING) {
					recorder.read(audioData, 0, RECORDER_SAMPLE_SIZE);
					for (int i = 0; i < RECORDER_SAMPLE_SIZE; i++)
						fAudioData[i] = audioData[i] / Short.MAX_VALUE;
				}
			}
		}).start();
	}

	public short[] getAudioData() {
		return audioData;
	}

	public float[] getFloatAudioData() {
		return fAudioData;
	}

	public boolean isNewSampleAvailable() {
		return newSampleAvailable;
	}

	public void stop() {
		this.RECORDING = false;
		recorder.stop();
		recorder.release();
		recorder = null;
	}

	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

	public AudioRecord findAudioRecord() {
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] {
					AudioFormat.ENCODING_PCM_8BIT,
					AudioFormat.ENCODING_PCM_16BIT }) {
				for (short channelConfig : new short[] {
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.CHANNEL_IN_STEREO }) {
					try {
						Log.d(TAG, "Attempting rate " + rate + "Hz, bits: "
								+ audioFormat + ", channel: " + channelConfig);
						int bufferSize = AudioRecord.getMinBufferSize(rate,
								channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(
									AudioSource.DEFAULT, rate, channelConfig,
									audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
								return recorder;
						}
					} catch (Exception e) {
						Log.e(TAG, rate + "Exception, keep trying.", e);
					}
				}
			}
		}
		return null;
	}

}
