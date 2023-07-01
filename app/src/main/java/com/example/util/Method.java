package com.example.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.example.androidebookapps.BookDetailsActivity;
import com.example.androidebookapps.R;
import com.example.item.DownloadList;
import com.example.item.SubCatListBook;
import com.example.response.FavoriteRP;
import com.example.rest.ApiClient;
import com.example.rest.ApiInterface;
import com.example.service.DownloadService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Method {

    public Activity activity;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    private final String myPreference = "login";
    public String IS_WELCOME = "is_welcome";
    String filename;
    public static boolean isDownload = true,personalizationAd = false;
    public String themSetting = "them";
    public String notification = "notification";
    public static final String LASTVIEW = "WebList_LastView";
    public static final String PREFS_NAME = "BOOK_LASTVIEW";
    private OnClicks onClicks;
    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity) {
        this.activity = activity;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, OnClick onClick) {
        this.activity = activity;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }



    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, OnClicks onClicks) {
        this.activity = activity;
        this.onClicks = onClicks;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    // This four methods are used for maintaining lastread.
    public static void saveFavorites(Context context, List<SubCatListBook> favorites) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(LASTVIEW, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, String bookId, String page_num) {
        Boolean cek = true;
        List<SubCatListBook> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<>();
        SubCatListBook category = new SubCatListBook();
        category.setPost_id(bookId);
        category.setPage_num(page_num);
        Log.w("adslog", "addFavorite: " + bookId);
        if (favorites != null) {
            for (SubCatListBook s : favorites) {
                if (s.getPost_id().equals(bookId)) {
                    Log.w("adslog", "addFavorite: favorites.indexOf(s); " + favorites.indexOf(s));
                    int i = favorites.indexOf(s);
                    favorites.get(i).setPage_num(page_num);
                    cek = false;
                }
            }
        }
        if (cek) {
            Log.w("adslog", "addFavorite: new ");
            favorites.add(category);
        }
        saveFavorites(context, favorites);
    }

    public static void removeFavorite(Context context, SubCatListBook product) {
        ArrayList<SubCatListBook> favorites = getFavorites(context);
        if (favorites != null) {
                for(int i = 0 ; i < favorites.size() ; i++){
                    Log.v("adslog", "removeFavorite: product.getPost_title() "+product.getPost_id());
                    Log.v("adslog", "removeFavorite: favorites.get(i).getPost_title() "+favorites.get(i).getPost_id());

                    if(product.getPost_id().equalsIgnoreCase(favorites.get(i).getPost_id())){
                        Log.e("adslog", "removeFavorite: "+favorites.get(i).getPost_id());
                        Log.i("adslog", "removeFavorite: pos "+i);
                        favorites.remove(i);
                    }
                }
            saveFavorites(context, favorites);
        }

//        if (favorites != null) {
//            favorites.remove(product);
//            saveFavorites(context, favorites);
//        }

    }



    public static ArrayList<SubCatListBook> getFavorites(Context context) {
        SharedPreferences settings;
        List<SubCatListBook> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(LASTVIEW)) {
            String jsonFavorites = settings.getString(LASTVIEW, null);
            Gson gson = new Gson();
            SubCatListBook[] favoriteItems = gson.fromJson(jsonFavorites,
                    SubCatListBook[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<>(favorites);
        } else
            return null;

        return (ArrayList<SubCatListBook>) favorites;
    }





    public boolean isWelcome() {
        return pref.getBoolean(IS_WELCOME, true);
    }

    public void setFirstWelcome(boolean isFirstTime) {
        editor.putBoolean(IS_WELCOME, isFirstTime);
        editor.commit();
    }

    //network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // view format
    public static String Format(Integer number) {
        String[] suffix = new String[]{"k", "m", "b", "t"};
        int size = (number != 0) ? (int) Math.log10(number) : 0;
        if (size >= 3) {
            while (size % 3 != 0) {
                size = size - 1;
            }
        }
        double notation = Math.pow(10, size);
        String result = (size >= 3) ? +(Math.round((number / notation) * 100) / 100.0d) + suffix[(size / 3) - 1] : +number + "";
        return result;
    }

    //alert message box
    public void alertBox(String message) {
        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }
    public void saveType(String type) {
        pref = activity.getSharedPreferences(myPreference, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("type", type);
        editor.commit();
    }
    public void setUserId(String userId) {
        pref = activity.getSharedPreferences(myPreference, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user_id", userId);
        editor.apply();
    }

    public String getUserType() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getString("type", "");
    }


    public boolean getFirstIsLogin() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getBoolean("IsLoggedInFirst", false);
    }

    public void saveFirstIsLogin(boolean flag) {
        pref = activity.getSharedPreferences(myPreference, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("IsLoggedInFirst", flag);
        editor.apply();
    }


    public void saveIsLogin(boolean flag) {
        pref = activity.getSharedPreferences(myPreference, 0);
        editor = pref.edit();
        editor.putBoolean("IsLoggedIn", flag);
        editor.apply();
    }

    public boolean getIsLogin() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getBoolean("IsLoggedIn", false);
    }

    public void saveLogin(String userId, String userName, String userEmail, String userType, String userAId) {
        pref = activity.getSharedPreferences(myPreference, 0);
        editor = pref.edit();
        editor.putString("user_id", userId);
        editor.putString("user_name", userName);
        editor.putString("email", userEmail);
        editor.putString("type", userType);
        editor.putString("aid", userAId);
        editor.apply();
    }

    public void savePhone(String userPhone) {
        pref = activity.getSharedPreferences(myPreference, 0);
        editor = pref.edit();
        editor.putString("user_phone", userPhone);
        editor.apply();
    }

    public String getUserPhone() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getString("user_phone", "");
    }

    public String getUserId() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getString("user_id", "");
    }

    public String getUserName() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getString("user_name", "");
    }

    public String getUserEmail() {
        pref = activity.getSharedPreferences(myPreference, 0);
        return pref.getString("email", "");
    }

    //rtl
    public void forceRTLIfSupported() {
        if (activity.getResources().getString(R.string.isRTL).equals("true")) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public String webViewText() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewTextDark;
        } else {
            color = Constant.webViewText;
        }
        return color;
    }

    public String webViewTextAuthor() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewTextDarkAuthor;
        } else {
            color = Constant.webViewTextAuthor;
        }
        return color;
    }

    public String webViewAboutText() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewTextAboutDark;
        } else {
            color = Constant.webViewTextAbout;
        }
        return color;
    }


    public String webViewLink() {
        String color;
        if (isDarkMode()) {
            color = Constant.webViewLinkDark;
        } else {
            color = Constant.webViewLink;
        }
        return color;
    }

    public String isWebViewTextRtl() {
        String isRtl;
        if (isRtl()) {
            isRtl = "rtl";
        } else {
            isRtl = "ltr";
        }
        return isRtl;
    }

    //rtl
    public boolean isRtl() {
        return activity.getResources().getString(R.string.isRTL).equals("true");
    }

    public String themMode() {
        return pref.getString(themSetting, "system");
    }

    //check dark mode or not
    public boolean isDarkMode() {
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                return false;
        }
    }
    /*<---------------------- download book ---------------------->*/

    public void download(String id, String bookName, String bookImage, String bookAuthor, String bookUrl, String type) {

        Log.e("doimage",""+bookImage);
        //Book file save folder name
        File rootBook = new File(bookStorage());
        if (!rootBook.exists()) {
            rootBook.mkdirs();
        }

        if (type.equals("epub")) {
            filename = "filename-" + id + ".epub";
        } else {
            filename = "filename-" + id + ".pdf";
        }

        File file = new File(bookStorage(), filename);
        if (!file.exists()) {

            Method.isDownload = false;

            Intent serviceIntent = new Intent(activity, DownloadService.class);
            serviceIntent.setAction(DownloadService.ACTION_START);
            serviceIntent.putExtra("id", id);
            serviceIntent.putExtra("downloadUrl", bookUrl);
            serviceIntent.putExtra("file_path", rootBook.toString());
            serviceIntent.putExtra("file_name", filename);

            activity.startService(serviceIntent);
            bookDownloadData(id);

        } else {
            alertBox(activity.getResources().getString(R.string.you_have_allReady_download_book));
        }

        new DownloadImage().execute(bookImage, id, bookName, bookAuthor);

    }

    private void bookDownloadData(String bookId) {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("post_id", bookId);
        jsObj.addProperty("user_id", getUserId());
        jsObj.addProperty("post_type", "Book");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonObject> call = apiService.getPostDownloadData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadImage extends AsyncTask<String, String, String> {

        private String filePath;
        private String iconsStoragePath;
        private String id, bookName, bookAuthor;
        private final DatabaseHandler db = new DatabaseHandler(activity);

        @Override
        protected String doInBackground(String... params) {
            try {

                URL url = new URL(params[0]);
                id = params[1];
                bookName = params[2];
                bookAuthor = params[3];

                iconsStoragePath = bookStorage();
                File sdIconStorageDir = new File(iconsStoragePath);
                //create storage directories, if they don't exist
                if (!sdIconStorageDir.exists()) {
                    sdIconStorageDir.mkdirs();
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                String currentTimeStamp = dateFormat.format(new Date());

                String fname = "Image-" + currentTimeStamp;
                filePath = iconsStoragePath + "/" + fname + ".jpg";

                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                        //choose another format if PNG doesn't suit you
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        Log.w("TAG", "Error saving image file: " + e.getMessage());
                    }

            } catch (IOException e) {
                // Log exception
                Log.d("error_info", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (db.checkIdDownloadBook(id)) {
                db.addDownload(new DownloadList(id, bookName, filePath, bookAuthor, iconsStoragePath + "/" + filename));
            }
            super.onPostExecute(s);
        }
     }
     /*<---------------------- download book ---------------------->*/
     //book storage folder path
     public String bookStorage() {
         return activity.getFilesDir().getAbsolutePath() + File.separator + ".AndroidEBook";
     }

    //get screen width
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point point = new Point();
        point.x = display.getWidth();
        point.y = display.getHeight();
        columnWidth = point.x;
        return columnWidth;
    }

    public void addToFav(String id, String userId, String type,FavouriteIF favouriteIF) {

        ProgressDialog progressDialog = new ProgressDialog(activity,R.style.MyAlertDialogStyle);

        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("post_id", id);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("post_type", type);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<FavoriteRP> call = apiService.getFavUnFavBookData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<FavoriteRP>() {
            @Override
            public void onResponse(@NotNull Call<FavoriteRP> call, @NotNull Response<FavoriteRP> response) {

                try {
                    FavoriteRP favoriteRP = response.body();

                    if (favoriteRP !=null && favoriteRP.getSuccess().equals("1")) {
                        if (favoriteRP.getItemFavoriteList().get(0).getSuccessFav().equals("true")) {
                            favouriteIF.isFavourite(favoriteRP.getItemFavoriteList().get(0).getSuccessFav(), favoriteRP.getItemFavoriteList().get(0).getMsgFav());
                        } else {
                            favouriteIF.isFavourite("", favoriteRP.getItemFavoriteList().get(0).getMsgFav());
                        }
                        Events.FavBook favBook = new Events.FavBook();
                        favBook.setBookId(id);
                        favBook.setIsFav(favoriteRP.getItemFavoriteList().get(0).getSuccessFav());
                        GlobalBus.getBus().post(favBook);
                        Toast.makeText(activity, favoriteRP.getItemFavoriteList().get(0).getMsgFav(), Toast.LENGTH_SHORT).show();
                    } else {
                        alertBox(activity.getResources().getString(R.string.failed_try_again));
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<FavoriteRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }


    //---------------Interstitial Ad---------------//

    public void onClickAd(final int position, final String type, final String id, final String subId,
                          final String title, final String fileType, final String fileUrl, String otherData) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        if (Constant.adsInfo != null) {

            if (Constant.isInterstitial) {

                Constant.AD_COUNT = Constant.AD_COUNT + 1;
                if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
                    Constant.AD_COUNT = 0;

                    switch (Constant.adNetworkType) {
                        case "admob":

                            AdRequest adRequest;
                            if (personalizationAd) {
                                adRequest = new AdRequest.Builder()
                                        .build();
                            } else {
                                Bundle extras = new Bundle();
                                extras.putString("npa", "1");
                                adRequest = new AdRequest.Builder()
                                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                        .build();
                            }

                            InterstitialAd.load(activity, Constant.adsInfo.getInterstitialId(), adRequest, new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    // The mInterstitialAd reference will be null until
                                    // an ad is loaded.
                                    Log.i("admob_error", "onAdLoaded");
                                    progressDialog.dismiss();
                                    interstitialAd.show(activity);
                                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when fullscreen content is dismissed.
                                            Log.e("TAG", "The ad was dismissed.");
                                            onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                            // Called when fullscreen content failed to show.
                                            Log.e("TAG", "The ad failed to show.");
                                            onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when fullscreen content is shown.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            Log.e("TAG", "The ad was shown.");
                                        }
                                    });
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    // Handle the error
                                    Log.i("admob_error", loadAdError.getMessage());
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                            });

                            break;
                        case "facebook":

                            com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(activity, Constant.adsInfo.getInterstitialId());
                            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                                @Override
                                public void onInterstitialDisplayed(Ad ad) {
                                    // Interstitial ad displayed callback
                                    Log.e("fb_ad", "Interstitial ad displayed.");
                                }

                                @Override
                                public void onInterstitialDismissed(Ad ad) {
                                    // Interstitial dismissed callback
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    Log.e("fb_ad", "Interstitial ad dismissed.");
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {
                                    // Ad error callback
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                    Log.e("fb_ad", "Interstitial ad failed to load: " + adError.getErrorMessage());
                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    // Interstitial ad is loaded and ready to be displayed
                                    Log.d("fb_ad", "Interstitial ad is loaded and ready to be displayed!");
                                    progressDialog.dismiss();
                                    // Show the ad
                                    interstitialAd.show();
                                }

                                @Override
                                public void onAdClicked(Ad ad) {
                                    // Ad clicked callback
                                    Log.d("fb_ad", "Interstitial ad clicked!");
                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {
                                    // Ad impression logged callback
                                    Log.d("fb_ad", "Interstitial ad impression logged!");
                                }
                            };

                            // For auto play video ads, it's recommended to load the ad
                            // at least 30 seconds before it is shown
                            com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = interstitialAd.buildLoadAdConfig().
                                    withAdListener(interstitialAdListener).withCacheFlags(CacheFlag.ALL).build();
                            interstitialAd.loadAd(loadAdConfig);

                            break;
                        case "unityds":
                            UnityAds.show((Activity) activity,Constant.adsInfo.getInterstitialId(), new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }

                                @Override
                                public void onUnityAdsShowStart(String s) {
                                }

                                @Override
                                public void onUnityAdsShowClick(String s) {
                                }

                                @Override
                                public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                            });
                            break;
                        case "startapp":
                            StartAppAd startAppAd = new StartAppAd(activity);
                            startAppAd.loadAd(new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                    progressDialog.dismiss();
                                    startAppAd.showAd(new AdDisplayListener() {
                                        @Override
                                        public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                            onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                        }

                                        @Override
                                        public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {

                                        }

                                        @Override
                                        public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                            onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                        }
                                    });
                                }

                                @Override
                                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                            });
                            break;
                        case "applovins":
                            MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.adsInfo.getInterstitialId(), (Activity) activity);
                            maxInterstitialAd.setListener(new MaxAdListener() {
                                @Override
                                public void onAdLoaded(MaxAd ad) {
                                    progressDialog.dismiss();
                                    maxInterstitialAd.showAd();
                                }

                                @Override
                                public void onAdDisplayed(MaxAd ad) {
                                }

                                @Override
                                public void onAdHidden(MaxAd ad) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }

                                @Override
                                public void onAdClicked(MaxAd ad) {
                                }

                                @Override
                                public void onAdLoadFailed(String adUnitId, MaxError error) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }

                                @Override
                                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                            });
                            // Load the first ad
                            maxInterstitialAd.loadAd();
                            break;
                        case "wortise":
                            com.wortise.ads.interstitial.InterstitialAd wInterstitial = new com.wortise.ads.interstitial.InterstitialAd(activity, Constant.adsInfo.getInterstitialId());
                            wInterstitial.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                                @Override
                                public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                                }
                                @Override
                                public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                                @Override
                                public void onInterstitialFailed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                    progressDialog.dismiss();
                                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                                }
                                @Override
                                public void onInterstitialLoaded(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    if (wInterstitial.isAvailable()) {
                                        wInterstitial.showAd();
                                    }
                                }
                                @Override
                                public void onInterstitialShown(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    progressDialog.dismiss();
                                }
                            });
                            wInterstitial.loadAd();
                            break;
                    }

                } else {
                    progressDialog.dismiss();
                    onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
                }

            } else {
                progressDialog.dismiss();
                onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
            }
        } else {
            progressDialog.dismiss();
            try {
                onClicks.position(position, type, id, subId, title, fileType, fileUrl, otherData);
            }catch (Exception e){
                Log.i("adslogd", "onClickAd: exceptione "+e.getMessage());
            }
        }
    }

    //---------------Interstitial Ad---------------//


}
