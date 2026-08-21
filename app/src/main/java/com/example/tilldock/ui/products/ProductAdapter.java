package com.example.tilldock.ui.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.StockStatus;
import com.example.tilldock.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {

    public interface Listener {
        void onProductClick(Product product);
        void onProductEdit(Product product);
        void onProductInventory(Product product);
        void onProductArchiveToggle(Product product);
        void onProductChangeImage(Product product);
        void onProductRemoveImage(Product product);
    }

    public interface CategoryResolver {
        String nameFor(String categoryId);
    }

    private final List<Product> items = new ArrayList<>();
    private final Listener listener;
    private final CategoryResolver categoryResolver;

    public ProductAdapter(Listener listener, CategoryResolver categoryResolver) {
        this.listener = listener;
        this.categoryResolver = categoryResolver;
    }

    public void submit(List<Product> next) {
        items.clear();
        if (next != null) items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = items.get(position);
        holder.bind(product, listener, categoryResolver);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ImageView thumb;
        private final TextView name;
        private final TextView category;
        private final TextView price;
        private final TextView stock;
        private final TextView statusPill;
        private final ImageButton overflow;

        Holder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.product_item_thumb);
            name = itemView.findViewById(R.id.product_item_name);
            category = itemView.findViewById(R.id.product_item_category);
            price = itemView.findViewById(R.id.product_item_selling_price);
            stock = itemView.findViewById(R.id.product_item_stock);
            statusPill = itemView.findViewById(R.id.product_item_status_pill);
            overflow = itemView.findViewById(R.id.product_item_overflow);
        }

        void bind(Product product, Listener listener, CategoryResolver categoryResolver) {
            name.setText(product.getName() == null ? "" : product.getName());
            String categoryName = product.getCategoryId() == null ? "" : (categoryResolver == null ? "" : categoryResolver.nameFor(product.getCategoryId()));
            category.setText(categoryName);
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
            if (product.getSellingPrice() != null) {
                price.setText(currency.format(product.getSellingPrice()));
            } else {
                price.setText("—");
            }
            Integer qty = product.getStockQuantity();
            stock.setText(itemView.getContext().getString(R.string.products_label_stock_value_format, qty == null ? 0 : qty));
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ImageLoader.get().load(imageUrl, thumb);
            } else {
                thumb.setImageResource(R.drawable.ic_image_placeholder);
            }

            StockStatus status = product.getStockStatus();
            if (product.isArchived() || status == null) {
                statusPill.setBackgroundResource(R.drawable.bg_pill_archived);
                statusPill.setTextColor(itemView.getContext().getColor(R.color.stock_archived));
                statusPill.setText(itemView.getContext().getString(R.string.product_status_archived));
            } else if (status == StockStatus.IN_STOCK) {
                statusPill.setBackgroundResource(R.drawable.bg_pill_in_stock);
                statusPill.setTextColor(itemView.getContext().getColor(R.color.stock_success));
                statusPill.setText(itemView.getContext().getString(R.string.product_status_in_stock));
            } else if (status == StockStatus.LOW) {
                statusPill.setBackgroundResource(R.drawable.bg_pill_low);
                statusPill.setTextColor(itemView.getContext().getColor(R.color.stock_warning));
                statusPill.setText(itemView.getContext().getString(R.string.product_status_low));
            } else {
                statusPill.setBackgroundResource(R.drawable.bg_pill_out);
                statusPill.setTextColor(itemView.getContext().getColor(R.color.stock_danger));
                statusPill.setText(itemView.getContext().getString(R.string.product_status_out));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
            overflow.setOnClickListener(v -> showOverflowMenu(v, product, listener));
        }

        private void showOverflowMenu(View anchor, Product product, Listener listener) {
            android.content.Context context = anchor.getContext();
            PopupMenu menu = new PopupMenu(context, anchor);
            menu.getMenu().add(0, 1, 0, R.string.common_edit);
            menu.getMenu().add(0, 2, 1, R.string.inventory_title);
            menu.getMenu().add(0, 3, 2,
                    product.isArchived() ? R.string.product_overflow_restore : R.string.product_overflow_archive);
            menu.getMenu().add(0, 4, 3, R.string.product_overflow_change_image);
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                menu.getMenu().add(0, 5, 4, R.string.product_overflow_delete_image);
            }
            menu.setOnMenuItemClickListener(item -> {
                if (listener == null) return true;
                int id = item.getItemId();
                if (id == 1) listener.onProductEdit(product);
                else if (id == 2) listener.onProductInventory(product);
                else if (id == 3) listener.onProductArchiveToggle(product);
                else if (id == 4) listener.onProductChangeImage(product);
                else if (id == 5) listener.onProductRemoveImage(product);
                return true;
            });
            menu.show();
        }
    }
}