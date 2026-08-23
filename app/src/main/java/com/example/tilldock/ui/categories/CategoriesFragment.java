package com.example.tilldock.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.CategoryRequest;
import com.example.tilldock.ui.ViewModelFactories;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Validators;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class CategoriesFragment extends Fragment {

    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private ProgressBar progress;
    private TextView errorText;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactories.categories()).get(CategoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_with_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.list_title);
        TextView subtitle = view.findViewById(R.id.list_subtitle);
        progress = view.findViewById(R.id.list_progress);
        errorText = view.findViewById(R.id.list_error_text);
        emptyState = view.findViewById(R.id.list_empty_state);
        emptyTitle = view.findViewById(R.id.list_empty_title);
        emptyBody = view.findViewById(R.id.list_empty_body);
        MaterialButton addButton = view.findViewById(R.id.list_button_add);

        title.setText(R.string.categories_title);
        subtitle.setText(R.string.categories_subtitle);
        emptyTitle.setText(R.string.categories_empty_title);
        emptyBody.setText(R.string.categories_empty_body);
        addButton.setText(R.string.categories_add);

        RecyclerView recycler = view.findViewById(R.id.list_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategoryAdapter(new CategoryAdapter.Listener() {
            @Override
            public void onEdit(Category category) {
                showDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                confirmDelete(category);
            }
        });
        recycler.setAdapter(adapter);

        addButton.setOnClickListener(v -> showDialog(null));

        View backButton = view.findViewById(R.id.list_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (!requireActivity().getSupportFragmentManager().popBackStackImmediate()) {
                    requireActivity().finish();
                }
            });
        }

        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        viewModel.load();
    }

    private void render(CategoryViewModel.State state) {
        if (state == null) return;
        progress.setVisibility(state.status == CategoryViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
        switch (state.status) {
            case IDLE:
                break;
            case LOADING:
                errorText.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
                break;
            case SUCCESS:
            case EMPTY:
                errorText.setVisibility(View.GONE);
                adapter.submit(state.categories);
                boolean empty = state.categories == null || state.categories.isEmpty();
                emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                break;
            case SAVED:
                adapter.submit(state.categories == null ? java.util.Collections.emptyList() : state.categories);
                viewModel.load();
                break;
            case ERROR:
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(messageOf(state.error));
                break;
        }
    }

    private void showDialog(@Nullable Category existing) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_form, null);
        TextInputEditText nameInput = view.findViewById(R.id.category_dialog_input_name);
        TextInputEditText descriptionInput = view.findViewById(R.id.category_dialog_input_description);
        if (existing != null) {
            nameInput.setText(existing.getName());
            descriptionInput.setText(existing.getDescription());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.categories_dialog_create_title : R.string.categories_dialog_edit_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String name = textOf(nameInput);
                    String description = textOf(descriptionInput);
                    if (!Validators.isValidBusinessName(name)) {
                        showError(getString(R.string.error_category_name_length));
                        return;
                    }
                    if (description.length() > 255) {
                        showError(getString(R.string.error_category_description_length));
                        return;
                    }
                    CategoryRequest request = new CategoryRequest(name, description);
                    if (existing == null) {
                        viewModel.create(request);
                    } else {
                        viewModel.update(existing.getId(), request);
                    }
                });
        builder.show();
    }

    private void confirmDelete(Category category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.categories_delete_confirm_title)
                .setMessage(R.string.categories_delete_confirm_message)
                .setNegativeButton(R.string.dialog_no, null)
                .setPositiveButton(R.string.dialog_yes, (d, w) -> viewModel.delete(category.getId()))
                .show();
    }

    private void showError(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString().trim();
    }

    private String messageOf(ApiError error) {
        if (error == null) return "";
        return error.message() == null ? getString(R.string.error_unknown) : error.message();
    }
}