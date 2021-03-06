package hamlah.pin.service;

/* stoled from DeskClock */

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * <p>Plays the alarm ringtone. Uses {@link Ringtone} in a separate thread so that this class can be
 * used from the main thread. Consequently, problems controlling the ringtone do not cause ANRs in
 * the main thread of the application.</p>
 *
 * <p>This class also serves a second purpose. It accomplishes alarm ringtone playback using two
 * different mechanisms depending on the underlying platform.</p>
 *
 * <ul>
 *     <li>Prior to the M platform release, ringtone playback is accomplished using
 *     {@link MediaPlayer}. android.permission.READ_EXTERNAL_STORAGE is required to play custom
 *     ringtones located on the SD card using this mechanism. {@link MediaPlayer} allows clients to
 *     adjust the volume of the stream and specify that the stream should be looped.</li>
 *
 *     <li>Starting with the M platform release, ringtone playback is accomplished using
 *     {@link Ringtone}. android.permission.READ_EXTERNAL_STORAGE is <strong>NOT</strong> required
 *     to play custom ringtones located on the SD card using this mechanism. {@link Ringtone} allows
 *     clients to adjust the volume of the stream and specify that the stream should be looped but
 *     those methods are marked @hide in M and thus invoked using reflection. Consequently, revoking
 *     the android.permission.READ_EXTERNAL_STORAGE permission has no effect on playback in M+.</li>
 * </ul>
 */
final class AsyncRingtonePlayer {

    private static final String TAG = "AsyncRingtonePlayer";

    private static final String DEFAULT_CRESCENDO_LENGTH = "0";

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    // Message codes used with the ringtone thread.
    private static final int EVENT_PLAY = 1;
    private static final int EVENT_STOP = 2;
    private static final int EVENT_VOLUME = 3;
    private static final String RINGTONE_URI_KEY = "RINGTONE_URI_KEY";
    private static AsyncRingtonePlayer sAsyncRingtonePlayer;

    /** Handler running on the ringtone thread. */
    private Handler mHandler;

    /** {@link MediaPlayerPlaybackDelegate} on pre M; {@link RingtonePlaybackDelegate} on M+ */
    private PlaybackDelegate mPlaybackDelegate;

    /** The context. */
    private final Context mContext;

    public AsyncRingtonePlayer(Context context) {
        mContext = context;
    }

    /** Plays the ringtone. */
    public void play(Uri ringtoneUri) {
        Log.d(TAG, "Posting play.");
        postMessage(EVENT_PLAY, ringtoneUri, 0);
    }

    /** Stops playing the ringtone. */
    public void stop() {
        Log.d(TAG, "Posting stop.");
        postMessage(EVENT_STOP, null, 0);
    }

    /** Schedules an adjustment of the playback volume 50ms in the future. */
    private void scheduleVolumeAdjustment() {

        // Ensure we never have more than one volume adjustment queued.
        mHandler.removeMessages(EVENT_VOLUME);

        // Queue the next volume adjustment.
        postMessage(EVENT_VOLUME, null, 50);
    }

    /**
     * Posts a message to the ringtone-thread handler.
     *
     * @param messageCode The message to post.
     * @param ringtoneUri The ringtone in question, if any.
     * @param delayMillis The amount of time to delay sending the message, if any.
     */
    private void postMessage(int messageCode, Uri ringtoneUri, long delayMillis) {
        synchronized (this) {
            if (mHandler == null) {
                mHandler = getNewHandler();
            }

            final Message message = mHandler.obtainMessage(messageCode);
            if (ringtoneUri != null) {
                final Bundle bundle = new Bundle();
                bundle.putParcelable(RINGTONE_URI_KEY, ringtoneUri);
                message.setData(bundle);
            }

            mHandler.sendMessageDelayed(message, delayMillis);
        }
    }

    /**
     * Creates a new ringtone Handler running in its own thread.
     */
    private Handler getNewHandler() {
        final HandlerThread thread = new HandlerThread("ringtone-player");
        thread.start();

        return new Derp(thread.getLooper(), mContext, this);
    }

    private static class Derp extends Handler {


        private final Context mContext;
        private final AsyncRingtonePlayer player;

