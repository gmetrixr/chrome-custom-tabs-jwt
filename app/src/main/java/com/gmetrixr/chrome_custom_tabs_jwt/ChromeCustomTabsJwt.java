package com.gmetrixr.chrome_custom_tabs_jwt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class ChromeCustomTabsJwt implements ServiceConnectionCallback {
  private static final String TAG = "chromecustomtabsjwt";
  final long SECOND = 1000;
  private String url = "";
  private String viewerIdentifier = "";
  private String apiKey = "";
  private CustomTabsSession customTabsSession;
  private Context context;

  void initialize(String viewerIdentifier, String apiKey, String url, Context c) {
    this.url = url;
    this.context = c;
    this.viewerIdentifier = viewerIdentifier;
    this.apiKey = apiKey;
    this.setupServiceConnectionAndWarmup();
  }

  private void setupServiceConnectionAndWarmup() {
    ServiceConnectionJWT serviceConnection = new ServiceConnectionJWT(this);
    CustomTabsClient.bindCustomTabsService(context, "com.android.chrome", serviceConnection);
    try {
      Date date = new Date(); //now
      date.setTime(date.getTime() + (1 * SECOND));

      assert this.viewerIdentifier != null;
      JSONObject obj = new JSONObject(this.viewerIdentifier);
      obj.put("exp", date.getTime());
      assert this.apiKey != null;
      Key key = Keys.hmacShaKeyFor(this.apiKey.getBytes());
      String jwt = Jwts.builder().setPayload(obj.toString()).signWith(key).compact();
      this.url += "?token=" + jwt;
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
  /**
   * Warm up and fire the ChromeCustomTab with the specified url
   *
   * @param client a CustomTabsClient
   */
  @Override
  public void onServiceConnected(CustomTabsClient client) {
    client.warmup(0);
    customTabsSession = client.newSession(new NavigationCallback());
    customTabsSession.mayLaunchUrl(Uri.parse(url), null, null);
  }

  void launch() {
    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder(customTabsSession);
    intentBuilder.enableUrlBarHiding();
    intentBuilder.setShowTitle(false);
    intentBuilder.setInstantAppsEnabled(false);

    CustomTabsIntent customTabsIntent = intentBuilder.build();
    customTabsIntent.launchUrl(context, Uri.parse(url));
    // add this app as the referrer for tracking purpose
    customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getApplicationContext().getPackageName()));
  }

  @Override
  public void onServiceDisconnected() { }

  /**
   * Logger for debugging the customTabs navigation
   */
  private static class NavigationCallback extends CustomTabsCallback {
    @Override
    public void onNavigationEvent(int navigationEvent, Bundle extras) {
      Log.w(TAG, "onNavigationEvent: Code = " + navigationEvent);
    }
  }
}

/**
 * Implementation for the CustomTabsServiceConnection that avoids leaking the
 * ServiceConnectionCallback
 */
class ServiceConnectionJWT extends CustomTabsServiceConnection {
  // A weak reference to the ServiceConnectionCallback to avoid leaking it.
  private WeakReference<ServiceConnectionCallback> mConnectionCallback;

  ServiceConnectionJWT(ServiceConnectionCallback connectionCallback) {
    mConnectionCallback = new WeakReference<>(connectionCallback);
  }

  @Override
  public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
    System.out.println("SERVICE CONNECTED");
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) connectionCallback.onServiceConnected(client);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    System.out.println("SERVICE DISCONNECTED");
    ServiceConnectionCallback connectionCallback = mConnectionCallback.get();
    if (connectionCallback != null) connectionCallback.onServiceDisconnected();
  }
}