package com.reactlibrary;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.rtoshiro.view.video.FullscreenVideoView;

import java.util.Locale;



public class SpreoVideoView extends FullscreenVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnPreparedListener, View.OnTouchListener {
    protected View videoControlsView;

    protected SeekBar seekBar;
    protected ImageButton imgplay;
    protected ImageButton imgfullscreen;
    protected TextView textTotal, textElapsed;
    protected View.OnTouchListener touchListener;
    protected static final Handler TIME_THREAD = new Handler();
    protected Runnable updateTimeRunnable = new Runnable() {
        public void run() {
            updateCounter();
            TIME_THREAD.postDelayed(this, 200);
        }
    };

    public SpreoVideoView(Context context) {
        super(context);
    }

    public SpreoVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpreoVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();

        if (this.isInEditMode())
            return;
        super.setOnTouchListener(this);
    }

    @Override
    protected void initObjects() {
        super.initObjects();

        if (this.videoControlsView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.videoControlsView = inflater.inflate(R.layout.spreo_media_controller, this, false);
        }

        if (videoControlsView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_BOTTOM);
            addView(videoControlsView, params);

            this.seekBar = (SeekBar) this.videoControlsView.findViewById(R.id.vcv_seekbar);
            this.imgfullscreen = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_fullscreen);
            this.imgplay = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_play);
            this.textTotal = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_total);
            this.textElapsed = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_elapsed);
        }

        if (this.imgplay != null)
            this.imgplay.setOnClickListener(this);
        if (this.imgfullscreen != null)
            this.imgfullscreen.setOnClickListener(this);
        if (this.seekBar != null)
            this.seekBar.setOnSeekBarChangeListener(this);

        if (this.videoControlsView != null)
            this.videoControlsView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void releaseObjects() {
        super.releaseObjects();

        if (this.videoControlsView != null)
            removeView(this.videoControlsView);
    }

    protected void startCounter() {
        TIME_THREAD.postDelayed(updateTimeRunnable, 200);
    }

    protected void stopCounter() {
        TIME_THREAD.removeCallbacks(updateTimeRunnable);
    }

    protected void updateCounter() {
        if (this.textElapsed == null)
            return;

        int elapsed = getCurrentPosition();
        if (elapsed > 0 && elapsed < getDuration()) {
            seekBar.setProgress(elapsed);

            elapsed = Math.round(elapsed / 1000.f);
            long s = elapsed % 60;
            long m = (elapsed / 60) % 60;
            long h = (elapsed / (60 * 60)) % 24;

            if (h > 0)
                textElapsed.setText(String.format(Locale.US, "%d:%02d:%02d", h, m, s));
            else
                textElapsed.setText(String.format(Locale.US, "%02d:%02d", m, s));
        }
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener l) {
        touchListener = l;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        super.onCompletion(mp);
        stopCounter();
        updateControls();
        if (currentState != State.ERROR)
            updateCounter();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        boolean result = super.onError(mp, what, extra);
        stopCounter();
        updateControls();
        return result;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (getCurrentState() == State.END) {
            stopCounter();
        }
    }

    @Override
    protected void tryToPrepare() {
        super.tryToPrepare();

        if (getCurrentState() == State.PREPARED || getCurrentState() == State.STARTED) {
            if (textElapsed != null && textTotal != null) {
                int total = getDuration();
                if (total > 0) {
                    seekBar.setMax(total);
                    seekBar.setProgress(0);

                    total = total / 1000;
                    long s = total % 60;
                    long m = (total / 60) % 60;
                    long h = (total / (60 * 60)) % 24;
                    if (h > 0) {
                        textElapsed.setText("00:00:00");
                        textTotal.setText(String.format(Locale.US, "%d:%02d:%02d", h, m, s));
                    } else {
                        textElapsed.setText("00:00");
                        textTotal.setText(String.format(Locale.US, "%02d:%02d", m, s));
                    }
                }
            }

            if (videoControlsView != null)
                videoControlsView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (!isPlaying()) {
            super.start();
            startCounter();
            updateControls();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (isPlaying()) {
            stopCounter();
            super.pause();
            updateControls();
        }
    }

    @Override
    public void reset() {
        super.reset();

        stopCounter();
        updateControls();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        stopCounter();
        updateControls();
    }

    protected void updateControls() {
        if (imgplay == null) return;

        Drawable icon;
        if (getCurrentState() == State.STARTED) {
            icon = ContextCompat.getDrawable(context,R.drawable.pause_btn);
        } else {
            icon = ContextCompat.getDrawable(context,R.drawable.play_btn);
        }
        imgplay.setBackground(icon);
    }

    public void hideControls() {
        if (videoControlsView != null) {
            videoControlsView.setVisibility(View.INVISIBLE);
        }
    }

    public void showControls() {
        if (videoControlsView != null) {
            videoControlsView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (videoControlsView != null) {
                if (videoControlsView.getVisibility() == View.VISIBLE)
                    hideControls();
                else
                    showControls();
            }
        }

        if (touchListener != null) {
            return touchListener.onTouch(SpreoVideoView.this, event);
        }

        return false;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.vcv_img_play) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
        } else {
            setFullscreen(!isFullscreen());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopCounter();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        seekTo(progress);
    }

//    @Override
//    public synchronized void onPrepared(MediaPlayer mp) {
//        super.onPrepared(mp);
//        try {
//            start();
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }
}
