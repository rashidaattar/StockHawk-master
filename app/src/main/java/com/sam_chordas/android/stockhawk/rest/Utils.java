package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.Map;
import yahoofinance.Stock;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(Map<String, Stock> stocks){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

    try{
      if (stocks != null && stocks.size() != 0){
        for (Map.Entry<String,Stock> entry : stocks.entrySet())
        {
          Stock stock=(Stock)entry.getValue();
          if(stock.getQuote().getChange()!=null)
          {
            batchOperations.add(buildBatchOperation(stock));
          }

        }
      }
    } catch (Exception e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static ContentProviderOperation buildBatchOperation(Stock stock){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = stock.getQuote().getChange().toString();
      builder.withValue(QuoteColumns.SYMBOL, stock.getSymbol());
      if(stock.getQuote().getBid()!=null)
        builder.withValue(QuoteColumns.BIDPRICE, stock.getQuote().getBid().toString());
      else
        builder.withValue(QuoteColumns.BIDPRICE, " ");
      builder.withValue(QuoteColumns.PERCENT_CHANGE,stock.getQuote().getChangeInPercent().toString());
      builder.withValue(QuoteColumns.CHANGE,change);
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (Exception e){
      e.printStackTrace();
    }
    return builder.build();
  }
}
