package com.example.pricepulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity - PricePulse Authentication Screen
 * Authenticates user credentials with backend and establishes persistent session.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;
    private TextView tvForgotPassword;
    private TextView tvGoToRegister;
    private TextView tvGuestContinue;

    private SessionManager sessionManager;
    private PricePulseApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login_submit);
        progressLogin = findViewById(R.id.progress_login);
        tvForgotPassword = findViewById(R.id.tv_login_forgot_password);
        tvGoToRegister = findViewById(R.id.tv_login_go_to_register);
        tvGuestContinue = findViewById(R.id.tv_login_guest_continue);
    }

    private void setupListeners() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLogin.setOnClickListener(v -> performLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this,
                    "Please contact support@pricepulse.in or use Change Password from your profile.",
                    Toast.LENGTH_LONG).show();
        });

        tvGuestContinue.setOnClickListener(v -> {
            // Continue as unauthenticated guest
            navigateToHome();
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.auth_err_empty_email));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.auth_err_empty_password));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!isValid) return;

        setLoading(true);

        ApiModels.LoginRequest request = new ApiModels.LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<ApiModels.TokenResponse>() {
            @Override
            public void onResponse(Call<ApiModels.TokenResponse> call, Response<ApiModels.TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.TokenResponse token = response.body();

                    // Persist authentication token
                    sessionManager.saveAuthToken(token.accessToken);

                    sessionManager.saveFullProfile(
                            token.userId,
                            token.username != null ? token.username : "",
                            token.email != null ? token.email : email,
                            token.fullName != null ? token.fullName : "User",
                            token.phone != null ? token.phone : "",
                            token.role != null ? token.role : "user",
                            token.createdAt != null ? token.createdAt : ""
                    );

                    // Re-initialize ApiClient singleton with authenticated session
                    ApiClient.getInstance(sessionManager);

                    // Fetch complete profile asynchronously to enrich phone/metadata
                    apiService.getMyProfile().enqueue(new Callback<ApiModels.UserModel>() {
                        @Override
                        public void onResponse(Call<ApiModels.UserModel> call, Response<ApiModels.UserModel> meResp) {
                            setLoading(false);
                            if (meResp.isSuccessful() && meResp.body() != null) {
                                ApiModels.UserModel u = meResp.body();
                                sessionManager.saveFullProfile(
                                        u.id,
                                        u.username != null ? u.username : "",
                                        u.email != null ? u.email : email,
                                        u.fullName != null ? u.fullName : "",
                                        u.phone != null ? u.phone : "",
                                        u.role != null ? u.role : "user",
                                        u.createdAt != null ? u.createdAt : ""
                                );
                            }
                            Toast.makeText(LoginActivity.this, "Welcome back, " + sessionManager.getUserName() + "!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        }

                        @Override
                        public void onFailure(Call<ApiModels.UserModel> call, Throwable t) {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        }
                    });

                } else {
                    setLoading(false);
                    if (response.code() == 401) {
                        tilPassword.setError("Invalid email/username or password");
                        Toast.makeText(LoginActivity.this, "Invalid credentials. Please verify and retry.", Toast.LENGTH_LONG).show();
                    } else if (response.code() == 403) {
                        Toast.makeText(LoginActivity.this, "Account is deactivated. Please contact support.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiModels.TokenResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Cannot connect to PricePulse server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressLogin != null) {
            progressLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);
        btnLogin.setAlpha(isLoading ? 0.6f : 1.0f);
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
