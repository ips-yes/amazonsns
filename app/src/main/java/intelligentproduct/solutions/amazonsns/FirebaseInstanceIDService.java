package intelligentproduct.solutions.amazonsns;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by mariettam on 9/15/17.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Amazon SNS Test", "Refreshed Firebase token: " + refreshedToken);
    }
}
