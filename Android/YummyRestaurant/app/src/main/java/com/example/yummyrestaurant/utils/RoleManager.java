package com.example.yummyrestaurant.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoleManager {

    // Callback interface to handle the result asynchronously
    public interface RoleCallback {
        void onRoleReceived(String role);
    }

    // Asynchronous method to fetch user role
    public static void getUserRole(String userId, RoleCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String role = "customer"; // default role
                if (snapshot.exists()) {
                    String fetchedRole = snapshot.child("role").getValue(String.class);
                    if (fetchedRole != null) {
                        role = fetchedRole;
                    }
                }
                callback.onRoleReceived(role);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error gracefully
                callback.onRoleReceived("customer"); // fallback role
            }
        });
    }
}