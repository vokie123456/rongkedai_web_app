package com.rongkedai;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.ImageButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.rongkedai.misc.Urls;

public class WebViewActivity extends Activity
{
    @InjectView(R.id.webview)
    WebView mWebView;

    @InjectView(R.id.btnRefresh)
    ImageButton btnRefresh;

    @InjectView(R.id.btnSignIn)
    ImageButton btnSignIn;

    @InjectView(R.id.btnNotice)
    ImageButton btnNotice;

    @InjectView(R.id.btnBorrow)
    ImageButton btnBorrow;

    @InjectView(R.id.btnAccount)
    ImageButton btnAccount;

    private static final String LOG_TAG="WebViewActivity";

    private final WebViewClient mWebViewClient=new WebViewClient()
    {
        // 处理页面导航
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String url)
        {
            Log.d(LOG_TAG,"Loading url="+url);
            mWebView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view,String url)
        {
            WebViewActivity.this.setProgressBarIndeterminateVisibility(false);
            super.onPageFinished(view,url);
            String javascript="javascript:document.getElementsByTagName('footer')[0].style.display = 'none';javascript:document.getElementsByClassName('header')[0].style.display = 'none';javascript:document.getElementsByClassName('banner')[0].style.display = 'none';void(0)";
            view.loadUrl(javascript);
        }

        @Override
        public void onPageStarted(WebView view,String url,Bitmap favicon)
        {
            WebViewActivity.this.setProgressBarIndeterminateVisibility(true);
            super.onPageStarted(view,url,favicon);
        }
    };

    // 浏览网页历史记录
    // goBack()和goForward()
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event)
    {
        if((keyCode==KeyEvent.KEYCODE_BACK)&&mWebView.canGoBack())
        {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode,event);
    }

    private WebChromeClient mChromeClient=new WebChromeClient()
    {
        private View myView=null;

        private CustomViewCallback myCallback=null;

        // 配置权限 （在WebChromeClinet中实现）
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback)
        {
            callback.invoke(origin,true,false);
            super.onGeolocationPermissionsShowPrompt(origin,callback);
        }

        // 扩充数据库的容量（在WebChromeClinet中实现）
        @Override
        public void onExceededDatabaseQuota(String url,String databaseIdentifier,long currentQuota,long estimatedSize,
                long totalUsedQuota,WebStorage.QuotaUpdater quotaUpdater)
        {

            quotaUpdater.updateQuota(estimatedSize*2);
        }

        // 扩充缓存的容量
        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded,long totalUsedQuota,WebStorage.QuotaUpdater quotaUpdater)
        {

            quotaUpdater.updateQuota(spaceNeeded*2);
        }

        // Android 使WebView支持HTML5 Video（全屏）播放的方法
        @Override
        public void onShowCustomView(View view,CustomViewCallback callback)
        {
            if(myCallback!=null)
            {
                myCallback.onCustomViewHidden();
                myCallback=null;
                return;
            }

            ViewGroup parent=(ViewGroup)mWebView.getParent();
            parent.removeView(mWebView);
            parent.addView(view);
            myView=view;
            myCallback=callback;
            mChromeClient=this;
        }

        @Override
        public void onHideCustomView()
        {
            if(myView!=null)
            {
                if(myCallback!=null)
                {
                    myCallback.onCustomViewHidden();
                    myCallback=null;
                }

                ViewGroup parent=(ViewGroup)myView.getParent();
                parent.removeView(myView);
                parent.addView(mWebView);
                myView=null;
            }
        }

        // 当WebView进度改变时更新窗口进度
        @Override
        public void onProgressChanged(WebView view,int newProgress)
        {
            // Activity的进度范围在0到10000之间,所以这里要乘以100
            // WebViewActivity.this.setProgress(newProgress*100);
        }

        /*
         * public boolean onJsAlert(WebView wv,String url,String
         * message,JsResult result) {
         * Toast.makeText(wv.getContext(),message,Toast.LENGTH_SHORT).show();
         * result.confirm(); return true; }
         */
        @Override
        public boolean onJsAlert(WebView view,String url,String message,JsResult result)
        {
            Log.d(LOG_TAG,String.format("WebView JsAlert message = %s",url,message));
            return false;
        }

        // public boolean onConsoleMessage(ConsoleMessage consoleMsg)
        // {
        // StringBuilder msg=new
        // StringBuilder(consoleMsg.messageLevel().name()).append('\t')
        // .append(consoleMsg.message()).append('\t').append(consoleMsg.sourceId()).append(" (")
        // .append(consoleMsg.lineNumber()).append(")\n");
        // if(consoleMsg.messageLevel()==ConsoleMessage.MessageLevel.ERROR)
        // {
        // Log.e(LOG_TAG,msg.toString());
        // }else
        // {
        // Log.d(LOG_TAG,msg.toString());
        // }
        // return true;
        // }
    };

    private void initSettings()
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // // 全屏
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.webview);
        ButterKnife.inject(this);

        WebSettings webSettings=mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 启用localStorage 和 SessionStorage
        webSettings.setDomStorageEnabled(true);
        // 开启应用程序缓存
        webSettings.setAppCacheEnabled(true);
        String appCacheDir=this.getApplicationContext().getDir("cache",Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCacheDir);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheMaxSize(1024*1024*10);// 设置缓冲大小，我设的是10M
        webSettings.setAllowFileAccess(false);
        // 启用Webdatabase数据库
        webSettings.setDatabaseEnabled(true);
        String databaseDir=this.getApplicationContext().getDir("database",Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(databaseDir);// 设置数据库路径
        webSettings.setGeolocationEnabled(true); // 启用地理定位
        // 设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(databaseDir);
        // 开启插件（对flash的支持）
        // webSettings.setPluginsEnabled(true);
        //webSettings.setRenderPriority(RenderPriority.HIGH);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 缩放支持
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setDefaultZoom(ZoomDensity.FAR);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        mWebView.setWebChromeClient(mChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        // mWebView.addJavascriptInterface(new UAJscriptHandler(this),"bms");

        btnRefresh.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mWebView.reload();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mWebView.loadUrl(Urls.SIGNIN_WEB);
            }
        });

        btnNotice.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mWebView.loadUrl(Urls.PROJECT_NOTICE_WEB);
                btnNotice.setBackgroundColor(Color.GREEN);
            }
        });

        btnBorrow.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mWebView.loadUrl(Urls.PROJECT_LIST_WEB);
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mWebView.loadUrl(Urls.ACCOUNT_WEB);
            }
        });

    }

    @OnClick(R.id.btnHome)
    public void goHome(View view) {
        //mWebView.loadUrl("https://www.rongkedai.com/wapborrow/nav.jhtml");
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.initSettings();
        String url=getIntent().getStringExtra("url");
        //String url="https://www.rongkedai.com/wapborrow/nav.jhtml";// intent.getStringExtra("url");

        mWebView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //add top-left icon click event deal
        switch(item.getItemId()){
        case android.R.id.home:
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}