package ua.com.sportevent.sportevent;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

import ua.com.sportevent.sportevent.helpers.Utility;

/**
 * Extend this class for each activity that should retrieve the value of a String in specific
 * locale regardless of the current locale setting of the device/app.
 */
public class LangActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        // Retrieved locale from the Settings
        Locale locale = Utility.getLocale(this);
        Locale.setDefault(locale);
        conf.locale = locale;
        res.updateConfiguration(conf, null); // second arg null means don't change

        super.onCreate(savedInstanceState);
    }
}
