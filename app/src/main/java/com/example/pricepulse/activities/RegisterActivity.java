package com.example.pricepulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pricepulse.MainActivity;
import com.example.pricepulse.R;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.example.pricepulse.data.remote.PricePulseApiService;
import com.example.pricepulse.data.remote.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RegisterActivity - PricePulse User Registration Screen
 * Registers user securely with PostgreSQL backend and initiates persistent authenticated session.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnRegister;
    private ProgressBar progressRegister;
    private TextView tvGoToLogin;

    private SessionManager sessionManager;
    private PricePulseApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.til_register_name);
        tilEmail = findViewById(R.id.til_register_email);
        tilPhone = findViewById(R.id.til_register_phone);
        tilPassword = findViewById(R.id.til_register_password);
        tilConfirmPassword = findViewById(R.id.til_register_confirm_password);

        etName = findViewById(R.id.et_register_name);
        etEmail = findViewById(R.id.et_register_email);
        etPhone = findViewById(R.id.et_register_phone);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);

        btnRegister = findViewById(R.id.btn_register_submit);
        progressRegister = findViewById(R.id.progress_register);
        tvGoToLogin = findViewById(R.id.tv_register_go_to_login);
    }

    private void setupListeners() {
        etName.addTextChangedListener(createClearErrorWatcher(tilName));
        etEmail.addTextChangedListener(createClearErrorWatcher(tilEmail));
        if (etPhone != null && tilPhone != null) {
            etPhone.addTextChangedListener(createClearErrorWatcher(tilPhone));
        }
        etPassword.addTextChangedListener(createClearErrorWatcher(tilPassword));
        etConfirmPassword.addTextChangedListener(createClearErrorWatcher(tilConfirmPassword));

        btnRegister.setOnClickListener(v -> performRegistration());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private TextWatcher createClearErrorWatcher(final TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void performRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean isValid = true;

        // 1. Full Name validation
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.auth_err_empty_name));
            isValid = false;
        } else if (name.length() < 2) {
            tilName.setError("Full name must be at least 2 characters");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        // 2. Email validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.auth_err_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.auth_err_invalid_email));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // 3. Mobile Number validation
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (TextUtils.isEmpty(phone)) {
            if (tilPhone != null) tilPhone.setError(getString(R.string.auth_err_empty_phone));
            isValid = false;
        } else if (digitsOnly.length() < 10) {
            if (tilPhone != null) tilPhone.setError(getString(R.string.auth_err_invalid_phone));
            isValid = false;
        } else {
            if (tilPhone != null) tilPhone.setError(null);
        }

        // 4. Password validation
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.auth_err_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.auth_err_short_password));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // 5. Confirm Password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.auth_err_empty_confirm_password));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.auth_err_passwords_mismatch));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (!isValid) return;

        // Derive unique username from email
        String emailPrefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (emailPrefix.length() < 3) {
            emailPrefix = emailPrefix + "_" + (100 + new Random().nextInt(900));
        }
        final String finalEmailPrefix = emailPrefix;

        setLoading(true);

        ApiModels.RegisterRequest request = new ApiModels.RegisterRequest(
                emailPrefix,
                email,
                password,
                name,
                phone
        );

        apiService.register(request).enqueue(new Callback<ApiModels.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiModels.TokenResponse> call, Response<ApiModels.TokenResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.TokenResponse token = response.body();

                    // Persist session
                    sessionManager.saveAuthToken(token.accessToken);
                    sessionManager.saveFullProfile(
                            token.userId,
                            token.username != null ? token.username : finalEmailPrefix,
                            token.email != null ? token.email : email,
                            token.fullName != null ? token.fullName : name,
                            token.phone != null ? token.phone : phone,
                            token.role != null ? token.role : "user",
                            token.createdAt != null ? token.createdAt : ""
                    );

                    Toast.makeText(RegisterActivity.this, "Welcome to PricePulse, " + name + "!", Toast.LENGTH_LONG).show();

                    // Navigate directly to MainActivity
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 400) {
                    tilEmail.setError("A user with this email or username already exists");
                    Toast.makeText(RegisterActivity.this, "Email is already registered. Please login.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.TokenResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressRegister != null) {
            progressRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnRegister.setEnabled(!isLoading);
        btnRegister.setAlpha(isLoading ? 0.6f : 1.0f);
    }
}
