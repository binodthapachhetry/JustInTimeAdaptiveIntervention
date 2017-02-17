package edu.neu.mhealth.android.wockets.library.support;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/**
 * Created by qutang on 2/1/17.
 */

public class HtmlTagger {
    public static Spanned convertStringToHtmlText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        }else{
            return Html.fromHtml(text);
        }
    }
}
