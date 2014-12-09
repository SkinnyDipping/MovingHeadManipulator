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
	private static final long CONTROL_THREAD_DELAY = 1000;

	private class ControlThread implements Runnable {

		boolean isWorking = false;

		@Override
		public void run() {
			isWorking = true;
			while (isWorking) {
				Log.d(TAG, "" + controlData[0]);
				// artnet.setDMX(dimmer, ChDIMMER);
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
	}

	private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

		private final int CONTROL_PROPETRY;

		public SeekBarListener(int c) {
			CONTROL_PROPETRY = c;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			controlData[CONTROL_PROPETRY] = (byte) progress;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			vibrator.vibrate(ControlActivity.VIBRATOR_MILIS);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	// GUI objects
	private Switch switchOn;
	private SeekBar dimBar, cyanBar, magentaBar, yellowBar;
	private ToggleButton pauseButton;
	private Button resetButton;

	private ArtNet artnet;
	private Vibrator vibrator;
	private SensorHandler sensors;
	private MicrophoneHandler microphone;

	private enum ControlType {
		NONE, MICROPHONE, SENSOR_AND_SCREEN, SCREEN
	};

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
	private final byte[] HOME_SETTINGS = { 127, 0, 0, 127, 127, 127 };

	private final ControlThread CONTROL_THREAD = new ControlThread();

	private class SwitchButtonListener implements
			CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			vibrator.vibrate(10);
			if (isChecked)
				try {
					// sensors.start();
					startBars();
					// artnet.startTransmition();
					// controlType = ControlType.MICROPHONE;
					// microphone.startRecording();
					new Thread(CONTROL_THREAD).start();
				} catch (Exception e) {
					e.printStackTrace();
					stopBars();
					// artnet.stopTransmition();
				}
			else if (!isChecked) {
				CONTROL_THREAD.stop();
				// controlThread.
				// controlType = ControlType.NONE;
				// sensors.stop();
				stopBars();
				// artnet.stopTransmition();
				// microphone.stopRecoding();
			}

		}

		private void startBars() {
			dimBar.setProgress(0);
			cyanBar.setProgress(255);
			magentaBar.setProgress(255);
			yellowBar.setProgress(255);
		}

		private void stopBars() {
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);

		artnet = new ArtNet();
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sensors = new SensorHandler(getApplicationContext(), artnet);
		microphone = new MicrophoneHandler(artnet);

		dimBar = (SeekBar) findViewById(R.id.dimmerBar);
		cyanBar = (SeekBar) findViewById(R.id.cyanBar);
		magentaBar = (SeekBar) findViewById(R.id.magentaBar);
		yellowBar = (SeekBar) findViewById(R.id.yellowBar);
		switchOn = (Switch) findViewById(R.id.startSwitch);
		pauseButton = (ToggleButton) findViewById(R.id.pauseButton);
		resetButton = (Button) findViewById(R.id.resetButton);

		controlData = HOME_SETTINGS;

		switchOn.setOnCheckedChangeListener(new SwitchButtonListener());

		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATOR_MILIS);
			}
		});

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
}
