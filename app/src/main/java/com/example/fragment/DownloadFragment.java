package com.example.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.adapter.DownloadAdapter;
import com.example.androidebookapps.BookDetailsActivity;
import com.example.androidebookapps.PDFShow;
import com.example.androidebookapps.R;
import com.example.androidebookapps.databinding.FragmentFavoriteBinding;
import com.example.item.DownloadList;
import com.example.item.SubCatListBook;
import com.example.util.DatabaseHandler;
import com.example.util.Method;
import com.example.util.OnClick;
import com.example.util.OnClicks;
import com.folioreader.FolioReader;
import com.folioreader.model.locators.ReadLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class DownloadFragment extends Fragment {

    FragmentFavoriteBinding viewDownload;
    List<File> inFiles;
    List<DownloadList> databaseLists;
    List<DownloadList> downloadLists;
    DatabaseHandler db;
    Method method;
    DownloadAdapter downloadAdapter;
    private OnClicks onClicks;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        viewDownload = FragmentFavoriteBinding.inflate(inflater, container, false);
        // Log.i("adslogoff", "onCreate: df");
        db = new DatabaseHandler(getActivity());
        databaseLists = new ArrayList<>();
        method = new Method(requireActivity());
        if (method.isNetworkAvailable()) {
            requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.app_bg_orange));
        }

        onClicks = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            // Log.e("adslogf", "onCreateView: fragment ");
            // Log.v("adslogf", "onCreateView: downloadLists.get(position).getId() "+downloadLists.get(position).getId());
            // Log.w("adslogf", "onCreateView: fileUrl "+fileUrl);

            String postLast = null;
            if(otherData != null){
                postLast = "continuePos";
            }
            // Log.e("adslogf", "onCreateView: postLast "+postLast);
            // Log.e("adslogf", "onCreateView: postLast "+otherData);
            if (fileUrl.contains(".epub")) {
                FolioReader folioReader = FolioReader.get();
                folioReader.setOnHighlightListener((highlight, type1) -> {

                });
                if (!db.checkIdEpub(id)) {

                    String string = db.getEpub(id);
                    ReadLocator readPosition = ReadLocator.fromJson(string);
                    folioReader.setReadLocator(readPosition);

                }
                folioReader.openBook(fileUrl);
                folioReader.setReadLocatorListener(readLocator -> {
                    if (db.checkIdEpub(id)) {
                        db.addEpub(id, readLocator.toJson());
                    } else {
                        db.updateEpub(id, readLocator.toJson());
                    }
                });
            } else {

                String[] strings = fileUrl.split("filename-");
                String[] idPdf = strings[1].split(".pdf");
                // Log.i("adslogd", "onCreateView: df pagenum "+otherData);
                startActivity(new Intent(getActivity(), PDFShow.class)
                        .putExtra("id", idPdf[0])
                        .putExtra("link", fileUrl)
                        .putExtra("title", title)
                        .putExtra("type", "file")
                        .putExtra("posLast", postLast)
                        .putExtra("PAGE_NUM", otherData));
            }
        };


        method = new Method(getActivity());

        inFiles = new ArrayList<>();
        downloadLists = new ArrayList<>();
        viewDownload.progressFav.setVisibility(View.GONE);
        viewDownload.llNoData.clNoDataFound.setVisibility(View.GONE);

        viewDownload.rvFav.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 3);
        viewDownload.rvFav.setLayoutManager(layoutManager);
        viewDownload.rvFav.setFocusable(false);

        new Execute().execute();
        return viewDownload.getRoot();
    }

    @SuppressLint("StaticFieldLeak")
    class Execute extends AsyncTask<String, String, String> {

        File file;

        @Override
        protected void onPreExecute() {

            viewDownload.progressFav.setVisibility(View.VISIBLE);
            databaseLists.clear();
            inFiles.clear();
            downloadLists.clear();
            db = new DatabaseHandler(getContext());
            file = new File(method.bookStorage());

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                databaseLists.addAll(db.getDownload());
                Queue<File> files = new LinkedList<>(Arrays.asList(file.listFiles()));
                while (!files.isEmpty()) {
                    File file = files.remove();
                    if (file.isDirectory()) {
                        files.addAll(Arrays.asList(file.listFiles()));
                    } else if (file.getName().endsWith(".epub") || file.getName().endsWith(".pdf")) {
                        inFiles.add(file);
                    }
                }
                for (int i = 0; i < databaseLists.size(); i++) {
                    for (int j = 0; j < inFiles.size(); j++) {
                        if (inFiles.get(j).toString().contains(databaseLists.get(i).getUrl())) {
                            downloadLists.add(databaseLists.get(i));
                            break;
                        } else {
                            if (j == inFiles.size() - 1) {
                                db.deleteDownloadBook(databaseLists.get(i).getId());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Log.d("error", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (downloadLists.size() == 0) {
                viewDownload.llNoData.clNoDataFound.setVisibility(View.VISIBLE);
                viewDownload.llNoData.textViewNoDataNoDataFound.setText(getString(R.string.no_download));
            } else {
                downloadAdapter = new DownloadAdapter(getActivity(), downloadLists, "download", onClicks);
                viewDownload.rvFav.setAdapter(downloadAdapter);
                downloadAdapter.setOnItemClickListener(new OnClick() {
                    @Override
                    public void position(int position) {
                        String pageNo = null;
                        String postLast = null;
                        DownloadList list = downloadLists.get(position);
                        ArrayList<SubCatListBook> favorites = method.getFavorites(getActivity());

                        if (favorites != null) {
                            for (SubCatListBook s : favorites) {
                                if (s.getPost_id().equals(list.getId())) {
                                    int i = favorites.indexOf(s);
                                    pageNo = favorites.get(i).getPage_num();
                                    postLast = "continuePos";
                                    // Log.i("adslogoff", "position: pageno "+pageNo);
                                }
                            }
                        }

                        if(pageNo != null){
                            postLast = "continuePos";
                        }
                        if (list.getUrl().endsWith(".epub")) {
                            FolioReader folioReader = FolioReader.get();
                            folioReader.setOnHighlightListener((highlight, type1) -> {

                            });
                            if (!db.checkIdEpub(list.getId())) {

                                String string = db.getEpub(list.getId());
                                ReadLocator readPosition = ReadLocator.fromJson(string);
                                folioReader.setReadLocator(readPosition);

                            }
                            folioReader.openBook(list.getUrl());
                            folioReader.setReadLocatorListener(readLocator -> {
                                if (db.checkIdEpub(list.getId())) {
                                    db.addEpub(list.getId(), readLocator.toJson());
                                } else {
                                    db.updateEpub(list.getId(), readLocator.toJson());
                                }
                            });
                        } else {
                            String[] strings = list.getUrl().split("filename-");
                            String[] idPdf = strings[1].split(".pdf");

                            startActivity(new Intent(getActivity(), PDFShow.class)
                                    .putExtra("id", idPdf[0])
                                    .putExtra("link", list.getUrl())
                                    .putExtra("toolbarTitle", list.getTitle())
                                    .putExtra("type", "file")
                                    .putExtra("posLast", postLast)
                                    .putExtra("PAGE_NUM", pageNo));
                        }
                    }
                });
            }

            viewDownload.progressFav.setVisibility(View.GONE);
            super.onPostExecute(s);
        }
    }

}
