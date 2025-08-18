package com.group.campus;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static androidx.constraintlayout.widget.ConstraintSet.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.group.campus.dtos.LoginRequest;
import com.group.campus.dtos.LoginResponse;
import com.group.campus.service.UserService;
import com.group.campus.utils.ApiClient;
import com.group.campus.viewmodels.LoginViewModel;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etRegistration, etPassword;
    private Button btnLogin;

    private CircularProgressIndicator circularIndicator;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etRegistration = findViewById(R.id.etRegistration);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
      
             loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginViewModel.getLoading().observe(this, isLoading -> {
            if (isLoading) {
                circularIndicator.setVisibility(View.VISIBLE);
                loginButton.setVisibility(INVISIBLE);
            } else {
                circularIndicator.setVisibility(GONE);
                loginButton.setVisibility(View.VISIBLE);
            }
        });

        loginViewModel.getSuccessState().observe(this, this::handleLoginSuccess);
        loginViewModel.getErrorMsg().observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        btnLogin.setOnClickListener(v -> {
            String reg = etRegistration.getText().toString().trim();
            String pw  = etPassword.getText().toString().trim();

            if (reg.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            String regNo = registrationEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginViewModel.login(regNo, password);
                       
        });
   
}


    private void handleLoginSuccess(Boolean successState) {
        if (successState) {
            Log.d("LOGIN_SUCCESS", "Login Successful");
            startActivity(new Intent(this, HomeActivity.class));
            finish();

        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            Log.d("LOGIN_SUCCESS", "Login Failed");
        }
    }
}

