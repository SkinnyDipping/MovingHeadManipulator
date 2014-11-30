package movinghead.manipulator.utils;

public class SensorDataPacket {

	private float[] m_accelerometer = new float[3];
	private float[] m_magnetometer = new float[3];
	private float[] m_gyroscope = new float[3];
	private long m_timestamp = (Long) null;

	SensorDataPacket(float[] accelerometer, float[] magnetometer,
			float[] gyroscope) {
		m_accelerometer = accelerometer;
		m_magnetometer = magnetometer;
		m_gyroscope = gyroscope;
	}
	
	SensorDataPacket(float[] accelerometer, float[] magnetometer,
			float[] gyroscope, long timestamp) {
		m_accelerometer = accelerometer;
		m_magnetometer = magnetometer;
		m_gyroscope = gyroscope;
		m_timestamp = timestamp;
	}
	
	public float[] accelerometer() {
		return m_accelerometer;
	}
	
	public float[] magnetometer() {
		return m_magnetometer;
	}
	
	public float[] gyroscope() {
		return m_gyroscope;
	}
	
	public long timestamp() {
		return m_timestamp;
	}

}
