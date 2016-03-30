package com.ashwinkachhara.circularseekbartest;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WearableListView.ClickListener{

    private WearableListView listView;
    public AudioManager audioManager;
    public CircularSeekBar seekBar;
    public CircularButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
//                listView = (WearableListView) stub.findViewById(R.id.sample_list_view);
//                loadAdapter();
                audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                seekBar = (CircularSeekBar) findViewById(R.id.circularSeekBar1);
                int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                Log.d("VOLUME", vol + " " + maxVol + " " + (seekBar != null));
                seekBar.setMax(maxVol);
                seekBar.setProgress(vol);

                playPauseButton = (CircularButton) findViewById(R.id.playPauseButton);
                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("PLAYPAUSE", "Hello");
                        MediaPlayer mp = new MediaPlayer();
                        mp = MediaPlayer.create(getApplicationContext(),R.raw.thrill);
                        if (mp.isPlaying()){
                            mp.stop();
                        } else {
                            mp.start();
                            if (mp.isPlaying())
                                Log.d("PLAYPAUSE", "It's playing");
                        }
                    }
                });

            }
        });

    }

    private void loadAdapter() {
        String[] items = new String[250];
        for (int i=0; i<250;i++) {
            items[i] = "Item "+i;
        }
        //items.add(new SettingsItems(R.drawable.ic_color, getString(R.string.theme)));

        listView.setAdapter(new com.ashwinkachhara.circularseekbartest.Adapter(this,items));

        listView.setClickListener(this);
    }
    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
    }

    @Override
    public void onTopEmptyRegionClick() {
    }
}
