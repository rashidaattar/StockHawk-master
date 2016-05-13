package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.widget.WidgetRemoteViewFactory;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;
  private WidgetRemoteViewFactory widgetRemoteViewFactory;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
    widgetRemoteViewFactory=new WidgetRemoteViewFactory(mContext);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    final View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));
    int sdk = Build.VERSION.SDK_INT;
    if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1){
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }else {
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    } else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      } else{
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    if (Utils.showPercent){
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
    } else{
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
    }
  }

  @Override
  public void onItemDismiss(final int position, final RecyclerView rv) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    final RecyclerView rv1=rv;
     final String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    final String percentChange=c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
    final String change=c.getString(c.getColumnIndex(QuoteColumns.CHANGE));
    final String bidprice=c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE));
//    final String created=c.getString(c.getColumnIndex(QuoteColumns.CREATED));
    final String isup=c.getString(c.getColumnIndex(QuoteColumns.ISUP));
//    final int isCurrent=c.getInt(c.getColumnIndex(QuoteColumns.ISCURRENT));
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
    Snackbar snackbar = Snackbar
            .make(rv, "Stock is deleted", Snackbar.LENGTH_LONG);
    snackbar.setAction("UNDO", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(QuoteColumns.PERCENT_CHANGE,percentChange);
        contentValues.put(QuoteColumns.CHANGE,change);
        contentValues.put(QuoteColumns.BIDPRICE,bidprice);
        contentValues.put(QuoteColumns.SYMBOL,symbol);
       // contentValues.put(QuoteColumns.CREATED,created);
        contentValues.put(QuoteColumns.ISCURRENT,1);
        contentValues.put(QuoteColumns.ISUP,isup);
        //contentValues.put(QuoteColumns._ID, position);
        mContext.getContentResolver().insert(QuoteProvider.Quotes.CONTENT_URI, contentValues);
        notifyDataSetChanged();
        widgetRemoteViewFactory.onDataSetChanged();
        Snackbar snackbar1 = Snackbar
                .make(rv1, "Stock is restored", Snackbar.LENGTH_SHORT);
        snackbar1.show();
      }
    }).show();

  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {

    }
  }


}
