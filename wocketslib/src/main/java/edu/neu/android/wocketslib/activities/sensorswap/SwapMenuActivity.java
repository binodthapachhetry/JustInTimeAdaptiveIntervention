package edu.neu.android.wocketslib.activities.sensorswap;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.neu.android.wocketslib.ApplicationManager;
import edu.neu.android.wocketslib.Globals;
import edu.neu.android.wocketslib.R;
import edu.neu.android.wocketslib.sensormonitor.WocketInfoGrabber;
import edu.neu.android.wocketslib.utils.AppUsageLogger;
import edu.neu.android.wocketslib.utils.BaseActivity;
import edu.neu.android.wocketslib.utils.Log;
import edu.neu.android.wocketslib.utils.Wockets;

public class SwapMenuActivity extends BaseActivity{
	private static final String TAG = "SwapMenuActivity"; 
	
	private Button swapBtn;
	private Button changeBtn;
	private Button mywocketsBtn;
	private Wockets wockets = null;
	private WocketsDataReceiver wocketsDataReceiver = null;
	private String warningMsg = null;
//	private static PasswordChecker password = new PasswordChecker(Globals.PW_STAFF_PASSWORD);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TAG);
		this.setContentView(R.layout.swaporchange_menu_activity);
		swapBtn = (Button)this.findViewById(R.id.menuswap);
		changeBtn = (Button)this.findViewById(R.id.menuchange);
		mywocketsBtn = (Button)this.findViewById(R.id.menumywockets);
		wockets = new Wockets();
		swapBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(SwapMenuActivity.this,CheckReadyActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				i.putExtra("wockets", wockets);
				SwapMenuActivity.this.startActivity(i);
				finish();
			}
			
		});	
		changeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(SwapMenuActivity.this,ChangeWocketsBodyDiagramActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("wockets", wockets);
				SwapMenuActivity.this.startActivity(i);
				finish();
			}			
		});
		mywocketsBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AppUsageLogger.logClick(Globals.SWAP_TAG, arg0);
				Intent i = new Intent(SwapMenuActivity.this,MyWockets.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("wockets", wockets);
				SwapMenuActivity.this.startActivity(i);
				finish();
			}			
		});	
		swapBtn.setClickable(false);
		changeBtn.setClickable(false);
		mywocketsBtn.setClickable(false);

		Intent i = new Intent(SwapMenuActivity.this, LoadingInfoActivity.class);
		i.putExtra("isReload", false);
		startActivity(i);
		
	}

	@Override
	public void onPause() {
		super.onPause();
		wocketsDataReceiver = new WocketsDataReceiver();
		registerReceiver(wocketsDataReceiver, new IntentFilter(LoadingInfoActivity.broadcastAction));

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(wocketsDataReceiver);
	}

	private class WocketsDataReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			swapBtn.setClickable(true);
			changeBtn.setClickable(true);
			mywocketsBtn.setClickable(true);
			
			wockets = (Wockets) intent.getSerializableExtra("wockets");	
			warningMsg = intent.getStringExtra("warningmsg");

			String errorMsg = "Oops. ";
			/**
			 * if there is no bluetooth service pop up a warning first.
			 */
			if(! BluetoothAdapter.getDefaultAdapter().isEnabled()){
				AlertDialog.Builder builder = new AlertDialog.Builder(SwapMenuActivity.this);
				builder.setMessage("Your Bluetooth service is off. You can choose to turn it on " +
						"immediately or wait a minute for the system to automatically turn it on.")
				.setPositiveButton("Turn on Bluetooth", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						Intent intentBluetooth = new Intent();
						intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(intentBluetooth); 
						Log.i(Globals.SWAP_TAG, "Exit app, some assigned Wockets are not paired, go to check and pair");
						((ApplicationManager) getApplication()).killAllActivities();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						Log.i(Globals.SWAP_TAG, "Exit app, no sensor info from both local file and server");
						((ApplicationManager) getApplication()).killAllActivities();

					}
				})
				.show();

			}
			else if(wockets == null || wockets.getSensors().size() == 0){
				errorMsg += "No sensors are assigned. Please check your record in server and reload.";
				if(warningMsg != null){
					errorMsg += warningMsg;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(SwapMenuActivity.this);
				builder.setMessage(errorMsg)
				.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if(!WocketInfoGrabber.isActiveInternetConnection(SwapMenuActivity.this)){
							Toast.makeText(SwapMenuActivity.this, 
									"No Internet connection. Unable to reload sensor info right now.", Toast.LENGTH_SHORT).show();
						}
						else{
							Log.i(Globals.SWAP_TAG, "Reload Wockets info from server");
							Intent i = new Intent(SwapMenuActivity.this, LoadingInfoActivity.class);
							i.putExtra("isReload", true);
							startActivity(i);
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Log.i(Globals.SWAP_TAG, "Exit app, no sensor info from both local file and server");
						((ApplicationManager) getApplication()).killAllActivities();

					}
				})
				.show();
			}
			else if(warningMsg != null){
				errorMsg += warningMsg;
				AlertDialog.Builder builder = new AlertDialog.Builder(SwapMenuActivity.this);
				builder.setMessage(errorMsg)
				.setNeutralButton("Fix Now", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						Intent intentBluetooth = new Intent();
						intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(intentBluetooth); 
						Log.i(Globals.SWAP_TAG, "Exit app, some assigned Wockets are not paired, go to check and pair");
						((ApplicationManager) getApplication()).killAllActivities();
	
					}
				})
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//TODO check logging
						Log.i(Globals.SWAP_TAG, "Exit app, some assigned Wockets are not paired, do later");
						((ApplicationManager) getApplication()).killAllActivities();
					}
				})
				.show();
			}
		}
		
	}
	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
	    	menu.add(0, 1, 1, "Reload sensors");
			return super.onCreateOptionsMenu(menu);
		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == 1){
			if(!WocketInfoGrabber.isActiveInternetConnection(SwapMenuActivity.this)){
				Toast.makeText(SwapMenuActivity.this, 
						"No Internet connection. Unable to load sensor info right now.", Toast.LENGTH_SHORT).show();
			}
			else{
				Log.i(Globals.SWAP_TAG, "Reload Wockets info from server");
				Intent i = new Intent(SwapMenuActivity.this, LoadingInfoActivity.class);
				i.putExtra("isReload", true);
				startActivity(i);
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_MENU) {
//			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//			imm.toggleSoftInput(0, 0);
//		} else if (password.isMatch(keyCode)){
//			addWockets();
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		// In case of some mobile devices (Samsung is one) , we don't get
//		// individual key strokes. Instead we get one string with
//		// all the characters that were keyed in
//		if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
//			if (password.equals(event.getCharacters())) {
//				addWockets();
//				return true;
//			}
//		}
//		return super.dispatchKeyEvent(event);
//	}
//	private void addWockets(){
//		Intent i = new Intent(SwapMenuActivity.this,GetPairedSensorsActivity.class);
//		i.putExtra("wockets", wockets);	
//		SwapMenuActivity.this.startActivity(i);
//		finish();
//	}

}
