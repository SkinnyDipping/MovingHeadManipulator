package movinghead.manipulator;

import movinghead.manipulator.utils.ArtNet;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorHandler implements SensorEventListener {

	private static final float NS2S = 1.0f / 1000000000.0f; // nanosec to sec
	private static final float RAD2DEG = 57.2957795f; // rad to degree
	private static final float DEG2PAN = 255f / 540f; // degree to pan:
														// PANdeg/255
	private static final float DEG2TILT = 255f / 270f; // degree to tilt:
														// TILTdeg/255
	private static final int HOME_PAN = 80;
	private static final int HOME_TILT = 50;

	private final String TAG = "SensorHandler";

	private final SensorManager m_sensorManager;
	private final Sensor m_sensorAccelerometer, m_sensorMagnetometer,
			m_sensorGyroscope;
	private float[] accelerometer = new float[3];
	private float[] magnetometer = new float[3];
	private float[] gyroscope = new float[3];
	private float[] orientation = { 0, 0, 0 };
	private long timestamp = 0;

	SensorHandler(Context context) {
		m_sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		m_sensorAccelerometer = m_sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		m_sensorMagnetometer = m_sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		m_sensorGyroscope = m_sensorManager
				.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}

	public void start() throws Exception {
		if (m_sensorManager.registerListener(this, m_sensorAccelerometer,
				SensorManager.SENSOR_DELAY_GAME)
				&& m_sensorManager.registerListener(this, m_sensorMagnetometer,
						SensorManager.SENSOR_DELAY_GAME)
				&& m_sensorManager.registerListener(this, m_sensorGyroscope,
						SensorManager.SENSOR_DELAY_GAME) == false) {
			Log.e(TAG, "Error registering sensors");
			throw new Exception("SensorHandler: error registering sensors");
		}
		Log.i(TAG, "Sensors registered");
	}

	public void stop() {
		m_sensorManager.unregisterListener(this);
		Log.i(TAG, "Sensors unregistered");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			for (int i = 0; i < event.values.length; i++)
				accelerometer[i] = event.values[i];
		}

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			for (int i = 0; i < event.values.length; i++)
				magnetometer[i] = event.values[i];
		}

		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			for (int i = 0; i < event.values.length; i++)
				gyroscope[i] = event.values[i];
		}

		// Integrating gyroscope data
		// TODO TEMP, remove after implementing Kalman filter
		// TODO compendate OX axis rotation
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			for (int i = 0; i < orientation.length; i++) {
				float temp = gyroscope[i] * dT;
				orientation[i] += temp;
			}
		}
		timestamp = event.timestamp;

		double pan = HOME_PAN + orientation[2] * RAD2DEG * DEG2PAN;
		double tilt = HOME_TILT + orientation[0] * RAD2DEG * DEG2TILT;
		if (pan > 255)
			pan = 255;
		if (pan < 0)
			pan = 0;
		if (tilt > 255)
			tilt = 255;
		if (tilt < 0)
			tilt = 0;

		
		//1 - field for PAN data
		//2 - field for TILT data
		ControlActivity.controlData[1] = (byte) pan;
		ControlActivity.controlData[2] = (byte) tilt;
	}

	public void getAccelerometer() {
		// TODO return accelerometer
	}

	public void getMagnetometer() {
		// TODO return magnetometer
	}

	public void getGyroscope() {
		// TODO return gyroscope
	}

	public float[] getOrientation() {
		return null;
		// TODO
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

}
