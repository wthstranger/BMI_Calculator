package com.wthstranger.bmi_calculator.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.MainActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityUserDetailsBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserDetailsActivity extends BaseActivity {

    private ActivityUserDetailsBinding binding;
    private String selectedWeightUnit = "KG";
    private String selectedHeightUnit = "CM";
    private String selectedGender = "Male";

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Weight Unit Selection
        binding.tvKg.setOnClickListener(v -> selectWeightUnit("KG"));
        binding.tvLbs.setOnClickListener(v -> selectWeightUnit("LBS"));

        // Height Unit Selection
        binding.tvCm.setOnClickListener(v -> selectHeightUnit("CM"));
        binding.tvInch.setOnClickListener(v -> selectHeightUnit("INCH"));

        // Gender Selection
        binding.tvMale.setOnClickListener(v -> selectGender("Male"));
        binding.tvFemale.setOnClickListener(v -> selectGender("Female"));
        binding.tvOther.setOnClickListener(v -> selectGender("Other"));

        binding.btnSave.setOnClickListener(v -> saveAndCalculate());
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
        binding.tvMale.setTextColor(gender.equals("Male") ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));

        binding.tvFemale.setBackgroundResource(gender.equals("Female") ? R.drawable.bg_segmented_container : R.drawable.bg_segmented_mid_unselected); // Mid unselected doesn't exist yet but we'll use container as fallback
        binding.tvFemale.setTextColor(gender.equals("Female") ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));
        if (gender.equals("Female")) binding.tvFemale.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

        binding.tvOther.setBackgroundResource(gender.equals("Other") ? R.drawable.bg_segmented_right_selected : R.drawable.bg_segmented_right_unselected); // Need right selected
        binding.tvOther.setTextColor(gender.equals("Other") ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));
    }

    private void updateSegmentedUI(TextView left, TextView right, boolean isLeftSelected) {
        left.setBackgroundResource(isLeftSelected ? R.drawable.bg_segmented_left_selected : R.drawable.bg_segmented_mid_unselected);
        left.setTextColor(isLeftSelected ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));
        
        right.setBackgroundResource(!isLeftSelected ? R.drawable.bg_segmented_right_selected : R.drawable.bg_segmented_right_unselected);
        right.setTextColor(!isLeftSelected ? ContextCompat.getColor(this, R.color.white) : ContextCompat.getColor(this, R.color.gray));
    }

    private void saveAndCalculate() {
        String name = binding.etName.getText().toString().trim();
        String weightStr = binding.etWeight.getText().toString().trim();
        String heightStr = binding.etHeight.getText().toString().trim();

        if (name.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);

        // Convert to Metric for calculation
        double weightKg = selectedWeightUnit.equals("KG") ? weight : weight * 0.45359237;
        double heightCm = selectedHeightUnit.equals("CM") ? height : height * 2.54;
        double heightM = heightCm / 100.0;

        double bmi = weightKg / (heightM * heightM);
        String category = getBmiCategory(bmi);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        UserModel user = new UserModel(uid, name, FirebaseAuth.getInstance().getCurrentUser().getEmail(), "", "email");
        user.setProfileCompleted(true);
        user.setWeight(weight);
        user.setWeightUnit(selectedWeightUnit);
        user.setHeight(height);
        user.setHeightUnit(selectedHeightUnit);
        user.setGender(selectedGender);
        user.setBmi(bmi);
        user.setBmiCategory(category);

        userRepository.createUserProfile(user).addOnSuccessListener(aVoid -> {
            // Add to Weight History
            com.wthstranger.bmi_calculator.model.WeightHistory history = new com.wthstranger.bmi_calculator.model.WeightHistory(weightKg, "KG", bmi);
            userRepository.addWeightHistory(uid, uid, history);

            // Navigate to Result
            Intent intent = new Intent(this, BmiResultActivity.class);
            intent.putExtra("BMI", bmi);
            intent.putExtra("CATEGORY", category);
            intent.putExtra("WEIGHT", weightKg);
            intent.putExtra("HEIGHT", heightCm);
            startActivity(intent);
            finish();
        });
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25) return "Normal Weight";
        else if (bmi < 30) return "Overweight";
        else return "Obese";
    }
}
