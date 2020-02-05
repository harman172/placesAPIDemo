package com.example.routes;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    public static final int REQUEST_CODE = 1;

    //get user location
    private FusedLocationProviderClient fusedLocationProviderClient;
//    private double latitude, longitude;
    private LatLng userLocation;
    final int RADIUS = 1500;

    LocationCallback locationCallback;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMap();
        getUserLocation();
        
        if(!checkPermission())
            requestPermission();
        else
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frag_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void getUserLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        setHomeMarker();
    }

    private void setHomeMarker(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location: locationResult.getLocations()){
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());


                    CameraPosition cameraPosition = CameraPosition.builder()
                            .target(userLocation)
                            .bearing(0)
                            .tilt(45)
                            .zoom(15)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mMap.addMarker(new MarkerOptions().position(userLocation)
                    .title("Your location"));
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
//                    .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.marker)));
                }
            }
        };
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                setHomeMarker();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceID){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceID);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void btnClick(View view){
        switch (view.getId()){
            case R.id.btn_restaurants:

                // get the url from places api
                String url = getUrl(userLocation.latitude, userLocation.longitude, "restaurant");
                Log.i("tag", "btnClick: " + url);
                Object[] dataTransfer = new Object[2];
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                GetNearByPlaceData getNearByPlaceData = new GetNearByPlaceData();
                getNearByPlaceData.execute(dataTransfer);
        }
    }


    private String getUrl(double lat, double lng, String nearByPlace){
        StringBuilder placeUrl = new StringBuilder(("https://maps.googleapis.com/maps/api/place/nearbysearch/json?"));
        placeUrl.append("location="+lat+","+lng);
        placeUrl.append("&radius="+RADIUS);
        placeUrl.append("&type="+nearByPlace);
        placeUrl.append("&key="+getString(R.string.api_key));
        return placeUrl.toString();
    }

}

















