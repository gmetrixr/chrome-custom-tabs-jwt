package com.gmetrixr.chrome_custom_tabs_jwt;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private ChromeCustomTabsJwt chromeCustomTabs;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setWindow();
    setContentView(R.layout.activity_main);
    this.attachEventHandlers();
    this.prepareChromeTabs();
  }

  private void setWindow() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  private void attachEventHandlers() {
    Button startCourse = (Button) findViewById(R.id.openSafeHands);
    startCourse.setOnClickListener(this);
  }

  private void prepareChromeTabs() {
    chromeCustomTabs = new ChromeCustomTabsJwt();
    JSONObject obj = new JSONObject();
    try {
      obj.put("identifier", "test@example.com");
    } catch (JSONException e) {
      e.printStackTrace();
    }

    String apiKey = "<your api secret key goes here>";
    String url = "<your experience link goes here>";
    chromeCustomTabs.initialize(obj.toString(), apiKey, url, this);
  }

  @Override
  public void onClick(View v) {
    chromeCustomTabs.launch();
  }
}
