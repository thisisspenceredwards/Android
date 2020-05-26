package edu.sjsu.android.design;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import java.util.Arrays;

import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Context;
import android.widget.AdapterView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.model.Place;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import com.google.maps.model.PlacesSearchResult;
import android.location.LocationListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import android.widget.Spinner;
import java.util.Locale;
import android.widget.AdapterView.OnItemSelectedListener;
import com.google.android.gms.maps.UiSettings;
import android.view.View.OnClickListener;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class FindActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener, LocationListener{
    Toolbar tb;
    Geocoder geocoder;
    List<Address> addresses;
    PlacesSearchResult[] placesSearchResults;
    int places;
    boolean clickedIn = false;
    boolean clickedOut = false;
    private static final String TAG = "TAG";
    GoogleMap map;
    GPSTracker gps;
    TextView businessName, businessAddress, businessPhone;
    static double lat, longi;
    Button btnGym, btnHike;
    int numResults;

    double getLat()
    {
        return this.lat;
    }
    double getLong()
    {
        return this.longi;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onHike(View v)
    {
        clickedOut = !clickedOut;
        Log.d(TAG, String.valueOf(clickedOut));
        restActivity();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onGym(View v)
    {
        clickedIn = !clickedIn;
        Log.d(TAG, String.valueOf(clickedIn));
        restActivity();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Toolbar
        setContentView(R.layout.map);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        restActivity();
    }

    private void restActivity()
    {
        tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle("StayFit");
        tb.setBackgroundColor(Color.WHITE);
        setSupportActionBar(tb);

        btnGym = (Button) findViewById(R.id.gym);
        btnHike = (Button) findViewById(R.id.outdoor);

        btnGym.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                onGym(v);
            }
        });
        btnHike.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                onHike(v);
            }
        });



        gps = new GPSTracker(FindActivity.this);
        lat = gps.getLatitude();
        longi = gps.getLongitude();

        // Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // Business Information
        businessName = findViewById(R.id.bizname);
        businessAddress = findViewById(R.id.bizaddress);
        businessPhone = findViewById(R.id.bizphone);

        // Autocomplete

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCrVLco1jo2RRAgtlHZv_Fqf86Z4WbBpPc");
        }
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {

                LatLng queriedLocation = place.getLatLng();
                map.addMarker(new MarkerOptions().position(place.getLatLng()));
                map.moveCamera(CameraUpdateFactory.newLatLng(queriedLocation));
                map.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );

                businessName.setText(place.getName());
                businessAddress.setText(place.getAddress());
                businessPhone.setText(place.getPhoneNumber());

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

        });
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        String addy;
        this.map = googleMap;

        if(clickedIn)
        {
            placesSearchResults = new NearbySearch().run().results;
            map.getUiSettings().setZoomControlsEnabled(true);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Geo
            geocoder = new Geocoder(this, Locale.getDefault());
            //Nearby Places

            placesSearchResults = new NearbySearch().run().results;
            int numResults = placesSearchResults.length;
            if(placesSearchResults == null)
                numResults = 0;
            else
                numResults = placesSearchResults.length;

            for (places =  1; places < numResults; places += 1)
            {
                double latPlace = placesSearchResults[places].geometry.location.lat;
                double longPlace = placesSearchResults[places].geometry.location.lng;
                try {
                    addresses = geocoder.getFromLocation(latPlace, longPlace, 1);
                    addy = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    addy = null;
                }
                String name = placesSearchResults[places].name;
                String formatAddr = addy;
                float rating = placesSearchResults[places].rating;

                double lngPlace = placesSearchResults[places].geometry.location.lng;
                map.setOnMarkerClickListener(this);
                map.addMarker(new MarkerOptions()
                        .title(name)
                        .snippet("\t\t\t\t\t\t\t\t\t\t\t\t\tRating:  " + rating + " stars" +
                                "\n\n " + formatAddr)
                        .position(new LatLng(latPlace, lngPlace))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            }
            map.setMinZoomPreference(10.0f);
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,longi)));
        }
        else if(clickedOut) {
            placesSearchResults = new NearbyOutdoor().run().results;
            map.getUiSettings().setZoomControlsEnabled(true);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Geo
            geocoder = new Geocoder(this, Locale.getDefault());
            //Nearby Places
            int numResults = placesSearchResults.length;
            if(placesSearchResults == null)
                numResults = 0;
            else
                numResults = placesSearchResults.length;

            for (places =  1; places < numResults; places += 1)
            {
                double latPlace = placesSearchResults[places].geometry.location.lat;
                double longPlace = placesSearchResults[places].geometry.location.lng;
                try {
                    addresses = geocoder.getFromLocation(latPlace, longPlace, 1);
                    addy = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    addy = null;
                }
                String name = placesSearchResults[places].name;
                String formatAddr = addy;
                float rating = placesSearchResults[places].rating;

                double lngPlace = placesSearchResults[places].geometry.location.lng;
                map.setOnMarkerClickListener(this);
                map.addMarker(new MarkerOptions()
                        .title(name)
                        .snippet("\t\t\t\t\t\t\t\t\t\t\t\t\tRating:  " + rating + " stars" +
                                "\n\n " + formatAddr)
                        .position(new LatLng(latPlace, lngPlace))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            }
            map.setMinZoomPreference(10.0f);
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,longi)));
        }
        else
            map.clear();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.findItem(R.id.GRAPHSETTINGS).setVisible(false);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.ACTIVITYLOG:
                Intent findActivityStart = new Intent(this, ActivityLog.class);
                startActivity(findActivityStart);
                return true;

            case R.id.FINDACTIVITY:
                Toast toast = Toast.makeText(this,
                        "You are already on this page", Toast.LENGTH_SHORT);
                toast.show();
                return true;

            case R.id.SETTINGS:
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                return true;

            case R.id.GRAPHSETTINGS:
                return true;

            default:

                super.onOptionsItemSelected(item);

        }
        return true;

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        businessName.setText(marker.getTitle());
        businessAddress.setText(marker.getSnippet());
        marker.showInfoWindow();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18);
        map.animateCamera(update);
        return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        gps.onLocationChanged(location);
        lat = location.getLongitude();
        longi =location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}