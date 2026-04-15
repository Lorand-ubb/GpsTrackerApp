package com.gpsapp.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gpsapp.client.api.RetrofitClient;
import com.gpsapp.client.databinding.ActivityMapsBinding;
import com.gpsapp.client.dto.LocationDto;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String token;
    private Marker tempMarker;
    private Marker markerToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.floatingActionButton.setOnClickListener(v -> {
            setTempMarkerButtonsVisibility(false);
            setTempMarkerVisibility(false);
        });

        binding.floatingActionButton2.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Új hely mentése");

            EditText labelEditText = new EditText(MapsActivity.this);
            builder.setView(labelEditText);
            builder.setPositiveButton("Ok", (dialog, which) -> {
                String label = labelEditText.getText().toString();
                if (!label.isEmpty()) {
                    if (tempMarker != null) {
                        LatLng position = tempMarker.getPosition();
                        saveDeviceLocation(position.latitude, position.longitude, label);
                        setTempMarkerButtonsVisibility(false);
                        setTempMarkerVisibility(false);
                    }
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        binding.floatingActionButton3.setOnClickListener(v -> {
            if (markerToDelete != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Biztosan törli a(z)" + markerToDelete.getTitle() + "helyet?");
                builder.setPositiveButton("Igen", (dialog, which) -> {
                    RetrofitClient
                            .getService()
                            .deleteLocation(markerToDelete.getPosition().latitude, markerToDelete.getPosition().longitude, "Bearer " + token)
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        markerToDelete.remove();
                                        Toast.makeText(MapsActivity.this, "Sikeresen törölve!", Toast.LENGTH_SHORT).show();
                                        binding.floatingActionButton3.setVisibility(View.GONE);
                                        markerToDelete = null;
                                    } else {
                                        Toast.makeText(MapsActivity.this, response.code(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(MapsActivity.this, "Hiba a törlés során: \n" + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                });
                builder.setNegativeButton("Nem", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });

        binding.fabProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
            intent.putExtra("USER_TOKEN", token);
            startActivity(intent);
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        token = getIntent().getStringExtra("USER_TOKEN");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable the blue dot
            mMap.setMyLocationEnabled(true);

            Intent intent = getIntent();

            // Check if the intent has the latitude and longitude
            if (intent.hasExtra("latitude")) {
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
            } else {
                // Get the current location
                getDeviceLocation();
            }
            loadSavedLocations();
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mMap.setOnMapClickListener(latLng -> {
            binding.floatingActionButton3.setVisibility(View.GONE);
            if (markerToDelete != null) {
                markerToDelete = null;
            }
            if (tempMarker != null) {
                tempMarker.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            tempMarker = mMap.addMarker(markerOptions);
            setTempMarkerButtonsVisibility(true);
        });

        mMap.setOnMarkerClickListener(marker -> {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
            if (!marker.equals(tempMarker)) {
                binding.floatingActionButton3.setVisibility(View.VISIBLE);
                markerToDelete = marker;
                setTempMarkerVisibility(false);
                setTempMarkerButtonsVisibility(false);
            }
            return false;
        });
    }

    private void setTempMarkerVisibility(boolean visibility) {
        if (tempMarker != null) {
            if (visibility == true)
                tempMarker.setVisible(true);
            else if (visibility == false) {
                tempMarker.remove();
                tempMarker = null;
            }
        }
    }
    private void setTempMarkerButtonsVisibility(boolean visibility) {
        binding.floatingActionButton.setVisibility(visibility ? View.VISIBLE : View.GONE);
        binding.floatingActionButton2.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if we have a answer
        if (requestCode == 1) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Enable the location layer
                mMap.setMyLocationEnabled(true);

                getDeviceLocation();
                loadSavedLocations();
            } else {
                Toast.makeText(this, "GPS engedély nélkül nem látjuk, hol vagy!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getDeviceLocation() {
        try {
           if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                   if (location != null) {
                       double lat = location.getLatitude();
                       double lng = location.getLongitude();

                       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
                   } else {
                       Toast.makeText(MapsActivity.this,
                               "A jelenlegi helyzet ismeretlen (kapcsold be a GPS-t!)",
                               Toast.LENGTH_SHORT).show();
                   }
               });
           }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void saveDeviceLocation(double lat, double lng, String label) {
        LocationDto locationDto = new LocationDto(lat, lng, label);

        RetrofitClient.getService().saveLocation(locationDto, "Bearer " + token).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MapsActivity.this, "A hely sikeresen el lett mentve!", Toast.LENGTH_SHORT).show();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(label));
                } else {
                    Toast.makeText(MapsActivity.this,
                            "Nem sikerült menteni a helyszínt!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Hiba: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSavedLocations() {
        RetrofitClient.getService().getAllLocations("Bearer " + token).enqueue(new Callback<List<LocationDto>>() {
            @Override
            public void onResponse(Call<List<LocationDto>> call, Response<List<LocationDto>> response) {
                if (response.body() != null){
                    for (LocationDto location : response.body()){
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location.getLabel()));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LocationDto>> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Hiba: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}