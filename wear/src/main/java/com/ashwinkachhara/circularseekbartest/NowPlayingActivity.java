package com.ashwinkachhara.circularseekbartest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devadvance.circularseekbar.CircularSeekBar;
import com.github.lzyzsd.circleprogress.DonutProgress;
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
import java.util.Date;
import java.util.Locale;

public class NowPlayingActivity extends WearableActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;

    ImageView playPauseB, prevB, nextB, searchB;
    CircularSeekBar volumeSeekBar;
    DonutProgress songProgressBar;
    TextView songNameText;

    private GoogleApiClient mApiClient;

    private static final String PLAY_TOGGLE_KEY = "com.ashwinkachhara.key.playtoggle";
    private static final String NEXTSONG_KEY = "com.ashwinkachhara.key.nextsong";
    private static final String PREVSONG_KEY = "com.ashwinkachhara.key.prevsong";
    private static final String VOLUME_KEY = "com.ashwinkachhara.key.volume";
    private static final String INITVOLUME_KEY = "com.ashwinkachhara.key.initvolume";
    private static final String WEARACTIVITY_KEY = "com.ashwinkachhara.key.wearactivity";
    private static final String SONGPROGRESS_KEY = "com.ashwinkachhara.key.songprogress";

    Boolean playToggleState = false;
    Boolean nextSongState = false;
    Boolean prevSongState = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        playPauseB = (ImageView) findViewById(R.id.playPauseButton);
        prevB = (ImageView) findViewById(R.id.prevButton);
        nextB = (ImageView) findViewById(R.id.nextButton);
        searchB = (ImageView) findViewById(R.id.searchButton);
        volumeSeekBar = (CircularSeekBar) findViewById(R.id.nowPlayingVolumeSeek);
        songProgressBar = (DonutProgress) findViewById(R.id.nowPlayingSongProgressBar);
        songNameText = (TextView) findViewById(R.id.nowPlayingSongName);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        playPauseB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBoolToPhone("/PlayToggle", PLAY_TOGGLE_KEY, playToggleState);
                playToggleState = !playToggleState;
            }
        });

        prevB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBoolToPhone("/PrevSong",PREVSONG_KEY,prevSongState);
                prevSongState = ! prevSongState;
            }
        });

        nextB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBoolToPhone("/NextSong",NEXTSONG_KEY,nextSongState);
                nextSongState = ! nextSongState;
            }
        });

        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NowPlayingActivity.this.startActivity(new Intent(NowPlayingActivity.this,MainActivity.class));

                Intent listint = new Intent(NowPlayingActivity.this, MainActivity.class);
                listint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                listint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                NowPlayingActivity.this.startActivity(listint);
            }
        });

        volumeSeekBar.setMax(100);

        volumeSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                sendIntToPhone("/MusicVolume", VOLUME_KEY, progress);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });

        songNameText.setText(getIntent().getExtras().getString("SONGNAME"));
    }

    private void sendIntToPhone(String path, String key, Integer data){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putInt(key, data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    private void sendBoolToPhone(String path, String key, Boolean data){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putBoolean(key, data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
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
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mTextView.setTextColor(getResources().getColor(android.R.color.white));
//            mClockView.setVisibility(View.VISIBLE);

//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
//            mContainerView.setBackground(null);
//            mTextView.setTextColor(getResources().getColor(android.R.color.black));
//            mClockView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
        sendIntToPhone("/WearActivity",WEARACTIVITY_KEY,1);
        // 0 = MainActivity, 1 = NowPlayingActivity
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        Log.d("NPDATACHNG", "Data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/InitMusicVolume") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int vol = Integer.parseInt(dataMap.getStringArrayList(INITVOLUME_KEY).get(0));
                    volumeSeekBar.setProgress(vol);

                }
                else if (item.getUri().getPath().compareTo("/SongProgress") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int prog = dataMap.getInt(SONGPROGRESS_KEY);
                    songProgressBar.setProgress(prog);

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
