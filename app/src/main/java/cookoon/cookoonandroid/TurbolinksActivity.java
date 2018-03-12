package cookoon.cookoonandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksView;

// import android.util.Log;

public class TurbolinksActivity extends AppCompatActivity implements TurbolinksAdapter {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String INTENT_URL = "intentUrl";

    private String location;
    private TurbolinksView turbolinksView;

    private ValueCallback<Uri[]> mFilePathCallback;
    private final int REQUEST_SELECT_FILE = 1001;
    private boolean onSelectFileCallback = false;

    class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE);

            return true;
        }
    }


    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);
        // TurbolinksSession.getDefault(this).setScreenshotsEnabled(false);
        TurbolinksSession.getDefault(this).setPullToRefreshEnabled(false);

        WebView webview = TurbolinksSession.getDefault(this).getWebView();
        webview.setWebChromeClient(new WebChromeClient());

        WebSettings settings = webview.getSettings();
        String defaultUserAgent = WebSettings.getDefaultUserAgent(this);
        settings.setUserAgentString(defaultUserAgent.concat(" Cookoon Inside Android; wv"));

        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        location = getIntent().getStringExtra(INTENT_URL);

        TurbolinksSession.getDefault(this)
                         .activity(this)
                         .adapter(this)
                         .view(turbolinksView)
                         .visit(location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    mFilePathCallback.onReceiveValue(new Uri[] { data.getData() });
                } else {
                    mFilePathCallback.onReceiveValue(null);
                }
                onSelectFileCallback = true;
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (onSelectFileCallback) {
            onSelectFileCallback = false;
        }
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
    }

    @Override
    public void visitCompleted() {

    }

    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        Intent intent;

        Uri uri = Uri.parse(location);

        if (location.startsWith(BASE_URL)) {
            intent = new Intent(this, TurbolinksActivity.class);
            intent.putExtra(INTENT_URL, location);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
        }

        this.startActivity(intent);
    }
}
