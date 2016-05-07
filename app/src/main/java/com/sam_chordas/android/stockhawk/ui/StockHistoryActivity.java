package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class StockHistoryActivity extends AppCompatActivity {
    private LineChart lineChart;
    private Context context;
    private String symbol="";
    private List<HistoricalQuote> historicalQuotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_stock_history);
        lineChart= (LineChart) findViewById(R.id.chart);
        if(getIntent().hasExtra("stocksymbol"))
            symbol=getIntent().getStringExtra("stocksymbol");
        new MyAsync().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class MyAsync extends AsyncTask{
        ProgressDialog progressBar=new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar.setMessage("Loading data. Please Wait..");
            progressBar.setCancelable(false);
            progressBar.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Calendar from=Calendar.getInstance();
                from.add(Calendar.YEAR, -1);
                Calendar to=Calendar.getInstance();
                Stock stock= YahooFinance.get(symbol, from, to, Interval.MONTHLY);
                historicalQuotes=stock.getHistory();
                Log.d("test","size is "+historicalQuotes.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressBar.dismiss();
            if(historicalQuotes!=null)
                plotgraph();
        }
    }

    private LineData plotgraph() {

        ArrayList<Entry> entriesHigh = new ArrayList<>();
        ArrayList<Entry> entriesLow = new ArrayList<>();
        ArrayList<String> labels=new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        ArrayList<List<Entry>> lists=new ArrayList<>();
        for(int i=0;i<historicalQuotes.size();i++)
        {
            HistoricalQuote historicalQuote=historicalQuotes.get(i);
            Entry entryHigh=new Entry(historicalQuote.getHigh().floatValue(),i);
            Entry entryLow=new Entry(historicalQuote.getLow().floatValue(),i);
            entriesHigh.add(entryHigh);
            entriesLow.add(entryLow);
            labels.add(simpleDateFormat.format(historicalQuote.getDate().getTime()));
        }
        LineDataSet datasetHigh = new LineDataSet(entriesHigh, "High");
        LineDataSet datasetLow=new LineDataSet(entriesLow,"Low");
        List<LineDataSet> lineDataSets=new ArrayList<>();
        lineDataSets.add(datasetHigh);
        lineDataSets.add(datasetLow);
        LineData data = new LineData(labels, lineDataSets);
        data.setValueTextColor(context.getResources().getColor(R.color.material_red_700));
        lineChart.setData(data);
        lineChart.setDescription("Stocks");
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.setBackgroundColor(context.getResources().getColor(R.color.material_blue_500));
        lineChart.fitScreen();
        return data;
    }
}

