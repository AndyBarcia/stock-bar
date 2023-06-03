package com.example.stockbar.ui.products;

import android.util.Log;

import com.example.stockbar.services.products.Product;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProductFilteredList {

    public interface ChangeListener {
        void added (int index);
        void updated (int index);
        void removed (int index);
    }

    private final List<Product> totalList = new ArrayList<>();
    private final List<Product> visibleList = new ArrayList<>();
    private String filter = null;
    private final ChangeListener listener;

    public ProductFilteredList(ChangeListener listener) {
        this.listener = listener;
    }

    public void setFilter (String filter) {
        this.filter = filter.isEmpty() ? null : filter;
        if (this.filter != null) {
            this.filter = Normalizer.normalize(filter, Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase();
        }
        recalculateVisibleList();
    }

    public void removeFilter () {
        this.filter = null;
        recalculateVisibleList();
    }

    public void addCard (Product newCard) {
        // Adds at position corresponding to alphabetical order
        int index = -Collections.binarySearch(totalList, newCard) - 1;
        totalList.add(index, newCard);
        if (cardChecks(newCard)) {
            int visibleIndex = -Collections.binarySearch(visibleList, newCard) - 1;
            visibleList.add(visibleIndex, newCard);
            this.listener.added(visibleIndex);
        }
    }

    public void removeCard (final Product card) {
        int i = findIndexByKey(card.key);
        if (i != -1) {
            totalList.remove(i);
            if (cardChecks(card)) {
                int vi = findVisibleIndexByKey(card.key);
                if (vi != -1) {
                    visibleList.remove(vi);
                    this.listener.removed(vi);
                }
            }
        }
    }

    public void updateCard (Product newCard) {
        int i = findIndexByKey(newCard.key);
        if (i != -1) {
            Product prevCard = totalList.get(i);
            totalList.set(i, newCard);
            // The previous card was visible
            if (cardChecks(prevCard)) {
                int vi = findVisibleIndexByKey(prevCard.key);
                if (vi != -1) {
                    if (cardChecks(newCard)) {
                        visibleList.set(vi, newCard);
                        this.listener.updated(vi);
                    } else {
                        visibleList.remove(vi);
                        this.listener.removed(vi);
                    }
                }
            // The previous card was NOT visible
            } else if (cardChecks(newCard)) {
                int vi = findVisibleIndexByKey(newCard.key);
                if (vi != -1) {
                    // If the previous card was NOT visible, but not it is, then we need to
                    // change the whole thing.
                    visibleList.add(vi, newCard);
                    this.listener.added(vi);
                }
            }
        }
    }

    public int size () {
        return visibleList.size();
    }

    public Product get (int i) {
        return visibleList.get(i);
    }

    private int findIndexByKey (String key) {
        for (int i = 0; i < totalList.size(); i++) {
            if (totalList.get(i).key.equals(key))
                return i;
        }
        return -1;
    }

    private int findVisibleIndexByKey (String key) {
        for (int i = 0; i < visibleList.size(); i++) {
            if (visibleList.get(i).key.equals(key))
                return i;
        }
        return -1;
    }

    private void recalculateVisibleList () {
        int vi = 0;
        for (int i = 0; i < totalList.size(); i++) {
            Product card = totalList.get(i);
            Product vcard = vi < visibleList.size() ? visibleList.get(vi) : null;
            boolean newCardChecks = cardChecks(card);

            if (vcard != null && card.key.equals(vcard.key)) {
                if (newCardChecks) {
                    //visibleList.set(vi, card);
                    //this.listener.updated(vi);
                    vi++;
                } else {
                    visibleList.remove(vi);
                    this.listener.removed(vi);
                }
            } else if (newCardChecks) {
                visibleList.add(vi, card);
                this.listener.added(vi);
                vi++;
            }
        }
    }

    private boolean cardChecks (Product card) {
        if (filter == null)
            return true;
        String name = Normalizer.normalize(card.name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return name.toLowerCase().contains(filter);
    }

}
