package movinghead.manipulator.util;

import java.io.*;
import java.net.*;

public class ArtNet {
	
	private static final String DEFAULT_ADDRESS = "255.255.255.255";
	private static final int DEFAULT_PORT = 0x1936;
	private static final int TEMPDATA_SIZE = 530;
	private static final int PROTOCOL = 14;
	
	private static byte[] DMX = new byte[512];
	
	private DatagramSocket socket;
	
	
	public ArtNet()
	{
		init();
	}
	
	public void sendPacket(byte[] DMX) {
		broadcastPacket(DMX);
	}
	
	private void init() {
		try {
			socket = new DatagramSocket(DEFAULT_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void broadcastPacket(final byte[] DMX) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DatagramPacket packet = generateArtDMX(DMX);
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void brPacket(final DatagramPacket packet) {
		
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
	
	public static DatagramPacket generateArtDMX(byte[] DMX) throws UnknownHostException {
		byte[] tempData = new byte[TEMPDATA_SIZE];
		//header
		byte[] header = new String("Art-Net\0").getBytes();
		System.arraycopy(header, 0, tempData, 0, header.length);
		//opCode
		tempData[9] = HByte(0x5000); tempData[8] = LByte(0x5000);
		//protocol version
		tempData[10] = HByte(PROTOCOL); tempData[11] = LByte(PROTOCOL);
		//sequence (unused)
		tempData[12]= (byte) 0;
		//Physical
		tempData[13] = (byte) 0;
		tempData[14] = (byte) 0;
		tempData[15] = (byte) 0;
		//length
		int dataLength = 512;
		tempData[16] = HByte(dataLength); tempData[17] = LByte(dataLength);
		//DMX data
		System.arraycopy(DMX, 0, tempData, 18, dataLength);
		
		return new DatagramPacket(tempData, TEMPDATA_SIZE, InetAddress.getByName(DEFAULT_ADDRESS), DEFAULT_PORT);
	}
	
	private static byte HByte (int num)
	{
		return Integer.valueOf(num >> 8 & 0xff).byteValue();
	}
	
	private static byte LByte (int num)
	{
		return Integer.valueOf(num & 0xff).byteValue();
	}	

}
