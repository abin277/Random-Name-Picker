package com.randomappsinc.studentpicker.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.randomappsinc.studentpicker.R;
import com.randomappsinc.studentpicker.Utils.JSONUtils;
import com.randomappsinc.studentpicker.Utils.NameUtils;
import com.randomappsinc.studentpicker.Utils.UIUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alexanderchiou on 3/6/16.
 */
public class PresentationActivity extends StandardActivity implements TextToSpeech.OnInitListener{
    public static final String NUM_NAMES_KEY = "numNames";

    @Bind(R.id.parent) View parent;
    @Bind(R.id.header) TextView header;
    @Bind(R.id.names) TextView names;

    private MediaPlayer player;
    private int numNames;
    private String namesList;
    private TextToSpeech textToSpeech;
    private boolean textToSpeechEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        numNames = getIntent().getIntExtra(NUM_NAMES_KEY, 0);
        if (numNames > 1) {
            header.setText(R.string.names_chosen);
        }
        else {
            header.setText(R.string.name_chosen);
        }

        namesList = getIntent().getStringExtra(JSONUtils.NAMES_KEY);
        names.setText(namesList);

        player = new MediaPlayer();

        playSound("drumroll.mp3");

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);
    }

    private void playSound(String filePath) {
        try {
            AssetFileDescriptor fileDescriptor = getAssets().openFd(filePath);
            player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            player.prepare();
            player.start();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateNames();
                }
            }, 2600);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void animateNames() {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(names, "alpha", 1.0f).setDuration(250);
        fadeIn.setInterpolator(new AccelerateInterpolator());

        AnimatorSet scaleSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(names, "scaleX", 1.0f, 3.0f, 1.0f).setDuration(250);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(names, "scaleY", 1.0f, 3.0f, 1.0f).setDuration(250);
        scaleSet.setInterpolator(new DecelerateInterpolator());
        scaleSet.playTogether(scaleX, scaleY);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playSequentially(fadeIn, scaleSet);
        fullSet.start();
    }

    public void sayNames(String names) {
        if (textToSpeechEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sayTextPostL(names);
            }
            else {
                sayTextPreL(names);
            }
        }
        else {
            UIUtils.showSnackbar(parent, getString(R.string.text_to_speech_fail));
        }
    }

    @SuppressWarnings("deprecation")
    private void sayTextPreL(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, this.hashCode() + "");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sayTextPostL(String text) {
        String utteranceId = this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    public void onInit(int status) {
        textToSpeechEnabled = (status == TextToSpeech.SUCCESS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.presentation_menu, menu);
        UIUtils.loadMenuIcon(menu, R.id.say_names, FontAwesomeIcons.fa_microphone, this);
        if (numNames > 1) {
            menu.findItem(R.id.say_names).setTitle(R.string.say_names);
        }
        UIUtils.loadMenuIcon(menu, R.id.copy_names, FontAwesomeIcons.fa_clipboard, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.say_names:
                sayNames(namesList);
                return true;
            case R.id.copy_names:
                NameUtils.copyNamesToClipboard(namesList, parent, numNames);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
