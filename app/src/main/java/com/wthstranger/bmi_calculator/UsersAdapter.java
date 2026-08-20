package com.wthstranger.bmi_calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wthstranger.bmi_calculator.databinding.ItemUserHorizontalBinding;
import com.wthstranger.bmi_calculator.model.UserModel;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<UserModel> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public UsersAdapter(List<UserModel> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserHorizontalBinding binding = ItemUserHorizontalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.binding.tvUserName.setText(user.getName());
        holder.binding.tvUserBmiInfo.setText(String.format(java.util.Locale.getDefault(), "%.1f (%s)", user.getBmi(), user.getBmiCategory()));
        
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserHorizontalBinding binding;
        UserViewHolder(ItemUserHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
