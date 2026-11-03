package com.example.blaylist;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.signature.ObjectKey;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/** Makes Glide.with(...).load(mp3Uri) show the cover embedded in the mp3. */
@GlideModule
public final class BlaylistGlideModule extends AppGlideModule {
    /** Cover art: reading the picture embedded in each mp3. */
    private static final String TAG = "Blaylist.CoverLoader";

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(Uri.class, InputStream.class, new CoverLoader(context));
    }

    /** Glide runs loadData() on a background thread. */
    private static class CoverLoader implements ModelLoader<Uri, InputStream>, ModelLoaderFactory<Uri, InputStream> {
        private final Context context;

        CoverLoader(Context context) {
            this.context = context;
        }

        @Override
        public LoadData<InputStream> buildLoadData(@NonNull Uri uri, int width, int height, @NonNull Options options) {
            return new LoadData<>(new ObjectKey(uri), new DataFetcher<InputStream>() {
                @Override
                public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    byte[] cover;
                    try {
                        retriever.setDataSource(context, uri);
                        cover = retriever.getEmbeddedPicture();
                    } catch (RuntimeException e) {
                        Log.e(TAG, "can't open " + uri + " to read its cover", e);
                        callback.onLoadFailed(e);
                        return;
                    } finally {
                        try {
                            retriever.release();
                        } catch (IOException ignored) {
                        }
                    }
                    if (cover == null) {
                        Log.e(TAG, "no cover image embedded in " + uri + " - showing the placeholder");
                        callback.onLoadFailed(new FileNotFoundException("no cover in " + uri));
                    } else {
                        Log.d(TAG, "read " + cover.length + " byte cover from " + uri);
                        callback.onDataReady(new ByteArrayInputStream(cover));
                    }
                }

                @Override public void cleanup() {}
                @Override public void cancel() {}
                @NonNull @Override public Class<InputStream> getDataClass() { return InputStream.class; }
                @NonNull @Override public DataSource getDataSource() { return DataSource.LOCAL; }
            });
        }

        @Override public boolean handles(@NonNull Uri uri) { return true; }
        @NonNull @Override public ModelLoader<Uri, InputStream> build(@NonNull MultiModelLoaderFactory f) { return this; }
        @Override public void teardown() {}
    }
}
