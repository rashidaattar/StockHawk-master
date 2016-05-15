package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

 // private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  private String[] symbols;
  SharedPreferences sharedPreferences=null;
  SharedPreferences.Editor  editor=null;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;

  }
  /*String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }*/

  @Override
  public int onRunTask(TaskParams params){
    Cursor initQueryCursor;
    if (mContext == null){
      mContext = this;
    }
    sharedPreferences= PreferenceManager.getDefaultSharedPreferences(mContext);
    editor=sharedPreferences.edit();
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
          new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
          null, null);
      if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
        symbols=new String[]{"YHOO","AAPL","GOOG","MSFT"};
      } else if (initQueryCursor != null){
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        List<String> storedSymbolsList=new ArrayList<>();
        for (int i = 0; i < initQueryCursor.getCount(); i++){

          storedSymbolsList.add(initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")));
          initQueryCursor.moveToNext();
        }

        symbols=storedSymbolsList.toArray(new String[storedSymbolsList.size()]);
      }
    } else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");

      symbols=new String[]{stockInput};
    }

    int result = GcmNetworkManager.RESULT_FAILURE;
      try{
        /*getResponse = fetchData(urlString);
       */
        Map<String, Stock> stocks = YahooFinance.get(symbols);
        result = GcmNetworkManager.RESULT_SUCCESS;
        try {
          ContentValues contentValues = new ContentValues();
          // update ISCURRENT to 0 (false) so new data is current
          if (isUpdate){
            contentValues.put(QuoteColumns.ISCURRENT, 0);
            mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                null, null);
          }
          if(stocks.size()==1 && stocks.containsKey(params.getExtras().getString("symbol"))
                  && stocks.get(params.getExtras().getString("symbol")).getName()==null)
          {

             // Toast.makeText(mContext,"No such stock exists",Toast.LENGTH_SHORT).show();
              editor.putBoolean(mContext.getString(R.string.is_data_available),false).commit();
              Handler h = new Handler(mContext.getMainLooper());

              h.post(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(mContext,"stock not found",Toast.LENGTH_LONG).show();
                }
              });

              //new MyStocksActivity().showToast(mContext);
              return result;

          }
          else
          {
            ArrayList<ContentProviderOperation> batchOperations= Utils.quoteJsonToContentVals(stocks);
            if(batchOperations!=null && batchOperations.size()>0)
            {
              editor.putBoolean(mContext.getString(R.string.is_data_available),true).commit();
              mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,batchOperations);
            }
          }

        }catch (RemoteException | OperationApplicationException e){
          Log.e(LOG_TAG, "Error applying batch insert", e);
        }
      } catch (IOException e){
        e.printStackTrace();
      }
    return result;
  }

}