        public Derp(Looper looper, Context mContext, AsyncRingtonePlayer player) {
            super(looper);
            this.mContext = mContext;
            this.player = player;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PLAY:
                    final Uri ringtoneUri = msg.getData().getParcelable(RINGTONE_URI_KEY);
                    if (player.getPlaybackDelegate().play(mContext, ringtoneUri)) {
                        player.scheduleVolumeAdjustment();
                    }
                    break;
                case EVENT_STOP:
                    player.getPlaybackDelegate().stop(mContext);
                    break;
                case EVENT_VOLUME:
                    if (player.getPlaybackDelegate().adjustVolume(mContext)) {
                        player.scheduleVolumeAdjustment();
                    }
                    break;
            }
        }
    }

    /**
     * @return <code>true</code> iff the device is currently in a telephone call
     */
    private static boolean isInTelephoneCall(Context context) {
        final TelephonyManager tm = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }


    /**
     * Check if the executing thread is the one dedicated to controlling the ringtone playback.
     */
    private void checkAsyncRingtonePlayerThread() {
        if (Looper.myLooper() != mHandler.getLooper()) {
            Log.e(TAG, "Must be on the AsyncRingtonePlayer thread!", new IllegalStateException());
        }
    }

    /**
     * @param currentTime current time of the device
     * @param stopTime time at which the crescendo finishes
     * @param duration length of time over which the crescendo occurs
     * @return the scalar volume value that produces a linear increase in volume (in decibels)
     */
    private static float computeVolume(long currentTime, long stopTime, long duration) {
        // Compute the percentage of the crescendo that has completed.
        final float elapsedCrescendoTime = stopTime - currentTime;
        float fractionComplete = (elapsedCrescendoTime / duration);
        fractionComplete = (float)Math.pow(fractionComplete, 2f);
        fractionComplete = 1-fractionComplete;

        // Use the fraction to compute a target decibel between -40dB (near silent) and 0dB (max).
        final float gain = (fractionComplete * 40) - 40;

        // Convert the target gain (in decibels) into the corresponding volume scalar.
        final float volume = (float) Math.pow(10f, gain/20f);

        //android.util.Log.v(TAG, "Ringtone crescendo %,.2f%% complete (scalar: %f, volume: %f dB)"
        //      fractionComplete * 100, volume, gain);

        return volume;
    }

    /**
     * @return {@code true} iff the crescendo duration is more than 0 seconds
     */
    private boolean isCrescendoEnabled(Context context) {
        return getCrescendoDurationMillis(context) > 0;
    }

    /**
     * @return the duration of the crescendo in milliseconds
     */
    private long getCrescendoDurationMillis(Context context) {
        final String crescendoSecondsStr = "30"; /* PreferenceManager.getDefaultSharedPreferences(context)
                .getString(mCrescendoPrefKey, DEFAULT_CRESCENDO_LENGTH)*/
        return Integer.parseInt(crescendoSecondsStr) * DateUtils.SECOND_IN_MILLIS;
    }

    /**
     * @return the platform-specific playback delegate to use to play the ringtone
     */
    private PlaybackDelegate getPlaybackDelegate() {
        checkAsyncRingtonePlayerThread();

        if (mPlaybackDelegate == null) {
            // Use the newer Ringtone-based playback delegate because it does not require
            // any permissions to read from the SD card. (M+)
            //     mPlaybackDelegate = new RingtonePlaybackDelegate();
            // } else {
            // Fall back to the older MediaPlayer-based playback delegate because it is the only
            // way to force the looping of the ringtone before M. (pre M)
            mPlaybackDelegate = new MediaPlayerPlaybackDelegate();
            //}
        }

        return mPlaybackDelegate;
    }

    /**
     * This interface abstracts away the differences between playing ringtones via {@link Ringtone}
     * vs {@link MediaPlayer}.
     */
    private interface PlaybackDelegate {
        /**
         * @return {@code true} iff a {@link #adjustVolume volume adjustment} should be scheduled
         */
        boolean play(Context context, Uri ringtoneUri);
        void stop(Context context);

        /**
         * @return {@code true} iff another volume adjustment should be scheduled
         */
        boolean adjustVolume(Context context);
    }

    /**
     * Loops playback of a ringtone using {@link MediaPlayer}.
     */
    private class MediaPlayerPlaybackDelegate implements PlaybackDelegate {

        /** The audio focus manager. Only used by the ringtone thread. */
        private AudioManager mAudioManager;

        /** Non-{@code null} while playing a ringtone; {@code null} otherwise. */
        private MediaPlayer mMediaPlayer;

        /** The duration over which to increase the volume. */
        private long mCrescendoDuration = 0;

        /** The time at which the crescendo shall cease; 0 if no crescendo is present. */
        private long mCrescendoStopTime = 0;

        /**
         * Starts the actual playback of the ringtone. Executes on ringtone-thread.
         */
        @Override
        public boolean play(final Context context, Uri ringtoneUri) {
            checkAsyncRingtonePlayerThread();

            Log.i(TAG, "Play ringtone via android.media.MediaPlayer.");

            if (mAudioManager == null) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }

            Uri alarmNoise = ringtoneUri;
            // Fall back to the default alarm if the database does not have an alarm stored.
            if (alarmNoise == null) {
                alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Log.v(TAG, "Using default alarm: " + alarmNoise.toString());
            }
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.v(TAG, "Error occurred while playing audio. Stopping AlarmKlaxon.");
                    stop(context);
                    return true;
                }
            });

            boolean scheduleVolumeAdjustment = false;
            // Check if we are in a call. If we are, use the in-call alarm resource at a
            // low volume to not disrupt the call.
            if (isCrescendoEnabled(context)) {
                mMediaPlayer.setVolume(0, 0);

                // Compute the time at which the crescendo will stop.
                mCrescendoDuration = getCrescendoDurationMillis(context);
                mCrescendoStopTime = System.currentTimeMillis() + mCrescendoDuration;
                scheduleVolumeAdjustment = true;
            }

            // If alarmNoise is a custom ringtone on the sd card the app must be granted
            // android.permission.READ_EXTERNAL_STORAGE. Pre-M this is ensured at app
            // installation time. M+, this permission can be revoked by the user any time.
            try {
                mMediaPlayer.setDataSource(context, alarmNoise);
                startAlarm(mMediaPlayer);
            } catch (IOException e) {
                throw new RuntimeException("CRASHY LOL");
            }

            scheduleVolumeAdjustment = true;

            return scheduleVolumeAdjustment;
        }

        /**
         * Do the common stuff when starting the alarm.
         */
        private void startAlarm(MediaPlayer player) throws IOException {
            // do not play alarms if stream volume is 0 (typically because ringer mode is silent).
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                //if (true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    player.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
                }
                //}

                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(true);
                player.prepare();
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                player.start();
            }
        }

        /**
         * Stops the playback of the ringtone. Executes on the ringtone-thread.
         */
        @Override
        public void stop(Context context) {
            checkAsyncRingtonePlayerThread();

            Log.i(TAG, "Stop ringtone via android.media.MediaPlayer.");

            mCrescendoDuration = 0;
            mCrescendoStopTime = 0;

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
            }
        }

        /**
         * Adjusts the volume of the ringtone being played to create a crescendo effect.
         */
        @Override
        public boolean adjustVolume(Context context) {
            checkAsyncRingtonePlayerThread();

            // If media player is absent or not playing, ignore volume adjustment.
            if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                mCrescendoDuration = 0;
                mCrescendoStopTime = 0;
                return false;
            }

            // If the crescendo is complete set the volume to the maximum; we're done.
            final long currentTime = System.currentTimeMillis();
            if (currentTime > mCrescendoStopTime) {
                mCrescendoDuration = 0;
                mCrescendoStopTime = 0;
                mMediaPlayer.setVolume(1, 1);
                return false;
            }

            // The current volume of the crescendo is the percentage of the crescendo completed.
            final float volume = computeVolume(currentTime, mCrescendoStopTime, mCrescendoDuration);
            mMediaPlayer.setVolume(volume, volume);

            // Schedule the next volume bump in the crescendo.
            return true;
        }
    }

    /**
     * Loops playback of a ringtone using {@link Ringtone}.
     */
    private class RingtonePlaybackDelegate implements PlaybackDelegate {

        /** The audio focus manager. Only used by the ringtone thread. */
        private AudioManager mAudioManager;

        /** The current ringtone. Only used by the ringtone thread. */
        private Ringtone mRingtone;

        /** The method to adjust playback volume; cannot be null. */
        private Method mSetVolumeMethod;

        /** The method to adjust playback looping; cannot be null. */
        private Method mSetLoopingMethod;

        /** The duration over which to increase the volume. */
        private long mCrescendoDuration = 0;

        /** The time at which the crescendo shall cease; 0 if no crescendo is present. */
        private long mCrescendoStopTime = 0;

        private RingtonePlaybackDelegate() {
            try {
                mSetVolumeMethod = Ringtone.class.getDeclaredMethod("setVolume", float.class);
            } catch (NoSuchMethodException nsme) {
                Log.e(TAG, "Unable to locate method: Ringtone.setVolume(float).", nsme);
            }

            try {
                mSetLoopingMethod = Ringtone.class.getDeclaredMethod("setLooping", boolean.class);
            } catch (NoSuchMethodException nsme) {
                Log.e(TAG, "Unable to locate method: Ringtone.setLooping(boolean).", nsme);
            }
        }

        /**
         * Starts the actual playback of the ringtone. Executes on ringtone-thread.
         */
        @Override
        public boolean play(Context context, Uri ringtoneUri) {
            checkAsyncRingtonePlayerThread();

            Log.i(TAG, "Play ringtone via android.media.Ringtone.");

            if (mAudioManager == null) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }

            if (mRingtone != null && mRingtone.isPlaying()) {
                mRingtone.stop();
            }


            // attempt to fetch the specified ringtone
            mRingtone = RingtoneManager.getRingtone(context, ringtoneUri);

            // Attempt to enable looping the ringtone.
            try {
                mSetLoopingMethod.invoke(mRingtone, true);
            } catch (Exception e) {
                Log.e(TAG, "Unable to turn looping on for android.media.Ringtone", e);

                // Fall back to the default ringtone if looping could not be enabled.
                // (Default alarm ringtone most likely has looping tags set within the .ogg file)
                mRingtone = null;
            }

            if (mRingtone == null) {
                // fall back to the default ringtone
                final Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                mRingtone = RingtoneManager.getRingtone(context, defaultUri);
            }

            // if we don't have a ringtone at this point there isn't much recourse
            if (mRingtone == null) {
                Log.i(TAG, "Unable to locate alarm ringtone.");
                return false;
            }

            //if (Utils.isLOrLater()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRingtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            }
            //}

            // Attempt to adjust the ringtone volume if the user is in a telephone call.
            boolean scheduleVolumeAdjustment = false;
            if (isCrescendoEnabled(context)) {
                setRingtoneVolume(0);

                // Compute the time at which the crescendo will stop.
                mCrescendoDuration = getCrescendoDurationMillis(context);
                mCrescendoStopTime = System.currentTimeMillis() + mCrescendoDuration;
                scheduleVolumeAdjustment = true;
            }

            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            mRingtone.play();

            return scheduleVolumeAdjustment;
        }

        /**
         * Sets the volume of the ringtone.
         *
         * @param volume a raw scalar in range 0.0 to 1.0, where 0.0 mutes this player, and 1.0
         *               corresponds to no attenuation being applied.
         */
        private void setRingtoneVolume(float volume) {
            try {
                mSetVolumeMethod.invoke(mRingtone, volume);
            } catch (Exception e) {
                Log.e(TAG, "Unable to set volume for android.media.Ringtone", e);
            }
        }

        /**
         * Stops the playback of the ringtone. Executes on the ringtone-thread.
         */
        @Override
        public void stop(Context context) {
            checkAsyncRingtonePlayerThread();

            Log.i(TAG, "Stop ringtone via android.media.Ringtone.");

            mCrescendoDuration = 0;
            mCrescendoStopTime = 0;

            if (mRingtone != null && mRingtone.isPlaying()) {
                Log.d(TAG, "Ringtone.stop() invoked.");
                mRingtone.stop();
            }

            mRingtone = null;

            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
            }
        }

        /**
         * Adjusts the volume of the ringtone being played to create a crescendo effect.
         */
        @Override
        public boolean adjustVolume(Context context) {
            checkAsyncRingtonePlayerThread();

            // If ringtone is absent or not playing, ignore volume adjustment.
            if (mRingtone == null || !mRingtone.isPlaying()) {
                mCrescendoDuration = 0;
                mCrescendoStopTime = 0;
                return false;
            }

            // If the crescendo is complete set the volume to the maximum; we're done.
            final long currentTime = System.currentTimeMillis();
            if (currentTime > mCrescendoStopTime) {
                mCrescendoDuration = 0;
                mCrescendoStopTime = 0;
                setRingtoneVolume(1);
                return false;
            }

            final float volume = computeVolume(currentTime, mCrescendoStopTime, mCrescendoDuration);
            setRingtoneVolume(volume);

            // Schedule the next volume bump in the crescendo.
            return true;
        }
    }

    public static synchronized AsyncRingtonePlayer get(Context context) {
        if (sAsyncRingtonePlayer == null) {
            sAsyncRingtonePlayer = new AsyncRingtonePlayer(context.getApplicationContext());
        }

        return sAsyncRingtonePlayer;
    }
}
