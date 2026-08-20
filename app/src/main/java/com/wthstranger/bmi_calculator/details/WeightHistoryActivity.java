package com.wthstranger.bmi_calculator.details;

import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wthstranger.bmi_calculator.BaseActivity;
import com.wthstranger.bmi_calculator.R;
import com.wthstranger.bmi_calculator.databinding.ActivityWeightHistoryBinding;
import com.wthstranger.bmi_calculator.model.WeightHistory;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WeightHistoryActivity extends BaseActivity {

    private ActivityWeightHistoryBinding binding;

    @Inject UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeightHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupEdgeToEdge();
        setupListeners();
        loadHistory();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = (int) (24 * getResources().getDisplayMetrics().density);
            v.setPadding(padding, insets.top + padding, padding, insets.bottom + padding);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        userRepository.getWeightHistory(uid, uid).addOnSuccessListener(queryDocumentSnapshots -> {
            binding.progressBar.setVisibility(View.GONE);
            List<WeightHistory> historyList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                WeightHistory history = doc.toObject(WeightHistory.class);
                if (history != null) historyList.add(history);
            }
            Collections.reverse(historyList);
            
            if (!historyList.isEmpty()) {
                WeightHistory latest = historyList.get(historyList.size() - 1);
                binding.tvLatestWeight.setText(String.format(Locale.getDefault(), "%.1f %s", latest.getWeight(), latest.getUnit()));
                if (latest.getDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    binding.tvLatestDate.setText(sdf.format(latest.getDate().toDate()));
                }
            } else {
                binding.tvLatestWeight.setText("--");
                binding.tvLatestDate.setText("No data available");
            }
            
            setupChart(historyList);
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void setupChart(List<WeightHistory> historyList) {
        LineChart chart = binding.chart;
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < historyList.size(); i++) {
            entries.add(new Entry(i, (float) historyList.get(i).getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight");
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(ContextCompat.getColor(this, R.color.white));
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.chart_gradient));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.gray));
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < historyList.size() && historyList.get(index).getDate() != null) {
                    return mFormat.format(historyList.get(index).getDate().toDate());
                }
                return "";
            }
        });

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.gray_light));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.gray));
        
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.animateX(1000);
        chart.invalidate();
    }
}
