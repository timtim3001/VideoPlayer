package nl.timherreijgers.videoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class VideoPlayer extends RelativeLayout implements MediaPlayer.OnPreparedListener, SurfaceHolder.Callback, VideoControlView.OnControlInteractedListener {

    private static final String TAG = VideoPlayer.class.getSimpleName();

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private VideoControlView videoControlView;
    private MediaPlayer mediaPlayer;

    private boolean sourceHasBeenSet = false;

    private int videoWidth;
    private int videoHeight;

    private boolean playing = false;

    private Thread timeThread;

    public VideoPlayer(Context context) {
        this(context, null);
    }

    public VideoPlayer(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.video_view, this);
        mediaPlayer = new MediaPlayer();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        videoControlView = findViewById(R.id.videoControlView);
        videoControlView.setListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        timeThread = new Thread(() -> {
            while(true){
                try{
                    Thread.sleep(1000);
                    if(mediaPlayer == null)
                        return;
                    post(() -> videoControlView.setCurrentTime(mediaPlayer.getCurrentPosition() / 1000));
                    Log.d(TAG, "VideoPlayer: Thread ticked :D");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        timeThread.start();
    }

    public void playVideo(String path) throws IOException{
        Log.d(TAG, "playVideo() called with: path = [" + path + "]");
        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setDataSource(path);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.prepareAsync();

        sourceHasBeenSet = true;
    }

    private void startVideoPlayback() {
        Log.d(TAG, "startVideoPlayback() called with: sourceHasBeenSet = [" + sourceHasBeenSet + "], surfaceHolder = [" + surfaceHolder + "]");
        if(!sourceHasBeenSet || surfaceHolder == null || videoHeight == 0 || videoWidth == 0)
            return;

        mediaPlayer.setDisplay(surfaceHolder);
        surfaceHolder.setFixedSize(videoWidth, videoHeight);
        mediaPlayer.start();
        playing = true;
        videoControlView.setPlaying(false);
        if(mediaPlayer.getDuration() != -1)
            videoControlView.setTotalTime(mediaPlayer.getDuration() / 1000);
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getTime(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared() called with: mp = [" + mp + "]");
        videoWidth = mp.getVideoWidth();
        videoHeight = mp.getVideoHeight();
        startVideoPlayback();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        startVideoPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mediaPlayer.stop();
        mediaPlayer = null;
    }

    @Override
    public void onPauseButtonClicked() {
        if(!sourceHasBeenSet || surfaceHolder == null || videoHeight == 0 || videoWidth == 0)
            return;

        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();

        videoControlView.setPlaying(!mediaPlayer.isPlaying());
    }

    @Override
    public void onBackButtonClicked() {

    }

    @Override
    public void onTimeChanged(int time) {

    }
}
