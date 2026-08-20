package com.wthstranger.bmi_calculator.details;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityAddUserBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddUserActivity extends BaseActivity {

    private ActivityAddUserBinding binding;
    private String selectedWeightUnit = "KG";
    private String selectedHeightUnit = "CM";
    private String selectedGender = "Male";

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvKg.setOnClickListener(v -> selectWeightUnit("KG"));
        binding.tvLbs.setOnClickListener(v -> selectWeightUnit("LBS"));
        binding.tvCm.setOnClickListener(v -> selectHeightUnit("CM"));
        binding.tvInch.setOnClickListener(v -> selectHeightUnit("INCH"));
        binding.tvMale.setOnClickListener(v -> selectGender("Male"));
        binding.tvFemale.setOnClickListener(v -> selectGender("Female"));
        binding.tvOther.setOnClickListener(v -> selectGender("Other"));
        binding.btnSave.setOnClickListener(v -> saveUser());
    }

    private void selectWeightUnit(String unit) {
        selectedWeightUnit = unit;
        updateSegmentedUI(binding.tvKg, binding.tvLbs, unit.equals("KG"));
    }

    private void selectHeightUnit(String unit) {
        selectedHeightUnit = unit;
        updateSegmentedUI(binding.tvCm, binding.tvInch, unit.equals("CM"));
    }

    private void selectGender(String gender) {
        selectedGender = gender;
        binding.tvMale.setBackgroundResource(gender.equals("Male") ? R.drawable.bg_segmented_left_selected : R.drawable.bg_segmented_mid_unselected);
        binding.tvFemale.setBackgroundResource(gender.equals("Female") ? R.drawable.bg_segmented_container : R.drawable.bg_segmented_mid_unselected);
        if (gender.equals("Female")) binding.tvFemale.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        binding.tvOther.setBackgroundResource(gender.equals("Other") ? R.drawable.bg_segmented_right_selected : R.drawable.bg_segmented_right_unselected);
    }

    private void updateSegmentedUI(TextView left, TextView right, boolean isLeftSelected) {
        left.setBackgroundResource(isLeftSelected ? R.drawable.bg_segmented_left_selected : R.drawable.bg_segmented_mid_unselected);
        right.setBackgroundResource(!isLeftSelected ? R.drawable.bg_segmented_right_selected : R.drawable.bg_segmented_right_unselected);
    }

    private void saveUser() {
        String name = binding.etName.getText().toString().trim();
        String weightStr = binding.etWeight.getText().toString().trim();
        String heightStr = binding.etHeight.getText().toString().trim();

        if (name.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        double bmi = com.wthstranger.bmi_calculator.utils.BmiUtils.calculateBmi(weight, selectedWeightUnit, height, selectedHeightUnit);
        String category = com.wthstranger.bmi_calculator.utils.BmiUtils.getBmiCategory(bmi);

        String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid == null) return;

        UserModel profile = new UserModel("", name, "", "", "local");
        profile.setProfileCompleted(true);
        profile.setGender(selectedGender);
        profile.setWeight(weight);
        profile.setWeightUnit(selectedWeightUnit);
        profile.setHeight(height);
        profile.setHeightUnit(selectedHeightUnit);
        profile.setBmi(bmi);
        profile.setBmiCategory(category);
        
        userRepository.addProfile(authUid, profile).addOnSuccessListener(ref -> {
            userRepository.updateProfileId(authUid, ref.getId());
            Toast.makeText(this, "User added!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
