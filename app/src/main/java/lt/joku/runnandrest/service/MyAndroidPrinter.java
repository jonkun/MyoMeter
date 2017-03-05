package lt.joku.runnandrest.service;

import com.elvishew.xlog.printer.AndroidPrinter;

public class MyAndroidPrinter extends AndroidPrinter {

    @Override
    public void println(int logLevel, String tag, String msg) {
        if (isMessageWithStackTrace(msg)) {
            msg = removePackageWithClassName(msg);
        }
        super.println(logLevel, tag, msg);
    }

    private boolean isMessageWithStackTrace(String msg) {
        return !msg.contains("╔") /* if it not contains borders */
                && msg.contains("\t─ "); /* and if it contains stacktrace info */
    }

    private String removePackageWithClassName(String msg) {
        /* Remove package with class name */
        msg = msg.substring(msg.indexOf("(")).replaceFirst("\n", " ");
        return msg;
    }

}