package com.ashwinkachhara.circularseekbartest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.TextView;

import com.devadvance.circularseekbar.CircularSeekBar;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private WearableListView listView;
    public AudioManager audioManager;
    public CircularSeekBar seekBar;
    public CircularButton playPauseButton;

    private static final String SONG_KEY = "com.ashwinkachhara.key.song";
    private static final String WEARACTIVITY_KEY = "com.ashwinkachhara.key.wearactivity";
    private static final String WEARSONGPICK_KEY = "com.ashwinkachhara.key.wearsongpick";

    private GoogleApiClient mApiClient;

    private ArrayList<String> songTitles;

    private boolean GOT_SONGS = false;
    private boolean FIRST_DONE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                listView = (WearableListView) stub.findViewById(R.id.sample_list_view);
                loadAdapter();
//                audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//                seekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar1);
//                int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                Log.d("VOLUME", vol + " " + maxVol + " " + (seekBar != null));
//                seekBar.setMax(maxVol);
//                seekBar.setProgress(vol);
//
//                playPauseButton = (CircularButton) findViewById(R.id.playPauseButton);
//                playPauseButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.d("PLAYPAUSE", "Hello");
//                        MediaPlayer mp = new MediaPlayer();
//                        mp = MediaPlayer.create(getApplicationContext(),R.raw.thrill);
//                        if (mp.isPlaying()){
//                            mp.stop();
//                        } else {
//                            mp.start();
//                            if (mp.isPlaying())
//                                Log.d("PLAYPAUSE", "It's playing");
//                        }
//                    }
//                });

            }
        });

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();


    }

    private void sendIntToPhone(String path, String key, Integer data){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putInt(key, data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
        Log.d("APICONN", "I am connected!");
        sendIntToPhone("/WearActivity",WEARACTIVITY_KEY,0);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
        sendIntToPhone("/PickSongFromWear", WEARSONGPICK_KEY, -100);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        Log.d("DATACHNG", "Data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/SongList") == 0) {
                    Log.d("DATACHNG", "Some Songs");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    songTitles = dataMap.getStringArrayList(SONG_KEY);
                    songTitles.remove(songTitles.size()-1);
                    GOT_SONGS = true;
                    loadAdapter();
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

    private void loadAdapter() {
        ArrayList<String> items = new ArrayList<String>();
        for (int i=0; i<250;i++) {
            items.add("Item "+i);
        }
        //items.add(new SettingsItems(R.drawable.ic_color, getString(R.string.theme)));
        if (GOT_SONGS)
            listView.setAdapter(new com.ashwinkachhara.circularseekbartest.Adapter(this,songTitles));
//        else
//            listView.setAdapter(new com.ashwinkachhara.circularseekbartest.Adapter(this, items));

        listView.setClickListener(this);
    }
    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
        Log.d("SONGPICKED", "Picked " + tag);
        sendIntToPhone("/PickSongFromWear", WEARSONGPICK_KEY, tag);

        Intent npint = new Intent(MainActivity.this, NowPlayingActivity.class);
        npint.putExtra("SONGNAME",songTitles.get(tag));
        npint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        npint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        MainActivity.this.startActivity(npint);

    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
