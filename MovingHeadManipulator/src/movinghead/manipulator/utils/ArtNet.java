package movinghead.manipulator.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

public class ArtNet {

	private static final String TAG = "ArtNet";
	private static final String BROADCAST_ADDRESS = "255.255.255.255";
	private static final int DEFAULT_PORT = 0x1936;
	private static final int TEMPDATA_SIZE = 530;
	private static final int PROTOCOL = 14;
	private static final int OpArtDMX = 0x5000;
	private static final int TRASMITION_DELAY = 100;

	private static byte[] DMX = new byte[512];

	private DatagramSocket socket;

	private class TransmitionThread implements Runnable {

		private boolean running = true;

		public void stop() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					socket.send(generateArtDMX(DMX));
					Thread.sleep(TRASMITION_DELAY);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					Log.e(TAG,
							"Error sending ArtDMX. Transmition thread terminated");
					break;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG,
							"Error sending ArtDMX. Transmition thread terminated");
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.w(TAG, "Transmition Thread delay interrupted");
				}

			}
		}
	}
	TransmitionThread transmitionThread = new TransmitionThread();

	public ArtNet() {
		try {
			socket = new DatagramSocket(DEFAULT_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void startTransmition() {
		new Thread(transmitionThread).start();
	}

	public void stopTransmition() {
		transmitionThread.stop();
	}
	
	public void setDMX(byte[] DMX) {
		this.DMX = DMX;
	}
	
	public void setDMX(byte DMX, int channel) {
		this.DMX[channel] = DMX;
	}

	public void sendPacket(final DatagramPacket packet) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void sendPacket(final byte[] DMX) {
		try {
			sendPacket(generateArtDMX(DMX));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(final byte[] DMX, final String address) {
		try {
			sendPacket(generateArtDMX(DMX, address));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public DatagramPacket generateArtDMX(byte[] DMX)
			throws UnknownHostException {
		return generateArtDMX(DMX, BROADCAST_ADDRESS);
	}

	public DatagramPacket generateArtDMX(byte[] DMX, final String address)
			throws UnknownHostException {
		byte[] tempData = new byte[TEMPDATA_SIZE];
		// header
		byte[] header = new String("Art-Net\0").getBytes();
		System.arraycopy(header, 0, tempData, 0, header.length);
		// opCode
		tempData[9] = HByte(OpArtDMX);
		tempData[8] = LByte(OpArtDMX);
		// protocol version
		tempData[10] = HByte(PROTOCOL);
		tempData[11] = LByte(PROTOCOL);
		// sequence (unused)
		tempData[12] = (byte) 0;
		// Physical
		tempData[13] = (byte) 0;
		tempData[14] = (byte) 0;
		tempData[15] = (byte) 0;
		// length
		int dataLength = 512;
		tempData[16] = HByte(dataLength);
		tempData[17] = LByte(dataLength);
		// DMX data
		System.arraycopy(DMX, 0, tempData, 18, dataLength);

		return new DatagramPacket(tempData, TEMPDATA_SIZE,
				InetAddress.getByName(address), DEFAULT_PORT);
	}

	private byte HByte(int num) {
		return Integer.valueOf(num >> 8 & 0xff).byteValue();
	}

	private byte LByte(int num) {
		return Integer.valueOf(num & 0xff).byteValue();
	}

}
