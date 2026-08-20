package com.wthstranger.bmi_calculator.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.wthstranger.bmi_calculator.model.UserModel;
import com.wthstranger.bmi_calculator.model.WeightHistory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {
    private final CollectionReference usersCollection;

    @Inject
    public UserRepository(FirebaseFirestore firestore) {
        this.usersCollection = firestore.collection("users");
    }

    public Task<DocumentSnapshot> getUserProfile(String uid) {
        return usersCollection.document(uid).get();
    }

    public Task<Void> createUserProfile(UserModel user) {
        return usersCollection.document(user.getUid()).set(user);
    }

    public Task<Void> updateLastLogin(String uid) {
        return usersCollection.document(uid).update("lastLogin", FieldValue.serverTimestamp());
    }

    public Task<DocumentReference> addWeightHistory(String authUid, String profileId, WeightHistory entry) {
        if (profileId == null || profileId.isEmpty() || profileId.equals(authUid)) {
            return usersCollection.document(authUid).collection("weightHistory").add(entry);
        } else {
            return usersCollection.document(authUid).collection("profiles").document(profileId).collection("weightHistory").add(entry);
        }
    }

    public Task<QuerySnapshot> getWeightHistory(String authUid, String profileId) {
        CollectionReference historyRef;
        if (profileId == null || profileId.isEmpty() || profileId.equals(authUid)) {
            historyRef = usersCollection.document(authUid).collection("weightHistory");
        } else {
            historyRef = usersCollection.document(authUid).collection("profiles").document(profileId).collection("weightHistory");
        }
        return historyRef.orderBy("date", Query.Direction.DESCENDING).limit(7).get();
    }

    public Task<DocumentReference> addProfile(String authUid, UserModel profile) {
        return usersCollection.document(authUid).collection("profiles").add(profile);
    }

    public Task<Void> updateProfile(String authUid, UserModel profile) {
        if (profile.getUid() == null || profile.getUid().isEmpty() || profile.getUid().equals(authUid)) {
            return usersCollection.document(authUid).set(profile);
        } else {
            return usersCollection.document(authUid).collection("profiles").document(profile.getUid()).set(profile);
        }
    }

    public Task<Void> updateProfileId(String authUid, String docId) {
        return usersCollection.document(authUid).collection("profiles").document(docId).update("uid", docId);
    }

    public Task<QuerySnapshot> getProfiles(String authUid) {
        return usersCollection.document(authUid).collection("profiles").get();
    }
}
