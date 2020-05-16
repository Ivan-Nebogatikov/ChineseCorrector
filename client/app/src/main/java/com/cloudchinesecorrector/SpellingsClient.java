package com.cloudchinesecorrector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.Layout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class SpellingsClient extends Activity implements SpellCheckerSession.SpellCheckerSessionListener {

    private TextView suggestions;
    private EditText rateEdit;
    private Button correct;
    private Button good;
    private Button bad;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        suggestions = new TextView(this);
        suggestions.setPadding(16, 16, 16, 600);
        suggestions.setTextSize(20);
        addContentView(suggestions, lp);

        result = new TextView(this);
        result.setPadding(16, 800, 16, 600);
        result.setTextSize(20);
        addContentView(result, lp);

        rateEdit = new EditText(this);
        rateEdit.setPadding(16, 1000, 46, 16);
        rateEdit.setTextSize(20);
        addContentView(rateEdit, lp);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        final String deviceId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        correct = new Button(this);
        correct.setText("Correct");
        correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchSuggestionsFor(rateEdit.getText().toString());
            }
        });
        correct.setWidth(30);
        correct.setHeight(20);
        correct.setX(width / 2 - 30);
        correct.setY(height / 2 - 46);
        addContentView(correct, lp);

        good = new Button(this);
        good.setText("Good");
        good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();
                try {
                    object.put("Id", deviceId);
                    object.put("Corrected", suggestions.getText().toString());
                    object.put("Input", rateEdit.getText().toString());
                    object.put("IsLike", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String z = object.toString();
                new RateAsync().execute(object.toString());
            }
        });
        good.setWidth(30);
        good.setHeight(20);
        good.setX(width / 4 * 3 - 30);
        good.setY(height / 2 - 46);
        addContentView(good, lp);

        bad = new Button(this);
        bad.setText("Bad");
        bad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();
                try {
                    object.put("Id", deviceId);
                    object.put("Corrected", suggestions.getText().toString().replaceAll("\n", ""));
                    object.put("Input", rateEdit.getText().toString().replaceAll("\n", ""));
                    object.put("IsLike", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String str = object.toString();
                new RateAsync().execute(str);
            }
        });
        bad.setWidth(30);
        bad.setHeight(20);
        bad.setX(width / 4 * 3 - 30);
        bad.setY(height / 2 + 46);
        addContentView(bad, lp);

        fetchSuggestionsFor("我不知道");
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        // Unused
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        final StringBuffer sb = new StringBuffer("");
        for(SentenceSuggestionsInfo result:results){
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++){
                int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                if((result.getSuggestionsInfoAt(i).getSuggestionsAttributes() &
                        SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO )
                    continue;

                for(int k=0; k < m; k++) {
                    sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k))
                            .append("\n");
                }
                sb.append("\n");
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                suggestions.setText(sb.toString());
            }
        });
    }

    private final int NUMBER_OF_SUGGESTIONS=8;

    private void fetchSuggestionsFor(String input){
        TextServicesManager tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);
        SpellCheckerSession session = tsm.newSpellCheckerSession(null, Locale.CHINESE, this, true);

        session.getSentenceSuggestions(new TextInfo[]{ new TextInfo(input) }, NUMBER_OF_SUGGESTIONS);
    }

    class RateAsync extends AsyncTask<String, Void, String> {

        private Exception exception;

        private static final String url = "https://1shknletu9.execute-api.us-east-2.amazonaws.com/default/serverlessrepo-RateCorrection-helloworldpython3-WBC9LG4EQ4KT";

        protected String doInBackground(String... input) {
            HttpURLConnection connection = null;
            try {
                String inputJson = input[0];
                //Create connection
                URL url = new URL(RateAsync.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/json");

                connection.setRequestProperty("Content-Length",
                        Integer.toString(inputJson.getBytes("UTF-8").length));
                connection.setUseCaches(false);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.write(inputJson.getBytes("UTF-8"));
                wr.close();

                //Get Response
                int status = connection.getResponseCode();
                if (status == 200) {
                    return "OK";
                }
                InputStream is = connection.getErrorStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String res) {
            result.setText(res);
        }
    }
}


