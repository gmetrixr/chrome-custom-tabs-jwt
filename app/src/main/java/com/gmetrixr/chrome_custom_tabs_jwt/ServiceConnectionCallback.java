package com.gmetrixr.chrome_custom_tabs_jwt;

import androidx.browser.customtabs.CustomTabsClient;

/**
 * A simple interface to detect chrome service connection and disconnection
 */
public interface ServiceConnectionCallback {
  /**
   * Called when the service is connected.
   * @param client a CustomTabsClient
   */
  void onServiceConnected(CustomTabsClient client);

  /**
   * Called when the service is disconnected.
   */
  void onServiceDisconnected();
}
