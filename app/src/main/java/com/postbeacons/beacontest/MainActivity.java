package com.postbeacons.beacontest;

import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconData;
import org.altbeacon.beacon.BeaconDataNotifier;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.client.BeaconDataFactory;
import org.altbeacon.beacon.client.DataProviderException;
import org.w3c.dom.Text;

import java.util.Collection;
import java.util.List;


public class MainActivity extends ActionBarActivity implements BeaconConsumer, RangeNotifier, BeaconDataNotifier
{

    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    private TextView text = null;
    private Region allRegion = new Region("myMonitoringUniqueId", null, null, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.textStatus);
        text.setText("Starting...");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);


        // beacon isn't found unless we set a parser
        // from http://stackoverflow.com/questions/25027983/is-this-the-correct-layout-to-detect-ibeacons-with-altbeacons-android-beacon-li
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));


        beaconManager.setForegroundScanPeriod(1000);
        beaconManager.setForegroundBetweenScanPeriod(1000);
        beaconManager.setBackgroundScanPeriod(1000);
        beaconManager.setBackgroundBetweenScanPeriod(1000);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText("I just saw an beacon for the first time!");
                    }
                });
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see a beacon");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText("I no longer see a beacon");
                    }
                });
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(allRegion);

            beaconManager.startRangingBeaconsInRegion(allRegion);
            beaconManager.setRangeNotifier(this);

        } catch (RemoteException e) {    }

        text.setText("Service Started!");
    }


    /*
    This is called if we have called startRangingBeaconsInRegion, and this is given to setRangeNotifier.
    It is every second with a list of the most recently seen Beacons
     */
    @Override
    public void didRangeBeaconsInRegion(final Collection<Beacon> iBeacons, Region region) {
        for (Beacon iBeacon : iBeacons) {
            iBeacon.requestData(this);

            final String name = iBeacon.getBluetoothName();
            final String dst = String.format("%.3f", iBeacon.getDistance());
            List<Identifier> ids =  iBeacon.getIdentifiers();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText("Beacon "+ name + " is at " + dst + " metres.");
                }
            });
            //Log.d(TAG, "I see an iBeacon: "+iBeacon.getProximityUuid()+","+iBeacon.getMajor()+","+iBeacon.getMinor());
            //String displayString = iBeacon.getProximityUuid()+" "+iBeacon.getMajor()+" "+iBeacon.getMinor()+"\n";
        }

    }


    /*
    I think, called when iBeacon.requestData(this) is called, and "this" implements BeaconDataNotifier
     */
    @Override
    public void beaconDataUpdate(Beacon iBeacon, BeaconData iBeaconData, DataProviderException e) {
        if (e != null) {
            Log.d(TAG, "data fetch error:"+e);
        }
        if (iBeaconData != null) {
            iBeaconData.setLatitude(1.0);
            iBeaconData.setLongitude(2.0);

            final String str = "" + iBeaconData.getLatitude();
            //Log.d(TAG, "I have an iBeacon with data: uuid="+iBeacon.getProximityUuid()+" major="+iBeacon.getMajor()+" minor="+iBeacon.getMinor()+" welcomeMessage="+iBeaconData.get("welcomeMessage"));
            //String displayString = iBeacon.getProximityUuid()+" "+iBeacon.getMajor()+" "+iBeacon.getMinor()+"\n"+"Welcome message:"+iBeaconData.get("welcomeMessage");
            //displayTableRow(iBeacon, displayString, true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //text.setText("I see " + str + " beacons.");
                }
            });
        }
    }
}
