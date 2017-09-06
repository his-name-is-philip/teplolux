package ru.obrazcenter.teplolux;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

class MyDividerItemDecoration extends DividerItemDecoration {

    public MyDividerItemDecoration(Context context) {
        super(context, VERTICAL);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }
}
