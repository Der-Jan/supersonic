/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.androidapp.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import net.sourceforge.subsonic.androidapp.R;
import net.sourceforge.subsonic.androidapp.activity.DownloadActivity;
import net.sourceforge.subsonic.androidapp.domain.MusicDirectory;
import net.sourceforge.subsonic.androidapp.domain.PlayerState;
import net.sourceforge.subsonic.androidapp.domain.RepeatMode;
import net.sourceforge.subsonic.androidapp.domain.Version;
import net.sourceforge.subsonic.androidapp.provider.SubsonicAppWidgetProvider;
import net.sourceforge.subsonic.androidapp.receiver.MediaButtonIntentReceiver;
import net.sourceforge.subsonic.androidapp.service.DownloadServiceImpl;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public final class NotificationUtil {

    private static final String TAG = NotificationUtil.class.getSimpleName();
    private final static Pair<Integer, Integer> NOTIFICATION_TEXT_COLORS = new Pair<Integer, Integer>();

    public static void showPlayingNotification(final Context context, final DownloadServiceImpl downloadService, Handler handler, MusicDirectory.Entry song) {

        String title = song.getTitle();
        String text = song.getArtist();

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification);

        // Set the album art.
        try {
            int size = context.getResources().getDrawable(R.drawable.unknown_album).getIntrinsicHeight();
            Bitmap bitmap = FileUtil.getAlbumArtBitmap(context, song, size);
            if (bitmap == null) {
                // set default album art
                contentView.setImageViewResource(R.id.notification_image, R.drawable.unknown_album);
            } else {
                contentView.setImageViewBitmap(R.id.notification_image, bitmap);
            }
        } catch (Exception x) {
            Log.w(TAG, "Failed to get notification cover art", x);
            contentView.setImageViewResource(R.id.notification_image, R.drawable.unknown_album);
        }

        contentView.setTextViewText(R.id.notification_title, title);
        contentView.setTextViewText(R.id.notification_artist, text);

        Pair<Integer, Integer> colors = getNotificationTextColors(context);
        if (colors.getFirst() != null) {
            contentView.setTextColor(R.id.notification_title, colors.getFirst());
        }
        if (colors.getSecond() != null) {
            contentView.setTextColor(R.id.notification_artist, colors.getSecond());
        }

        Intent notificationIntent = new Intent(context, DownloadActivity.class);

        final Notification notification = new NotificationCompat.Builder(context)
                .setOngoing(true)
                .setSmallIcon(R.drawable.stat_notify_playing)
                .setContentTitle(title)
                .setContent(contentView)
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0))
                .build();

        // Send the notification and put the service in the foreground.
        handler.post(new Runnable() {
            @Override
            public void run() {
                downloadService.startForeground(Constants.NOTIFICATION_ID_PLAYING, notification);
            }
        });

        // Update widget
        SubsonicAppWidgetProvider.getInstance().notifyChange(context, downloadService, true);
    }

    public static void hidePlayingNotification(final Context context, final DownloadServiceImpl downloadService, Handler handler) {

        // Remove notification and remove the service from the foreground
        handler.post(new Runnable() {
            @Override
            public void run() {
                downloadService.stopForeground(true);
            }
        });

        // Update widget
        SubsonicAppWidgetProvider.getInstance().notifyChange(context, downloadService, false);
    }

    /**
     * Resolves the default text color for notifications.
     * <p/>
     * Based on http://stackoverflow.com/questions/4867338/custom-notification-layouts-and-text-colors/7320604#7320604
     */
    private static Pair<Integer, Integer> getNotificationTextColors(Context context) {
        if (NOTIFICATION_TEXT_COLORS.getFirst() == null && NOTIFICATION_TEXT_COLORS.getSecond() == null) {
            try {
                Notification notification = new Notification();
                String title = "title";
                String content = "content";
                notification.setLatestEventInfo(context, title, content, null);
                LinearLayout group = new LinearLayout(context);
                ViewGroup event = (ViewGroup) notification.contentView.apply(context, group);
                findNotificationTextColors(event, title, content);
                group.removeAllViews();
            } catch (Exception x) {
                Log.w(TAG, "Failed to resolve notification text colors.", x);
            }
        }
        return NOTIFICATION_TEXT_COLORS;
    }

    private static void findNotificationTextColors(ViewGroup group, String title, String content) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof TextView) {
                TextView textView = (TextView) group.getChildAt(i);
                String text = textView.getText().toString();
                if (title.equals(text)) {
                    NOTIFICATION_TEXT_COLORS.setFirst(textView.getTextColors().getDefaultColor());
                } else if (content.equals(text)) {
                    NOTIFICATION_TEXT_COLORS.setSecond(textView.getTextColors().getDefaultColor());
                }
            } else if (group.getChildAt(i) instanceof ViewGroup) {
                findNotificationTextColors((ViewGroup) group.getChildAt(i), title, content);
            }
        }
    }
}
