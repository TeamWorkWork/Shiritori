package jp.ac.it_college.std.shiritori;


import android.app.Activity;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextServicesManager;

import java.util.Locale;

/**
 * Created by kabotya on 15/08/25.
 */
public class SpellChecker extends Activity {

    public SpellCheckerSession session;
    //private String inputText;




    public  SpellChecker(String inputText) {

        TextServicesManager manager = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);

        //リスナーの登録　スペルチェックは英語
        session = manager.newSpellCheckerSession(null, Locale.ENGLISH,
                new SpellCheckerSession.SpellCheckerSessionListener() {
                    public void onGetSuggestions(SuggestionsInfo[] results) {
                        for (SuggestionsInfo result : results) {
                            //候補が無かった場合
                            if (result.getSuggestionsCount() <= 0) {
                                //無かった場合の処理

                            }

                            else {
                                //あった場合の処理
                            }
                        }
                    }

                    @Override
                    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfos) {

                    }


                }
                , false);
    }


}
