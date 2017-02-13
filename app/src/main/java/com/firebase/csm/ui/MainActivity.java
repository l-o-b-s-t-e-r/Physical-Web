package com.firebase.csm.ui;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import com.firebase.csm.App;
import com.firebase.csm.BackgroundSubscribeIntentService;
import com.firebase.csm.R;
import com.firebase.csm.adapters.CommentsAdapter;
import com.firebase.csm.custom.AnimationHelper;
import com.firebase.csm.databinding.ActivityMainBinding;
import com.firebase.csm.events.PreparedEvent;
import com.firebase.csm.firebase.CommentService;
import com.firebase.csm.firebase.IArticleService;
import com.firebase.csm.firebase.ICommentService;
import com.firebase.csm.media.MediaPlaybackService;
import com.firebase.csm.models.Article;
import com.firebase.csm.models.Comment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements MainPerformance.View {

    private final GoogleApiClient.OnConnectionFailedListener googleConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e("GoogleApi", "ConnectionFailed " + String.valueOf(connectionResult.getErrorCode()));
        }
    };
    @Inject
    public IArticleService articleReference;
    @Inject
    public ICommentService commentReference;
    private ActivityMainBinding mBinding;
    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_STOPPED:
                    mBinding.fabPlay.setImageResource(R.drawable.ic_replay_white_48dp);
                    break;
            }
        }
    };
    private final TextWatcher commentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBinding.addCommentBtn.setAlpha(0.1f + 2 * s.toString().trim().length() / 100.0f);
            mBinding.addCommentBtn.setVisibility(s.toString().trim().length() > 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private MainPresenter mMainPresenter;
    private CommentsAdapter mCommentsAdapter;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        mMediaController = new MediaControllerCompat(MainActivity.this, mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }

                    MediaControllerCompat.setMediaController(MainActivity.this, mMediaController);

                    if (mBinding.getData() != null)
                        prepareIfNeeded(mBinding.getData().getAudioUri(), mMediaController.getPlaybackState());

                    mMediaController.registerCallback(mediaControllerCallback);
                }

                @Override
                public void onConnectionSuspended() {

                }

                @Override
                public void onConnectionFailed() {

                }
            };
    private GoogleApiClient mGoogleApiClient;
    private final GoogleApiClient.ConnectionCallbacks googleConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            subscribe();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e("GoogleApi", "ConnectionSuspended " + String.valueOf(i));
        }
    };
    private AnimationHelper mAnimationHelper;
    private final Animation.AnimationListener playAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            switch (mMediaController.getPlaybackState().getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_STOPPED:
                    mBinding.fabPlay.setImageResource(R.drawable.ic_pause_white_48dp);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mBinding.fabPlay.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    break;
            }

            mAnimationHelper.onClickEndAnimation(mBinding.fabPlay);
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    };
    private NestedScrollView.OnScrollChangeListener scrollChangeListener = new NestedScrollView.OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            mBinding.image.setAlpha(1.0f - scrollY / 1000.0f);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().appComponent().inject(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        initialization();
    }

    private void initialization() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                        .setPermissions(NearbyPermissions.BLE)
                        .build())
                .addConnectionCallbacks(googleConnectionCallbacks)
                .enableAutoManage(this, googleConnectionFailedListener)
                .build();

        /* Presenter */
        mMainPresenter = new MainPresenter(this, articleReference, commentReference);

        /* RecyclerView */
        mCommentsAdapter = new CommentsAdapter();
        mBinding.comments.setAdapter(mCommentsAdapter);
        mBinding.comments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        /* Media */
        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaPlaybackService.class),
                connectionCallbacks,
                null
        );

        /* Listeners */
        mBinding.comment.addTextChangedListener(commentTextWatcher);
        mBinding.scrollView.setOnScrollChangeListener(scrollChangeListener);

        /* Temporary */
        mMainPresenter.loadArticle(1L);
        mMainPresenter.loadComments(1L, mCommentsAdapter.getItemCount());

        mAnimationHelper = App.getInstance().appComponent().getAnimationHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBinding.fabPlay.setVisibility(View.GONE);
        EventBus.getDefault().unregister(this);
        mMediaBrowser.disconnect();
    }

    @Override
    public void showArticle(Article article) {
        mBinding.setData(article);
    }

    @Override
    public void showComments(List<Comment> comments) {
        mCommentsAdapter.clear();
        mCommentsAdapter.addComments(comments);

        if (comments.size() > CommentService.OFFSET)
            mBinding.scrollView.post(() ->
                    mBinding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void showNewComment(Comment comment) {
        mBinding.comment.getText().clear();
        mCommentsAdapter.add(comment);
    }

    @Override
    public void prepareAudio(Uri audioUri) {
        if (mMediaController != null)
            prepareIfNeeded(audioUri, mMediaController.getPlaybackState());
    }

    public void onLoadMore(View view) {
        mMainPresenter.loadComments(1L, mCommentsAdapter.getItemCount());
    }

    public void onAddComment(View view) {
        if (mBinding.getData() == null) {
            return;
        }

        Comment comment = new Comment();
        comment.setArticleId(mBinding.getData().getId());
        comment.setTime(DateTime.now().getMillis());
        comment.setComment(mBinding.comment.getText().toString());

        mMainPresenter.addComment(comment);
    }

    private void prepareIfNeeded(Uri audioUri, PlaybackStateCompat state) {
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mMediaController.getTransportControls().prepareFromUri(audioUri, null);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.e("fab", "visible");
                mBinding.fabPlay.setVisibility(View.VISIBLE);
                mBinding.fabPlay.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                Log.e("fab", "visible");
                mBinding.fabPlay.setVisibility(View.VISIBLE);
                mBinding.fabPlay.setImageResource(R.drawable.ic_pause_white_48dp);
                break;

        }
    }

    public void onPlay(View view) {
        switch (mMediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_PAUSED:
                mMediaController.getTransportControls().play();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                mMediaController.getTransportControls().pause();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                mMediaController.getTransportControls().playFromUri(mBinding.getData().getAudioUri(), null);
                break;
        }

        mAnimationHelper.onClickStartAnimation(mBinding.fabPlay, playAnimationListener);
    }

    private void subscribe() {
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();

        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.e("nearby", "success");
                        startService(getBackgroundSubscribeServiceIntent());
                    } else {
                        Log.e("nearby", "error");
                    }
                });
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(this, 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(this, BackgroundSubscribeIntentService.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PreparedEvent event) {
        mBinding.fabPlay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
