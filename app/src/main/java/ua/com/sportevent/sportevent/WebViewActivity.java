package ua.com.sportevent.sportevent;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.sportevent.sportevent.helpers.BuyingInformation;
import ua.com.sportevent.sportevent.serverData.RetrofitHelper;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Open site purchase page.
 */
public class WebViewActivity  extends ErrorActivity {
    boolean emptyProfileFields = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BuyingInformation info = getIntent().getParcelableExtra(DetailActivity.EXTRA_BUYING);

        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final WebView webview = (WebView) findViewById(R.id.webview);
        ((TextView) findViewById(R.id.webview_description)).setText(getString(R.string.format_buying_info, info.getDisciplineName(), info.getPackageName()));

//        webview.addJavascriptInterface(new WebAppInterface(this), "Android");

        webview.getSettings().setJavaScriptEnabled(true);
        /*
        <input type="button" value="Say hello" onClick="close()" />
        <script type="text/javascript">
            function close() {
                Android.finish();
            }
        </script>
        */

        // To open links clicked by the user in the webview.
        webview.setWebViewClient(new WebViewClient());

        final ProgressBar pBar = (ProgressBar) findViewById(R.id.webview_pb);
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && pBar.getVisibility() == ProgressBar.GONE) {
                    pBar.setVisibility(ProgressBar.VISIBLE);
                }
                pBar.setProgress(progress);
                if (progress == 100) {
                    pBar.setVisibility(ProgressBar.GONE);
                }
            }
        });

        // TODO: 09.01.2016 check Profile fields. If OK, load the page, else show the error.
        // Add status error if empty profile fields
        emptyProfileFields = true;
        RetrofitHelper.setErrorStatus(RetrofitHelper.BUYING_STATUS_EMPTY_PROFILE);
        updateEmptyView(true);

        // Identify the user.
        CookieManager.getInstance().setCookie("http://sportevent.com.ua/", RetrofitHelper.getCookiesString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String pageHTML = RetrofitHelper.addEventParticipant(info.getEventId(), info.getDisciplineId(), info.getPackageId());
                webview.post(new Runnable() {
                    @Override
                    public void run() {
                        updateEmptyView(false);
                        if (!emptyProfileFields) {
                            webview.loadDataWithBaseURL(ServerContract.ServerEventsEntry.BASE_URL_API, pageHTML, "text/html", "utf-8", null);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_webview);
    }

    @Override
    protected boolean dataNotAvailable() {
        return emptyProfileFields;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    public class WebAppInterface {
        Activity mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Activity activity) {
            mContext = activity;
        }

        /** Finish the web page */
        @JavascriptInterface
        public void finish() {
            mContext.finish();
        }

        /** Go to the Profile */
        @JavascriptInterface
        public void gotoProfile() {
            startActivity(new Intent(mContext, ProfileActivity.class));
        }
    }
}
