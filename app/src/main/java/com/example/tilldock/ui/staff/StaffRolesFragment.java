package com.example.tilldock.ui.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

public class StaffRolesFragment extends Fragment {

    private static final List<Role> DEFAULT_ROLES = Arrays.asList(
            new Role("Owner", "Full access"),
            new Role("Manager", "Reports, sales, refunds"),
            new Role("Cashier", "Create sales, take payment"),
            new Role("Inventory", "Products & stock only")
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_roles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar toolbar = view.findViewById(R.id.staff_toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        RecyclerView recycler = view.findViewById(R.id.staff_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(new RoleAdapter(DEFAULT_ROLES));
    }

    public static class Role {
        public final String name;
        public final String description;
        public Role(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}