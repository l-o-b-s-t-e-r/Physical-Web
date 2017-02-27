package com.firebase.csm.misc;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.firebase.csm.R;
import com.firebase.csm.ui.main.MainActivity;
import com.google.android.gms.nearby.messages.Message;

/**
 * Created by Lobster on 15.02.17.
 */

public class NotificationHelper {

    private NotificationManager mNotificationManager;
    private Context mContext;

    public NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(Message message) {
        //Mock data, will be replaced by attachments from message
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/fir-csm.appspot.com/o/articles%2Fimages%2FVenusdeMilo.jpg?alt=media&token=deb2944c-781a-4460-8bae-2a470793da50";
        String title = "Venus de Milo";

        Bitmap notificationBigImage = null;
        try {
            notificationBigImage =
                    Glide.with(mContext)
                            .load(imageUrl)
                            .asBitmap()
                            .into(-1, -1)
                            .get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mContext)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(notificationBigImage)
                        );

        notificationBuilder.setContentIntent(MainActivity.createPendingIntent(title, true, mContext));
        mNotificationManager.notify(title.hashCode(), notificationBuilder.build());
    }

    public void cancelNotification(Message message) {
        String title = "Venus de Milo"; //Mock data, will be replaced by attachments from message
        mNotificationManager.cancel(title.hashCode());
    }

}
