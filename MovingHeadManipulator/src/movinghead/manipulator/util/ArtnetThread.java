package movinghead.manipulator.util;

import java.io.*;
import java.net.*;

//import artnet4j.*;
//import artnet4j.events.*;
//import artnet4j.packets.*; 

public class ArtnetThread implements Runnable {// implements ArtNetDiscoveryListener {
	
	private static final String DEFAULT_ADDRESS = "255.255.255.255";//2.139.86.182
	private static final int DEFAULT_PORT = 0x1936;
	private static final int TEMPDATA_SIZE = 530;
	private static final int PROTOCOL = 14;
	
	private static byte[] tempDMX = new byte[512];
	
	
	public ArtnetThread(byte[] data)
	{
//		tempDMX[30] = data[0];	//PAN
//		tempDMX[32] = data[1];	//TILT
//		tempDMX[0] = data[2];	//RED
//		tempDMX[1] = data[3];	//GREEN
//		tempDMX[2] = data[4];	//BLUE
//		tempDMX[7] = data[5];	//DIMMER
		for (int i=0; i<512; i++)
			tempDMX[i] = data[i];
	}

	public void run() {
		
		try {
			
			int index = 0;
			DatagramPacket packet = null;
			DatagramSocket socket = new DatagramSocket(0x1936);
			socket.setBroadcast(true);
				packet = generatePacket(index);
				socket.send(packet);
				index++;
				System.out.println("sent!");
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void broadcastDMX(byte[] data)
	{
		try {			
			int index = 0;
			DatagramPacket packet = null;
			DatagramSocket socket = new DatagramSocket(0x1936);
			socket.setBroadcast(true);
				packet = generatePacket(index);
//				packet = generatePacket(MainActivity.data, index);
				socket.send(packet);
				System.out.println("sent!");
			socket.close();
		} catch (IOException e) {
			System.out.println("Jaki� kurwa error");
			e.printStackTrace();
		}
	}
	
	private static DatagramPacket generatePacket(int seq) throws IOException
	{
		byte[] tempData = new byte[TEMPDATA_SIZE];
		//header
		byte[] header = new String("Art-Net\0").getBytes();
		System.arraycopy(header, 0, tempData, 0, header.length);
//		tempData[7] = (Byte) null;
		//opCode
//		byte[] opCode = new String(Integer.toString(0x5000)).getBytes();
//		System.arraycopy(opCode, 0, tempData, 1, opCode.length);
//		tempData[7] = (0x5000 >> 8 & 0xff); tempData[8] = (0x5000 & 0xff);
		tempData[9] = HByte(0x5000); tempData[8] = LByte(0x5000);
		//protocol version
		tempData[10] = HByte(PROTOCOL); tempData[11] = LByte(PROTOCOL);
		//sequence
//		byte[] sequence = new String(new Integer(seq % 255).toString()).getBytes();
//		System.arraycopy(sequence, 0, tempData, 12, sequence.length);
		tempData[12]=(byte)0;
		//Physical
		tempData[13] = (byte) 0;
		tempData[14] = (byte) 0;//SubUni(14)?
		tempData[15] = (byte) 0;//Net(15)?
		//length
		int dataLength = 512;
		tempData[16] = HByte(dataLength); tempData[17] = LByte(dataLength);
		//dmxdata
//		byte[] tempDMX = new byte[dataLength];
//		tempDMX[0]=(byte) 200; tempDMX[1]=(byte) 200; tempDMX[2]=(byte) 200;
//		for (int i=0; i<dataLength; i++)
//			tempDMX[i] = (byte) (Math.sin(seq * 0.05 + i * 0.8) * 127 + 128);
//		tempDMX[8] = (byte) (seq % 255);
		
		
		System.arraycopy(tempDMX, 0, tempData, 18, dataLength);
		
		return new DatagramPacket(tempData, TEMPDATA_SIZE, InetAddress.getByName(DEFAULT_ADDRESS), DEFAULT_PORT);
	}
	
	@SuppressWarnings("unused")
	private DatagramPacket generatePacket(byte[] data, int seq) throws IOException
	{
		byte[] tempData = new byte[TEMPDATA_SIZE];
		//header
		byte[] header = new String("Art-Net\0").getBytes();
		System.arraycopy(header, 0, tempData, 0, header.length);
		//opCode
		tempData[9] = HByte(0x5000); tempData[8] = LByte(0x5000);
		//protocol version
		tempData[10] = HByte(PROTOCOL); tempData[11] = LByte(PROTOCOL);
		//sequence
		tempData[12]=(byte)0;
		//Physical
		tempData[13] = (byte) 0;
		tempData[14] = (byte) 0;//SubUni(14)?
		tempData[15] = (byte) 0;//Net(15)?
		//length
		int dataLength = 512;
		tempData[16] = HByte(dataLength); tempData[17] = LByte(dataLength);
		//dmxdata
//		byte[] tempDMX = new byte[dataLength];
		System.arraycopy(data, 0, tempDMX, 0, data.length);
		
		System.arraycopy(tempDMX, 0, tempData, 18, dataLength);
		
		
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
	
//	private ArtNetNode netLynx;
//
//    private int sequenceID;
//
//    @Override
//    public void discoveredNewNode(ArtNetNode node) {
//        if (netLynx == null) {
//            netLynx = node;
//            System.out.println("found net lynx");
//        }
//    }
//
//    @Override
//    public void discoveredNodeDisconnected(ArtNetNode node) {
//        System.out.println("node disconnected: " + node);
//        if (node == netLynx) {
//            netLynx = null;
//        }
//    }
//
//    @Override
//    public void discoveryCompleted(List<ArtNetNode> nodes) {
//        System.out.println(nodes.size() + " nodes found:");
//        for (ArtNetNode n : nodes) {
//            System.out.println(n);
//        }
//    }
//
//    @Override
//    public void discoveryFailed(Throwable t) {
//        System.out.println("discovery failed");
//    }
//
//	
//	public void run() {
//		ArtNet artnet = new ArtNet();
//        try {
//            artnet.start();
//            artnet.getNodeDiscovery().addListener(this);
//            artnet.startNodeDiscovery();
//            while (true) {
//                if (netLynx != null) {
//                    ArtDmxPacket dmx = new ArtDmxPacket();
//                    dmx.setUniverse(netLynx.getSubNet(),
//                            netLynx.getDmxOuts()[0]);
//                    dmx.setSequenceID(sequenceID % 255);
//                    byte[] buffer = new byte[510];
//                    for (int i = 0; i < buffer.length; i++) {
//                        buffer[i] =
//                                (byte) (Math.sin(sequenceID * 0.05 + i * 0.8) * 127 + 128);
//                    }
//                    dmx.setDMX(buffer, buffer.length);
//                    artnet.unicastPacket(dmx, netLynx.getIPAddress());
//                    dmx.setUniverse(netLynx.getSubNet(),
//                            netLynx.getDmxOuts()[1]);
//                    artnet.unicastPacket(dmx, netLynx.getIPAddress());
//                    sequenceID++;
//                }
//                Thread.sleep(30);
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (ArtNetException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//	}
	

}
