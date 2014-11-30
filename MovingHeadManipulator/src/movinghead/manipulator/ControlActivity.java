package movinghead.manipulator;

import movinghead.manipulator.utils.ArtNet;
import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
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

	protected static final long VIBRATOR_MILIS = 100;

	// GUI objects
	private Switch switchOn;
	private SeekBar dimBar, cyanBar, magentaBar, yellowBar;
	private ToggleButton pauseButton;
	private Button resetButton;

	private ArtNet artnet;
	private Vibrator vibrator;
	private SensorHandler sensors;
	private MicrophoneHandler microphone;

	private static byte[] DMX = new byte[512];
	static final int ChPAN = 0;
	static final int ChTILT = 1;
	static final int ChCYAN = 2;
	static final int ChMAGENTA = 3;
	static final int ChYELLOW = 4;
	static final int ChDIMMER = 5;

	private boolean isWorking = false;

	private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

		private final int CHANNEL;

		public SeekBarListener(int targetChannel) {
			CHANNEL = targetChannel;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (isWorking) {
				artnet.setDMX((byte) progress, CHANNEL);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			vibrator.vibrate(VIBRATOR_MILIS);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
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

		switchOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				vibrator.vibrate(10);
				if (isChecked)
					try {
//						sensors.start();
//						startBars();
//						artnet.startTransmition();
						microphone.startRecording();
					} catch (Exception e) {
						e.printStackTrace();
						stopBars();
//						artnet.stopTransmition();
					}
				else if (!isChecked) {
//					sensors.stop();
//					stopBars();
//					artnet.stopTransmition();
					microphone.stopRecoding();
				}

			}

			private void startBars() {
				isWorking = true;
				dimBar.setProgress(0);
				cyanBar.setProgress(255);
				magentaBar.setProgress(255);
				yellowBar.setProgress(255);
			}

			private void stopBars() {
				isWorking = false;
			}
		});

		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATOR_MILIS);
			}
		});

		dimBar.setMax(255);
		dimBar.setOnSeekBarChangeListener(new SeekBarListener(ChDIMMER));
		cyanBar.setMax(255);
		cyanBar.setOnSeekBarChangeListener(new SeekBarListener(ChCYAN));
		magentaBar.setMax(255);
		magentaBar.setOnSeekBarChangeListener(new SeekBarListener(ChMAGENTA));
		yellowBar.setMax(255);
		yellowBar.setOnSeekBarChangeListener(new SeekBarListener(ChYELLOW));

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
