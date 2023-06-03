package com.example.stockbar.ui.profile;

import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockbar.LoadPictureTask;
import com.example.stockbar.R;
import com.example.stockbar.services.users.UserInfo;
import com.example.stockbar.services.users.UsersManagerService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.ViewHolder> implements UsersManagerService.UserListListener {

    private final Resources res;
    private final List<String> availableSites;

    private final List<UserInfo> users = new ArrayList<>();
    private final boolean showSuperUsers;

    public UserCardAdapter(Resources res, boolean showSuperUsers) {
        this.res = res;
        this.showSuperUsers = showSuperUsers;
        this.availableSites = Arrays.asList(res.getStringArray(R.array.bar_array));

        UsersManagerService.setReference(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        UsersManagerService.removeReference(this);
    }

    @Override
    public void onUserInfo(UserInfo userInfo) {
        if (showSuperUsers || !userInfo.isSuperUser()) {
            users.add(userInfo);
            notifyItemInserted(users.size()-1);
        }
    }

    @Override
    public void onUserInfoChanged(UserInfo userInfo) {
        if (showSuperUsers || !userInfo.isSuperUser()) {
            for (int i = 0; i < users.size(); ++i) {
                if (users.get(i).getUid().equals(userInfo.getUid())) {
                    users.set(i, userInfo);
                    notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserCardAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        final UserInfo userInfo = users.get(position);

        // Picture
        LoadPictureTask task = new LoadPictureTask(this.res, picture -> {
            picture.setCircular(true);
            holder.profileImage.setImageDrawable(picture);
        });
        task.execute(userInfo.getProfilePictureURL());

        // Set name and email
        holder.name.setText(userInfo.getDisplayName());
        holder.email.setText(userInfo.getEmail());

        // Set values enable button and bar selector
        holder.enabled.setChecked(userInfo.hasAccess());
        holder.setEnabled(userInfo.hasAccess());

        try{
            if (userInfo.getAccessibleSite() != null) {
                int index = this.availableSites.indexOf(userInfo.getAccessibleSite());
                holder.barSelector.setSelection(index);
            }
        } catch(Exception ignored){ }

        // Set listeners for bar selector and enable button.
        holder.barSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (holder.enabled.isChecked()) {
                    // Don't use calculated userInfo on beginning of method onBindViewHolder
                    // it may change when view is recycled. Use getAdapterPosition instead.
                    String uid = users.get(holder.getAdapterPosition()).getUid();
                    UsersManagerService.updateUserAccessibleSite(uid, availableSites.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        holder.enabled.setOnCheckedChangeListener((v, isChecked) -> {
            String accessibleSite = null;
            if (isChecked)
                accessibleSite = holder.barSelector.getSelectedItem().toString();

            String uid = users.get(holder.getAdapterPosition()).getUid();
            UsersManagerService.updateUserAccessibleSite(uid, accessibleSite);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        holder.profileImage.setImageDrawable(ResourcesCompat.getDrawable(res, R.drawable.ic_profile_picture, null));
        holder.barSelector.setOnItemSelectedListener(null);
        holder.enabled.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;
        private final TextView name;
        private final TextView email;
        private final SwitchCompat enabled;
        private final AppCompatSpinner barSelector;

        public ViewHolder(View v) {
            super(v);
            profileImage = v.findViewById(R.id.user_profile_picture);
            name = v.findViewById(R.id.product_name);
            email = v.findViewById(R.id.product_stock);
            enabled = v.findViewById(R.id.has_access);
            barSelector = v.findViewById(R.id.bar_spinner);
        }

        public void setEnabled(boolean enabled) {
            barSelector.setEnabled(enabled);

            // https://stackoverflow.com/questions/28308325/androidset-gray-scale-filter-to-imageview
            if (enabled) {
                // Disable grayscale
                profileImage.setColorFilter(null);
                profileImage.setImageAlpha(255);
            } else {
                // Make image profile grayscale
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                profileImage.setColorFilter(cf);
                profileImage.setImageAlpha(128);
            }
        }
    }

}
