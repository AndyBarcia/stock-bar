package com.example.stockbar.services.users;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stockbar.services.DatabaseService;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersManagerService {

    public interface UserInfoListener {
        void onUserInfo (UserInfo userInfo);
    }

    public interface UserListListener {
        void onUserInfo(UserInfo userInfo);
        void onUserInfoChanged(UserInfo userInfo);
    }

    public static void getUserInfoOrCreate(FirebaseUser user, final UserInfoListener listener) {
        FirebaseDatabase database = DatabaseService.getDatabase();

        DatabaseReference ref = database.getReference("/users/"+user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Map<String, Object> newUserInfo = new HashMap<>();
                if (user.getDisplayName() != null)
                    newUserInfo.put("/displayName", user.getDisplayName());
                if (user.getEmail() != null)
                    newUserInfo.put("/email", user.getEmail());
                if (user.getPhotoUrl() != null)
                    newUserInfo.put("/pictureURL", user.getPhotoUrl().toString());
                String accessibleSite = null;
                if (dataSnapshot.exists() && dataSnapshot.hasChild("accessibleSite"))
                    accessibleSite = dataSnapshot.child("accessibleSite").getValue(String.class);
                listener.onUserInfo(new UserInfo(user, accessibleSite));
                database.getReference("/users/"+user.getUid()).updateChildren(newUserInfo);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                listener.onUserInfo(null);
            }
        });
    }

    private static HashMap<UserListListener, ChildEventListener> childEventListeners = new HashMap<>();

    public static void setReference(final UserListListener listener) {
        if (childEventListeners.containsKey(listener))
            return;

        DatabaseReference ref = DatabaseService.getDatabase().getReference("/users");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                try {
                    listener.onUserInfo(new UserInfo(snapshot));
                } catch (Exception ignored) { }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                try {
                    listener.onUserInfoChanged(new UserInfo(snapshot));
                } catch (Exception ignored) { }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };

        childEventListeners.put(listener, childEventListener);
        ref.orderByChild("displayName").addChildEventListener(childEventListener);
    }

    public static void removeReference(final UserListListener listener) {
        if (childEventListeners.containsKey(listener)) {
            DatabaseService.getDatabase().getReference("/users").orderByChild("displayName").removeEventListener(childEventListeners.get(listener));
            childEventListeners.remove(listener);
        }
    }

    public static void updateUserAccessibleSite(@NonNull String uid, @Nullable String accessibleSite) {
        DatabaseReference ref = DatabaseService.getDatabase().getReference("/users/"+uid+"/accessibleSite");
        if (accessibleSite != null)
            ref.setValue(accessibleSite);
        else
            ref.removeValue();
    }

}
