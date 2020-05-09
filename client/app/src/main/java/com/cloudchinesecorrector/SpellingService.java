package com.cloudchinesecorrector;

import android.service.textservice.SpellCheckerService;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;

import androidx.annotation.RequiresPermission;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SpellingService extends SpellCheckerService {

    @Override
    public Session createSession() {
        return new MySpellingSession();
    }

    class MySpellingSession extends Session {
        @Override
        public void onCreate() {

        }

        private static final String url = "http://ec2-18-223-97-193.us-east-2.compute.amazonaws.com:5000/correct/best";

        private String DoCloudCorrect(String input) {
            HttpURLConnection connection = null;
            try {
                String inputJson = "{ \"text\": \"" + input + "\"}";
                //Create connection
                URL url = new URL(MySpellingSession.url);
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
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {

            String word = textInfo.getText();
            String res = DoCloudCorrect(word);
            JSONParser parser = new JSONParser();
            String suggestion = null;
            try {
                JSONObject json = (JSONObject) parser.parse(res);
                suggestion = (String) json.get("best");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int attr = SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO;
            String suggestions[] = null;
            if(suggestion != null){
                suggestions = new String[]{suggestion};
            }else{
                suggestions = new String[]{};
            }
            SuggestionsInfo suggestionsInfo = new SuggestionsInfo(attr, suggestions);
            return suggestionsInfo;
        }

        @Override
        public SentenceSuggestionsInfo[] onGetSentenceSuggestionsMultiple(TextInfo[] textInfos, int suggestionsLimit) {

            List<SuggestionsInfo> suggestionsInfos = new ArrayList<>();

            for(int i=0; i<textInfos.length; i++){
                TextInfo cur = textInfos[i];

                // Convert the sentence into an array of words
                String words[] = cur.getText().split("\\s+");
                for(String word:words){
                    TextInfo tmp = new TextInfo(word);
                    // Generate suggestions for each word
                    suggestionsInfos.add(onGetSuggestions(tmp, suggestionsLimit));
                }
            }
            return new SentenceSuggestionsInfo[]{
                    new SentenceSuggestionsInfo(
                            suggestionsInfos.toArray(new SuggestionsInfo[suggestionsInfos.size()]),
                            new int[suggestionsInfos.size()],
                            new int[suggestionsInfos.size()]
                    )
            };
        }
    }

}
