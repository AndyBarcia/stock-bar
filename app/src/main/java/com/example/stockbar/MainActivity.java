package com.example.stockbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.stockbar.services.users.UserInfo;
import com.example.stockbar.services.users.UsersManagerService;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersManagerService.UserListListener, NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private MenuItem searchItem;
    private SearchView searchView;
    public static SearchView.OnQueryTextListener queryTextListener;
    public static String search = "";

    private NavController navController;

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private LoadPictureTask loadUserPictureTask;

    private UserInfo userInfo;
    private TextView drawerBarName;
    private Spinner drawerSpinner;
    private ImageView profilePic;

    private NavigationView navigationView;
    private MenuItem selectedSection;
    private OnSectionSelected onSectionSelectedListener;
    private String selectedBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        setDrawerVisibility(drawerVisibility);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_vinos);
        onNavigationItemSelected(navigationView.getCheckedItem());

        profilePic = navigationView.getHeaderView(0).findViewById(R.id.profile_picture);
        profilePic.setOnClickListener(v -> navController.navigate(R.id.action_nav_content_to_nav_profile));

        drawerBarName = navigationView.getHeaderView(0).findViewById(R.id.text_view);
        drawerSpinner = navigationView.getHeaderView(0).findViewById(R.id.spinner);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_content)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        setHomeButtonVisibility(homeButtonVisibility);
    }

    public interface OnSectionSelected {
        void onSectionSelected(String selectedBar, MenuItem selectedSection);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.close();
        if (onSectionSelectedListener != null && (this.selectedSection == null || this.selectedSection.getItemId() != item.getItemId()))
            onSectionSelectedListener.onSectionSelected(selectedBar, item);
        this.selectedSection = item;
        return true;
    }

    public void setOnSectionSelectedListener(OnSectionSelected onSectionSelectedListener) {
        this.onSectionSelectedListener = onSectionSelectedListener;
    }

    public MenuItem getSelectedSection() {
        return selectedSection;
    }

    public  enum Visibility {
        HIDDEN, SHOWN
    }

    private Visibility homeButtonVisibility = Visibility.SHOWN;
    private Visibility drawerVisibility = Visibility.SHOWN;
    private Visibility searchViewVisibility = Visibility.SHOWN;

    public void setDrawerVisibility(Visibility v) {
        drawerVisibility = v;
        if (drawer == null)
            return;

        if (v == Visibility.HIDDEN)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void setSearchBarVisibility(Visibility v) {
        searchViewVisibility = v;
        if (searchItem != null)
            searchItem.setVisible(v != Visibility.HIDDEN);
    }

    public void setHomeButtonVisibility(Visibility v) {
        homeButtonVisibility = v;
        if (getSupportActionBar() == null)
            return;

        if (v == Visibility.HIDDEN) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadUserPictureTask != null)
            loadUserPictureTask.cancel(true);
        UsersManagerService.removeReference(this);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setUserInfo((UserInfo) savedInstanceState.getParcelable("USUARIO"));
        this.selectedSection = this.navigationView.getMenu().findItem(savedInstanceState.getInt("SECTION_ID"));
        this.drawerSpinner.setSelection(savedInstanceState.getInt("BAR_ID"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("USUARIO", this.userInfo);
        savedInstanceState.putString("BAR", selectedBar);
        savedInstanceState.putInt("BAR_ID", this.drawerSpinner.getSelectedItemPosition());
        savedInstanceState.putInt("SECTION_ID", selectedSection.getItemId());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onUserInfo(UserInfo userInfo) {

    }

    @Override
    public void onUserInfoChanged(UserInfo userInfo) {
        if (this.userInfo == null)
            return;

        if (userInfo.getUid().equals(this.userInfo.getUid())) {
            if (!userInfo.hasAccess()) {
                navController.navigate(R.id.nav_login);
            } else {
                this.setUserInfo(userInfo);
            }
        }
    }

    public void setUserInfo(UserInfo info) {
        this.userInfo = info;
        UsersManagerService.setReference(this);

        // Set profile picture
        if (loadUserPictureTask != null)
            loadUserPictureTask.cancel(true);
        loadUserPictureTask = new LoadPictureTask(this.getResources(), picture -> {
            picture.setCircular(true);
            profilePic.setImageDrawable(picture);
        });
        loadUserPictureTask.execute(info.getProfilePictureURL());

        // Set accessible sites dropdown in drawer
        if (!info.isSuperUser()) {
            // Just one option

            drawerSpinner.setVisibility(View.GONE);
            drawerBarName.setVisibility(View.VISIBLE);
            selectedBar = info.getAccessibleSite();
            drawerBarName.setText(selectedBar);
        } else {
            // Multiple options. Select one in spinner in drawer

            drawerBarName.setVisibility(View.GONE);
            drawerSpinner.setVisibility(View.VISIBLE);

            List<String> accessibleSites = Arrays.asList(this.getResources().getStringArray(R.array.bar_array));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.nav_header_main_title,
                    accessibleSites);

            drawerSpinner.setAdapter(adapter);

            drawerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedBar = accessibleSites.get(position);

                    if (onSectionSelectedListener != null){
                        onSectionSelectedListener.onSectionSelected(selectedBar, selectedSection);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getSelectedBar() {
        return selectedBar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchItem = menu.findItem(R.id.search);
        if (searchItem != null) {
            setSearchBarVisibility(searchViewVisibility);
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (queryTextListener != null)
                            queryTextListener.onQueryTextChange(newText);
                        search = newText;
                        return true;
                    }
                });
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified())
            searchView.onActionViewCollapsed();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public ImageView getProfilePic() {
        return profilePic;
    }
}