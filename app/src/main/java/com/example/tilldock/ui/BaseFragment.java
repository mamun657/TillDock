package com.example.tilldock.ui;

import androidx.fragment.app.Fragment;

import com.example.tilldock.auth.AuthLocator;
import com.example.tilldock.auth.AuthSession;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.data.repository.TokenStore;

public class BaseFragment extends Fragment {

    protected AuthSession session() {
        return AuthLocator.session(requireContext());
    }

    protected AuthRepository repository() {
        return AuthLocator.repository(requireContext());
    }

    protected TokenStore tokenStore() {
        return AuthLocator.tokenStore(requireContext());
    }

    protected void goBack() {
        if (!requireActivity().getSupportFragmentManager().popBackStackImmediate()) {
            requireActivity().finish();
        }
    }
}
