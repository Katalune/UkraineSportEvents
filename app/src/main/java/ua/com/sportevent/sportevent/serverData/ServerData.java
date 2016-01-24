package ua.com.sportevent.sportevent.serverData;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Class created to get data from the server.
 */
public class ServerData {
    // TODO: 24.01.2016 replace this class with RetrofitHelper

    public static final String COOKIE_HEADER_GET = "Cookie";
    private static final String CHARSET = "UTF-8";
    private static final String POST_SUCCESS = "\"success\"";
    private static String PHPSESSID;

    private static final String LOG_TAG = ServerData.class.getSimpleName();

    /**
     * @return the array of content values with event information. In case of some error
     * returns ContentValues[] of zero length.
     */
    public static Map<String, Object> getEventsValues() {
        return RetrofitHelper.getEventsValues();
    }

    // Save profile values as a map to be able to save map keys to access them in more generic way.
    /**
     * @return the array of content values with profile information. Contains 1 element.
     * In case of an error returns ContentValues[] of zero length.
     */
    public static Map<String, Object> getProfileValues()  {
        return RetrofitHelper.getProfileValues();
    }

    /**
     * Post information to the server with POST protocol.
     * @param requestURL server url to post information to.
     * @param postData String to post to the server with the request.
     * @return Server response after the POST request.
     */
    public static String postValues(String requestURL, String postData) {
        DataOutputStream writer = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            // It will use POST if setDoOutput(true) has been called
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(15000);
            // Recognise user on the server.
            if (PHPSESSID != null) {
                urlConnection.setRequestProperty(COOKIE_HEADER_GET, PHPSESSID);
            }
            // Encode key and values with charset.
            urlConnection.setRequestProperty("Accept-Charset", CHARSET);
            // POST body in url form kay1=value1&key2=value2
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);

            // Send request.
            OutputStream os = urlConnection.getOutputStream();
            writer = new DataOutputStream(os);
            writer.writeBytes(postData);

            // Read response.
            String line;
            StringBuilder response = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            if (response.toString().equals(POST_SUCCESS)) {
                RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_DEFAULT_OK);
            } else {
                // Server return something wrong.
                RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_SERVER_INVALID);
            }
            return response.toString();
        } catch (MalformedURLException e) {
            // URL was wrong - need to rewrite those part.
            RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_SERVER_INVALID);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // Connection timeout or can't open output stream.
            RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_BAD_CONNECTION);
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * @param postDataValues Map with profile values to post to the server. Should contain String
     *                       key-value pairs and array of fields "fields" => array("first_name", "last_name").
     *                       Add *json: 1* pair to receive response on success.
     */
    public static void postProfileValues(Map<String, Object> postDataValues, List<String> updatedFields) {
        RetrofitHelper.postProfileValues(postDataValues, updatedFields);
        // fields[] => first_name
        // [opt] => UserEdit
        // [json] => 1
        // [first_name] => Kata
//        postDataValues.put("fields[]", updatedFields);
//        postDataValues.put("json", 1);
//        postDataValues.put("opt", "UserEdit");
//        String url = buildEncodedUrl(postDataValues, CHARSET);
//        postValues(ServerContract.ServerProfileEntry.BASE_URL_POST, url);
    }

    public static String addParticipant(String sessid, String id_event, String id_distance, String id_package) {
        PHPSESSID = sessid;
        String temp = "opt=ParticipantAdd&id_event=227&id_items=228&id_price=186";
        return postValues(ServerContract.BASE_URL, temp);
    }

    @NonNull
    private static String buildEncodedUrl(@Nullable Map<String, Object> postDataValues, String charset) {
        String url = "";
        if (postDataValues != null && postDataValues.size() > 0) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Object> entry : postDataValues.entrySet()) {
                if (first)
                    first = false;
                else
                    builder.append("&");

                try {
                    if (entry.getValue() instanceof List) {
                        List<String> items = (List) entry.getValue();
                        boolean firstItem = true;
                        for (String item : items) {
                            if (firstItem) {
                                firstItem = false;
                            } else {
                                builder.append("&");
                            }
                            builder.append(URLEncoder.encode(entry.getKey(), charset));
                            builder.append("=");
                            builder.append(URLEncoder.encode(item, charset));
                        }
                    } else {
                        builder.append(URLEncoder.encode(entry.getKey(), charset));
                        builder.append("=");
                        builder.append(URLEncoder.encode(entry.getValue().toString(), charset));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            url = builder.toString();
        }
        return url;
    }

}
