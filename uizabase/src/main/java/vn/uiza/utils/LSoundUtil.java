package vn.uiza.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created by www.muathu@gmail.com on 6/1/2017.
 */

public final class LSoundUtil {

    private LSoundUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void startMusicFromAsset(@NonNull Context context, String fileName) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(fileName);
            mediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength()
            );
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                mediaPlayer1.stop();
                mediaPlayer1.reset();
                mediaPlayer1.release();
            });
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
