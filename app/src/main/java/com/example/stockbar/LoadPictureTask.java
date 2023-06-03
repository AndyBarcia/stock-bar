package com.example.stockbar;

import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class LoadPictureTask extends AsyncTask<Uri, Void, RoundedBitmapDrawable>  {

    public interface OnResult {
        void onPicture(@NonNull RoundedBitmapDrawable picture);
    }

    private final Resources res;
    private final OnResult onResult;

    public LoadPictureTask(Resources res, OnResult onResult) {
        this.res = res;
        this.onResult = onResult;
    }

    @Override
    protected RoundedBitmapDrawable doInBackground(Uri... params) {
        for (Uri param : params) {
            try {
                InputStream is = (InputStream) new URL(param.toString()).getContent();
                return RoundedBitmapDrawableFactory.create(res, is);
            } catch (IOException e) {
                Log.e("Error", e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(RoundedBitmapDrawable result) {
        if (result != null)
            this.onResult.onPicture(result);
    }

}
