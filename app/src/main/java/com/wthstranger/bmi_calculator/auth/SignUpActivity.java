package com.wthstranger.bmi_calculator.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.databinding.ActivitySignupBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.AuthRepository;
import com.wthstranger.bmi_calculator.repository.UserRepository;
import com.wthstranger.bmi_calculator.utils.ValidationUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends BaseActivity {

    private ActivitySignupBinding binding;

    @Inject AuthRepository authRepository;
    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLogin.setOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> handleSignUp());
    }

    private void handleSignUp() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        boolean termsAccepted = binding.cbTerms.isChecked();

        // Validation
        if (!ValidationUtils.isValidName(name)) {
            showError("Name must be at least 2 characters");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            showError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        if (!termsAccepted) {
            showError("Please agree to the Terms of Service");
            return;
        }

        setLoading(true);

        authRepository.signUpWithEmail(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        createFirestoreUser(user, name, email);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getLocalizedMessage());
                });
    }

    private void createFirestoreUser(FirebaseUser firebaseUser, String name, String email) {
        UserModel userModel = new UserModel(
                firebaseUser.getUid(),
                name,
                email,
                "",
                "email"
        );

        userRepository.createUserProfile(userModel)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Error creating profile: " + e.getLocalizedMessage());
                });
    }

    private void showSuccessDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Account Created")
                .setMessage("Your account has been created successfully. You can now start your health journey.")
                .setCancelable(false)
                .setPositiveButton("Get Started", (dialog, which) -> {
                    startActivity(new Intent(SignUpActivity.this, com.wthstranger.bmi_calculator.MainActivity.class));
                    finishAffinity();
                })
                .show();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignUp.setEnabled(!isLoading);
        binding.btnSignUp.setText(isLoading ? "" : "Sign Up");
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
