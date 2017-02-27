package com.firebase.csm.ui.main;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import com.firebase.csm.App;
import com.firebase.csm.R;
import com.firebase.csm.adapters.CommentsAdapter;
import com.firebase.csm.databinding.ActivityMainBinding;
import com.firebase.csm.events.PreparedEvent;
import com.firebase.csm.firebase.CommentService;
import com.firebase.csm.firebase.IArticleService;
import com.firebase.csm.firebase.ICommentService;
import com.firebase.csm.media.MediaPlaybackService;
import com.firebase.csm.misc.AnalyticsHelper;
import com.firebase.csm.misc.AnimationHelper;
import com.firebase.csm.misc.BackgroundSubscribeIntentService;
import com.firebase.csm.models.Article;
import com.firebase.csm.models.Comment;
import com.firebase.csm.ui.base.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class MainActivity extends BaseActivity implements MainPerformance.View {

    private static final int MAIN_REQUEST_CODE = 988;
    public static final String EXHIBIT = "exhibit";
    public static final String IS_NOTIFICATION = "is_notification";

    @Inject
    public IArticleService articleReference;
    @Inject
    public ICommentService commentReference;
    @Inject
    public AnimationHelper animationHelper;

    private ActivityMainBinding mBinding;
    private MainPresenter mMainPresenter;
    private CommentsAdapter mCommentsAdapter;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private GoogleApiClient mGoogleApiClient;
    private Article mArticle;

    public static PendingIntent createPendingIntent(String exhibitTitle, boolean isNotification, Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXHIBIT, exhibitTitle);
        intent.putExtra(IS_NOTIFICATION, isNotification);

        return PendingIntent.getActivity(context, MAIN_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().appComponent().inject(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        initialization();
    }

    protected void initialization() {
        /* Presenter */
        mMainPresenter = new MainPresenter(this, articleReference, commentReference, realm);

        /* RecyclerView */
        mCommentsAdapter = new CommentsAdapter();
        mBinding.comments.setAdapter(mCommentsAdapter);
        mBinding.comments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean(IS_NOTIFICATION)) {
            String artifactName = extras.getString(EXHIBIT);
            mMainPresenter.loadArticle(artifactName);
            AnalyticsHelper.userClickNotification(artifactName);
        } else {
            extras = new Bundle();
            extras.putString(EXHIBIT, "Mona Lisa");
            mMainPresenter.loadArticle(extras.getString(EXHIBIT)); //Mock data for init loading
        }

        /* Media */
        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaPlaybackService.class),
                connectionCallbacks,
                extras
        );

        /* Listeners */
        mBinding.comment.addTextChangedListener(commentTextWatcher);
        mBinding.scrollView.setOnScrollChangeListener(scrollChangeListener);

        if (!isNetworkAvailable()) {
            showError("Please check internet connection");
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                        .setPermissions(NearbyPermissions.BLE)
                        .build())
                .addConnectionCallbacks(googleConnectionCallbacks)
                .enableAutoManage(this, googleConnectionFailedListener)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBinding.fabPlay.setVisibility(View.INVISIBLE);
        mMediaBrowser.disconnect();
    }

    @Override
    public void showArticle(Article article) {
        mBinding.setData(mArticle = article);
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
        mMainPresenter.loadComments(mBinding.getData().getId(), mCommentsAdapter.getItemCount());
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
                mBinding.fabPlay.setVisibility(View.VISIBLE);
                mBinding.fabPlay.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                mBinding.fabPlay.setVisibility(View.VISIBLE);
                mBinding.fabPlay.setImageResource(R.drawable.ic_pause_white_48dp);
                break;
        }
    }

    public void onPlay(View view) {
        switch (mMediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_NONE:
                AnalyticsHelper.userStartPlayAudio(mArticle.getTitle());
                mMediaController.getTransportControls().play();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mMediaController.getTransportControls().play();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                mMediaController.getTransportControls().pause();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                AnalyticsHelper.userStartPlayAudio(mArticle.getTitle());
                mMediaController.getTransportControls().playFromUri(mBinding.getData().getAudioUri(), null);
                break;
        }

        animationHelper.onClickStartAnimation(mBinding.fabPlay, playAnimationListener);
    }

    private void subscribe() {
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();

        Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options)
                .setResultCallback(status -> {
                    Timber.d("Nearby subscription callback: " + status);
                    if (status.isSuccess()) {
                        startService(getBackgroundSubscribeServiceIntent());
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

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG)
                .show();
    }

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

            animationHelper.onClickEndAnimation(mBinding.fabPlay);
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

    private final GoogleApiClient.ConnectionCallbacks googleConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Timber.d("GoogleApi is connected");
            subscribe();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Timber.d("GoogleApi is suspended: %d", i);
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener googleConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Timber.d("GoogleApi is failed %d", connectionResult.getErrorCode());
        }
    };

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Timber.d("Player is connected");
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

    private final MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Timber.d("Player stated changed: %d", state.getState());
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

    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PreparedEvent event) {
        mBinding.fabPlay.setVisibility(View.VISIBLE);
    }
}
