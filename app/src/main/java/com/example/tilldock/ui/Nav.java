package com.example.tilldock.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.tilldock.R;
import com.example.tilldock.ui.auth.LoginFragment;
import com.example.tilldock.ui.auth.SignupFragment;
import com.example.tilldock.ui.business.BusinessProfileFragment;
import com.example.tilldock.ui.categories.CategoriesFragment;
import com.example.tilldock.ui.dashboard.DashboardFragment;
import com.example.tilldock.ui.home.HomeFragment;
import com.example.tilldock.ui.inventory.InventoryFragment;
import com.example.tilldock.ui.products.ProductsFragment;

public final class Nav {

    private Nav() {
    }

    public static void showHome(FragmentActivity activity) {
        replace(activity, true, new HomeFragment());
    }

    public static void showLogin(FragmentActivity activity) {
        replace(activity, true, new LoginFragment());
    }

    public static void showSignup(FragmentActivity activity) {
        replace(activity, true, new SignupFragment());
    }

    public static void showDashboard(FragmentActivity activity) {
        replace(activity, false, new DashboardFragment());
    }

    public static void showBusinessProfile(FragmentActivity activity) {
        replace(activity, true, new BusinessProfileFragment());
    }

    public static void showCategories(FragmentActivity activity) {
        replace(activity, true, new CategoriesFragment());
    }

    public static void showProducts(FragmentActivity activity) {
        replace(activity, true, new ProductsFragment());
    }

    public static void showInventory(FragmentActivity activity) {
        replace(activity, true, new InventoryFragment());
    }

    public static void showProductDetail(FragmentActivity activity, String productId) {
        android.content.Intent intent = new android.content.Intent(activity,
                com.example.tilldock.ui.products.ProductDetailActivity.class);
        intent.putExtra(com.example.tilldock.ui.products.ProductDetailActivity.EXTRA_PRODUCT_ID, productId);
        activity.startActivity(intent);
    }

    public static void showTransactionDetail(FragmentActivity activity, String saleId) {
        if (activity == null || saleId == null) return;
        com.example.tilldock.ui.transactions.TransactionDetailFragment fragment = com.example.tilldock.ui.transactions.TransactionDetailFragment.newInstance(saleId);
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, fragment);
        tx.addToBackStack("TransactionDetail");
        tx.commit();
    }
    public static void showTransactions(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.transactions.TransactionsFragment());
        tx.commit();
    }
    public static void showReports(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.reports.ReportsFragment());
        tx.commit();
    }
    public static void showStaff(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.staff.StaffRolesFragment());
        tx.commit();
    }
    public static void showSettings(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.settings.SettingsFragment());
        tx.commit();
    }
    public static void showProfile(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.profile.ProfileFragment());
        tx.commit();
    }

    public static void showNewSale(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.sales.NewSaleFragment());
        tx.commit();
    }

    public static void showMore(FragmentActivity activity) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.more.MoreFragment());
        tx.commit();
    }

    public static void signOut(FragmentActivity activity) {
        if (activity == null) return;
        com.example.tilldock.TillDockApplication app = com.example.tilldock.TillDockApplication.get();
        if (app != null && app.getAuthSession() != null) {
            app.getAuthSession().logout(null);
        }
        androidx.fragment.app.FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        androidx.fragment.app.FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.main_container, new com.example.tilldock.ui.auth.LoginFragment());
        tx.commit();
    }

    private static void replace(FragmentActivity activity, boolean addToBackStack, Fragment fragment) {
        androidx.fragment.app.FragmentTransaction tx = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.main_container, fragment);
        if (addToBackStack) {
            tx.addToBackStack(fragment.getClass().getSimpleName());
        }
        tx.commit();
    }
}
