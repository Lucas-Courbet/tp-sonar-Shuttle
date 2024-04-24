package com.simplecity.amp_library.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.Stream;
import com.mlsdev.rximagepicker.RxImageConverters;
import com.mlsdev.rximagepicker.RxImagePicker;
import com.mlsdev.rximagepicker.Sources;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.model.ArtworkModel;
import com.simplecity.amp_library.model.ArtworkProvider;
import com.simplecity.amp_library.model.UserSelectedArtwork;
import com.simplecity.amp_library.sql.databases.CustomArtworkTable;
import com.simplecity.amp_library.ui.modelviews.ArtworkLoadingView;
import com.simplecity.amp_library.ui.modelviews.ArtworkView;
import com.simplecity.amp_library.ui.views.recyclerview.SpacesItemDecoration;
import com.simplecityapps.recycler_adapter.adapter.ViewModelAdapter;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import com.simplecityapps.recycler_adapter.recyclerview.RecyclerListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArtworkDialog {

    private ArtworkDialog() {
    }

    public static MaterialDialog build(Context context, ArtworkProvider artworkProvider) {
        View customView = inflateCustomView(context);
        ViewModelAdapter adapter = setupRecyclerView(context, customView, artworkProvider);
        setupArtworkViews(context, adapter, artworkProvider);
        return createMaterialDialog(context, customView, adapter, artworkProvider);
    }

    private static View inflateCustomView(Context context) {
        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_artwork, null);
        return customView;
    }

    private static ViewModelAdapter setupRecyclerView(Context context, View customView) {
        ViewModelAdapter adapter = new ViewModelAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = customView.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new SpacesItemDecoration(16));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(0);
        recyclerView.setRecyclerListener(new RecyclerListener());
        adapter.items.add(0, new ArtworkLoadingView());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    private static void setupArtworkViews(Context context, ViewModelAdapter adapter, ArtworkProvider artworkProvider) {
        List<ViewModel> viewModels = new ArrayList<>();
        UserSelectedArtwork userSelectedArtwork = ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.get(artworkProvider.getArtworkKey());
        if (userSelectedArtwork != null) {
            File file = null;
            if (userSelectedArtwork.path != null) {
                file = new File(userSelectedArtwork.path);
            }
            ArtworkView artworkView = new ArtworkView(userSelectedArtwork.type, artworkProvider, glideListener, file, true);
            artworkView.setSelected(true);
            viewModels.add(artworkView);
        }
        adapter.setItems(viewModels);
    }

    private static MaterialDialog createMaterialDialog(Context context, View customView, ViewModelAdapter adapter, ArtworkProvider artworkProvider) {
        return new MaterialDialog.Builder(context)
                .title(R.string.artwork_edit)
                .customView(customView, false)
                .autoDismiss(false)
                .positiveText(context.getString(R.string.save))
                .onPositive((dialog, which) -> {
                    ArtworkView checkedView = getCheckedView(adapter.items);
                    if (checkedView != null) {
                        ArtworkModel artworkModel = checkedView.getItem();
                        ContentValues values = new ContentValues();
                        values.put(CustomArtworkTable.COLUMN_KEY, artworkProvider.getArtworkKey());
                        values.put(CustomArtworkTable.COLUMN_TYPE, artworkModel.type);
                        values.put(CustomArtworkTable.COLUMN_PATH, artworkModel.file == null ? null : artworkModel.file.getPath());
                        context.getContentResolver().insert(CustomArtworkTable.URI, values);
                        ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.put(artworkProvider.getArtworkKey(),
                                new UserSelectedArtwork(artworkModel.type, artworkModel.file == null ? null : artworkModel.file.getPath()));
                    } else {
                        context.getContentResolver().delete(CustomArtworkTable.URI, CustomArtworkTable.COLUMN_KEY + "='" + artworkProvider.getArtworkKey().replaceAll("'", "\''") + "'", null);
                        ((ShuttleApplication) context.getApplicationContext()).userSelectedArtwork.remove(artworkProvider.getArtworkKey());
                    }
                    dialog.dismiss();
                })
                .negativeText(context.getString(R.string.close))
                .onNegative((dialog, which) -> dialog.dismiss())
                .neutralText(context.getString(R.string.artwork_gallery))
                .onNeutral((dialog, which) -> {
                    RxImagePicker.with(context)
                        .requestImage(Sources.GALLERY)
                        .flatMap(RxImageConverters.toBitmap())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            ArtworkView artworkView = new ArtworkView(ArtworkType.ALBUM, artworkProvider, glideListener, bitmap, true);
                            adapter.items.add(artworkView);
                            adapter.notifyDataSetChanged();
                        }, throwable -> {
                            Log.e(TAG, "Error picking image", throwable);
                        });
            })
                })
                .cancelable(false)
                .build();
    }

    @Nullable
    public static ArtworkView getCheckedView(List<ViewModel> viewModels) {
        return (ArtworkView) Stream.of(viewModels)
                .filter(viewModel -> viewModel instanceof ArtworkView && ((ArtworkView) viewModel).isSelected())
                .findFirst()
                .orElse(null);
    }
}