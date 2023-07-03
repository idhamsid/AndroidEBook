package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebookapps.BookDetailsActivity;
import com.example.androidebookapps.R;
import com.example.androidebookapps.databinding.RowDownloadBinding;
import com.example.androidebookapps.databinding.RowFavoriteBinding;
import com.example.item.DownloadList;
import com.example.item.SubCatListBook;
import com.example.response.SubCatListBookRP;
import com.example.rest.ApiClient;
import com.example.rest.ApiInterface;
import com.example.util.API;
import com.example.util.AdInterstitialAds;
import com.example.util.DatabaseHandler;
import com.example.util.Method;
import com.example.util.OnClick;
import com.example.util.OnClicks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    Method method;
    Activity activity;
    DatabaseHandler db;
    List<DownloadList> downloadLists;
    OnClick onClick;
    int columnWidth;
    String pageNum;
    private String type;

    public DownloadAdapter(Activity activity, List<DownloadList> downloadLists, String type, OnClicks onClicks) {
        this.activity = activity;
        this.downloadLists = downloadLists;
        this.type = type;
        db = new DatabaseHandler(activity);
        method = new Method(activity, onClicks);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((3 + 1) * padding)));
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(RowDownloadBinding.inflate(activity.getLayoutInflater()));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.rowDownloadBinding.llHomeBook.setLayoutParams(new LinearLayout.LayoutParams(columnWidth / 3, columnWidth / 2));
        holder.rowDownloadBinding.tvHomeConTitle.setText(downloadLists.get(position).getTitle());

        Glide.with(activity.getApplicationContext()).load("file://" + downloadLists.get(position).getImage())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.rowDownloadBinding.ivHomeCont);
        holder.rowDownloadBinding.rlFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("adslogd", "onClick: adapter");

                if(!method.isNetworkAvailable()){
                    AdInterstitialAds.ShowInterstitialAds(activity,holder.getBindingAdapterPosition(),onClick);
                 } else {
                    Call<SubCatListBookRP> call;
                    JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
                    jsObj.addProperty("user_id", method.getUserId());
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                    call = apiService.getContinueData(API.toBase64(jsObj.toString()));
                    String json = new Gson().toJson(jsObj);
                    Log.v("adslogd", "bookContinuePageData: json " + json);
                    call.enqueue(new Callback<SubCatListBookRP>() {
                        @Override
                        public void onResponse(Call<SubCatListBookRP> call, Response<SubCatListBookRP> response) {
                            SubCatListBookRP favListBookRP = response.body();
                            List<SubCatListBook> bookContent = favListBookRP.getSubCatListBooks();

                            Log.i("adslogad", "onResponse: book size " + favListBookRP.getSubCatListBooks().size());
                            if (position <= favListBookRP.getSubCatListBooks().size() - 1) {

                                if (favListBookRP != null) {
                                    for (SubCatListBook s : bookContent) {
                                        if (s.getPost_id().equals(downloadLists.get(position).getId())) {
                                            Log.w("adslogad", "addFavorite: favorites.indexOf(s); " + bookContent.indexOf(s));
                                            int i = bookContent.indexOf(s);
                                            pageNum = bookContent.get(i).getPage_num();
                                            Log.i("adslogad", "onResponse: judul " + bookContent.get(i).getPost_title());
                                            Log.i("adslogad", "onResponse: pageNum " + pageNum);
                                        }
                                    }
                                }

                                Log.i("adslogad", "onResponse: pageNum" + pageNum);
                                method.onClickAd(position, "download", downloadLists.get(position).getId(), "", downloadLists.get(position).getId(), "", downloadLists.get(position).getUrl(), pageNum);
                            } else
                                method.onClickAd(position, "download", downloadLists.get(position).getId(), "", downloadLists.get(position).getId(), "", downloadLists.get(position).getUrl(), "0");


                        }

                        @Override
                        public void onFailure(Call<SubCatListBookRP> call, Throwable t) {

                        }
                    });
                }
            }
        });


        holder.rowDownloadBinding.cvDelete.setOnClickListener(v -> {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
            builder.setMessage(activity.getResources().getString(R.string.delete_msg));
            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.lbl_delete),
                    (arg0, arg1) -> {

                        if (!db.checkIdDownloadBook(downloadLists.get(position).getId())) {
                            db.deleteDownloadBook(downloadLists.get(position).getId());
                            File file = new File(downloadLists.get(position).getUrl());
                            File file_image = new File(downloadLists.get(position).getImage());
                            file.delete();
                            file_image.delete();
                            downloadLists.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                        }

                    });
            builder.setNegativeButton(activity.getResources().getString(R.string.cancel),
                    (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        });

    }

    @Override
    public int getItemCount() {
        return downloadLists.size();
    }

    public void setOnItemClickListener(OnClick clickListener) {
        this.onClick = clickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RowDownloadBinding rowDownloadBinding;

        public ViewHolder(RowDownloadBinding rowDownloadBinding) {
            super(rowDownloadBinding.getRoot());
            this.rowDownloadBinding = rowDownloadBinding;
        }
    }
}
