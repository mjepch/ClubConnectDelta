package com.CCDelta.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends Activity implements LocationListener {

    TextView content;
    EditText username;
    String SentUser;
    GoogleMap map;
    LocationManager myLocation;
    String provider;
    String ourUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/


        //this is the tab block
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Members");
        tabSpec.setContent(R.id.Members);
        tabSpec.setIndicator("Members");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Clubs");
        tabSpec.setContent(R.id.Clubs);
        tabSpec.setIndicator("Clubs");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Results");
        tabSpec.setContent(R.id.Results);
        tabSpec.setIndicator("Results");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Map");
        tabSpec.setContent(R.id.Map);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);


        //this is all the stuff displayed, for the most part.
        username = (EditText)findViewById(R.id.username); //R.id.user, so that
        content = (TextView)findViewById( R.id.content ); //R.id.content
        Button sending=(Button)findViewById(R.id.senddata);
        sending.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    final String toResponse = "http://hnat-server.cs.memphis.edu/~mhrnndez/Club%20Connect/Phase1Beta/?member=" + username.getText().toString();
                    GetResponse(toResponse);
                } catch (Exception e) {
                    Log.e("log_observe_head", e.toString());
                }
            }
        });

        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        Button toMap=(Button)findViewById(R.id.sendtomap);
        toMap.setOnClickListener(new Button.OnClickListener() {
             public void onClick(View v) {
                 try{
                    final String toMapThing = "http://hnat-server.cs.memphis.edu/~mhrnndez/Club%20Connect/Phase1Beta/?member=" + username.getText().toString();
                    GetMapDone(toMapThing);
                 } catch (Exception e) {
                    Log.e("log_observe_mapper", e.toString());
                 }
             }
        });

        Button gamers=(Button)findViewById(R.id.GamingClub);
        gamers.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    final String toResponse = "http://hnat-server.cs.memphis.edu:2345/clubs/The%20Gaming%20Club";
                    GetMulti(toResponse);
                } catch (Exception e) {
                    Log.e("log_observe_game", e.toString());
                }
            }
        });
        Button writers=(Button)findViewById(R.id.BookClub);
        writers.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    final String toResponse = "http://hnat-server.cs.memphis.edu:2345/clubs/The%20Book%20Club";
                    GetMulti(toResponse);
                } catch (Exception e) {
                    Log.e("log_observe_book", e.toString());
                }
            }
        });
        Button actors=(Button)findViewById(R.id.MovieClub);
        actors.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    final String toResponse = "http://hnat-server.cs.memphis.edu:2345/clubs/The%20Movie%20Club";
                    GetMulti(toResponse);
                } catch (Exception e) {
                    Log.e("log_observe_movie",  e.toString());
                }
            }
        });



    final EditText thisName = new EditText(this);
    thisName.setHint("MarioM");

    new AlertDialog.Builder(this)
            .setTitle("Club Connect Name")
            .setMessage("Please input your username within Club Connect")
            .setView(thisName)
            .setPositiveButton("Enter", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    ourUser = thisName.getText().toString();
                    locationUpdater();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){

                }
            })
            .show();

    }


    //this is for the location updating, as indicated by the name.
    public void locationUpdater(){

        myLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabledGPS = myLocation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = myLocation.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!enabledGPS && !enabledWiFi) {
            Toast.makeText(getApplicationContext(), "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        Criteria criteria = new Criteria();
        provider = myLocation.getBestProvider(criteria, true);
        Location location = myLocation.getLastKnownLocation(provider);

        if (location != null) {
            Toast.makeText(getApplicationContext(), "Selected Provider " + provider,
                    Toast.LENGTH_SHORT).show();
            onLocationChanged(location);
        }
        myLocation.requestLocationUpdates(provider, 400, 1, this);
    }

    //these three are more or less autogenerated stuff. Regard as regarded.
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    //this one is actually important, since it's how you update your location and all.
    double newLat;
    double newLong;
    @Override
    public void onLocationChanged(Location location) {
        newLat = (location.getLatitude());
        newLong = (location.getLongitude());
        try
        {
            new HttpLocation().execute(new URI("http://hnat-server.cs.memphis.edu/~mhrnndez/Club%20Connect/LocationPHP/?member=" + ourUser + "&lat=" + newLat + "&lon=" + newLong));
        } catch (Exception e) {
            Log.e("log_locator", "new location " + e.toString());
        }
    }

    private class HttpLocation extends AsyncTask<URI, Void, Void> {  //this will be used to update your location in the database.

        @Override
        protected Void doInBackground(URI... urls) {
            try{
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet request = new HttpGet();
                request.setURI(urls[0]);
                HttpResponse response = httpclient.execute(request);

                //actually very basic since we aren't trying to print anything back.
            }catch(Exception e){
                Log.e("log_locator", "async " + e.toString());
            }

            return null;
        }


        protected void onPostExecute(Void moot){

        }
    }




    public void GetResponse(String toGetter){
        try {
            new HttpGetter().execute(new URI(toGetter));
        } catch (URISyntaxException e) {
            Log.e("log_observe", "printres" + e.toString());
        }
    } //this is for printing back stuff from HttpGetter

    private class HttpGetter extends AsyncTask<URI, Void, Void> {  //this version will end up being used for the "Members" tab, for id-ing club affiliation and rank
        String toBeJSONed = null;
        String toBePrinted = null;
        @Override
        protected Void doInBackground(URI... urls) {
            BufferedReader in = null;
            String data = null;
            StringBuilder lastbit = new StringBuilder();
            String line = null;
            try{
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet request = new HttpGet();
                request.setURI(urls[0]);
                HttpResponse response = httpclient.execute(request);
                in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                while (true)
                {
                line = in.readLine();
                if (line == null)
                    break;
                    lastbit.append(line + "\n");
                }
                toBeJSONed = lastbit.toString();


                StringBuilder toPrinter = new StringBuilder();

                JSONObject j_item = new JSONObject(toBeJSONed);
                String clubname = null;
                String rankname = null;
                clubname = j_item.getString("club");
                rankname = j_item.getString("rank");
                toPrinter.append(clubname + "\n" + rankname + "\n");

                toBePrinted = toPrinter.toString();


                //send toBePrinted to the main page.
            }catch(Exception e){
                Log.e("log_observe_async", "async " + e.toString());
            }

            return null;
        }


        protected void onPostExecute(Void moot){
            content.setText( toBePrinted );
        }
    }



    public void GetMulti(String toGetter){
        try {
            new HttpClubber().execute(new URI(toGetter));
        } catch (URISyntaxException e) {
            Log.e("log_observe", "printres" + e.toString());
        }
    } //this is for printing back stuff from HttpClubber, not to be confused with HttpGetter

    private class HttpClubber extends AsyncTask<URI, Void, Void> { //this one for the finding club members and their ranks
        String toBeJSONed = null;
        String toBePrinted = null;
        @Override
        protected Void doInBackground(URI... urls) {
            BufferedReader in = null;
            String data = null;
            StringBuilder lastbit = new StringBuilder();
            String line = null;
            try{
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet request = new HttpGet();
                request.setURI(urls[0]);
                HttpResponse response = httpclient.execute(request);
                in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                while (true)
                {
                    line = in.readLine();
                    if (line == null)
                        break;
                    lastbit.append(line + "\n");
                }
                toBeJSONed = lastbit.toString();


                JSONArray j_set = new JSONArray(toBeJSONed);
                StringBuilder toPrinter = new StringBuilder();
                for(int i=0;i<j_set.length();i++)
                {
                    JSONObject j_row = j_set.getJSONObject(i);
                    String item1 = j_row.getString("members");
                    String item2 = j_row.getString("rank");
                    toPrinter.append(item1 + " | " + item2 + "\n");
                }
                toBePrinted = toPrinter.toString(); //does it work?


                //send toBePrinted to the main page.
            }catch(Exception e){
                Log.e("log_observe", "async" + e.toString());
            }

            return null;
        }


        protected void onPostExecute(Void moot){
            content.setText( toBePrinted );
        }
    }



    public void GetMapDone(String toGetter){
        try {
            new MapFinder().execute(new URI(toGetter));
        } catch (URISyntaxException e) {
            Log.e("log_observe", "printres" + e.toString());
        }
    } //this is for sending individual stuffs to map

    private class MapFinder extends AsyncTask<URI, Void, Void> { //this one isn't for until later
        String toBeJSONed = null;
        double latitude = 0.000000;
        double longitude = 0.000000;
        String clubname = null;
        String rankname = null;
        @Override
        protected Void doInBackground(URI... urls) {
            BufferedReader in = null;
            String data = null;
            StringBuilder lastbit = new StringBuilder();
            String line = null;
            try{
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet request = new HttpGet();
                request.setURI(urls[0]);
                HttpResponse response = httpclient.execute(request);
                in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));

                while (true)
                {
                    line = in.readLine();
                    if (line == null)
                        break;
                    lastbit.append(line + "\n");
                }
                toBeJSONed = lastbit.toString();


                JSONObject j_item = new JSONObject(toBeJSONed);
                latitude = j_item.getDouble("lat");
                longitude = j_item.getDouble("lon");
                clubname = j_item.getString("club");
                rankname = j_item.getString("rank");


                //send toBePrinted to the main page.
            }catch(Exception e){
                Log.e("log_observe", "async" + e.toString());
            }

            return null;
        }


        protected void onPostExecute(Void moot){
            LatLng newPoint = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions()
                    .title(clubname)
                    .snippet(rankname)
                    .position(newPoint));
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
