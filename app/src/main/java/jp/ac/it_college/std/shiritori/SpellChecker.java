package jp.ac.it_college.std.shiritori;


import android.content.Context;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import java.util.Locale;

/**
 * Created by kabotya on 15/08/25.
 */
public class SpellChecker {

    private SpellCheckerSession session;
    private TextServicesManager manager;

    public SpellChecker(Context context, SpellCheckerSessionListener listener) {
        manager = (TextServicesManager) context.getSystemService(
                        Context.TEXT_SERVICES_MANAGER_SERVICE);
        session = manager.newSpellCheckerSession(null, Locale.ENGLISH, listener, false);
    }

    public void spellCheck(String word) {
        session.getSuggestions(new TextInfo(word), 1);
    }

}
