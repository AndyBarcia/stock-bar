package com.example.stockbar.services.users;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserInfo implements Parcelable {

    private final String uid;
    private final String displayName;
    private final String email;
    private final Uri profilePictureURL;
    private final String accessibleSite;

    public UserInfo(FirebaseUser user, String accessibleSite) {
        this.uid = user.getUid();
        this.accessibleSite = accessibleSite;
        this.displayName = user.getDisplayName();
        this.email = user.getEmail();
        this.profilePictureURL = user.getPhotoUrl();
    }

    public UserInfo(DataSnapshot data) {
        this.uid = data.getKey();
        this.displayName = data.child("displayName").getValue(String.class);
        this.email = data.child("email").getValue(String.class);
        this.profilePictureURL = Uri.parse(data.child("pictureURL").getValue(String.class));
        if (data.hasChild("accessibleSite"))
            this.accessibleSite = data.child("accessibleSite").getValue(String.class);
        else
            this.accessibleSite = null;
    }

    public boolean isSuperUser() {
        if (email == null)
            return false;
        return email.equals("maria310171@gmail.com") || email.equals("andybarcia4@gmail.com");
    }

    public boolean hasAccess() {
        return accessibleSite != null || isSuperUser();
    }

    public String getUid() {
        return uid;
    }

    public String getAccessibleSite() {
        return accessibleSite;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public Uri getProfilePictureURL() {
        return profilePictureURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(displayName);
        parcel.writeString(email);
        parcel.writeParcelable(profilePictureURL, i);
        parcel.writeString(accessibleSite);
    }

    protected UserInfo(Parcel in) {
        uid = in.readString();
        displayName = in.readString();
        email = in.readString();
        profilePictureURL = in.readParcelable(Uri.class.getClassLoader());
        accessibleSite = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
