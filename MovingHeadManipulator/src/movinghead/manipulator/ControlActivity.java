package movinghead.manipulator;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import movinghead.manipulator.util.ArtnetThread;
import movinghead.manipulator.util.ArtNet;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ControlActivity extends Activity implements SensorEventListener {
	
	//Constants
	private static final int VIBRATOR_DURATION = 100;
	private static final float NS2S = 1.0f / 1000000000.0f;	//nanosec to sec
	private static final float RAD2DEG = 57.2957795f;		//rad to degree
	private static final float DEG2PAN = 255f/540f;				//degree to pan:	PANdeg/255
	private static final float DEG2TILT = 255f/270f;			//degree to tilt	TILTdeg/255
	//DMX512 Channel constants
	private static final int ChPAN = 13;//13
	private static final int ChTILT = 15;//15
	private static final int ChDIMMER = 11;//11
	private static final int ChCYAN = 0;//0
	private static final int ChMAGENTA = 2;//2
	private static final int ChYELLOW = 4;//4
	
	
	//Pause
	private boolean freeze = false;
	
	//Moving head home position
	private int homePAN = 80;
	private int homeTILT = 50;
	
	//Data containers
	private float[] gravity = new float[3];
	private float[] geomagnetic = new float[3];
	private float[] gyroscope = new float[3];
	private float[] orientation = {0,0,0};
	private long timestamp;
	private byte[] DMX512 = new byte[512];
	
	private Float[] inputOrientation = {(Float) null,(Float) null,(Float) null};
	
	//GUI objects
	private Switch switchOn;
	private SeekBar dimBar, cyanBar, magentaBar, yellowBar;
	private ToggleButton pauseButton;
	private Button resetButton;
	
	//Network connection object
	ArtNet artnet;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        
        //Network connection initialization
        artnet = new ArtNet();
        
        //Accessing system resources
        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        
        final SensorManager sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);   
        
        final Sensor accel = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final Sensor grav = sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);
        final Sensor magn = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        final Sensor gyro = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        //Accessing GUI resources         
        switchOn = (Switch)findViewById(R.id.startSwitch);
        dimBar = (SeekBar)findViewById(R.id.dimmerBar);
        cyanBar = (SeekBar) findViewById(R.id.cyanBar);
        magentaBar = (SeekBar) findViewById(R.id.magentaBar);
        yellowBar = (SeekBar) findViewById(R.id.yellowBar);
        resetButton = (Button) findViewById(R.id.resetButton);
        pauseButton = (ToggleButton) findViewById(R.id.pauseButton);
        
        
        //Setting Listeners on GUI objects
        
        switchOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked) {
					vibrator.vibrate(VIBRATOR_DURATION);
					//Setting Listeners on Sensors
					sensors.registerListener(ControlActivity.this, grav, SensorManager.SENSOR_DELAY_GAME);
					sensors.registerListener(ControlActivity.this, magn, SensorManager.SENSOR_DELAY_GAME);
					sensors.registerListener(ControlActivity.this, gyro, SensorManager.SENSOR_DELAY_GAME);
					sensors.registerListener(ControlActivity.this, accel, SensorManager.SENSOR_DELAY_GAME);
//					for (int i=0; i<inputOrientation.length; i++)
//						inputOrientation[i] = (Float)null;
//					// ustawienie g�owy w HOME TODO
				}
				else
				{
					//Deleting Listeners from Sensors
					sensors.unregisterListener(ControlActivity.this);
				}
				
			}
		});
        
        dimBar.setMax(255);
        dimBar.setProgress(0);
        dimBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				vibrator.vibrate(VIBRATOR_DURATION);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				DMX512[ChDIMMER] = (byte) progress;
				sendPacket(DMX512);
			}
		});
        
        cyanBar.setMax(255);
        cyanBar.setProgress(0);
        cyanBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				vibrator.vibrate(VIBRATOR_DURATION);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				DMX512[ChCYAN] = (byte) progress;
				sendPacket(DMX512);
			}
		});
        
        magentaBar.setMax(255);
        magentaBar.setProgress(0);
        magentaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				vibrator.vibrate(VIBRATOR_DURATION);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				DMX512[ChMAGENTA] = (byte) progress;
				sendPacket(DMX512);
			}
		});
        
        yellowBar.setMax(255);
        yellowBar.setProgress(0);
        yellowBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				vibrator.vibrate(VIBRATOR_DURATION);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				DMX512[ChYELLOW] = (byte) progress;
				sendPacket(DMX512);
			}
		});
        
        resetButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				vibrator.vibrate(VIBRATOR_DURATION);
				//TODO reseting
				sensors.unregisterListener(ControlActivity.this);
				sensors.registerListener(ControlActivity.this, grav, SensorManager.SENSOR_DELAY_GAME);
				sensors.registerListener(ControlActivity.this, magn, SensorManager.SENSOR_DELAY_GAME);
				sensors.registerListener(ControlActivity.this, gyro, SensorManager.SENSOR_DELAY_GAME);
				sensors.registerListener(ControlActivity.this, accel, SensorManager.SENSOR_DELAY_GAME);
				for (int i=0; i<orientation.length; i++)
					orientation[i] = 0;
			}
		});
        
        pauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					freeze = true;
				else
					freeze = false;
			}
		});
    }
    
    //Method invoked on state change of sensor handled by ControlActivity
    public void onSensorChanged(SensorEvent event)
    {
    	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
    	{
    		for(int i=0; i<event.values.length; i++)
    			gravity[i] = event.values[i];
    	}
    	
    	if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
    	{
    		for(int i=0; i<event.values.length; i++)
    			geomagnetic[i] = event.values[i];
    	}
    	
    	if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
    	{
    		for(int i=0; i<event.values.length; i++)
    			gyroscope[i] = event.values[i];
    	}
    	
    	//Integrating gyroscope data
    	if (timestamp != 0) {
    		final float dT = (event.timestamp - timestamp) * NS2S;
    		for (int i=0; i<orientation.length; i++)
    		{
    			float temp = gyroscope[i] * dT;
    			orientation[i] += temp;
    		}
    	}
    	timestamp = event.timestamp;
    	
    	double pan = homePAN + orientation[2]*RAD2DEG * DEG2PAN;
    	double tilt = homeTILT + orientation[0]*RAD2DEG * DEG2TILT;
    	if (pan > 255) pan = 255;
    	if (pan < 0) pan = 0;
    	if (tilt > 255) tilt = 255;
    	if (tilt < 0) tilt = 0;
    	
//    	DMX512[ChPAN] =  Double.valueOf(127 + orientation[2]*RAD2DEG * DEG2PAN).byteValue();
    	DMX512[ChPAN] = Double.valueOf(pan).byteValue();
//		DMX512[ChTILT] =  Double.valueOf(30 + orientation[0]*RAD2DEG * DEG2TILT).byteValue();
    	DMX512[ChTILT] = Double.valueOf(tilt).byteValue();

		sendPacket(DMX512);
    }
	
	private void sendPacket (byte[] DMX512) {
		if (!freeze)
		{
//			artnet.generateArtDMX(DMX512);
			try {
				artnet.brPacket(artnet.generateArtDMX(DMX512));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			artnet.sendPacket(DMX512);
		}
	}
	
	//Abstract methods from Activity base-class and SensorEventListener interface
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}