package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;

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
    //private LineChart lineChart;
    private Context context;
    private String symbol="";
    private List<HistoricalQuote> historicalQuotes = new ArrayList<>();
    private ArrayList<Entry> entriesHigh = new ArrayList<>();
    private ArrayList<Entry> entriesLow = new ArrayList<>();
    private ArrayList<String> labels=new ArrayList<>();
    private ArrayList<String> stockElements=new ArrayList<>();
    @BindView(R.id.chart)LineChart lineChart;
    @BindView(R.id.main_toolbar)Toolbar toolbar;
    @BindView(R.id.currency_text)TextView currencyText;
    @BindView(R.id.name_text) TextView nameText;
    @BindView(R.id.stockExchange_text)TextView stockExText;
    @BindView(R.id.symbol_text)TextView symbolText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_stock_history);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_navigate_before_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                finishAffinity();
                Intent intent=new Intent(context,MyStocksActivity.class);
                startActivity(intent);
            }
        });
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        if(getIntent().hasExtra(getString(R.string.symbole_label)))
            symbol=getIntent().getStringExtra(getString(R.string.symbole_label));
        if(savedInstanceState==null){
            new MyAsync().execute();
        }
        else{
            entriesHigh=savedInstanceState.getParcelableArrayList(getString(R.string.high_label));
            entriesLow=savedInstanceState.getParcelableArrayList(getString(R.string.low_label));
            labels=savedInstanceState.getStringArrayList(getString(R.string.label_tag));
            stockElements=savedInstanceState.getStringArrayList(getString(R.string.extrastock_tag));
            symbol=savedInstanceState.getString(getString(R.string.symbole_label));
            populateChart(entriesHigh,entriesLow,labels);
        }

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
                stockElements.add(0,stock.getSymbol());
                stockElements.add(1,stock.getName());
                stockElements.add(2,stock.getCurrency());
                stockElements.add(3,stock.getStockExchange());
               // Log.d("test","size is "+historicalQuotes.size());
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

    private void plotgraph() {

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        for(int i=0;i<historicalQuotes.size();i++)
        {
            HistoricalQuote historicalQuote=historicalQuotes.get(i);
            Entry entryHigh=new Entry(historicalQuote.getHigh().floatValue(),i);
            Entry entryLow=new Entry(historicalQuote.getLow().floatValue(),i);
            entriesHigh.add(entryHigh);
            entriesLow.add(entryLow);
            labels.add(simpleDateFormat.format(historicalQuote.getDate().getTime()));
        }
        populateChart(entriesHigh,entriesLow,labels);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if(entriesHigh!=null){
            outState.putParcelableArrayList(getString(R.string.high_label),entriesHigh);
        }
       if(entriesLow!=null){
           outState.putParcelableArrayList(getString(R.string.low_label),entriesLow);
       }
       if(labels!=null){
           outState.putStringArrayList(getString(R.string.label_tag),labels);
       }

        if(stockElements!=null){
            outState.putStringArrayList(getString(R.string.extrastock_tag),stockElements);
        }
        if(symbol!=null){
            outState.putString(getString(R.string.symbole_label),symbol);
        }
        super.onSaveInstanceState(outState);
    }

    public void populateChart( ArrayList<Entry> entriesHigh,ArrayList<Entry> entriesLow,ArrayList<String> labels ){
        LineDataSet datasetHigh = new LineDataSet(entriesHigh, getString(R.string.high_label));
        LineDataSet datasetLow=new LineDataSet(entriesLow,getString(R.string.low_label));
        datasetHigh.setValueTextColor(context.getResources().getColor(R.color.material_green_700));
        datasetHigh.setColor(context.getResources().getColor(R.color.material_green_700));
        datasetLow.setValueTextColor(context.getResources().getColor(R.color.material_red_700));
        datasetLow.setColor(context.getResources().getColor(R.color.material_red_700));
        List<LineDataSet> lineDataSets=new ArrayList<>();
        lineDataSets.add(datasetHigh);
        lineDataSets.add(datasetLow);
        LineData data = new LineData(labels, lineDataSets);
       // data.setValueTextColor(context.getResources().getColor(R.color.material_red_700));
        lineChart.setData(data);
        lineChart.setDescription(symbol);
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.fitScreen();
        symbolText.setText(stockElements.get(0));
        symbolText.setContentDescription(String.format(getString(R.string.symbol_description),stockElements.get(0)));
        nameText.setText(stockElements.get(1));
        nameText.setContentDescription(String.format(getString(R.string.name_description),stockElements.get(1)));
        currencyText.setText(stockElements.get(2));
        currencyText.setContentDescription(String.format(getString(R.string.currency_description),stockElements.get(2)));
        stockExText.setText(stockElements.get(3));
        stockExText.setContentDescription(String.format(getString(R.string.exchange_description),stockElements.get(3)));
    }
}

