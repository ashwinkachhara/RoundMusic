package com.ashwinkachhara.circularseekbartest;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import com.ashwinkachhara.circularseekbartest.MusicService.MusicBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.os.Handler;

import android.widget.MediaController.MediaPlayerControl;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ArrayList<Song> songList;
    private ArrayList<String> songTitles;
    private ArrayList<String> songArtists;
    private ArrayList<String> songAlbums;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    private MusicController controller;
    private AudioManager audioManager;
    int maxVol;

    private boolean paused=false, playbackPaused=false;

    GoogleApiClient mApiClient;

    private static final String SONG_KEY = "com.ashwinkachhara.key.song";
    private static final String ARTISTS_KEY = "com.ashwinkachhara.key.artists";
    private static final String ALBUMS_KEY = "com.ashwinkachhara.key.albums";
    private static final String INITVOLUME_KEY = "com.ashwinkachhara.key.initvolume";
    private static final String VOLUME_KEY = "com.ashwinkachhara.key.volume";
    private static final String WEARACTIVITY_KEY = "com.ashwinkachhara.key.wearactivity";
    private static final String WEARSONGPICK_KEY = "com.ashwinkachhara.key.wearsongpick";
    private static final String SONGPROGRESS_KEY = "com.ashwinkachhara.key.songprogress";
    private static final String CURRENTPLAYINGSONG_KEY = "com.ashwinkachhara.key.currentplayingsong";

    Handler songProgressUpdHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        Button play = (Button) findViewById(R.id.playButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
//        play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                audioPlayer("mnt/sdcard/Music", "01 - Step Out.mp3");
//            }
//        });

        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getAlbum().compareTo(b.getAlbum());
            }
        });

        for (int i=0; i<songList.size(); i++){
            Log.i("ALBUMDBG",songList.get(i).getTitle() + "; " + songList.get(i).getAlbum());
        }

        songTitles = new ArrayList<String>();
        songArtists = new ArrayList<String>();
        songAlbums = new ArrayList<String>();
        getSongTitles();

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        setController();

