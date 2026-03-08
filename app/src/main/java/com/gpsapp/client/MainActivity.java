package com.gpsapp.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gpsapp.client.api.RetrofitClient;
import com.gpsapp.client.model.LoginRequest;

public class MainActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                LoginRequest loginRequest = new LoginRequest(username, password);
                RetrofitClient.getService().loginUser(loginRequest).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String token = response.body().string();

                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                intent.putExtra("USER_TOKEN", token);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                            "Hiba: Rossz név vagy jelszó!",
                                            Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Hálózati hiba: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}