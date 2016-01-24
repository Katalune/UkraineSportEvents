package ua.com.sportevent.sportevent.serverData;

import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.WorkerThread;

import com.facebook.AccessToken;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.jsonModels.ServerEventApi;
import ua.com.sportevent.sportevent.jsonModels.ServerUserProfile;

/**
 * Handle receiving data from the server.
 */
public class RetrofitHelper {

    @IntDef({EVENTS_STATUS_DEFAULT_OK, EVENTS_STATUS_SERVER_EMPTY, EVENTS_STATUS_SERVER_INVALID,
            EVENTS_STATUS_UNKNOWN, EVENTS_STATUS_BAD_CONNECTION, PROFILE_STATUS_NOT_ACTIVATED,
            PROFILE_STATUS_ERROR, PROFILE_STATUS_LOGOUT, BUYING_STATUS_EMPTY_PROFILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventsStatus {}

    public static final int EVENTS_STATUS_DEFAULT_OK = 0;
    public static final int EVENTS_STATUS_SERVER_EMPTY = 1;
    public static final int EVENTS_STATUS_SERVER_INVALID = 2;
    public static final int EVENTS_STATUS_UNKNOWN = 3;
    public static final int EVENTS_STATUS_BAD_CONNECTION = 4;
    public static final int PROFILE_STATUS_NOT_ACTIVATED = 5;
    public static final int PROFILE_STATUS_ERROR = 6;
    public static final int PROFILE_STATUS_LOGOUT = 7;
    public static final int BUYING_STATUS_EMPTY_PROFILE = 8;

    @EventsStatus
    public static int ERROR_STATUS = EVENTS_STATUS_DEFAULT_OK;

    static final String COOKIE_HEADER_GET = "Cookie";
    private static String sPHPSESSID;
    private static String sUSERID;
    private static String sHASH;
    private static final String COOKIE_HEADER_SET = "Set-Cookie";
    private static final String PHPSESSID_LABEL = "PHPSESSID";
    private static final String USERID_LABEL = "USERSSITE_ID";
    private static final String HASH_LABEL = "USERSSITE_HASH";
    private static final String POST_SUCCESS = "success";

    private static EventsService sEventsService;

    /**
     * Sets the events status into static variable.
     * @param errorStatus the IntDef value to set.
     */
    public static void setErrorStatus(@EventsStatus int errorStatus) {
        ERROR_STATUS = errorStatus;
    }

    public static EventsService getEventsService() {
//        client.connectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

        if (sEventsService == null) {
            // Set the custom client when building adapter
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ServerContract.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(new OkHttpClient())
                    .build();
            sEventsService = retrofit.create(EventsService.class);
        }
        return sEventsService;
    }

    interface EventsService {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter
        @GET(ServerContract.PATH_API)
        Call<ServerEventApi> getEvents(@Header(COOKIE_HEADER_GET) List<String> sessid);

        @GET(ServerContract.ServerProfileEntry.PATH_PROFILE_JSON)
        Call<ServerUserProfile> getProfile(@Header(COOKIE_HEADER_GET) List<String> sessid,
                                           @Query("access_token") String token);

        // In case of some error / redirect.
        @GET(ServerContract.ServerProfileEntry.PATH_PROFILE)
        Call<ResponseBody> getProfileHTML(@Header(COOKIE_HEADER_GET) List<String> sessid,
                                          @Query("access_token") String token);

        @FormUrlEncoded
        @POST(ServerContract.ServerProfileEntry.PATH_POST)
        Call<String> postProfile(@Header(COOKIE_HEADER_GET) List<String> sessid,
                                 @FieldMap Map<String, Object> fields);

        @FormUrlEncoded
        @POST("/")
        Call<ResponseBody> addParticipant(@Header(COOKIE_HEADER_GET) List<String> sessid,
                                          @Field("opt") String option,
                                          @Field("id_event") String id_event,
                                          @Field("id_items") String id_distance,
                                          @Field("id_price") String id_package);
    }

    @WorkerThread
    public static Map<String, Object> getEventsValues() {
        try {
            // Don't clone because we can have new sess id.
            Response<ServerEventApi> response = getEventsService().getEvents(getCookies()).execute();

            if (!response.isSuccess()) {
                setErrorStatus(EVENTS_STATUS_SERVER_EMPTY);
                return null;
            }

            updateCookie(response);
            ServerEventApi eventApi = response.body();
            String time = eventApi.current_time;
            ServerEvent[] serverEvents = eventApi.appoints;

            if (time == null || serverEvents == null) {
                setErrorStatus(EVENTS_STATUS_SERVER_INVALID);
                return null;
            }

            Map<String, Object> eventValues = new HashMap<>(3);
            eventValues.put(ServerContract.EventsEntry.LAST_UPDATE, time);
            // The time since the device last booted. Reset when the device shutdown (!!).
            eventValues.put(ServerContract.EventsEntry.ELAPSED_FOR_LAST_UPDATE, SystemClock.elapsedRealtime());
            eventValues.put(ServerContract.EventsEntry.EVENTS, serverEvents);
            setErrorStatus(EVENTS_STATUS_DEFAULT_OK);
            return eventValues;

        } catch (IOException e) {
            setErrorStatus(EVENTS_STATUS_BAD_CONNECTION);
            return null;
        }
    }

    @WorkerThread
    public static Map<String, Object> getProfileValues() {
        String token;
        try {
            token = AccessToken.getCurrentAccessToken().getToken();
        } catch (NullPointerException e) {
            // We have not token.
            setErrorStatus(PROFILE_STATUS_LOGOUT);
            return null;
        }
        try {
            // Don't clone because we can have new token.
            Response<ServerUserProfile> response = getEventsService().getProfile(getCookies(), token).execute();

            if (!response.isSuccess()) {
                setErrorStatus(EVENTS_STATUS_SERVER_EMPTY);
                return null;
            }

            updateCookie(response);
            ServerUserProfile profile = response.body();

            if (profile.error != null && profile.error.equals(ServerContract.ServerProfileEntry.NOT_ACTIVATED)) {
                setErrorStatus(PROFILE_STATUS_NOT_ACTIVATED);
                return null;
            }

            if (profile.success == null) {
                setErrorStatus(PROFILE_STATUS_ERROR);
                return null;
            }

            if (profile.success.equals(ServerContract.ServerProfileEntry.SUCCESS_REG)) {
                setErrorStatus(PROFILE_STATUS_NOT_ACTIVATED);
                return null;
            }

            if (!profile.success.equals(ServerContract.ServerProfileEntry.SUCCESS_LOGIN)) {
                setErrorStatus(PROFILE_STATUS_ERROR);
                return null;
            }

            // Replace null with empty string.
            Map<String, Object> profileValues = new HashMap<>(22);
            setNonNull(profileValues, ServerContract.ProfileEntry.ID, profile.id);
            setNonNull(profileValues, ServerContract.ProfileEntry.LOGIN, profile.login);
            setNonNull(profileValues, ServerContract.ProfileEntry.AVATAR, profile.avatara);
            setNonNull(profileValues, ServerContract.ProfileEntry.FIRST_NAME, profile.first_name);
            setNonNull(profileValues, ServerContract.ProfileEntry.FATHER_NAME, profile.father_name);
            setNonNull(profileValues, ServerContract.ProfileEntry.LAST_NAME, profile.last_name);
            setNonNull(profileValues, ServerContract.ProfileEntry.SEX, profile.sex);
            setNonNull(profileValues, ServerContract.ProfileEntry.BDAY, profile.birthday);
            setNonNull(profileValues, ServerContract.ProfileEntry.CLUB, profile.club_name);
            setNonNull(profileValues, ServerContract.ProfileEntry.TSHIRT_SIZE, profile.tshirt_size);
            setNonNull(profileValues, ServerContract.ProfileEntry.POSTCODE, profile.postcode);
            setNonNull(profileValues, ServerContract.ProfileEntry.COUNTRY, profile.country);
            setNonNull(profileValues, ServerContract.ProfileEntry.REGION, profile.region);
            setNonNull(profileValues, ServerContract.ProfileEntry.CITY, profile.city);
            setNonNull(profileValues, ServerContract.ProfileEntry.STREET, profile.street);
            setNonNull(profileValues, ServerContract.ProfileEntry.HOUSE, profile.house);
            setNonNull(profileValues, ServerContract.ProfileEntry.APARTMENT, profile.apartment);
            setNonNull(profileValues, ServerContract.ProfileEntry.ADDRESS, profile.full_address);
            setNonNull(profileValues, ServerContract.ProfileEntry.PHONE, profile.phone);
            setNonNull(profileValues, ServerContract.ProfileEntry.PHONE_2, profile.phone_2);
            setNonNull(profileValues, ServerContract.ProfileEntry.ABOUT, profile.about);
            setNonNull(profileValues, ServerContract.ProfileEntry.ACTIVE, profile.active);

            setErrorStatus(EVENTS_STATUS_DEFAULT_OK);
            return profileValues;
        } catch (IOException e) {
            setErrorStatus(EVENTS_STATUS_BAD_CONNECTION);
            return null;
        } catch (JsonSyntaxException e) {
            // TODO: 20.12.2015 set "follow link in the email" error or "warning not activated"
            // maybe we have redirect here?
            try {
                Response<ResponseBody> response = getEventsService().getProfileHTML(getCookies(), token).execute();
                String html = response.body().string();
                if (html.equals(ServerContract.ServerProfileEntry.EMPTY_RESPONSE))
                    return null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            // TODO: 20.12.2015 figure out what kind of exception it is.
            return null;
        }
    }

    private static void setNonNull(Map<String, Object> profileValues, String tag, String value) {
        if (value == null) {
            profileValues.put(tag, "");
        } else {
            profileValues.put(tag, value);
        }
    }

    @WorkerThread
    public static void postProfileValues(Map<String, Object> postDataValues, List<String> updatedFields) {
        // fields[] => first_name
        // [opt] => UserEdit
        // [json] => 1
        // [first_name] => Kata
        postDataValues.put("fields[]", updatedFields);
        postDataValues.put("json", 1);
        postDataValues.put("opt", "UserEdit");

        Call<String> call = getEventsService().postProfile(getCookies(), postDataValues);

        try {
            Response<String> response = call.execute();
            String text = response.body();
            // Error handling
            if (text != null && text.equals(POST_SUCCESS)) {
                RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_DEFAULT_OK);
            } else {
                // Server return something wrong.
                RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_SERVER_INVALID);
            }
        } catch (IOException e) {
            // Connection timeout or can't open output stream.
            RetrofitHelper.setErrorStatus(RetrofitHelper.EVENTS_STATUS_BAD_CONNECTION);
        }
    }

    @WorkerThread
    public static String addEventParticipant(String id_event, String id_distance, String id_package) {
        Call<ResponseBody> call = getEventsService().addParticipant(getCookies(), "ParticipantAdd", id_event, id_distance, id_package);
        try {
            Response<ResponseBody> response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void updateCookie(Response response) {
        Map<String, List<String>> headers = response.headers().toMultimap();
        List<String> cookiesHeader = headers.get(COOKIE_HEADER_SET);
        if (cookiesHeader != null) {
            for (String header : cookiesHeader) {
                String cookie = header.split(";")[0];
                String label = cookie.split("=")[0];
                if (label.equals(PHPSESSID_LABEL)) {
                    sPHPSESSID = cookie;
                }
                if (label.equals(USERID_LABEL)) {
                    sUSERID = cookie;
                }
                if (label.equals(HASH_LABEL)) {
                    sHASH = cookie;
                }
            }
        }
    }

    public static String getCookiesString() {
        return (new StringBuilder(sPHPSESSID)).append(";")
                .append(sUSERID).append(";")
                .append(sHASH)
                .toString();
    }

    public static List<String> getCookies() {
        return new ArrayList<>(Arrays.asList(sPHPSESSID, sUSERID, sHASH));
    }

    public static void resetCookie() {
        sUSERID = null;
        sPHPSESSID = null;
    }
}
