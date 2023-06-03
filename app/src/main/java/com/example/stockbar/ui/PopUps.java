package com.example.stockbar.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.stockbar.R;
import com.example.stockbar.services.products.ProductManagerService;
import com.example.stockbar.services.products.Product;

import java.util.ArrayList;
import java.util.Arrays;

public class PopUps {

    private final Context context;

    public PopUps (Context context) {
        this.context = context;
    }

    public void showMenuPopUp (final View view, final Product card) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.edit)
                PopUps.this.showEditPopUp(card);
            else if (menuItem.getItemId() == R.id.move_to) {
                PopUps.this.showMovePopUp(card);
            } else if (menuItem.getItemId() == R.id.remove)
                PopUps.this.showRemovePopUp(card);
            return true;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.edit_menu, popup.getMenu());
        popup.show();
    }

    public void showMovePopUp (final Product card) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.move_popup_title, card.name, card.section));
        //builder.setTitle("Mover \"" + card.name + "\" de \"" + card.section + "\" a:");
        //builder.setMessage("Mover " + card.name + " de \""+card.section+"\" a ...");
        final ArrayList<String> options = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.move_menu_array)));
        options.remove(card.section);
        //options.remove(options.indexOf(card.section));
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                context, R.layout.move_dialog_option, options.toArray(new String[options.size()]));
        final int[] selected = new int[1];
        selected[0] = -1;

        builder.setSingleChoiceItems(adapter, selected[0], (dialogInterface, i) -> {
            //DatabaseService.service.moveProduct(card, options.get(i));
            selected[0] = i;
        });
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> ProductManagerService.moveProduct(card, options.get(selected[0])));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showAddPopUp () {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.add_dialog, null, false);
        builder.setView(dialogView);
        builder.setTitle(R.string.add_popup_title);
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
            EditText productName = dialogView.findViewById(R.id.edit_product_name);
            EditText productStock = dialogView.findViewById(R.id.edit_product_count);
            int stock = 0;
            try {
                stock = Integer.parseInt(productStock.getText().toString());
            } catch (Exception ignored) {}

            ProductManagerService.addNewProduct(productName.getText().toString(), stock);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {});

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showEditPopUp (final Product card) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null, false);
        final EditText name = dialogView.findViewById(R.id.edit_product_name);
        name.setText(card.name);
        final EditText amount = dialogView.findViewById(R.id.stock);
        amount.setText(String.valueOf(card.stock));
        amount.setRawInputType(Configuration.KEYBOARD_12KEY);
        final TextView changeText = dialogView.findViewById(R.id.stock_change);
        final ImageButton plus = dialogView.findViewById(R.id.button_plus);
        final ImageButton minus = dialogView.findViewById(R.id.button_minus);

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int new_amount = 0;
                try {
                    new_amount = Integer.parseInt(amount.getText().toString());
                } catch (NumberFormatException ignored) {}
                int count = new_amount - card.stock;
                changeText.setText(context.getString(R.string.edit_popup_change_text, (count > 0 ? "+" : ""), count));
                minus.setEnabled(new_amount > 0);
            }
        });
        plus.setOnClickListener((view) -> {
            int new_amount = 0;
            try {
                new_amount = Integer.parseInt(amount.getText().toString());
            } catch (NumberFormatException ignored) {}

            ++new_amount;
            int count = new_amount - card.stock;

            changeText.setText(context.getString(R.string.edit_popup_change_text, (count > 0 ? "+" : ""), count));
            amount.setText(String.valueOf(new_amount));
            minus.setEnabled(new_amount > 0);
        });
        minus.setOnClickListener((view) -> {
            int new_amount = 0;
            try {
                new_amount = Integer.parseInt(amount.getText().toString());
            } catch (NumberFormatException ignored) {}

            --new_amount;
            int count = new_amount - card.stock;

            changeText.setText(context.getString(R.string.edit_popup_change_text, (count > 0 ? "+" : ""), count));
            amount.setText(String.valueOf(new_amount));
            minus.setEnabled(new_amount > 0);
        });

        if (card.stock <= 0)
            minus.setEnabled(false);

        builder.setView(dialogView);
        builder.setTitle(R.string.edit_popup_title);
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
            int newStock = card.stock;
            try {
                newStock = Integer.parseInt(changeText.getText().toString()) + card.stock;
            } catch (Exception ignored) { }
            ProductManagerService.updateProduct(new Product(card.key, card.bar, card.section, name.getText().toString(), newStock));
        });
        builder.setNegativeButton(R.string.cancel, (inter,i) -> {});

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showRemovePopUp (final Product card) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.remove_popup_title);

        String message = context.getString(R.string.remove_popup_are_you_sure, card.name);
        if (card.stock > 0)
            message += "\n" + context.getResources().getQuantityString(R.plurals.remove_popup_still_n_units_left, card.stock, card.stock);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.remove, (dialogInterface, i) -> ProductManagerService.removeProduct(card));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> { });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
