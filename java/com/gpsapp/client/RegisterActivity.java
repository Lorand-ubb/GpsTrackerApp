package com.gpsapp.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gpsapp.client.api.RetrofitClient;
import com.gpsapp.client.dto.RegisterRequest;

public class RegisterActivity extends AppCompatActivity {
    private EditText regUsername;
    private EditText regEmail;
    private EditText regPassword;
    private Button btnRegister;
    private Button btnToLogin;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regEmail = findViewById(R.id.regEmail);
        btnRegister = findViewById(R.id.btnRegister);
        btnToLogin = findViewById(R.id.btnToLogin);
        errorMessage = findViewById(R.id.errorMessage);

        btnRegister.setOnClickListener(v -> {
            String username = regUsername.getText().toString();
            String email = regEmail.getText().toString();
            String password = regPassword.getText().toString();

            RegisterRequest registerRequest = new RegisterRequest(username, email, password);
            RetrofitClient.getService().registerUser(registerRequest).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Már használatban levő név vagy email!",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Hálózati hiba: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        btnToLogin.setOnClickListener(v -> {


            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}