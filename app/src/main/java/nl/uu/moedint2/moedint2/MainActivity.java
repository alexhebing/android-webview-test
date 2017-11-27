package nl.uu.moedint2.moedint2;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;

import static nl.uu.moedint2.moedint2.R.id.webview;

public class MainActivity extends AppCompatActivity {
    private int VOICE_RECOGNITION_REQUEST_CODE = 83;
    private String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        initWebView();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start met spreken..");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    private void initWebView() {
        WebView webView = (WebView)findViewById(webview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavascriptInterface(), "androidAppInterface");

        MyWebViewClient webViewClient = new MyWebViewClient();
        webView.setWebViewClient(webViewClient);

        webView.loadUrl("http://192.168.2.4:4200");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            return false;
        }
    }

    private class JavascriptInterface {

        private String onTextCreatedFromSpeechTemplate =
                "window.angularComponentRef.zone.run(window.angularComponentRef.component.onTextCreatedFromSpeech('%s'))";

        @android.webkit.JavascriptInterface
        public void collectTextFromSpeech() {
            startVoiceRecognition();
        }
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String bestMatch = matches.get(0);

            JavascriptInterface jsi = new JavascriptInterface();
            String javascriptCall = String.format(jsi.onTextCreatedFromSpeechTemplate, bestMatch);

            WebView webView = (WebView)findViewById(webview);
            webView.evaluateJavascript(javascriptCall, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    // console output (i.e. the javascript console output) is automatically logged.
                }
            });

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
