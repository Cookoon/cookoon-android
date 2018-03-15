package cookoon.cookoonandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

// import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final String INTENT_URL = "intentUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCenter.start(getApplication(), BuildConfig.APP_CENTER_SECRET,
                        Analytics.class, Crashes.class);

        Intent intent = new Intent(MainActivity.this, TurbolinksActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        intent.putExtra(INTENT_URL, BASE_URL);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                Intent intent = new Intent(MainActivity.this, TurbolinksActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                if (error == null) {
                    String deeplinkPath = referringParams.optString("$deeplink_path", "");
                    String nonBranchLink = referringParams.optString("+non_branch_link", "");

                    if (!deeplinkPath.equals("") && deeplinkPath.startsWith(BASE_URL)) {
                        intent.putExtra(INTENT_URL, deeplinkPath);
                        startActivity(intent);
                    } else if (!nonBranchLink.equals("")){
                        intent.putExtra(INTENT_URL, nonBranchLink);
                        startActivity(intent);
                    }
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
