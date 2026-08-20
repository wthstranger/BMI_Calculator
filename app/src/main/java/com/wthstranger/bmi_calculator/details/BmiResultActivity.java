package com.wthstranger.bmi_calculator.details;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

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
import com.wthstranger.bmi_calculator.MainActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityBmiResultBinding;
import com.wthstranger.bmi_calculator.model.WeightHistory;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BmiResultActivity extends BaseActivity {

    private ActivityBmiResultBinding binding;

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBmiResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyInsets(binding.getRoot());

        double bmi = getIntent().getDoubleExtra("BMI", 0.0);
        String category = getIntent().getStringExtra("CATEGORY");
        double weight = getIntent().getDoubleExtra("WEIGHT", 0.0);
        double height = getIntent().getDoubleExtra("HEIGHT", 0.0);

        displayResult(bmi, category, weight, height);
        setupListeners();
        loadWeightHistory();
    }

    private void displayResult(double bmi, String category, double weight, double height) {
        binding.tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        binding.tvBmiCategory.setText(category);
        binding.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
        binding.tvHeight.setText(String.format(Locale.getDefault(), "%.0f cm", height));

        // Update Gauge
        binding.pbGauge.setProgress((int) bmi);

        // Category specific styling
        int colorRes;
        String message;
        if (bmi < 18.5) {
            colorRes = R.color.warning;
            message = "You are underweight. Consider consulting a nutritionist.";
        } else if (bmi < 25) {
            colorRes = R.color.success;
            message = "You have a normal body weight. Keep it up!";
        } else if (bmi < 30) {
            colorRes = R.color.warning;
            message = "You are overweight. Regular exercise can help.";
        } else {
            colorRes = R.color.danger;
            message = "You are in the obese range. Please consult a doctor.";
        }

        binding.tvBmiCategory.setTextColor(ContextCompat.getColor(this, colorRes));
        binding.tvMessage.setText(message);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadWeightHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        userRepository.getWeightHistory(uid, uid).addOnSuccessListener(queryDocumentSnapshots -> {
            List<WeightHistory> historyList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                WeightHistory history = doc.toObject(WeightHistory.class);
                if (history != null) historyList.add(history);
            }
            // Reverse to show chronological order
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

        LineDataSet dataSet = new LineDataSet(entries, "Weight History");
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
        xAxis.setDrawLabels(false); // Dates can be complex to format on small space

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
