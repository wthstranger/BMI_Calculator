package com.wthstranger.bmi_calculator.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class UserModel implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String profileImage;
    private String authProvider;
    private String gender;
    private double weight;
    private String weightUnit;
    private double height;
    private String heightUnit;
    private double bmi;
    private String bmiCategory;
    
    @ServerTimestamp
    private Date createdAt;
    
    @ServerTimestamp
    private Date lastLogin;
    
    private boolean profileCompleted;

    public UserModel() {
        // Default constructor for Firestore
    }

    public UserModel(String uid, String name, String email, String profileImage, String authProvider) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.authProvider = authProvider;
        this.profileCompleted = false;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getHeightUnit() { return heightUnit; }
    public void setHeightUnit(String heightUnit) { this.heightUnit = heightUnit; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getBmiCategory() { return bmiCategory; }
    public void setBmiCategory(String bmiCategory) { this.bmiCategory = bmiCategory; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getLastLogin() { return lastLogin; }
    public void setLastLogin(Date lastLogin) { this.lastLogin = lastLogin; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }
}
