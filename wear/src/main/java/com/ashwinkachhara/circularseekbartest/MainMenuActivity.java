package com.ashwinkachhara.circularseekbartest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainMenuActivity extends WearableActivity implements WearableListView.ClickListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private WearableListView mainMenuList;

    private GoogleApiClient mApiClient;

    private ArrayList<String> songTitles;

    private static final String WEARACTIVITY_KEY = "com.ashwinkachhara.key.wearactivity";
    private static final String SONG_KEY = "com.ashwinkachhara.key.song";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setAmbientEnabled();

        mainMenuList = (WearableListView) findViewById(R.id.mainMenuList);
        mainMenuList.setAdapter(new MainMenuListAdapter(this));

        mainMenuList.setClickListener(this);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {

        } else {

        }
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
        switch(tag){
            case 0:
                Intent npint = new Intent(MainMenuActivity.this, NowPlayingActivity.class);
                npint.putExtra("SONGLIST",songTitles);
                npint.putExtra("SONGNAME",1);
                npint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                npint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                MainMenuActivity.this.startActivity(npint);
                break;
            case 1:
                Intent mint = new Intent(MainMenuActivity.this, MainActivity.class);
                mint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                MainMenuActivity.this.startActivity(mint);
                break;
            case 2:
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
        sendIntToPhone("/WearActivity",WEARACTIVITY_KEY,2);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/SongList") == 0) {
                    Log.d("DATACHNG", "Some Songs");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    songTitles = dataMap.getStringArrayList(SONG_KEY);
                    songTitles.remove(songTitles.size() - 1);
//                    if (!FIRST_DONE) {
//                        FIRST_DONE = true;
//                        Intent npint = new Intent(MainActivity.this, NowPlayingActivity.class);
//                        npint.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                        MainActivity.this.startActivity(npint);
//                    }
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void sendIntToPhone(String path, String key, Integer data){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putInt(key, data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }
}
