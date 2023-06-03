package com.example.stockbar.services.products;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stockbar.services.DatabaseService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProductManagerService {

    private static String currentBar;
    private static String currentSection;
    private static DatabaseReference currentRef;
    private static ChildEventListener referenceChildEventListener;

    public interface ServiceListener {
        void onProductAdded (Product card);
        void onProductEdited (Product card);
        void onProductRemoved (Product card);
    }

    public static void setReference(@NonNull String barName, @NonNull String section, final ServiceListener listener) {
        if (barName == null)
            Log.e("ProductManagerService", "barName es null");
        if (section == null)
            Log.e("ProductManagerService", "section es null");

        if (referenceChildEventListener != null && currentRef != null)
            currentRef.orderByChild("name").removeEventListener(referenceChildEventListener);

        referenceChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    listener.onProductAdded(new Product(snapshot, ProductManagerService.currentBar, ProductManagerService.currentSection));
                } catch (Exception ignored) { }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    listener.onProductEdited(new Product(snapshot, ProductManagerService.currentBar, ProductManagerService.currentSection));
                } catch (Exception ignored) { }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                try {
                    listener.onProductRemoved(new Product(snapshot, ProductManagerService.currentBar, ProductManagerService.currentSection));
                } catch (Exception ignored) { }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        FirebaseDatabase database = DatabaseService.getDatabase();

        database.getReference("/bars/"+ProductManagerService.currentBar).keepSynced(false);
        database.getReference("/bars/"+barName).keepSynced(true);

        ProductManagerService.currentSection = section;
        ProductManagerService.currentBar = barName;

        currentRef = database.getReference("/bars/"+barName+"/"+section+"/products");
        currentRef.orderByChild("name").addChildEventListener(referenceChildEventListener);
    }

    public static void addNewProduct(String name, int stock) {
        updateProduct(new Product(currentRef.push().getKey(), currentBar, currentSection, name, stock));
    }

    public static void updateProduct(Product card) {
        Map<String, Object> newProd = new HashMap<>();
        newProd.put("/name", card.name);
        newProd.put("/stock", card.stock);
        currentRef.child(card.key).updateChildren(newProd);
    }

    public static void removeProduct(Product card) {
        currentRef.child(card.key).removeValue();
    }

    public static void moveProduct(Product card, String newSection) {
        FirebaseDatabase database = DatabaseService.getDatabase();

        DatabaseReference tempRef = database.getReference("/bars/"+currentBar+"/"+newSection+"/products");
        String newKey = tempRef.push().getKey();

        Map<String, Object> newProd = new HashMap<>();
        newProd.put("/"+newSection+"/products/"+newKey+"/name", card.name);
        newProd.put("/"+newSection+"/products/"+newKey+"/stock", card.stock);
        newProd.put("/"+currentSection+"/products/"+card.key, null);
        database.getReference("/bars/"+currentBar).updateChildren(newProd);
    }

}
