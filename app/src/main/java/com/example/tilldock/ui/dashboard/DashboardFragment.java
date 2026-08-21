package com.example.tilldock.ui.dashboard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.ui.BaseFragment;
import com.example.tilldock.ui.Nav;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DashboardFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView avatarInitial = view.findViewById(R.id.dashboard_avatar_initial);
        TextView userName = view.findViewById(R.id.dashboard_user_name);
        TextView userRole = view.findViewById(R.id.dashboard_user_role);
        TextView welcome = view.findViewById(R.id.dashboard_welcome);
        TextView business = view.findViewById(R.id.dashboard_business);

        View settings = view.findViewById(R.id.dashboard_action_settings);
        View businessTile = view.findViewById(R.id.dashboard_action_business);
        View categoriesTile = view.findViewById(R.id.dashboard_action_categories);
        View productsTile = view.findViewById(R.id.dashboard_action_products);
        View inventoryTile = view.findViewById(R.id.dashboard_action_inventory);
        View accountTile = view.findViewById(R.id.dashboard_action_account);
        TextView logoutLink = view.findViewById(R.id.dashboard_action_logout);

        session().merchant().observe(getViewLifecycleOwner(), merchant -> {
            if (merchant == null) {
                Nav.showHome(requireActivity());
                return;
            }
            applyMerchant(merchant, avatarInitial, userName, userRole, welcome, business);
        });

        settings.setOnClickListener(v -> Nav.showBusinessProfile(requireActivity()));
        businessTile.setOnClickListener(v -> Nav.showBusinessProfile(requireActivity()));
        categoriesTile.setOnClickListener(v -> Nav.showCategories(requireActivity()));
        productsTile.setOnClickListener(v -> Nav.showProducts(requireActivity()));
        inventoryTile.setOnClickListener(v -> Nav.showInventory(requireActivity()));
        accountTile.setOnClickListener(v -> Nav.showBusinessProfile(requireActivity()));
        logoutLink.setOnClickListener(v -> confirmSignOut());
    }

    private void applyMerchant(Merchant m,
                               TextView avatarInitial,
                               TextView userName,
                               TextView userRole,
                               TextView welcome,
                               TextView business) {
        String fullName = safe(m.getFullName());
        String bizName = safe(m.getBusinessName());
        String role = safe(m.getRole());

        avatarInitial.setText(initialOf(fullName));
        userName.setText(fullName.isEmpty() ? getString(R.string.app_name) : fullName);
        userRole.setText(role);
        userRole.setVisibility(TextUtils.isEmpty(role) ? View.GONE : View.VISIBLE);

        String firstName = firstTokenOf(fullName);
        welcome.setText(getString(R.string.dashboard_welcome, firstName.isEmpty() ? "merchant" : firstName));
        business.setText(getString(R.string.dashboard_business_name, bizName));
    }

    private String initialOf(String name) {
        if (TextUtils.isEmpty(name)) return "M";
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase();
    }

    private String firstTokenOf(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String[] parts = name.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void confirmSignOut() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dashboard_logout)
                .setMessage(R.string.dashboard_logout_confirm)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.dashboard_logout, (dialog, which) -> session().logout(null))
                .show();
    }
}