//        mApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(Bundle bundle) {
//                        Log.d("GOOGAPI", "onConnected: " + bundle);
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//                        Log.d("GOOGAPI", "onConnectionSuspended: " + i);
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult connectionResult) {
//                        Log.d("GOOGAPI", "onConnectionFailed " + connectionResult);
//                    }
//                })
//                .addApi(Wearable.API)
//                .build();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();

        songProgressUpdHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    sendSongProgressToWear();
                    if (musicSrv.JUST_COMPLETED){
                        sendCurrentPlayingSongToWear();
                        musicSrv.JUST_COMPLETED = false;
                    }
                }
                songProgressUpdHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
        sendSongListToWear();
        sendSongArtistsToWear();
        sendSongAlbumsToWear();
        sendVolumeToWear(getCurrentPhoneVolume());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public int getCurrentPhoneVolume(){
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("VOLUME", vol + " " + maxVol + " ");
        return vol*100/maxVol;
    }

    public void setCurrentVolume(int vol){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol * maxVol / 100, 0);
    }

    public void sendVolumeToWear(int volume){
//        Wearable.DataApi.deleteDataItems((mApiClient),VOLUME_KEY);
        ArrayList<String> vol = new ArrayList<>();
        vol.add(Integer.toString(volume));
        vol.add(Long.toString(System.currentTimeMillis()));

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/InitMusicVolume");
        putDataMapReq.getDataMap().putStringArrayList(INITVOLUME_KEY, vol);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    public void sendCurrentPlayingSongToWear(){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/CurrentPlayingSong");
        putDataMapReq.getDataMap().putInt(CURRENTPLAYINGSONG_KEY, musicSrv.songPosn);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    public void sendSongProgressToWear(){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/SongProgress");
        putDataMapReq.getDataMap().putInt(SONGPROGRESS_KEY, getCurrentPosition()*100/getDuration());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    public void sendSongListToWear() {
        Log.d("SENDSONGS", "Sending Songs!");
        songTitles.remove(songTitles.size() - 1);
        songTitles.add(Long.toString(System.currentTimeMillis()));
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/SongList");
        putDataMapReq.getDataMap().putStringArrayList(SONG_KEY, songTitles);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
        Log.d("SENDSONGS", pendingResult.toString());

    }

    public void sendSongArtistsToWear() {
//        Log.d("SENDARTISTS", "Sending Songs!");
        songArtists.remove(songArtists.size() - 1);
        songArtists.add(Long.toString(System.currentTimeMillis()));
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/ArtistsList");
        putDataMapReq.getDataMap().putStringArrayList(ARTISTS_KEY,songArtists);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
//        Log.d("SENDSONGS", pendingResult.toString());
    }

    public void sendSongAlbumsToWear() {
        songAlbums.remove(songAlbums.size()-1);
        songAlbums.add(Long.toString(System.currentTimeMillis()));
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/AlbumsList");
        putDataMapReq.getDataMap().putStringArrayList(ALBUMS_KEY,songAlbums);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mApiClient,putDataReq);
    }

    public void getSongTitles(){
        for (int i=0; i<songList.size();i++) {
            songTitles.add(songList.get(i).getTitle());
            songArtists.add(songList.get(i).getArtist());
            songAlbums.add(songList.get(i).getAlbum());
        }
        songTitles.add(Long.toString(System.currentTimeMillis()));
        songArtists.add(Long.toString(System.currentTimeMillis()));
        songAlbums.add(Long.toString(System.currentTimeMillis()));
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("SERVICECONN", "service connected!");
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            Log.d("SERVICECONN", Boolean.toString(musicSrv == null));
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

//    public void audioPlayer(String path, String filename){
//        MediaPlayer mp = new MediaPlayer();
//
//        try {
//            mp.setDataSource(path+"/"+filename);
//        } catch (IllegalArgumentException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        try {
//            mp.prepare();
//        } catch (IllegalStateException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        mp.start();
//
//    }

    public void songPicked(View view){
//        Log.d("SONG",Integer.toString(Integer.parseInt(view.getTag().toString())));
        Integer songid = Integer.parseInt(view.getTag().toString());
        Log.d("SONG", Integer.toString(songid));
        Log.d("SONG", Boolean.toString(musicSrv == null));
        musicSrv.setSong(songid);
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    public void songPickedFromWear(int tag){
        musicSrv.setSong(tag);
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    protected void onStart() {
        Log.d("ONSTART", "I started!");
        super.onStart();
        if(playIntent==null){
            Log.d("ONSTART", "Inside");
            playIntent = new Intent(this, MusicService.class);
            Boolean isBound = getApplicationContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.d("ONSTART", Boolean.toString(isBound));
        }
    }

    public void getSongList() {
        songList.clear();
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }



    }

    private void setController(){
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/PlayToggle") == 0) {
                    if (isPlaying())
                        pause();
                    else
                        start();
                } else if (item.getUri().getPath().compareTo("/NextSong") == 0){
                    playNext();
                } else if (item.getUri().getPath().compareTo("/PrevSong") == 0){
                    playPrev();
                } else if (item.getUri().getPath().compareTo("/MusicVolume") == 0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    setCurrentVolume(dataMap.getInt(VOLUME_KEY));
                } else if (item.getUri().getPath().compareTo("/WearActivity") == 0){
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int activity = dataMap.getInt(WEARACTIVITY_KEY);
                    switch(activity){
                        case 0:
                            sendSongListToWear();
                            sendSongArtistsToWear();
                            sendSongAlbumsToWear();
                            break;
                        case 1:
                            sendVolumeToWear(getCurrentPhoneVolume());
                            if (!isPlaying())
                                songPickedFromWear(musicSrv.songPosn);
                            break;
                        case 2:
                            sendSongListToWear();
                            sendSongArtistsToWear();
                            sendSongAlbumsToWear();
                            break;
                    }
                } else if (item.getUri().getPath().compareTo("/PickSongFromWear") == 0){
                    Log.d("SONGFROMWEAR", "Got it");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int songid = dataMap.getInt(WEARSONGPICK_KEY);
                    if (songid>=0)
                        songPickedFromWear(songid);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}
