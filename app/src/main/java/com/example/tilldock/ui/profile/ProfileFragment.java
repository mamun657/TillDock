package com.example.tilldock.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar toolbar = view.findViewById(R.id.profile_toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        TextView email = view.findViewById(R.id.profile_email);
        TextView business = view.findViewById(R.id.profile_business);

        com.example.tilldock.data.model.Merchant m = TillDockApplication.get().getAuthSession().current(); String userEmail = m == null ? null : m.getEmail();
        email.setText(userEmail == null || userEmail.isEmpty() ? "—" : userEmail);

        String businessName = "TillDock business";
        business.setText(businessName);
    }
}