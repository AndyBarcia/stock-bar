package com.example.stockbar.ui.products;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockbar.MainActivity;
import com.example.stockbar.services.products.Product;
import com.example.stockbar.ui.PopUps;
import com.example.stockbar.R;
import com.example.stockbar.services.products.ProductManagerService;

import java.util.Locale;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ViewHolder> implements ProductManagerService.ServiceListener {

    private final ProductFilteredList productList = new ProductFilteredList(new ProductFilteredList.ChangeListener() {
        @Override
        public void added(int index) {
            notifyItemInserted(index);
        }

        @Override
        public void updated(int index) {
            notifyItemChanged(index);
        }

        @Override
        public void removed(int index) {
            notifyItemRemoved(index);
        }
    });

    private final PopUps popUps;

    public ProductCardAdapter(final PopUps popUps, @NonNull String barName, String section) {
        this.popUps = popUps;
        ProductManagerService.setReference(barName, section, this);
        
        MainActivity.queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productList.setFilter(newText);
                return false;
            }
        };
        productList.setFilter(MainActivity.search);
    }

    @Override
    public void onProductAdded(Product card) {
        productList.addCard(card);
    }

    @Override
    public void onProductEdited(Product card) {
        productList.updateCard(card);
    }

    @Override
    public void onProductRemoved(Product card) {
        productList.removeCard(card);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Product product = productList.get(position);
        holder.setGrayedOut(product.stock <= 0);
        holder.name.setText(product.name);
        holder.count.setText(String.format(Locale.getDefault(),"%d", product.stock));
        holder.barName.setText(product.bar);
        holder.config.setOnClickListener(view -> popUps.showMenuPopUp (holder.config, productList.get(holder.getAdapterPosition())));
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.setGrayedOut(false);
        holder.name.setText(R.string.default_product_name);
        holder.count.setText(R.string.default_product_stock);
        holder.barName.setText(R.string.default_product_bar_name);
        holder.config.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout layout;
        private final TextView name;
        private final TextView count;
        private final TextView barName;
        private final ImageView config;

        public ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.constraint_layout);
            name = v.findViewById(R.id.product_name);
            count = v.findViewById(R.id.product_stock);
            barName = v.findViewById(R.id.product_bar_name);
            config = v.findViewById(R.id.edit_button);
        }

        public void setGrayedOut(boolean grayOut) {
            if (grayOut) {
                //layout.getForeground().setColorFilter(Color.alpha(90), PorterDuff.Mode.ADD);
                //layout.getForeground().setColorFilter(Color.parseColor("#EFEF"), PorterDuff.Mode.ADD);
                layout.getBackground().setColorFilter(Color.parseColor("#EFEFEF"), PorterDuff.Mode.SRC_IN);
            } else {
                layout.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        }
    }

}
