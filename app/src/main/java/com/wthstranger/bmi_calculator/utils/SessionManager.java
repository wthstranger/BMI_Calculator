package com.wthstranger.bmi_calculator.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.wthstranger.bmi_calculator.model.UserModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {
    private final MutableLiveData<UserModel> activeUser = new MutableLiveData<>();

    @Inject
    public SessionManager() {}

    public void setActiveUser(UserModel user) {
        activeUser.postValue(user);
    }

    public UserModel getActiveUserValue() {
        return activeUser.getValue();
    }

    public LiveData<UserModel> getActiveUser() {
        return activeUser;
    }
}
