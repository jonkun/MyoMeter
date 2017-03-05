package lt.joku.runnandrest;

import android.app.Application;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.Printer;

import lt.joku.runnandrest.service.MyAndroidPrinter;

/**
 * @author Jonas Kundra
 * @since 0.1
 */
public class RunAndRestApp extends Application {

    private static final String LOG_TG = "#APP";

    @Override
    public void onCreate() {
        super.onCreate();
        initXLog();
    }

    public static void initXLog() {
        LogConfiguration logConfiguration = new LogConfiguration.Builder()
                .logLevel(BuildConfig.BUILD_TYPE.toLowerCase().contains("debug") ? LogLevel.ALL : LogLevel.NONE)
                .tag(LOG_TG)    // Specify TAG, default: "X-LOG"
                .nt()           // Disable thread info
                .st(1)          // Enable stack trace info with depth 1, disabled by default
                .nb()           // Disable border
                .build();
        Printer androidPrinter = new MyAndroidPrinter();
        XLog.init(logConfiguration, androidPrinter);
    }
}


