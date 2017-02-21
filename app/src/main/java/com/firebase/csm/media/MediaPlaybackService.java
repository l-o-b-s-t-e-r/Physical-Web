package com.firebase.csm.media;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.firebase.csm.events.PreparedEvent;
import com.firebase.csm.ui.MainActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

/**
 * Created by Lobster on 09.02.17.
 */

public class MediaPlaybackService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener {

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaPlayer mPlayer;
    private WifiManager.WifiLock mWifiLock;
    private Bundle info;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setOnCompletionListener(this);
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "wifi_lock");

        mMediaSession = new MediaSessionCompat(this, "media_service");
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(mediaCallback);

        setSessionToken(mMediaSession.getSessionToken());
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (info != null && rootHints != null &&
                !info.getString(MainActivity.EXHIBIT).equals(rootHints.getString(MainActivity.EXHIBIT))) {
            mPlayer.reset();
            mWifiLock.release();
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f).build());
        }

        info = rootHints;
        return new BrowserRoot("csm_root", rootHints);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    private final MediaSessionCompat.Callback mediaCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            mWifiLock.acquire();
            mPlayer.setOnPreparedListener(mp -> EventBus.getDefault().post(new PreparedEvent()));
            try {
                mPlayer.setDataSource(MediaPlaybackService.this, uri);
                mPlayer.prepareAsync();
                mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f).build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            mWifiLock.acquire();
            try {
                mPlayer.setDataSource(MediaPlaybackService.this, uri);
                mPlayer.setOnPreparedListener(mp -> {
                    EventBus.getDefault().post(new PreparedEvent());
                    startService(new Intent(getApplicationContext(), MediaPlaybackService.class));
                    mPlayer.start();
                    mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build());
                });
                mPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlay() {
            startService(new Intent(getApplicationContext(), MediaPlaybackService.class));
            mPlayer.start();
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build());
        }

        @Override
        public void onPause() {
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f).build());
            mPlayer.pause();
        }

        @Override
        public void onStop() {
            mPlayer.stop();
            mWifiLock.release();
            stopSelf();
        }


    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayer.reset();
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f).build());
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
    }
}