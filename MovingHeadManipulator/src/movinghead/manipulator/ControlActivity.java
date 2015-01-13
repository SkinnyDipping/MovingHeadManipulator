package movinghead.manipulator;

import movinghead.manipulator.utils.ArtNet;
import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ControlActivity extends Activity {

	public static final long VIBRATOR_MILIS = 100;
	private static final String TAG = "ControlActivity";
	private static final long CONTROL_THREAD_DELAY = 1;

	private class ControlThread implements Runnable {

		boolean isWorking = false;

		@Override
		public void run() {
			isWorking = true;
			while (isWorking) {
				mArtnet.sendPacket(createDMXTable(controlData));
				try {
					Thread.sleep(CONTROL_THREAD_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
					isWorking = false;
				}
			}
		}

		public final void stop() {
			isWorking = false;
		}

		private byte[] createDMXTable(byte[] data) {
			byte[] output = new byte[512];
			output[ChDIMMER] = data[0];
			output[ChPAN] = data[1];
			output[ChTILT] = data[2];
			output[ChRED] = data[3];
			output[ChGREEN] = data[4];
			output[ChBLUE] = data[5];
			return output;
		}
	}

	private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

		private final int CONTROL_PROPETRY;

		public SeekBarListener(int c) {
			CONTROL_PROPETRY = c;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (CONTROL_TYPE == ControlType.SENSOR_AND_SCREEN
					|| CONTROL_TYPE == ControlType.SCREEN)
				controlData[CONTROL_PROPETRY] = (byte) progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mVibrator.vibrate(ControlActivity.VIBRATOR_MILIS);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	// GUI objects
	private Switch switchOn;
	private SeekBar dimBar, cyanBar, magentaBar, yellowBar;
	private ToggleButton musicProcessorSwitch;

	private ArtNet mArtnet;
	private Vibrator mVibrator;
	private SensorHandler mSensors;
	// private MicrophoneHandler mMicrophone;
	private MusicProcessor mMusicProcessor;

	// DMX512 channels
	public static final int ChDIMMER = 5;
	public static final int ChPAN = 0;
	public static final int ChTILT = 1;
	public static final int ChRED = 2;
	public static final int ChGREEN = 3;
	public static final int ChBLUE = 4;

// @formatter:off 
	/**
	 * This is the source of control for the control thread:
	 * [0] -> DIMMER
	 * [1] -> PAN
	 * [2] -> TILT
	 * [3] -> RED (CYAN)
	 * [4] -> GREEN (MAGENTA)
	 * [5] -> BLUE (YELLOW)
	 */
	public volatile static byte[] controlData = new byte[6];
// @formatter:on
	private final byte[] HOME_SETTINGS = { 0, 0, 0, 127, 127, 127 };

	 private static ControlType CONTROL_TYPE = ControlType.MICROPHONE;
//	private static ControlType CONTROL_TYPE = ControlType.SENSOR_AND_SCREEN;
	private final ControlThread CONTROL_THREAD = new ControlThread();

	private void controlTypeChange(ControlType newControlType) throws Exception {

		Log.i(TAG, "controlTypeChange: terminating control");
		switch (CONTROL_TYPE) {
		case MICROPHONE:
			mMusicProcessor.stopProcessing();
			Log.i(TAG, "Microphone control terminated");
			break; //TODO USUNAC
		case SENSOR_AND_SCREEN:
			mSensors.stop();
			Log.i(TAG, "Sensor&Screen control terminated");
			break;
		case OFF:
			Log.i(TAG, "Control thread already off. Nothing happened");
			break;
		default:
			Log.i(TAG, "Control terminated");
			break;
		}
		CONTROL_THREAD.stop();

		mVibrator.vibrate(10);
		CONTROL_TYPE = newControlType;

		Log.i(TAG, "controlTypeChange: Initializing control");
		switch (CONTROL_TYPE) {
		case SCREEN:
			dimBar.setProgress(0);
			cyanBar.setProgress(255);
			magentaBar.setProgress(255);
			yellowBar.setProgress(255);
			Log.i(TAG, "Control initiated");
			break;
		case MICROPHONE:
			Log.i(TAG, "Microphone control initiated");
			mMusicProcessor.startProcessing();
			break; //TODO USUNAC
		case SENSOR_AND_SCREEN:
			try {
				mSensors.start();
			} catch (Exception e) {
				mSensors.stop();
				Log.e(TAG, e + "\nWork terminated");
				throw new Exception("Failed to initialize SENSOR_AND_SCREEN control");
			}
			dimBar.setProgress(0);
			cyanBar.setProgress(255);
			magentaBar.setProgress(255);
			yellowBar.setProgress(255);
			Log.i(TAG, "Sensor&Screen control initiated");
			break;
		default:
			break;
		}
		new Thread(CONTROL_THREAD).start();

	}

	private class SwitchButtonListener implements
			CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mVibrator.vibrate(10);
			if (isChecked) {

				switch (CONTROL_TYPE) {
				case SCREEN:
					setInitBars();
					Log.i(TAG, "Control initiated");
					break;
				case SENSOR_AND_SCREEN:
					try {
						mSensors.start();
					} catch (Exception e) {
						mSensors.stop();
						switchOn.setChecked(false);
						Log.e(TAG, e + "\nWork terminated");
						return;
					}
					setInitBars();
					Log.i(TAG, "Sensor&Screen control initiated");
					break;
				case MICROPHONE:
					Log.i(TAG, "Microphone control initiated");
					mMusicProcessor.startProcessing();
					break;
				default:
					break;
				}
				new Thread(CONTROL_THREAD).start();

			} else if (!isChecked) {

				switch (CONTROL_TYPE) {
				case SENSOR_AND_SCREEN:
					mSensors.stop();
					Log.i(TAG, "Sensor&Screen control terminated");
					break;
				case MICROPHONE:
					mMusicProcessor.stopProcessing();
					Log.i(TAG, "Microphone control terminated");
					break;
				default:
					Log.i(TAG, "Control terminated");
					break;
				}
				CONTROL_THREAD.stop();
			}

		}

		private void setInitBars() {
			dimBar.setProgress(0);
			cyanBar.setProgress(255);
			magentaBar.setProgress(255);
			yellowBar.setProgress(255);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);

		mArtnet = new ArtNet();
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mSensors = new SensorHandler(getApplicationContext());
		mMusicProcessor = new MusicProcessor();

		dimBar = (SeekBar) findViewById(R.id.dimmerBar);
		cyanBar = (SeekBar) findViewById(R.id.cyanBar);
		magentaBar = (SeekBar) findViewById(R.id.magentaBar);
		yellowBar = (SeekBar) findViewById(R.id.yellowBar);
		switchOn = (Switch) findViewById(R.id.startSwitch);

		controlData = HOME_SETTINGS;

		 switchOn.setOnCheckedChangeListener(new SwitchButtonListener());
//		switchOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//					boolean isChecked) {
//				try {
//					controlTypeChange(CONTROL_TYPE);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});

		dimBar.setMax(255);
		dimBar.setOnSeekBarChangeListener(new SeekBarListener(0));
		cyanBar.setMax(255);
		cyanBar.setOnSeekBarChangeListener(new SeekBarListener(3));
		magentaBar.setMax(255);
		magentaBar.setOnSeekBarChangeListener(new SeekBarListener(4));
		yellowBar.setMax(255);
		yellowBar.setOnSeekBarChangeListener(new SeekBarListener(5));

	}

	@Override
	public void onPause() {
		super.onPause();
		/*
		 * TODO Turn control off i.e. perform "switchOff" operation
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	public ControlType getControlType() {
		return CONTROL_TYPE;
	}
}
