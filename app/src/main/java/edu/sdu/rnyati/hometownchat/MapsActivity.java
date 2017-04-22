package edu.sdu.rnyati.hometownchat;

import android.content.Intent;
import android.location.Address;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Geocoder;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener  {

    private GoogleMap mMap;
    private double latitude, longitude;
    private String country, state, nickname, city, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        country = getIntent().getStringExtra("country");
        state = getIntent().getStringExtra("state");
        nickname = getIntent().getStringExtra("nickname");
        city = getIntent().getStringExtra("city");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getUserLocation(){
        if(country != null && state != null){
            address = state + ", " + country;
            Geocoder locator = new Geocoder(this);
            try{
                List<Address> state = locator.getFromLocationName(address, 1);
                for (Address stateLocation: state){
                    if(stateLocation.hasLatitude())
                        latitude = stateLocation.getLatitude();
                    if(stateLocation.hasLongitude())
                        longitude = stateLocation.getLongitude();
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.e("rag", "Address lookup Error.", e);
            }
            LatLng stateLatLng = new LatLng(latitude, longitude);
            CameraUpdate newLocation = CameraUpdateFactory.newLatLngZoom(stateLatLng, 6);
            mMap.addMarker(new MarkerOptions().position(stateLatLng).title(nickname));
            mMap.moveCamera(newLocation);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraChangeListener(this);

        getUserLocation();
    }

    public void onSetLocationClicked(View button) {
        Intent toPassBack = getIntent();
        toPassBack.putExtra("latitude", latitude);
        toPassBack.putExtra("longitude", longitude);
        setResult(RESULT_OK, toPassBack);
        finish();
    }

    @Override
    public void onMapClick(LatLng location) {
        Log.i("rag", "new Location " + location.latitude + " longitude " + location.longitude );
        latitude = location.latitude;
        longitude = location.longitude;
        LatLng myLocation = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions().position(myLocation).title(nickname));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }

    @Override
    public void onCameraChange(CameraPosition position){
        //Log.i("rag", " position " + position.target.latitude + " longitude " + position.target.longitude);
    }
}
