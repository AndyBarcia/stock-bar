package com.example.stockbar.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockbar.LoadPictureTask;
import com.example.stockbar.MainActivity;
import com.example.stockbar.R;
import com.example.stockbar.services.users.UserInfo;
import com.example.stockbar.ui.login.AuthUtils;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_profile, container, false);

        MainActivity activity = (MainActivity) getActivity();
        activity.setDrawerVisibility(MainActivity.Visibility.HIDDEN);
        activity.setSearchBarVisibility(MainActivity.Visibility.HIDDEN);
        activity.setHomeButtonVisibility(MainActivity.Visibility.SHOWN);

        UserInfo userInfo = activity.getUserInfo();

        TextView name = root.findViewById(R.id.profile_name);
        name.setText(userInfo.getDisplayName());

        TextView email = root.findViewById(R.id.profile_email);
        email.setText(userInfo.getEmail());

        ImageView profilePic = root.findViewById(R.id.profile_picture);
        LoadPictureTask loadUserPictureTask = new LoadPictureTask(this.getResources(), picture -> {
            picture.setCircular(true);
            profilePic.setImageDrawable(picture);
        });
        loadUserPictureTask.execute(userInfo.getProfilePictureURL());

        root.findViewById(R.id.button).setOnClickListener(v -> {
            AuthUtils.getSignInClient(activity).signOut().addOnCompleteListener(task -> {
                AuthUtils.getAuth().signOut();
                Navigation.findNavController(root).navigate(R.id.nav_login);
            });
        });

        recyclerView = root.findViewById(R.id.recycler_view);

        if (userInfo.isSuperUser()) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(new UserCardAdapter(this.getResources(), false));
        } else {
            recyclerView.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        recyclerView.setAdapter(null);
    }
}
