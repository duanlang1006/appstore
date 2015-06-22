package com.android.applite.model;

import android.content.Intent;

public interface ImplListener {
    public void onFinish(boolean success,String cmd,Intent result);
}
