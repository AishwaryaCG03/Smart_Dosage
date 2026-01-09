package com.example.smart_dosage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        TextInputEditText etEmail = findViewById(R.id.et_email);
        TextInputEditText etPassword = findViewById(R.id.et_password);

        android.content.SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        boolean alreadyLogged = sp.getBoolean("logged_in", false);
        if (alreadyLogged) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            try {
                String email = safeText(etEmail).trim().toLowerCase();
                String pass = safeText(etPassword);
                String savedEmail = sp.getString("email", null);
                String savedPass = sp.getString("password", null);
                if (savedEmail == null || savedPass == null) {
                    Toast.makeText(this, "Please sign up first", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (savedEmail.equals(email) && savedPass.equals(pass)) {
                    sp.edit().putBoolean("logged_in", true).apply();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_signup).setOnClickListener(v -> {
            try {
                String email = safeText(etEmail).trim().toLowerCase();
                String pass = safeText(etPassword);
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pass.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.edit().putString("email", email).putString("password", pass).putBoolean("logged_in", true).apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safeText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }
}
