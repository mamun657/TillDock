package com.example.tilldock.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tilldock.R;
import com.example.tilldock.ui.BaseFragment;
import com.example.tilldock.ui.Nav;

public class HomeFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.home_button_get_started).setOnClickListener(v -> Nav.showSignup(requireActivity()));
        view.findViewById(R.id.home_button_login).setOnClickListener(v -> Nav.showLogin(requireActivity()));
    }
}
