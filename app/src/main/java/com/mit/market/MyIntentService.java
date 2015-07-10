package com.mit.market;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.mit.impl.ImplAgent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    private static final String ORIGIONAL_INTENT = "origional_intent";

    public static void startByOriginIntent(Context context, Intent origional) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(origional.getAction());
        intent.putExtra(ORIGIONAL_INTENT, origional);
        context.startService(intent);
    }

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent origionalIntent = intent.getParcelableExtra(ORIGIONAL_INTENT);
            if (null != origionalIntent){
                ImplAgent.onReceive(this,origionalIntent);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
