package com.wthstranger.bmi_calculator.details;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityUserProfileBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.model.WeightHistory;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserProfileActivity extends BaseActivity {

    private ActivityUserProfileBinding binding;
    private UserModel user;

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        user = (UserModel) getIntent().getSerializableExtra("USER");
        if (user == null) {
            finish();
            return;
        }

        displayUserInfo();
        setupListeners();
        loadWeightHistory();
    }

    private void displayUserInfo() {
        binding.tvUserName.setText(user.getName());
        binding.tvUserBmiInfo.setText(String.format(Locale.getDefault(), "%.1f (%s)", user.getBmi(), user.getBmiCategory()));
        binding.tvWeight.setText(String.format(Locale.getDefault(), "%.1f %s", user.getWeight(), user.getWeightUnit()));
        binding.tvHeight.setText(String.format(Locale.getDefault(), "%.0f %s", user.getHeight(), user.getHeightUnit()));
        binding.tvGender.setText(user.getGender());
        
        if (user.getName() != null && !user.getName().isEmpty()) {
            binding.tvInitial.setText(user.getName().substring(0, 1).toUpperCase());
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnMenu.setOnClickListener(v -> {
            // Options menu could go here
            android.widget.Toast.makeText(this, "Menu clicked", android.widget.Toast.LENGTH_SHORT).show();
        });
        binding.btnUpdateDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, UpdateDetailsActivity.class);
            intent.putExtra("USER", user);
            startActivity(intent);
        });
    }

    private void loadWeightHistory() {
        String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid == null) return;

        userRepository.getWeightHistory(authUid, user.getUid()).addOnSuccessListener(queryDocumentSnapshots -> {
            List<WeightHistory> historyList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                WeightHistory history = doc.toObject(WeightHistory.class);
                if (history != null) historyList.add(history);
            }
            Collections.reverse(historyList);
            setupChart(historyList);
        });
    }

    private void setupChart(List<WeightHistory> historyList) {
        LineChart chart = binding.chart;
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < historyList.size(); i++) {
            entries.add(new Entry(i, (float) historyList.get(i).getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "BMI History");
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(false);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
