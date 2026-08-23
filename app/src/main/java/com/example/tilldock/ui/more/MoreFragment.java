package com.example.tilldock.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tilldock.R;
import com.example.tilldock.ui.Nav;
import com.google.android.material.button.MaterialButton;

public class MoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindTile(view, R.id.more_tile_profile,
                getString(R.string.more_tile_profile),
                getString(R.string.more_tile_profile_sub),
                v -> Nav.showProfile(requireActivity()));
        bindTile(view, R.id.more_tile_business,
                getString(R.string.more_tile_business),
                getString(R.string.more_tile_business_sub),
                v -> Nav.showBusinessProfile(requireActivity()));
        bindTile(view, R.id.more_tile_staff,
                getString(R.string.more_tile_staff),
                getString(R.string.more_tile_staff_sub),
                v -> Nav.showStaff(requireActivity()));
        bindTile(view, R.id.more_tile_reports,
                getString(R.string.more_tile_reports),
                getString(R.string.more_tile_reports_sub),
                v -> Nav.showReports(requireActivity()));
        bindTile(view, R.id.more_tile_settings,
                getString(R.string.more_tile_settings),
                getString(R.string.more_tile_settings_sub),
                v -> Nav.showSettings(requireActivity()));
        bindTile(view, R.id.more_tile_help,
                getString(R.string.more_tile_help),
                getString(R.string.more_tile_help_sub),
                v -> { /* TODO: help center */ });

        MaterialButton logout = view.findViewById(R.id.more_logout);
        logout.setOnClickListener(v -> Nav.signOut(requireActivity()));
    }

    private void bindTile(View root, int containerId, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout container = root.findViewById(containerId);
        if (container == null) return;
        container.removeAllViews();
        View tile = LayoutInflater.from(requireContext()).inflate(R.layout.item_more_tile, container, false);
        ((TextView) tile.findViewById(R.id.more_tile_title)).setText(title);
        ((TextView) tile.findViewById(R.id.more_tile_subtitle)).setText(subtitle);
        tile.setOnClickListener(listener);
        container.addView(tile);
    }
}
