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
import com.wthstranger.bmi_calculator.databinding.FragmentUsersBinding;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    @Inject UserRepository userRepository;

    @Inject com.wthstranger.bmi_calculator.utils.SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadUsers() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Load main profile first
        userRepository.getUserProfile(uid).addOnSuccessListener(doc -> {
            if (binding == null) return;
            List<UserModel> allUsers = new ArrayList<>();
            if (doc.exists()) {
                UserModel mainUser = doc.toObject(UserModel.class);
                if (mainUser != null) {
                    if (mainUser.getUid() == null || mainUser.getUid().isEmpty()) mainUser.setUid(uid);
                    allUsers.add(mainUser);
                }
            }

            // Then load sub-profiles
            userRepository.getProfiles(uid).addOnSuccessListener(query -> {
                if (binding == null) return;
                for (com.google.firebase.firestore.DocumentSnapshot subDoc : query.getDocuments()) {
                    UserModel subProfile = subDoc.toObject(UserModel.class);
                    if (subProfile != null) {
                        if (subProfile.getUid() == null || subProfile.getUid().isEmpty()) subProfile.setUid(subDoc.getId());
                        allUsers.add(subProfile);
                    }
                }

                binding.rvUsers.setAdapter(new UsersAdapter(allUsers, clickedUser -> {
                    Intent intent = new Intent(requireContext(), com.wthstranger.bmi_calculator.details.UserOptionsActivity.class);
                    intent.putExtra("USER", clickedUser);
                    startActivity(intent);
                }));
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
