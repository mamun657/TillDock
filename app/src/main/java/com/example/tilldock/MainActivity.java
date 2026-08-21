package com.example.tilldock;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tilldock.auth.AuthSession;
import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.ui.Nav;

public class MainActivity extends AppCompatActivity {

    private AuthSession authSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authSession = ((TillDockApplication) getApplication()).getAuthSession();
        authSession.merchant().observe(this, (Merchant merchant) -> {
            if (merchant != null) {
                Nav.showDashboard(MainActivity.this);
            } else {
                Nav.showHome(MainActivity.this);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
                if (current != null && current.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    current.getChildFragmentManager().popBackStack();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
