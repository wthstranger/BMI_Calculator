package com.wthstranger.bmi_calculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wthstranger.bmi_calculator.auth.LoginActivity;
import com.wthstranger.bmi_calculator.databinding.ActivityMainBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        checkUserProfile(user.getUid());
        setupNavigation();
        
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_history) {
                fragment = new HistoryFragment();
            } else if (id == R.id.nav_users) {
                fragment = new UsersFragment();
            } else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkUserProfile(String uid) {
        userRepository.getUserProfile(uid)
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserModel model = doc.toObject(UserModel.class);
                        if (model != null && !model.isProfileCompleted()) {
                            // Navigate to User Details if profile is not completed
                            startActivity(new Intent(this, com.wthstranger.bmi_calculator.details.UserDetailsActivity.class));
                        }
                    } else {
                        // This case should ideally be handled during registration, but as a fallback:
                        startActivity(new Intent(this, com.wthstranger.bmi_calculator.details.UserDetailsActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
