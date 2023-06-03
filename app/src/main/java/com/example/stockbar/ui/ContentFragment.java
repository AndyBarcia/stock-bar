package com.example.stockbar.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockbar.MainActivity;
import com.example.stockbar.R;
import com.example.stockbar.ui.products.ProductCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ContentFragment extends Fragment implements MainActivity.OnSectionSelected {

    private MainActivity activity;

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private PopUps popUps;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_content, container, false);

        activity = (MainActivity) getActivity();
        assert activity != null;

        activity.setDrawerVisibility(MainActivity.Visibility.SHOWN);
        activity.setSearchBarVisibility(MainActivity.Visibility.SHOWN);
        activity.setHomeButtonVisibility(MainActivity.Visibility.SHOWN);

        popUps = new PopUps(activity);

        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(view -> popUps.showAddPopUp());

        activity.setOnSectionSelectedListener(this);
        if (activity.getSelectedBar() != null && activity.getSelectedSection() != null) {
            this.onSectionSelected(activity.getSelectedBar(), activity.getSelectedSection());
        }

        return root;
    }

    @Override
    public void onSectionSelected(String selectedBar, @NonNull MenuItem selectedSection) {
        String sectionName = selectedSection.getTitle().toString();
        activity.getToolbar().setTitle(sectionName);

        ProductCardAdapter adapter = new ProductCardAdapter(popUps, selectedBar, sectionName);

        if (recyclerView.getAdapter() == null)
            recyclerView.setAdapter(adapter);
        else
            recyclerView.swapAdapter(adapter, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        popUps = null;
        recyclerView.setAdapter(null);
        fab.setOnClickListener(null);
    }

}