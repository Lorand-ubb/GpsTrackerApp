package com.gpsapp.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gpsapp.client.api.RetrofitClient;
import com.gpsapp.client.dto.LocationDto;
import com.gpsapp.client.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private EditText etProfileUsername;
    private EditText etProfileEmail;
    private Button btnEditProfile;
    private Button btnSaveProfile;
    private Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etProfileUsername = findViewById(R.id.etProfileUsername);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Felhasználói adatok betöltése
        String token = getIntent().getStringExtra("USER_TOKEN");
        RetrofitClient.getService()
                .getMe("Bearer " + token)
                .enqueue(new retrofit2.Callback<UserDTO>() {
                    @Override
                    public void onResponse(retrofit2.Call<UserDTO> call, retrofit2.Response<UserDTO> response) {
                        if (response.isSuccessful()) {
                            UserDTO userDTO = response.body();
                            etProfileUsername.setText(userDTO.getUsername());
                            etProfileEmail.setText(userDTO.getEmail());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<UserDTO> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Adatok módosítása
        btnEditProfile.setOnClickListener(v ->{
            etProfileUsername.setEnabled(true);
            etProfileEmail.setEnabled(true);

            etProfileUsername.requestFocus();

            btnSaveProfile.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.GONE);
        });
        btnSaveProfile.setOnClickListener(v ->{
        etProfileUsername.setEnabled(false);
        etProfileEmail.setEnabled(false);
        btnSaveProfile.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);

        String newUsername = etProfileUsername.getText().toString();
        String newEmail = etProfileEmail.getText().toString();

        RetrofitClient.getService()
                    .updateMe("Bearer " + token, new UserDTO(newUsername, newEmail))
                    .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                        @Override
                        public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    String message = response.body().string();
                                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        "Ez a név vagy email már foglalt!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                            Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        RetrofitClient.getService()
                .getAllLocations("Bearer " + token)
                .enqueue(new Callback<List<LocationDto>>() {
                    @Override
                    public void onResponse(Call<List<LocationDto>> call, Response<List<LocationDto>> response) {
                        if (response.isSuccessful()) {
                            List<LocationDto> locations = response.body();
                            List<String> locationStrings = new ArrayList<>();
                            for (LocationDto location : locations) {
                                locationStrings.add(location.getLabel());
                            }

                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<>(ProfileActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            locationStrings);

                            ListView listView = findViewById(R.id.etProfileList);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    LocationDto location = locations.get(position);

                                    Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
                                    intent.putExtra("USER_TOKEN", token);
                                    intent.putExtra("latitude", location.getLatitude());
                                    intent.putExtra("longitude", location.getLongitude());
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LocationDto>> call, Throwable t) {

                    }
                });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}