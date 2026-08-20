package com.wthstranger.bmi_calculator.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.databinding.ActivityUserOptionsBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserOptionsActivity extends BaseActivity {

    private ActivityUserOptionsBinding binding;
    private UserModel user;

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        user = (UserModel) getIntent().getSerializableExtra("USER");
        if (user == null) {
            finish();
            return;
        }

        displayUserInfo();
        setupListeners();
    }

    private void displayUserInfo() {
        binding.tvUserName.setText(user.getName());
        binding.tvUserBmiInfo.setText(String.format(Locale.getDefault(), "%.1f (%s)", user.getBmi(), user.getBmiCategory()));
        
        // You could also set an initial here if needed, but the layout uses an ImageView
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());

        binding.btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("USER", user);
            startActivity(intent);
        });

        binding.btnEditDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateDetailsActivity.class);
            intent.putExtra("USER", user);
            startActivity(intent);
        });

        binding.btnDeleteUser.setOnClickListener(v -> {
            // Implement delete logic here if needed
            Toast.makeText(this, "Delete functionality not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }
}
