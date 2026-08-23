package com.example.tilldock;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tilldock.auth.AuthSession;
import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.ui.Nav;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private AuthSession authSession;
    private BottomNavigationView bottomNav;
    private boolean navVisible;
    private int activeTabId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        authSession = ((TillDockApplication) getApplication()).getAuthSession();
        authSession.merchant().observe(this, (Merchant merchant) -> {
            if (merchant != null) {
                showAppShell();
            } else {
                showAuthShell();
            }
        });

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (activeTabId == id) {
                    return true;
                }
                if (id == R.id.nav_home) {
                    return selectTab(new com.example.tilldock.ui.dashboard.DashboardFragment(), R.id.nav_home);
                } else if (id == R.id.nav_products) {
                    return selectTab(new com.example.tilldock.ui.products.ProductsFragment(), R.id.nav_products);
                } else if (id == R.id.nav_sales) {
                    return selectTab(new com.example.tilldock.ui.sales.NewSaleFragment(), R.id.nav_sales);
                } else if (id == R.id.nav_reports) {
                    return selectTab(new com.example.tilldock.ui.reports.ReportsFragment(), R.id.nav_reports);
                } else if (id == R.id.nav_more) {
                    return selectTab(new com.example.tilldock.ui.more.MoreFragment(), R.id.nav_more);
                }
                return false;
            });

            bottomNav.setOnItemReselectedListener(item -> {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
                if (current instanceof com.example.tilldock.ui.sales.NewSaleFragment) {
                    ((com.example.tilldock.ui.sales.NewSaleFragment) current).resetCart();
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
                if (current != null && current.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    current.getChildFragmentManager().popBackStack();
                    return;
                }
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return;
                }
                if (navVisible && bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void showAuthShell() {
        navVisible = false;
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
        if (getSupportFragmentManager().findFragmentById(R.id.main_container) instanceof com.example.tilldock.ui.home.HomeFragment) {
            return;
        }
        Nav.showHome(this);
    }

    private void showAppShell() {
        navVisible = true;
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        // Reset guard so the first selectTab() actually loads the Home fragment.
        activeTabId = 0;
        // Call selectTab() directly rather than relying on the listener, because
        // this may run before bottomNav.setOnItemSelectedListener() is attached.
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        selectTab(new com.example.tilldock.ui.dashboard.DashboardFragment(), R.id.nav_home);
    }

    private boolean selectTab(Fragment fragment, int tabId) {
        if (activeTabId == tabId) {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_container);
            if (current != null && fragment.getClass().equals(current.getClass())) {
                return true;
            }
        }
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.main_container, fragment, fragment.getClass().getSimpleName());
        tx.commitNow();
        activeTabId = tabId;
        if (bottomNav != null && bottomNav.getSelectedItemId() != tabId) {
            bottomNav.setSelectedItemId(tabId);
        }
        return true;
    }
}
