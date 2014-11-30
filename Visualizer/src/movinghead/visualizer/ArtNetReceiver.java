package movinghead.visualizer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ArtNetReceiver {

	private final int ARTNET_PORT = 0x1936;
	private final int ARTNET_PACKET_LENGTH = 530;
	private final int ARTNET_TIMEOUT = (int) 3e3; // [ms]
	private byte[] ArtNetData = new byte[530];
	private byte[] DMXData = new byte[512];

	private boolean TRANSMITION = false;
	private DatagramSocket socket;
	private volatile DatagramPacket packet = new DatagramPacket(ArtNetData,
			ARTNET_PACKET_LENGTH);

	private Thread receiveThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (true) {
				if (TRANSMITION) {
					try {
						socket.receive(packet);
						System.arraycopy(packet.getData(), 18, DMXData, 0, 512);
					} catch (SocketTimeoutException e) {
						System.out.println("Receiving packet timeout");
					} catch (IOException e) {
						e.printStackTrace();
						TRANSMITION = false;
					}
				}
			}
		}
	});

	ArtNetReceiver() {

		try {
			socket = new DatagramSocket(ARTNET_PORT);
			socket.setSoTimeout(ARTNET_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		receiveThread.start();
	}

	public void beginTransmition() {
		TRANSMITION = true;
	}

	public void terminateTransmition() {
		TRANSMITION = false;
	}

	public int getData(int channel) {
		return DMXData[channel] & 0xFF;
	}

}
