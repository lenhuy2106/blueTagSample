package net.deerest.bluetagsample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int DISCOVERY_STATUS = 0;
    private static final String NO_DEVICES_FOUND_YET = "No devices found yet.";
    private final ArrayList<String> scans = new ArrayList<>();
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter scansAdapter;

    private static class ViewHolder {
        final Switch scanning;
        final Switch discoverable;
        final ListView scanList;
        final EditText tags;

        public ViewHolder(
                final Switch scanning,
                final Switch discoverable,
                final ListView scanList,
                final EditText tags) {
            this.scanning     = scanning;
            this.discoverable = discoverable;
            this.scanList     = scanList;
            this.tags         = tags;
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.i(TAG, "bt device found.");
                Toast.makeText(context, "bt device found.", Toast.LENGTH_LONG).show();
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                scansAdapter.add(
                        device.getName()
                                + "\n" + device.getAddress()
                                + "\n" + new Date());
                scansAdapter.remove(NO_DEVICES_FOUND_YET);
                // loop

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.i(TAG, "discovery finished.");
            }

            bluetoothAdapter.startDiscovery();
        }
    };

    private final TextWatcher watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            final String name = s.length() == 0 ? "default" : s.toString();
            bluetoothAdapter.setName(name);
        }
    };

    private final CompoundButton.OnCheckedChangeListener discoverableChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Intent discoverableIntent;
            if (isChecked) {
                Log.i(TAG, "being discoverable: 0s (endless)");
                discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivityForResult(discoverableIntent, DISCOVERY_STATUS);

            } else {
                // workaround cancel
                Log.i(TAG, "being discoverable: 1s (stop)");
                discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
                startActivityForResult(discoverableIntent, DISCOVERY_STATUS);
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener scanningChangeListener =
        new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "start discovery");
                    bluetoothAdapter.startDiscovery();

                } else {
                    Log.i(TAG, "stop discovery");
                    bluetoothAdapter.cancelDiscovery();
                }
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewHolder views = new ViewHolder(
                (Switch) findViewById(R.id.scan),
                (Switch) findViewById(R.id.discoverable),
                (ListView) findViewById(R.id.scanList),
                (EditText) findViewById(R.id.tags));

        if (bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "bluetooth enabled");
            // TODO: on discoverable and scan
            if (bluetoothAdapter.isDiscovering()) views.scanning.setChecked(true);
            else bluetoothAdapter.startDiscovery();
            views.discoverable.setChecked(true);

        } else {
            views.scanning.setChecked(false);
            views.discoverable.setChecked(false);
        }

        // Register for broadcasts when a device is discovered
        this.registerReceiver(
                receiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // Register for broadcasts when discovery has finished
        this.registerReceiver(
                receiver,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        views.discoverable.setOnCheckedChangeListener(discoverableChangeListener);
        views.scanning.setOnCheckedChangeListener(scanningChangeListener);
        scansAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                scans);
        scansAdapter.add(NO_DEVICES_FOUND_YET);
        views.scanList.setAdapter(scansAdapter);
        views.tags.addTextChangedListener(watcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(this, "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
