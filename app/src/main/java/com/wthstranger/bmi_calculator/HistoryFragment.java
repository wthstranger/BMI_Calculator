package com.wthstranger.bmi_calculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wthstranger.bmi_calculator.databinding.FragmentHistoryBinding;
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
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    @Inject UserRepository userRepository;

    @Inject com.wthstranger.bmi_calculator.utils.SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeActiveUser();
    }

    private void observeActiveUser() {
        sessionManager.getActiveUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvTitle.setText(user.getName() + "'s History");
                loadHistory(user.getUid());
            }
        });
    }

    private void loadHistory(String profileId) {
        String authUid = FirebaseAuth.getInstance().getUid();
        if (authUid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        userRepository.getWeightHistory(authUid, profileId).addOnSuccessListener(queryDocumentSnapshots -> {
            if (binding == null) return;
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
                binding.tvLatestDate.setText("No data yet");
            }
            
            setupChart(historyList);
        }).addOnFailureListener(e -> {
            if (binding != null) binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void setupChart(List<WeightHistory> historyList) {
        LineChart chart = binding.chart;
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < historyList.size(); i++) {
            entries.add(new Entry(i, (float) historyList.get(i).getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.white));
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
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
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(requireContext(), R.color.gray_light));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.animateX(1000);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
