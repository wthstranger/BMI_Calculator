package com.wthstranger.bmi_calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.wthstranger.bmi_calculator.databinding.FragmentHomeBinding;
import com.wthstranger.bmi_calculator.details.UpdateDetailsActivity;
import com.wthstranger.bmi_calculator.details.WeightHistoryActivity;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    @Inject UserRepository userRepository;

    @Inject com.wthstranger.bmi_calculator.utils.SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        setupRecyclerView();
        observeActiveUser();
        loadData();
    }

    private void observeActiveUser() {
        sessionManager.getActiveUser().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void setupListeners() {
        binding.cvUpdateDetails.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), UpdateDetailsActivity.class));
        });

        binding.cvWeightHistory.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WeightHistoryActivity.class));
        });

        binding.cvAddUser.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.wthstranger.bmi_calculator.details.AddUserActivity.class));
        });
    }

    private void setupRecyclerView() {
        binding.rvRecentUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Load main profile first and set as active if none
        userRepository.getUserProfile(uid).addOnSuccessListener(doc -> {
            if (binding == null) return;
            if (doc.exists()) {
                UserModel user = doc.toObject(UserModel.class);
                if (user != null) {
                    if (user.getUid() == null || user.getUid().isEmpty()) user.setUid(uid);
                    if (sessionManager.getActiveUserValue() == null) {
                        sessionManager.setActiveUser(user);
                    }
                    loadOtherProfiles(uid, user);
                }
            }
        });
    }

    private void loadOtherProfiles(String authUid, UserModel currentUser) {
        userRepository.getProfiles(authUid).addOnSuccessListener(query -> {
            if (binding == null) return;
            List<UserModel> list = new ArrayList<>();
            list.add(currentUser);
            for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                UserModel profile = doc.toObject(UserModel.class);
                if (profile != null) {
                    if (profile.getUid() == null || profile.getUid().isEmpty()) profile.setUid(doc.getId());
                    list.add(profile);
                }
            }
            binding.rvRecentUsers.setAdapter(new UsersAdapter(list, clickedUser -> {
                sessionManager.setActiveUser(clickedUser);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Switched to " + clickedUser.getName(), Toast.LENGTH_SHORT).show();
                }
            }));
        });
    }

    private void updateUI(UserModel user) {
        if (binding == null || user == null) return;
        binding.tvGreeting.setText(user.getName() + "'s Dashboard");
        binding.tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", user.getBmi()));
        binding.tvBmiCategory.setText(user.getBmiCategory());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
