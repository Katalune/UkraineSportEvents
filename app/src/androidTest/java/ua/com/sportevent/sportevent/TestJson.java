package ua.com.sportevent.sportevent;

import android.test.AndroidTestCase;

import java.util.Map;

import ua.com.sportevent.sportevent.serverData.ServerData;

public class TestJson extends AndroidTestCase {

    public static final String LOG_TAG = TestJson.class.getSimpleName();

    public void testGetJson() throws Throwable {
        Map cv = ServerData.getEventsValues();
        assertFalse("Information from server can't be parsed.", cv == null);
    }
}
