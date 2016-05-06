package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
      //jsonObject = new JSONObject(JSON);
      if (stocks != null && stocks.size() != 0){
        for (Map.Entry<String,Stock> entry : stocks.entrySet())
        {
          Stock stock=(Stock)entry.getValue();
          if(stock.getQuote()!=null)
          batchOperations.add(buildBatchOperation(stock));
        }
      }
    } catch (Exception e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(Stock stock){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = stock.getQuote().getChange().toString();
      builder.withValue(QuoteColumns.SYMBOL, stock.getSymbol());
      builder.withValue(QuoteColumns.BIDPRICE, stock.getQuote().getBid().toString());
      builder.withValue(QuoteColumns.PERCENT_CHANGE,stock.getQuote().getChangeInPercent().toString() /*truncateChange(
         stock.getQuote().getChangeInPercent().toString(), true)*/);
      builder.withValue(QuoteColumns.CHANGE,change /*truncateChange(change, false)*/);
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
  public boolean isDataAvailable(String json){
    JSONObject jsonObject = null;
    try
    {
      jsonObject=new JSONObject(json);


    } catch (JSONException e) {
      e.printStackTrace();
    }


    return false;
  }
}
