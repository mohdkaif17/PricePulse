package com.example.pricepulse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pricepulse.activities.LoginActivity;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.example.pricepulse.data.remote.PricePulseApiService;
import com.example.pricepulse.data.remote.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PricePulseApiService apiService;

    private TextView txtAvatarInitials;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private TextView txtUserPhone;
    private TextView txtUserRole;
    private TextView txtUserStatus;
    private TextView txtUserMemberId;
    private TextView txtUserMemberSince;
    private ProgressBar progressProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        View rootView = findViewById(R.id.profileRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        initViews();
        bindCachedProfile();
        fetchLiveProfile();
    }

    private void initViews() {
        txtAvatarInitials = findViewById(R.id.txtAvatarInitials);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtUserPhone = findViewById(R.id.txtUserPhone);
        txtUserRole = findViewById(R.id.txtUserRole);
        txtUserStatus = findViewById(R.id.txtUserStatus);
        txtUserMemberId = findViewById(R.id.txtUserMemberId);
        txtUserMemberSince = findViewById(R.id.txtUserMemberSince);
        progressProfile = findViewById(R.id.progressProfile);

        // Top bar back button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Edit Profile row
        View rowEditProfile = findViewById(R.id.rowEditProfile);
        if (rowEditProfile != null) {
            rowEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        // Change Password row
        View rowChangePassword = findViewById(R.id.rowChangePassword);
        if (rowChangePassword != null) {
            rowChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // Manage Listings row
        View rowManageListings = findViewById(R.id.rowManageListings);
        if (rowManageListings != null) {
            rowManageListings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ManageListingsActivity.class);
                startActivity(intent);
            });
        }

        // Location row
        View rowLocation = findViewById(R.id.rowLocation);
        if (rowLocation != null) {
            rowLocation.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.setting_location),
                    getString(R.string.msg_location_dialog)
            ));
        }

        // Notifications switch
        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Notifications enabled" : "Notifications disabled";
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            });
        }

        // Price Alerts switch
        SwitchMaterial switchPriceAlerts = findViewById(R.id.switchPriceAlerts);
        if (switchPriceAlerts != null) {
            switchPriceAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Price alerts enabled" : "Price alerts disabled";
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            });
        }

        // Dark Mode switch
        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Dark mode preview ON" : "Dark mode OFF";
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            });
        }

        // About row
        View rowAbout = findViewById(R.id.rowAbout);
        if (rowAbout != null) {
            rowAbout.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.setting_about),
                    getString(R.string.msg_about_dialog)
            ));
        }

        // Help row
        View rowHelp = findViewById(R.id.rowHelp);
        if (rowHelp != null) {
            rowHelp.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.setting_help),
                    getString(R.string.msg_help_dialog)
            ));
        }

        // Privacy Policy row
        View rowPrivacy = findViewById(R.id.rowPrivacy);
        if (rowPrivacy != null) {
            rowPrivacy.setOnClickListener(v -> showSimpleDialog(
                    getString(R.string.setting_privacy),
                    getString(R.string.msg_privacy_dialog)
            ));
        }

        // Log Out button
        View btnLogOut = findViewById(R.id.btnLogOut);
        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> showLogoutConfirmationDialog());
        }
    }

    private void bindCachedProfile() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String phone = sessionManager.getUserPhone();
        String role = sessionManager.getUserRole();
        long id = sessionManager.getUserId();
        String createdAt = sessionManager.getCreatedAt();

        updateUiFields(name, email, phone, role, id, createdAt, true);
    }

    private void fetchLiveProfile() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        if (progressProfile != null) progressProfile.setVisibility(View.VISIBLE);

        apiService.getMyProfile().enqueue(new Callback<ApiModels.UserModel>() {
            @Override
            public void onResponse(Call<ApiModels.UserModel> call, Response<ApiModels.UserModel> response) {
                if (progressProfile != null) progressProfile.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.UserModel user = response.body();

                    sessionManager.saveFullProfile(
                            user.id,
                            user.username != null ? user.username : "",
                            user.email != null ? user.email : "",
                            user.fullName != null ? user.fullName : "",
                            user.phone != null ? user.phone : "",
                            user.role != null ? user.role : "user",
                            user.createdAt != null ? user.createdAt : ""
                    );

                    updateUiFields(user.fullName, user.email, user.phone, user.role, user.id, user.createdAt, user.isActive);
                } else if (response.code() == 401) {
                    Toast.makeText(ProfileActivity.this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
                    sessionManager.clearSession();
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.UserModel> call, Throwable t) {
                if (progressProfile != null) progressProfile.setVisibility(View.GONE);
                // Keep showing cached profile smoothly
            }
        });
    }

    private void updateUiFields(String name, String email, String phone, String role, long id, String createdAt, boolean isActive) {
        if (txtUserName != null && !TextUtils.isEmpty(name)) {
            txtUserName.setText(name);
        }
        if (txtUserEmail != null && !TextUtils.isEmpty(email)) {
            txtUserEmail.setText(email);
        }
        if (txtUserPhone != null) {
            txtUserPhone.setText(!TextUtils.isEmpty(phone) ? phone : "No phone added");
        }
        if (txtUserRole != null && !TextUtils.isEmpty(role)) {
            txtUserRole.setText(role.toUpperCase(Locale.ROOT));
        }
        if (txtUserStatus != null) {
            txtUserStatus.setText(isActive ? "Active" : "Inactive");
        }
        if (txtUserMemberId != null) {
            txtUserMemberId.setText(id > 0 ? "Account ID: #" + id : "Account ID: #--");
        }
        if (txtUserMemberSince != null && !TextUtils.isEmpty(createdAt)) {
            txtUserMemberSince.setText("Member: " + formatDisplayDate(createdAt));
        }
        if (txtAvatarInitials != null) {
            txtAvatarInitials.setText(extractInitials(name));
        }
    }

    private String extractInitials(String name) {
        if (TextUtils.isEmpty(name)) return "PP";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
        } else if (parts.length == 1 && parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase(Locale.ROOT);
        } else if (parts.length == 1 && parts[0].length() == 1) {
            return parts[0].toUpperCase(Locale.ROOT);
        }
        return "PP";
    }

    private String formatDisplayDate(String dateStr) {
        try {
            if (dateStr.length() >= 10) {
                return dateStr.substring(0, 10);
            }
        } catch (Exception ignored) {}
        return dateStr;
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etEditFullName);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);

        etName.setText(sessionManager.getUserName());
        etPhone.setText(sessionManager.getUserPhone());

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();

                    if (newName.length() < 2) {
                        Toast.makeText(this, "Full name must be at least 2 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (progressProfile != null) progressProfile.setVisibility(View.VISIBLE);

                    ApiModels.UpdateProfileRequest req = new ApiModels.UpdateProfileRequest(newName, newPhone);
                    apiService.updateProfile(req).enqueue(new Callback<ApiModels.UserModel>() {
                        @Override
                        public void onResponse(Call<ApiModels.UserModel> call, Response<ApiModels.UserModel> response) {
                            if (progressProfile != null) progressProfile.setVisibility(View.GONE);

                            if (response.isSuccessful() && response.body() != null) {
                                ApiModels.UserModel user = response.body();
                                sessionManager.saveFullProfile(
                                        user.id,
                                        user.username != null ? user.username : "",
                                        user.email != null ? user.email : "",
                                        user.fullName != null ? user.fullName : "",
                                        user.phone != null ? user.phone : "",
                                        user.role != null ? user.role : "user",
                                        user.createdAt != null ? user.createdAt : ""
                                );
                                updateUiFields(user.fullName, user.email, user.phone, user.role, user.id, user.createdAt, user.isActive);
                                Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiModels.UserModel> call, Throwable t) {
                            if (progressProfile != null) progressProfile.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmNewPassword);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change Password", (dialog, which) -> {
                    String currentPw = etCurrent.getText().toString();
                    String newPw = etNew.getText().toString();
                    String confirmPw = etConfirm.getText().toString();

                    if (TextUtils.isEmpty(currentPw)) {
                        Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPw.length() < 6) {
                        Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPw.equals(confirmPw)) {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (progressProfile != null) progressProfile.setVisibility(View.VISIBLE);

                    ApiModels.ChangePasswordRequest req = new ApiModels.ChangePasswordRequest(currentPw, newPw);
                    apiService.changePassword(req).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (progressProfile != null) progressProfile.setVisibility(View.GONE);

                            if (response.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Password changed successfully! Please log in again.", Toast.LENGTH_LONG).show();
                                sessionManager.clearSession();
                                redirectToLogin();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to change password. Current password may be incorrect.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            if (progressProfile != null) progressProfile.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_logout))
                .setMessage(getString(R.string.msg_logout_confirm))
                .setPositiveButton(getString(R.string.action_logout), (dialog, which) -> {
                    if (apiService != null) {
                        apiService.logout().enqueue(new Callback<Map<String, Object>>() {
                            @Override
                            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {}
                            @Override
                            public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                        });
                    }
                    sessionManager.clearSession();
                    Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSimpleDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
