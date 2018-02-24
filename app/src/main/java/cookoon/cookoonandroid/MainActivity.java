package cookoon.cookoonandroid;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

//Uncomment to use Logs
//import android.util.Log;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String INTENT_URL = "intentUrl";

    private Boolean mUploadingFile = false;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private String location;
    private TurbolinksView turbolinksView;
    private WebView webView;

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCenter.start(getApplication(), "8d13f10f-66d2-452c-b4d6-5a0d14f7099a",
                Analytics.class, Crashes.class);

        TurbolinksSession.getDefault(this).getWebView().getSettings().setAllowFileAccess(true);
        webView = TurbolinksSession.getDefault(this).getWebView();

        // Find the custom TurbolinksView object in your layout
        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        // TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);
        // TurbolinksSession.getDefault(this).setScreenshotsEnabled(false);
        TurbolinksSession.getDefault(this).setPullToRefreshEnabled(false);

        // Code to open library
        webView.setWebChromeClient(new WebChromeClient() {
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadingFile = true;
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadingFile = true;
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG)
                            .show();
                    return false;
                }
                return true;
            }
        });

       handleIntent(getIntent());
    }

    void handleIntent(Intent intent) {
        // Need to improve this part
        Uri appLinkData = intent.getData();
        String locationWithLink = intent.getStringExtra(INTENT_URL);
        if (appLinkData != null) {
            location = appLinkData.toString();
        }   else if (locationWithLink != null) {
            location = locationWithLink;
        } else {
            location = BASE_URL;
        }

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        if (mUploadingFile) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .view(turbolinksView);
        } else {
            // Since the webView is shared between activities, we need to tell Turbolinks
            // to load the location from the previous activity upon restarting
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(true)
                    .view(turbolinksView)
                    .visit(location);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return;
                }
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = data == null || resultCode != MainActivity.RESULT_OK
                    ? null
                    : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_LONG)
                    .show();
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

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        if(!location.contains(BASE_URL)) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(location));
            startActivity(i);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(INTENT_URL, location);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
}
