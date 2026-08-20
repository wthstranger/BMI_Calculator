package com.wthstranger.bmi_calculator.details;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityUpdateDetailsBinding;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UpdateDetailsActivity extends BaseActivity {

    private ActivityUpdateDetailsBinding binding;
    private String selectedWeightUnit = "KG";
    private String selectedHeightUnit = "CM";
    private com.wthstranger.bmi_calculator.model.UserModel intentUser;

    @Inject UserRepository userRepository;

    @Inject com.wthstranger.bmi_calculator.utils.SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        intentUser = (com.wthstranger.bmi_calculator.model.UserModel) getIntent().getSerializableExtra("USER");

        setupListeners();
        loadExistingData();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvKg.setOnClickListener(v -> selectWeightUnit("KG"));
        binding.tvLbs.setOnClickListener(v -> selectWeightUnit("LBS"));

        binding.tvCm.setOnClickListener(v -> selectHeightUnit("CM"));
        binding.tvInch.setOnClickListener(v -> selectHeightUnit("INCH"));

        binding.btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadExistingData() {
        com.wthstranger.bmi_calculator.model.UserModel activeUser = intentUser != null ? intentUser : sessionManager.getActiveUserValue();
        if (activeUser != null) {
            binding.etWeight.setText(String.valueOf(activeUser.getWeight()));
            binding.etHeight.setText(String.valueOf(activeUser.getHeight()));
            selectWeightUnit(activeUser.getWeightUnit() != null ? activeUser.getWeightUnit() : "KG");
            selectHeightUnit(activeUser.getHeightUnit() != null ? activeUser.getHeightUnit() : "CM");
        } else {
            // Fallback to main user if none selected
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;
            userRepository.getUserProfile(uid).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    com.wthstranger.bmi_calculator.model.UserModel user = doc.toObject(com.wthstranger.bmi_calculator.model.UserModel.class);
                    if (user != null) {
                        binding.etWeight.setText(String.valueOf(user.getWeight()));
                        binding.etHeight.setText(String.valueOf(user.getHeight()));
                        selectWeightUnit(user.getWeightUnit() != null ? user.getWeightUnit() : "KG");
                        selectHeightUnit(user.getHeightUnit() != null ? user.getHeightUnit() : "CM");
                    }
                }
            });
        }
    }

    private void selectWeightUnit(String unit) {
        selectedWeightUnit = unit;
        updateSegmentedUI(binding.tvKg, binding.tvLbs, unit.equals("KG"));
    }

    private void selectHeightUnit(String unit) {
        selectedHeightUnit = unit;
        updateSegmentedUI(binding.tvCm, binding.tvInch, unit.equals("CM"));
    }

    private void updateSegmentedUI(TextView left, TextView right, boolean isLeftSelected) {
        left.setBackgroundResource(isLeftSelected ? R.drawable.bg_segmented_left_selected : R.drawable.bg_segmented_mid_unselected);
        left.setTextColor(isLeftSelected ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));

        right.setBackgroundResource(!isLeftSelected ? R.drawable.bg_segmented_right_selected : R.drawable.bg_segmented_right_unselected);
        right.setTextColor(!isLeftSelected ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));
    }

    private void saveChanges() {
        String weightStr = binding.etWeight.getText().toString().trim();
        String heightStr = binding.etHeight.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        double bmi = com.wthstranger.bmi_calculator.utils.BmiUtils.calculateBmi(weight, selectedWeightUnit, height, selectedHeightUnit);
        String category = com.wthstranger.bmi_calculator.utils.BmiUtils.getBmiCategory(bmi);

        String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid == null) return;

        com.wthstranger.bmi_calculator.model.UserModel activeUser = intentUser != null ? intentUser : sessionManager.getActiveUserValue();
        if (activeUser == null) return;

        activeUser.setWeight(weight);
        activeUser.setWeightUnit(selectedWeightUnit);
        activeUser.setHeight(height);
        activeUser.setHeightUnit(selectedHeightUnit);
        activeUser.setBmi(bmi);
        activeUser.setBmiCategory(category);

        userRepository.updateProfile(authUid, activeUser).addOnSuccessListener(aVoid -> {
            // Update session manager
            sessionManager.setActiveUser(activeUser);

            // Also add to weight history
            double weightKg = selectedWeightUnit.equals("KG") ? weight : weight * 0.45359237;
            com.wthstranger.bmi_calculator.model.WeightHistory history = new com.wthstranger.bmi_calculator.model.WeightHistory(weightKg, "KG", bmi);
            userRepository.addWeightHistory(authUid, activeUser.getUid(), history);

            Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
