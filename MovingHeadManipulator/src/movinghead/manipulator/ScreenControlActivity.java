package movinghead.manipulator;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import movinghead.manipulator.util.ArtNetThreadArtnet4j;
import movinghead.manipulator.util.ArtnetThread;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import artnet4j.ArtNet;
import artnet4j.ArtNetException;
import artnet4j.ArtNetNode;
import artnet4j.packets.ArtDmxPacket;

public class ScreenControlActivity extends Activity {
	
	private byte[] data = {0, 0, 0, 0, 0, 0, 0}; // 0PAN, 1TILT, 2RED, 3GREEN, 4BLUE, 5DIMMER, 6SHUTTER
	private boolean broadcast = false;
	private ArtNet artnet = new ArtNet();
	private static final String unicastAddress = "192.168.0.124";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_control);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_screen_control, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		try {
			artnet.start();
		} catch (ArtNetException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		//SeekBars defs
        SeekBar pan = (SeekBar)findViewById(R.id.pan);
        SeekBar tilt = (SeekBar)findViewById(R.id.tilt);
        SeekBar red = (SeekBar)findViewById(R.id.red);
        SeekBar green = (SeekBar)findViewById(R.id.green);
        SeekBar blue = (SeekBar)findViewById(R.id.blue);
        SeekBar dimmer = (SeekBar)findViewById(R.id.dimmer);
        Switch broadcastSwitch = (Switch)findViewById(R.id.unicastPacket);
                
        broadcastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					broadcast = true;
				else
					broadcast = false;
			}
		});
        
        pan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.pantxt)).setText("PAN "+progress);
				data[0] = (byte)progress;
		        //((TextView)findViewById(R.id.testtext)).setText(showByteArray(data));
//				ArtnetThread.broadcastDMX(new byte[1]);
				if (broadcast)
					broadcastPacket(data);
//				else
//					unicastPacket(data, new ArtNetNode());

			}
		});
        
        tilt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.tilttxt)).setText("TILT "+progress);
				data[1] = (byte)progress;
				if (broadcast)
					broadcastPacket(data);
			}
		});
        
        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.redtxt)).setText("RED "+progress);
				data[2] = (byte)progress;
				if (broadcast)
					broadcastPacket(data);
			}
        });
        
        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.greentxt)).setText("GREEN "+progress);
				data[3] = (byte)progress;
				if (broadcast)
					broadcastPacket(data);
			}
		});
        
        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.bluetxt)).setText("BLUE "+progress);
				data[4] = (byte)progress;
				if (broadcast)
					broadcastPacket(data);
			}
		});
        
        dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				((TextView)findViewById(R.id.dimmertxt)).setText("DIMMER "+progress);
				data[5] = (byte)progress;
				if (broadcast)
					broadcastPacket(data);
			}
		});
        
//        new Thread(new ArtnetThread()).start();
        
//        ((TextView)findViewById(R.id.testtext)).setText(new String(data).subSequence(0, data.length));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		artnet.stop();
	}
	
	private void broadcastPacket(byte[] values) {
		byte[] tempDMX = new byte[512];
		tempDMX[30] = values[0];	//PAN
		tempDMX[32] = values[1];	//TILT
		tempDMX[0] = values[2];		//RED
		tempDMX[1] = values[3];		//GREEN
		tempDMX[2] = values[4];		//BLUE
		tempDMX[7] = values[5];		//DIMMMER
		tempDMX[99] = (byte) 255; 	//SHUTTER
		new Thread(new ArtNetThreadArtnet4j(artnet, tempDMX)).start();
	}
	
	@SuppressWarnings("unused")
	private void unicastPacket(byte[] values, ArtNetNode node) {
		byte[] tempDMX = new byte[512];
		tempDMX[30] = values[0];	//PAN
		tempDMX[32] = values[1];	//TILT
		tempDMX[0] = values[2];		//RED
		tempDMX[1] = values[3];		//GREEN
		tempDMX[2] = values[4];		//BLUE
		tempDMX[7] = values[5];		//DIMMMER
		tempDMX[99] = (byte) 255; 	//SHUTTER
		ArtNetNode node2 = new ArtNetNode();
		try {
			node2.setIPAddress(InetAddress.getByName(unicastAddress));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new ArtNetThreadArtnet4j(artnet, tempDMX, node2)).start();
	}

}
