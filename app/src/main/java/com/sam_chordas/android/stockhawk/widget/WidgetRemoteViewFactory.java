package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;



/**
 * Created by Rashida on 12-05-2016.
 */
public class WidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor mCursor;
    private Context mContext;

    public WidgetRemoteViewFactory(Context context){
        mContext = context;
    }
    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
       mCursor= mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                QuoteColumns.PROJECTION_ALL, QuoteColumns.ISCURRENT + " LIKE ?",
                new String[]{mContext.getString(R.string.is_current_check)}, null);
        for(int i=0;i<mCursor.getCount();i++){
            getCount();
            getViewAt(i);
        }

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        mCursor.moveToPosition(position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_layout);
        rv.setTextViewText(R.id.stock_symbol,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
        rv.setTextViewText(R.id.bid_price,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
        rv.setTextViewText(R.id.change,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(mContext.getString(R.string.symbole_label),mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
        rv.setOnClickFillInIntent(R.id.stock_symbol, fillInIntent);

        return rv;


    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
