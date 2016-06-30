package com.arduino_car.activity.main;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.led.arduino_car.R;

import java.io.IOException;
import java.util.UUID;


public class HomeActivity extends ActionBarActivity implements View.OnTouchListener
{

    //widgets
    Button btnPaired;
    Button forward;
    Button left;
    Button backward;
    Button right;
    //ListView locationsList;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    //private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "98:D3:31:FC:15:BD";
    private BluetoothDevice car;
    public static BluetoothSocket btSocket = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //db = new ACDBHelper(this);

        //Calling widgets
        btnPaired = (Button)findViewById(R.id.connect);
        forward = (Button)findViewById(R.id.forward);
        left = (Button)findViewById(R.id.left);
        backward = (Button)findViewById(R.id.backward);
        right = (Button)findViewById(R.id.right);
        forward.setOnTouchListener(this);
        left.setOnTouchListener(this);
        backward.setOnTouchListener(this);
        right.setOnTouchListener(this);
        //locationsList = (ListView)findViewById(R.id.locationsList);
        //setLocationsList();

        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        /*
        Listener for connect/disconnect button
         */
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(btSocket == null)
                    pairedDevicesList();
                else if(btSocket.isConnected()){
                    try{
                        btSocket.close();
                        btSocket = null;
                        btnPaired.setText(R.string.connect);
                        msg("Disonnected.");
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    private void pairedDevicesList(){

        car = myBluetooth.getRemoteDevice(EXTRA_ADDRESS);
        if(car != null){
            new ConnectBT().execute(); //Call the class to connect
        }
        /*pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                if(bt.getAddress().equals(EXTRA_ADDRESS)) {
                    //list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address

                }
            }
        } */
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        /* final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked */

    }

    /*private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getApplication().getBaseContext(),address, Toast.LENGTH_LONG).show();

            // Make an intent to start next activity.
            Intent i = new Intent(HomeActivity.this, LocationControlActivity.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    Class for managing bluetootj connection.
     */
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(HomeActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(EXTRA_ADDRESS);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                btnPaired.setText(R.string.disconnect);
            }
            progress.dismiss();
        }
    }

    public BluetoothSocket getBtSocket(){
        return this.btSocket;
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    /*private void setLocationsList(){
        ArrayAdapter<String> locations = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,db.getAllLocations());
        locationsList.setAdapter(locations);
        locationsList.setOnItemClickListener(this);
    }*/

    /*
    Listener for the command sent by arrow pressing on the screen.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        int action = event.getAction();
        Log.d("HomeActivity", "Action detected: " + action);
        try {
            if(btSocket != null && !btSocket.isConnected())
                btSocket.connect();
            if(action == MotionEvent.ACTION_DOWN) {
                switch (id) {
                    case R.id.forward:
                        btSocket.getOutputStream().write("F".toString().getBytes());
                        Log.d("HomeActivity", "Command: forward.");
                        break;
                    case R.id.left:
                        btSocket.getOutputStream().write("L".toString().getBytes());
                        Log.d("HomeActivity", "Command: left.");
                        break;
                    case R.id.backward:
                        btSocket.getOutputStream().write("B".toString().getBytes());
                        Log.d("HomeActivity", "Command: backward.");
                        break;
                    case R.id.right:
                        btSocket.getOutputStream().write("R".toString().getBytes());
                        Log.d("HomeActivity", "Command: right.");
                        break;
                }
            }
            else if(action == MotionEvent.ACTION_UP){
                btSocket.getOutputStream().write("S".toString().getBytes());
                Log.d("HomeActivity", "Command: stop.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
