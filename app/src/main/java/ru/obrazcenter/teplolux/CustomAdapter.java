package ru.obrazcenter.teplolux;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import static android.view.View.GONE;

class CustomAdapter<CharSequence> extends ArrayAdapter<CharSequence>{
    private final CharSequence[] objects;

    CustomAdapter(Context cntxt, int resource, CharSequence[] objects) {
        super(cntxt, resource, objects);
        this.objects = objects;
    }
//    @NonNull
//    public static CustomAdapter<CharSequence> createFromResource(@NonNull Context mainActivity,
//                                                                 @ArrayRes int txtArrResId, @LayoutRes int txtViewResId) {
//        final CharSequence[] txtArr = mainActivity.getResources().getTextArray(txtArrResId);
//        return new CustomAdapter<>(mainActivity, txtViewResId, txtArr);
//    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup p) {
        if (pos != objects.length-1)
            return super.getView(pos, convertView, p);
        TextView tv = (TextView) super.getView(pos, convertView, p);
        tv.setTextColor(Color.BLUE);
        return tv;
    }
//    int getWidth(ViewGroup vg) {
//        int width = super.getView(0, null, vg).getWidth();
//        if (vg.getWidth() < width)
//            return width;
//        return vg.getWidth();
//    }

    public View getDropDownView(int pos, @Nullable View convertView, @NonNull ViewGroup p) {
//        TextView tv = (TextView) super.getDropDownView(pos, convertView, p);
//        if (pos != 0) {
//            tv.setTextColor(Color.BLACK);
//            tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
//        } else {
//            tv.setTextColor(Color.MAGENTA);
//            tv.setTypeface(tv.getTypeface(), Typeface.ITALIC);
//        }
        if (pos == objects.length-1) {
            TextView tv = new TextView(getContext());
            tv.setVisibility(GONE);
            tv.setHeight(0);
            return tv;
        } else return super.getDropDownView(pos, null, p);
    }

}