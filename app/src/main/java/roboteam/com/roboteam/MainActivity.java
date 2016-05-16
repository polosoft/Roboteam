package roboteam.com.roboteam;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private static String TAG = "Roboteam";
    private SpeechToText service;
    private Button first_button, second_button;

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            Toast.makeText(activity ,(String)msg.obj ,Toast.LENGTH_SHORT).show();
        }
    }

    private final MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first_button = (Button) findViewById(R.id.first_button);
        second_button = (Button) findViewById(R.id.second_button);

        first_button.setOnClickListener(this);
        second_button.setOnClickListener(this);

        service = new SpeechToText();
        service.setUsernameAndPassword("ac1f6995-2288-4523-ba67-20819a005ad8", "ftM2hTw33IUI");
        service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
    }


    private void toastAudio(String path)
    {
        RecognizeOptions options = new RecognizeOptions.Builder()
                .continuous(true)
                .interimResults(true)
                .contentType(HttpMediaType.AUDIO_WAV)
                .build();


        try {
            final InputStream is = getAssets().open(path);

            service.recognizeUsingWebSocket(is, options, new BaseRecognizeCallback() {
                @Override
                public void onTranscription(SpeechResults speechResults) {
                    List<Transcript> list = speechResults.getResults();
                    Transcript transcript = list.get(0);
                    List<SpeechAlternative> speechAlternatives = transcript.getAlternatives();

                    for (SpeechAlternative speechAlternative: speechAlternatives) {
                        Message msg = handler.obtainMessage();
                        msg.obj = speechAlternative.getTranscript();

                        handler.sendMessage(msg);
                    }
                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                    Log.e(TAG, "failed to display the message", e);

                    Message msg = handler.obtainMessage();
                    msg.obj = "failed to display the message, try again";

                    handler.sendMessage(msg);
                }

                @Override
                public void onDisconnected() {
                    super.onDisconnected();
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.first_button:
                playAudio("Zira1.wav");
                toastAudio("Zira1.wav");
                break;

            case R.id.second_button:
                playAudio("Zira2.wav");
                toastAudio("Zira2.wav");
                break;
        }
    }

    private void playAudio(String path)
    {
        try {
            AssetFileDescriptor afd = getAssets().openFd(path);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
