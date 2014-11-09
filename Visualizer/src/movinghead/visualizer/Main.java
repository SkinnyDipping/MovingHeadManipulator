package movinghead.visualizer;

import java.net.DatagramSocket;
import java.net.SocketException;

import processing.core.PApplet;

public class Main extends PApplet {

	public static class Point {
		public float x, y;

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private static final int ARTNET_PORT = 0x1936;
	private static final String PAN_STRING = "PAN";
	private static final String TILT_STRING = "TILT";
	private Point colorCircleCenter, panCircleCenter, tiltCircleCenter;
	private int circleRadius;
	private int r, g, b;
	private float panValue, tiltValue;
	private DatagramSocket socket;

	public Main() {
		try {
			socket = new DatagramSocket(ARTNET_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "movinghead.visualizer.Main" });
	}

	public void setup() {
		size(1280, 720);
		background(0);
		circleRadius = height / 4;
		colorCircleCenter = new Point(width / 4, height / 2);
		panCircleCenter = new Point(3 * width / 4 - 1.3f * width / 10,
				height / 4);
		tiltCircleCenter = new Point(3 * width / 4 - 1.3f * width / 10,
				3 * height / 4);
		noStroke();
		ellipseMode(RADIUS);
		textAlign(LEFT, CENTER);
		textSize(circleRadius / 3);
	}

	public void draw() {

		clear();

		r = 0x8C;
		g = 0x04;
		b = 0xA8;
		panValue = frameCount % 100; // percent
		tiltValue = frameCount % 72; // percent

		fill(color(r, g, b));
		ellipse(colorCircleCenter.x, colorCircleCenter.y, circleRadius,
				circleRadius);
		fill(color(0x39, 0x14, 0xAF));
		arc(panCircleCenter.x, panCircleCenter.y, circleRadius / 2,
				circleRadius / 2, 0, 2 * PI * panValue / 100, PIE);
		fill(color(0xFF, 0xD3, 0x00));
		arc(tiltCircleCenter.x, tiltCircleCenter.y, circleRadius / 2,
				circleRadius / 2, 0, 2 * tiltValue * PI / 100, PIE);

		fill(255, 255, 255);
		text(PAN_STRING + "  " + (int) panValue + "%",
				(float) (panCircleCenter.x + circleRadius * 3.5 / 5),
				panCircleCenter.y);
		text(TILT_STRING + "  " + (int) tiltValue + "%",
				(float) (tiltCircleCenter.x + circleRadius * 3.5 / 5),
				tiltCircleCenter.y);

		delay(100);
	}

}
