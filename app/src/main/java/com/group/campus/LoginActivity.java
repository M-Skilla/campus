package com.group.campus;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etRegistration, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etRegistration = findViewById(R.id.etRegistration);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String reg = etRegistration.getText().toString().trim();
            String pw  = etPassword.getText().toString().trim();

            if (reg.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simple authentication check
            if (reg.equals("admin") && pw.equals("admin")) {
                // Successful login, proceed as needed
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to main/home activity if available
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
