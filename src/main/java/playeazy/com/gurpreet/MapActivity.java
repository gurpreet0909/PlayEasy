package playeazy.com.gurpreet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    private Location lastlocation;
    private Marker currentLocationMarker;

    public FusedLocationProviderClient fusedLocationProviderClient;

    private static final float CAMERA_ZOOM = 18f;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "LocationAddress";
    private boolean mLocationPermissionsGranted = false;
    private PlaceAutoCompleteAdapter placeAutoCompleteAdapter;
    private AutoCompleteTextView search_map;
    private LatLngBounds LAT_LNG_BOUNDS =  new LatLngBounds(new LatLng(-40.041458, 168.790100),
                                                            new LatLng(71.682247, 136.383362));
    GeoDataClient geoDataClient;
    private TextView mPlaceDetailsText;
    private TextView cancelText;
    private ImageView placePicker;
    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkLocationPermission();

        search_map = findViewById(R.id.input_search);
        cancelText = findViewById(R.id.clear);
        placePicker = findViewById(R.id.placePicker_button);
        search_map.setSingleLine();



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geoDataClient = Places.getGeoDataClient(this,null);
        placeAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this,geoDataClient,LAT_LNG_BOUNDS,null);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (mLocationPermissionsGranted) {
            getLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            mMap.setMyLocationEnabled(true);
            init();
        }
    }

    private void init() {

        search_map.setAdapter(placeAutoCompleteAdapter);
        search_map.setOnItemClickListener(mAutocompleteClickListener);
        cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_map.setText("");
            }
        });
        search_map.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == KeyEvent.ACTION_DOWN
                        || actionId == KeyEvent.KEYCODE_ENTER) {

                    geoLocate();
                }
                return false;
            }
        });

        placePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }


    private void getLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            lastlocation = (Location) task.getResult();
                            moveCamera(new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude()), CAMERA_ZOOM, "You are here");
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

    void moveCamera(LatLng latLng, float zoom, String title) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

      if(!title.equals("You are Here")) {
          MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
          mMap.addMarker(markerOptions);
      }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1000:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                Task<PlaceBufferResponse> placeResult = geoDataClient.getPlaceById(place.getId());
                placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection Failed!", Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void checkLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;

            } else {
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++) {

                        mLocationPermissionsGranted = false;
                        return;
                    }
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }

    private void geoLocate() {

        String searchString = search_map.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList.size() > 0) {

            final Address address = addressList.get(0);
            Runnable NameOfRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), CAMERA_ZOOM, address.getAddressLine(0));

                }
            };
            NameOfRunnable.run();
            Log.i("Current addressssss", address.toString());
        }
    }

    /* ------------------- google places api autocomplete suggetions OnClickListener Impl----------------*/

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = placeAutoCompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            Task<PlaceBufferResponse> placeResult = geoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                final Place place = places.get(0);
                final CharSequence thirdPartyAttribution = places.getAttributions();
                // Format details of the place for display and show it in a TextView.
                //  mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
               //          place.getId(), place.getAddress(),place.getPhoneNumber(),
               //           place.getWebsiteUri()));

                moveCamera(place.getLatLng(),CAMERA_ZOOM, (String) place.getName());

                Log.i(TAG, "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    @SuppressLint("StringFormatInvalid")
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }
}