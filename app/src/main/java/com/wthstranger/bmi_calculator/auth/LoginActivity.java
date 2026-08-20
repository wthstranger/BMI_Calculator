package com.wthstranger.bmi_calculator.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.MainActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityLoginBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.AuthRepository;
import com.wthstranger.bmi_calculator.repository.UserRepository;
import com.wthstranger.bmi_calculator.utils.ValidationUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private GoogleSignInClient googleSignInClient;

    @Inject AuthRepository authRepository;
    @Inject UserRepository userRepository;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        initGoogleSignIn();
        setupListeners();
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        binding.btnSignIn.setOnClickListener(v -> handleLogin());
        binding.btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        binding.tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void signInWithGoogle() {
        setLoading(true);
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            setLoading(false);
            showError("Google Sign In failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        authRepository.signInWithGoogle(idToken)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        checkUserProfile(user.getUid(), true, user.getDisplayName(), user.getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Firebase Auth with Google failed: " + e.getMessage());
                });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email");
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            return;
        }

        setLoading(true);

        authRepository.loginWithEmail(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        checkUserProfile(user.getUid(), false, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Incorrect email or password.");
                });
    }

    private void checkUserProfile(String uid, boolean isGoogle, String name, String email) {
        userRepository.updateLastLogin(uid)
                .addOnSuccessListener(aVoid -> {
                    userRepository.getUserProfile(uid)
                            .addOnSuccessListener(documentSnapshot -> {
                                setLoading(false);
                                if (documentSnapshot.exists()) {
                                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                                    if (userModel != null) {
                                        navigateToNextScreen(userModel.isProfileCompleted());
                                    }
                                } else if (isGoogle) {
                                    // Create profile for new Google user
                                    createGoogleProfile(uid, name, email);
                                } else {
                                    showError("Account not found. Please create an account first.");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(e.getLocalizedMessage());
                });
    }

    private void createGoogleProfile(String uid, String name, String email) {
        setLoading(true);
        UserModel newUser = new UserModel(uid, name != null ? name : "", email != null ? email : "", "", "google");
        userRepository.createUserProfile(newUser)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    navigateToNextScreen(false);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Failed to create profile: " + e.getMessage());
                });
    }

    private void handleForgotPassword() {
        String email = binding.etEmail.getText().toString().trim();
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Enter email to reset password");
            return;
        }

        authRepository.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password reset link has been sent to your email.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> showError(e.getLocalizedMessage()));
    }

    private void navigateToNextScreen(boolean profileCompleted) {
        if (profileCompleted) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Navigate to User Details (to be implemented)
            Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class)); 
        }
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignIn.setEnabled(!isLoading);
        binding.btnSignIn.setText(isLoading ? "" : "Sign In");
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }
}
