package com.example.fragment;

import static com.example.adapter.ContinueAdapter.favListBookList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.braintreepayments.api.Json;
import com.example.adapter.ContinueAdapter;
import com.example.androidebookapps.BookDetailsActivity;
import com.example.androidebookapps.PDFShow;
import com.example.androidebookapps.R;
import com.example.androidebookapps.databinding.FragmentFavoriteBinding;
import com.example.androidebookapps.databinding.RowFavoriteBinding;
import com.example.item.SubCatListBook;
import com.example.response.SubCatListBookRP;
import com.example.rest.ApiClient;
import com.example.rest.ApiInterface;
import com.example.util.API;
import com.example.util.Method;
import com.example.util.OnClick;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ContinueFragment extends Fragment{

    FragmentFavoriteBinding viewContinue;
    Method method;
    ContinueAdapter continueAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.app_bg_orange));
        viewContinue = FragmentFavoriteBinding.inflate(inflater, container, false);
        method = new Method(requireActivity());

        viewContinue.progressFav.setVisibility(View.GONE);
        viewContinue.llNoData.clNoDataFound.setVisibility(View.GONE);

        viewContinue.rvFav.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 3);
        viewContinue.rvFav.setLayoutManager(layoutManager);
        viewContinue.rvFav.setFocusable(false);

        if (method.isNetworkAvailable()) {
            if (!method.getUserId().isEmpty())
                ContinueData();
            else
                continueNoprofile();
        } else {
            method.alertBox(getString(R.string.internet_connection));
        }

        return viewContinue.getRoot();
    }

    private void DeleteContinueRead(SubCatListBook book,int pos){
        if((getActivity() != null)) {
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(requireActivity()));
            jsObj.addProperty("user_id",  method.getUserId());
            jsObj.addProperty("post_id", book.getPost_id());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            retrofit2.Call<JsonObject> call = apiService.delBookContinueData(API.toBase64(jsObj.toString()));
            String json = new Gson().toJson(jsObj);
            Log.i("adslog", "DeleteContinueRead: json delete "+json);
            call.enqueue(new retrofit2.Callback<JsonObject>(){
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.i("adslog", "onResponse: "+response);
                    final Animation animation = AnimationUtils.loadAnimation(getContext(),
                            R.anim.slide_out);
                    View v = Objects.requireNonNull(viewContinue.rvFav.findViewHolderForAdapterPosition(pos)).itemView;
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            favListBookList.remove(pos);
                            continueAdapter.notifyDataSetChanged();
                        }
                    });
                    v.startAnimation(animation);

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.i("adslog", "onFailure: "+t.getMessage());
                }
            });
        }
    }
    private void ContinueData() {
        if (getActivity() != null) {
            viewContinue.progressFav.setVisibility(View.VISIBLE);
            Call<SubCatListBookRP> call;
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(requireActivity()));
            jsObj.addProperty("user_id", method.getUserId());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            call = apiService.getContinueData(API.toBase64(jsObj.toString()));
            Log.i("adslog", "ContinueData: apiService " + apiService);
            Log.i("adslog", "ContinueData: call " + call.toString());
            String json = new Gson().toJson(jsObj);
            Log.v("adslog", "bookContinuePageData: json " + json);
            call.enqueue(new Callback<SubCatListBookRP>() {
                @Override
                public void onResponse(@NotNull Call<SubCatListBookRP> call, @NotNull Response<SubCatListBookRP> response) {
                    if (getActivity() != null) {
                        try {

                            SubCatListBookRP favListBookRP = response.body();
                            Log.i("adslog", "onResponse: favListBookRP.getTotal_records() " + favListBookRP.getTotal_records());
                            if (favListBookRP != null && favListBookRP.getSuccess().equals("1")) {
                                if (favListBookRP.getSubCatListBooks().size() != 0) {
                                    viewContinue.rvFav.setVisibility(View.VISIBLE);
                                    continueAdapter = new ContinueAdapter(requireActivity(), favListBookRP.getSubCatListBooks(), new ContinueAdapter.SetOnClickListener() {
                                        @Override
                                        public void onDelete(View view , int pos) {
                                            SubCatListBook book = favListBookRP.getSubCatListBooks().get(pos);
                                            DeleteContinueRead(book,pos);
                                        }
                                    });
                                    viewContinue.rvFav.setAdapter(continueAdapter);
                                    continueAdapter.setOnItemClickListener(new OnClick() {
                                        @Override
                                        public void position(int position) {
                                            SubCatListBook book = favListBookRP.getSubCatListBooks().get(position);
                                            Intent intentDetail = new Intent(requireActivity(), BookDetailsActivity.class);
                                            intentDetail.putExtra("BOOK_ID", book.getPost_id());
                                            intentDetail.putExtra("LAST_POS", "continuePos");
                                            intentDetail.putExtra("PAGE_NUM", book.getPage_num());
                                            startActivity(intentDetail);
                                        }
                                    });

                                } else {
                                    viewContinue.llNoData.clNoDataFound.setVisibility(View.VISIBLE);
                                    viewContinue.llNoData.textViewNoDataNoDataFound.setText(getString(R.string.no_continue));
                                    viewContinue.rvFav.setVisibility(View.GONE);
                                    viewContinue.progressFav.setVisibility(View.GONE);
                                }
                            } else {
                                viewContinue.llNoData.clNoDataFound.setVisibility(View.VISIBLE);
                                viewContinue.rvFav.setVisibility(View.GONE);
                                viewContinue.progressFav.setVisibility(View.GONE);
                                method.alertBox(getString(R.string.failed_try_again));
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getString(R.string.failed_try_again));
                        }
                    }
                    viewContinue.progressFav.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NotNull Call<SubCatListBookRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    viewContinue.llNoData.clNoDataFound.setVisibility(View.VISIBLE);
                    viewContinue.progressFav.setVisibility(View.GONE);
                    method.alertBox(getString(R.string.failed_try_again));
                }
            });
        }
    }

    ArrayList<SubCatListBook> listFave;
    int j = 1;
    private int pageIndex = 1;

    private List<SubCatListBook> continue2read ;
    private void continueNoprofile() {
        listFave = method.getFavorites(getContext());
        viewContinue.rvFav.setVisibility(View.VISIBLE);
        if (listFave != null) {
            Log.e("adslog", "continueNoprofile: " + listFave.size());
            continue2read = new ArrayList<>();
            continue2read.clear();
            Call<SubCatListBookRP> call;
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(requireActivity()));
            jsObj.addProperty("user_id", method.getUserId());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            call = apiService.getBookLatestDatas(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<SubCatListBookRP>() {
                @Override
                public void onResponse(Call<SubCatListBookRP> call, Response<SubCatListBookRP> response) {
                    if (getActivity() != null) {
                        try {
                            SubCatListBookRP subCatListBookRP = response.body();
                            Log.i("adslog", "onResponse: subcatlist "+subCatListBookRP.getSubCatListBooks().size());
                            if (subCatListBookRP != null && subCatListBookRP.getSuccess().equals("1")) {
                                if (subCatListBookRP.getSubCatListBooks().size() != 0) {
                                    for (int i = 0; i < listFave.size(); i++) {
                                        for (int j = 0; j < subCatListBookRP.getSubCatListBooks().size(); j++) {
//                                            Log.w("adslog", "onResponse: listFave "+listFave.get(j).getPost_id());
//                                            Log.v("adslog", "onResponse: subCtListBookRP "+subCatListBookRP.getSubCatListBooks().get(i).getPost_id());
                                            if (Objects.equals(listFave.get(i).getPost_id(), subCatListBookRP.getSubCatListBooks().get(j).getPost_id())) {
                                                Log.i("adslog", "onResponse: sama yg " + subCatListBookRP.getSubCatListBooks().get(j).getPost_title());
                                                continue2read.add(i,subCatListBookRP.getSubCatListBooks().get(j));
                                                Log.i("adslog", "onResponse: add pos "+i);
                                            }
                                        }
                                    }
                                    Log.i("adslog", "onResponse: continue2read.size() "+continue2read.size());
                                    viewContinue.rvFav.setVisibility(View.VISIBLE);
                                    continueAdapter = new ContinueAdapter(requireActivity(), continue2read, new ContinueAdapter.SetOnClickListener() {
                                        @Override
                                        public void onDelete(View view, int pos) {
                                            final Animation animation = AnimationUtils.loadAnimation(getContext(),
                                                    R.anim.slide_out);
                                            View v = Objects.requireNonNull(viewContinue.rvFav.findViewHolderForAdapterPosition(pos)).itemView;
                                            animation.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {
                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {
                                                    Method.removeFavorite(getContext(),favListBookList.get(pos));
                                                    favListBookList.remove(pos);
                                                    continueAdapter.notifyDataSetChanged();
                                                }
                                            });
                                            v.startAnimation(animation);
                                        }
                                    });
                                    viewContinue.rvFav.setAdapter(continueAdapter);

                                    continueAdapter.setOnItemClickListener(new OnClick() {
                                        @Override
                                        public void position(int position) {
                                            SubCatListBook book = subCatListBookRP.getSubCatListBooks().get(position);
                                            Intent intentDetail = new Intent(requireActivity(), BookDetailsActivity.class);
                                            intentDetail.putExtra("BOOK_ID", listFave.get(position).getPost_id());
                                            intentDetail.putExtra("LAST_POS", "continuePos");
                                            intentDetail.putExtra("PAGE_NUM", listFave.get(position).getPage_num());
                                            startActivity(intentDetail);
                                        }
                                    });
                                }
                            }

                        } catch (Exception e) {
                            Log.d("adslog", "exception "+e.toString());
                            method.alertBox(getString(R.string.failed_try_again));
                        }
                    }
                }

                @Override
                public void onFailure(Call<SubCatListBookRP> call, Throwable t) {

                }
            });
        }
    }
}
