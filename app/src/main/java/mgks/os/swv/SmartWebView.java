package mgks.os.swv;

/*
  Smart WebView v7
  https://github.com/mgks/Android-SmartWebView

  A modern, open-source WebView wrapper for building advanced hybrid Android apps.
  Native features, modular plugins, and full customisation—built for developers.

  - Documentation: https://docs.mgks.dev/smart-webview  
  - Plugins: https://docs.mgks.dev/smart-webview/plugins  
  - Discussions: https://github.com/mgks/Android-SmartWebView/discussions  
  - Sponsor the Project: https://github.com/sponsors/mgks  

  MIT License — https://opensource.org/licenses/MIT  

  Mentioning Smart WebView in your project helps others find it and keeps the dev loop alive.
*/

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration and utility class for Smart WebView
 * Contains all the configuration variables and shared objects between activities
 */
public class SmartWebView {

    // ===============================================================================================
    // CORE CONFIGURATION
    // ===============================================================================================
    
    // Debug options
    public static boolean SWV_DEBUGMODE = true;  // Enable for detailed logs and toast alerts
    
    // Version information
    public static String ASWV_VERSION = "7.1";
    
    // ===============================================================================================
    // URL CONFIGURATION
    // ===============================================================================================
    
    // URL configurations
    public static String ASWV_APP_URL = "https://mgks.github.io/Android-SmartWebView/";
    public static String ASWV_OFFLINE_URL = "file:///android_asset/offline.html";
    public static String ASWV_SEARCH = "https://www.google.com/search?q=";
    
    // Determine app URL based on offline status
    public static String ASWV_URL;
    public static String ASWV_SHARE_URL;
    public static String ASWV_HOST;
    public static String CURR_URL;
    
    // External URL handling
    public static String ASWV_EXC_LIST = "mgks.dev,mgks.github.io,github.com";  // Comma-separated domains
    
    // ===============================================================================================
    // FEATURE FLAGS
    // ===============================================================================================
    
    // Core features
    public static boolean ASWP_OFFLINE;       // True if app loads from local file or no internet
    public static boolean ASWP_FUPLOAD = true;     // Upload file from webview
    public static boolean ASWP_CAMUPLOAD = true;   // Enable upload from camera for photos
    public static boolean ASWP_MULFILE = true;     // Upload multiple files in webview
    public static boolean ASWP_LOCATION = true;    // Track GPS locations
    public static boolean ASWP_COPYPASTE = false;  // Enable copy/paste within webview
    public static boolean ASWP_RATINGS = true;     // Show ratings dialog
    public static boolean ASWP_PULLFRESH = true;   // Pull refresh current url
    public static boolean ASWP_PBAR = true;        // Show progress bar in app
    public static boolean ASWP_ZOOM = false;       // Zoom control for webpages view
    public static boolean ASWP_SFORM = false;      // Save form cache and auto-fill
    public static boolean ASWP_EXTURL = true;      // Open external url with default browser
    public static boolean ASWP_TAB = true;         // Use Chrome tabs for external URLs
    public static boolean ASWP_EXITDIAL = true;    // Confirm exit on back press
    
    // Security options
    public static boolean ASWP_CERT_VERI = true;   // Verify SSL certificate (recommended)
    
    // Layout and display
    public static int ASWV_ORIENTATION = 0;        // 0: unspecified, 1: portrait, 2: landscape
    public static int ASWV_LAYOUT = 0;             // 0: fullscreen, 1: drawer layout
    
    // User agent configuration
    public static boolean POSTFIX_USER_AGENT = true;
    public static boolean OVERRIDE_USER_AGENT = false;
    public static String USER_AGENT_POSTFIX = "SWVAndroid";
    public static String CUSTOM_USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36";
    
    // File upload configuration
    public static String ASWV_F_TYPE = "*/*";      // Upload file type (use "*/*" for any file)
    
    // Analytics
    public static String ASWV_GTAG = "G-7XXC1C7CRQ";   // Analytics ID
    
    // ===============================================================================================
    // RATING CONFIGURATION
    // ===============================================================================================
    
    static int ASWR_DAYS = 3;         // Days before showing the dialog
    static int ASWR_TIMES = 10;       // Launch times before showing
    static int ASWR_INTERVAL = 2;     // Days interval for reminders
    
    // ===============================================================================================
    // INTERNAL STATE VARIABLES
    // ===============================================================================================
    
    // Shared UI components
    static WebView asw_view;
    static WebView print_view;
    static CookieManager cookie_manager;
    static ProgressBar asw_progress;
    static TextView asw_loading_text;
    static NotificationManager asw_notification;
    static Notification asw_notification_new;
    static ValueCallback<Uri[]> asw_file_path;
    
    // Permission request codes
    static int loc_perm = 1;
    static int file_perm = 2;
    static int cam_perm = 3;
    static int noti_perm = 4;
    
    // State tracking
    static String fcm_token;
    static String asw_pcam_message;
    static String asw_vcam_message;
    static String asw_fcm_channel = "1";
    static int ASWV_FCM_ID = (int) System.currentTimeMillis();
    static int asw_error_counter = 0;
    static boolean true_online = !ASWP_OFFLINE;
    
    // ===============================================================================================
    // INITIALIZATION MANAGEMENT
    // ===============================================================================================
    
    private static Context appContext;
    private static PluginManager pluginManagerInstance;
    private static boolean isInitialized = false;
    private static List<Runnable> initCallbacks = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public SmartWebView() {
        // Empty constructor
    }
    
    /**
     * Set the application context
     * @param context Application context
     */
    public static void setAppContext(Context context) {
        appContext = context.getApplicationContext();
        
        // Initialize URL configuration after context is set
        ASWP_OFFLINE = ASWV_APP_URL.matches("^(file)://.*$") && Functions.isInternetAvailable(appContext);
        ASWV_URL = ASWP_OFFLINE ? ASWV_OFFLINE_URL : ASWV_APP_URL;
        ASWV_SHARE_URL = ASWV_URL + "?share=";
        ASWV_HOST = Functions.aswm_host(ASWV_URL);
        CURR_URL = ASWV_URL;
    }
    
    /**
     * Get the application context
     * @return Application context
     */
    public static Context getAppContext() {
        return appContext;
    }
    
    /**
     * Get the plugin manager instance (singleton)
     * @return PluginManager instance
     */
    public static synchronized PluginManager getPluginManager() {
        if (pluginManagerInstance == null) {
            pluginManagerInstance = new PluginManager();
        }
        return pluginManagerInstance;
    }
    
    /**
     * Initialize Smart WebView with required components
     * @param activity Activity instance
     * @param webView WebView instance
     * @param functions Functions instance
     */
    public static void init(Activity activity, WebView webView, Functions functions) {
        getPluginManager().setContext(activity, webView, functions);
        initializePlugins();
    }
    
    /**
     * Initialize plugins and trigger callbacks
     */
    public static void initializePlugins() {
        if (!isInitialized) {
            isInitialized = true;
            for (Runnable callback : initCallbacks) {
                callback.run();
            }
            initCallbacks.clear();
        }
    }
    
    /**
     * Register callback for plugin initialization
     * @param callback Callback to run after initialization
     */
    public static void onPluginsInitialized(Runnable callback) {
        if (isInitialized) {
            callback.run();
        } else {
            initCallbacks.add(callback);
        }
    }
}
