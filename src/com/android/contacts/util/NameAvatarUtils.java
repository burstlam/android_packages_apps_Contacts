package com.android.contacts.util;

import com.android.contacts.R;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.TextView;

public class NameAvatarUtils {
    
    /**
     * Judgment for a character is chinese or not
     * @author Wang
     * @param a char
     * @return boolean
     * @date 2012-11-01
     */
    public static boolean isChinese(char a) { 
//         int v = (int)a; 
//         return (v >=19968 && v <= 171941); 
        boolean isChina =String.valueOf(a).matches("[\u4E00-\u9FA5]");
        return isChina;
    }
    
    /**
     * Judgment for the given string contain chinese or not ,and return the last chinese character (null stand for there is no chinese character in this string)
     * @author Wang
     * @param a String
     * @return the last chinese character
     * @date 2012-10-10
     */
    public static String containsChinese(String str){
        if (TextUtils.isEmpty(str)) return null;
        int length = str.length();
        for(int i = length - 1; i >= 0; i--){
            if (isChinese(str.charAt(i))) {
                String chinese = str.substring(i, i+1);
                return chinese;
            }
        }
        return null;
      }

    
    /**
     * Draw the view into a bitmap.
     * @author Wang
     * @date 2012-10-10
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.setDrawingCacheEnabled(true);
        v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    /**
     * Set the charater in the imageview.
     * @author Wang
     * @date 2012-10-29
     */
    public static void setAvatar(ImageView view, String chara){
        TextView tv = (TextView)LayoutInflater.from(view.getContext()).inflate(R.layout.shendu_name_avatar_view, null);
        tv.setText(chara);
        Bitmap bm = getViewBitmap(tv);
        view.setImageBitmap(bm);
    }

}
