package md.habibure.dhaka.callrecorder_easyandsimple;


import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dyanamitechetan.vusikview.VusikView;
import md.habibure.dhaka.callrecorder_easyandsimple.R;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {

    private ImageButton btn_play_pause;
    private SeekBar seekBar;
    private TextView textView;

    private VusikView musicView;

    private MediaPlayer mediaPlayer;
    private int mediaFileLength=0;
    private int realtimeLength;
    final Handler handler = new Handler();
    private String filePath;
    private AsyncTask<String, String, String> mp3Play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicView = (VusikView) findViewById(R.id.musicView);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        textView = (TextView) findViewById(R.id.textTimer);
        btn_play_pause = (ImageButton) findViewById(R.id.btn_play_pause);
        mediaPlayer = new MediaPlayer();
        filePath = getIntent().getExtras().getString("uri");

        if (savedInstanceState!=null){
            mediaPlayer.reset();
        }

        seekBar.setMax(99); // 100% (0~99)
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaPlayer.isPlaying()) {
                    SeekBar seekBar = (SeekBar) v;
                    int playPosition = (mediaFileLength / 100) * seekBar.getProgress();
                    mediaPlayer.seekTo(playPosition);

                    realtimeLength = mediaFileLength - playPosition;
                }
                return false;
            }
        });


        btn_play_pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(PlayerActivity.this);


                mp3Play = new AsyncTask<String, String, String>() {

                    @Override
                    protected void onPreExecute() {
                        mDialog.setMessage("Please wait");
                        mDialog.show();
                    }

                    @Override
                    protected String doInBackground(String... params) {
                        try {
                            mediaPlayer.setDataSource(params[0]);
                            mediaPlayer.prepare();
                        } catch (Exception ex) {

                        }
                        return "";
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mediaFileLength = mediaPlayer.getDuration();
                        realtimeLength = mediaFileLength-mediaPlayer.getCurrentPosition();
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            btn_play_pause.setImageResource(R.drawable.ic_pause_circle);
                        } else {
                            mediaPlayer.pause();
                            btn_play_pause.setImageResource(R.drawable.ic_play_circle_for_player);
                        }

                        updateSeekBar();
                        mDialog.dismiss();
                    }
                };

                mp3Play.execute(filePath); // direct link mp3 file

                musicView.start();
            }
        });


        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);


    }

    private void updateSeekBar() {
        if (seekBar != null && mediaPlayer!=null) {
            int currentPosition=0;

            try {
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (final Exception e) {
                e.printStackTrace();
                if (e instanceof IllegalStateException) { // bypass IllegalStateException
                    // You can again call the method and make a counter for deadlock situation or implement your own code according to your situation
                    boolean checkAgain = true;
                    int counter = 0;
                    for(int i = 0; i < 2; i++){
                        if (checkAgain) {
                            mediaPlayer.reset();
                            if(mediaPlayer != null & mediaPlayer.isPlaying()) {
                                currentPosition = mediaPlayer.getCurrentPosition();
                            } else {
                                currentPosition = 0;
                            }
                            if(currentPosition > 0) {
                                checkAgain = false;
                                counter++;
                            }
                        } else {
                            if(counter == 0){
                                throw e;
                            }
                        }
                    }


                }
            }

            int position=(int) (((float) currentPosition / mediaFileLength) * 100);
            seekBar.setProgress(position);
        }
        if (mediaPlayer.isPlaying()) {
            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                    realtimeLength -= 1000; // declare 1 second
                    textView.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(realtimeLength),
                            TimeUnit.MILLISECONDS.toSeconds(realtimeLength) -
                                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(realtimeLength))));

                }

            };
            handler.postDelayed(updater, 1000); // 1 second
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play_pause.setImageResource(R.drawable.ic_pause_circle);
        musicView.stopNotesFall();

    }


    @Override
    protected void onStop() {
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.stop();
        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (mp3Play != null && !mp3Play.isCancelled()) {
            mp3Play.cancel(true);
        }
        super.onDestroy();
    }
}
