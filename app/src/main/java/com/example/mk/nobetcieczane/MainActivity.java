package com.example.mk.nobetcieczane;

import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.mk.nobetcieczane.Models.Eczane;
import com.example.mk.nobetcieczane.Models.EczaneDetay;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    String tokenText = "";
    WebView webView;
    Spinner spinner;
    Document document;
    List<EczaneDetay> eczaneList ;
    EczaneAdapter eczaneAdapter;
    ListView listView;
    Button listeleButon;
    HashMap<String, String> ilceler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView)findViewById(R.id.listview);
        webView = new WebView(getApplicationContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsBridge(), "Android");//name android yazılmalı /// başka birşey yazılmamalı


        listeleButon = (Button)findViewById(R.id.listeleButon);
        listeleButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ilceTitle = spinner.getSelectedItem().toString();
                getIlceNobetcileri(ilceTitle);
            }
        });

        ilceler = new HashMap<>();
        ilceler.put("Adalar", "en güzel ilçe");
        getilceler();
    }

    private void getIlceNobetcileri(String ilceTitle) {
        // listele butonuna basılınca o ılceye aıt sayfayı yukle ve sayfayı parse et, bulunan eczanelerı ekrana yazdır


        webView.setWebViewClient(new SSLTolerentWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.Android.htmlilceNobetcileri(" +
                        "'<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");


            }
        });
        // ela başka site deneyelim
        //http://www.aeo.org.tr/NobetModulu/Nobet?NobetTarihiAsString=10.05.2020&IlceKey=365965f0-b3e7-4bbc-ba1a-09b2f336ebd5
        //http://www.aeo.org.tr/NobetModulu/Nobet?IlceKey=365965f0-b3e7-4bbc-ba1a-09b2f336ebd5
        webView.loadUrl("http://www.aeo.org.tr/NobetModulu/Nobet?IlceKey="+ilceler.get(ilceTitle)) ;

    }

    public void loadSpinnerValues(){
        spinner = (Spinner) findViewById(R.id.ilceSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,  ilceler.keySet().toArray(new String[ilceler.size()]));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    private void getilceler() {
        webView.setWebViewClient(new SSLTolerentWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //super.onPageFinished(view, url);
                 view.loadUrl("javascript:window.Android.htmlilceler(" +
                         "'<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");


            }
        });
          webView.loadUrl("http://www.aeo.org.tr/NobetModulu/Nobet") ;
    }

    private void showNobetciler(List<String> nobetcilerlistesi) {
        List<EczaneDetay> eczaneObjeler = new ArrayList<>();
        for(String ecz: nobetcilerlistesi){
            EczaneDetay detay = new EczaneDetay();
            detay.setEczaneIsmi(ecz);
            eczaneObjeler.add(detay);
        }

        eczaneAdapter = new EczaneAdapter(eczaneObjeler, MainActivity.this, MainActivity.this);
        listView.setAdapter(eczaneAdapter);
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (msg.what == 1) {

            } else if (msg.what == 2) {

                Eczane ec = parseEczaneHtml((String) msg.obj);
                eczaneList = ec.getEczaneDetay();
                eczaneAdapter = new EczaneAdapter(eczaneList, MainActivity.this, MainActivity.this);
                listView.setAdapter(eczaneAdapter);

            } else if (msg.what == 3) {
                Log.i("what 3 ilk sayfa", msg.toString());
                document = Jsoup.parse(msg.toString());
                Elements ilceSatırHtmller = document.getElementById("IlceKey").getElementsByTag("option");
                for (Element el : ilceSatırHtmller) {
                    String ad =  el.text();
                    String url = el.attr("value");

                    ilceler.put(ad, url);
                }
                loadSpinnerValues();
            } else if (msg.what == 4){
                Log.i("what 4 ilce nobtcileri", msg.toString());
                document = Jsoup.parse(msg.toString());
                List nobetcilerlistesi = new ArrayList(){};
                Elements elemsNobetciler = document.getElementsByClass("list-group-item");

                for (Element el : elemsNobetciler) {
                    nobetcilerlistesi.add(el.text());
                }
                showNobetciler(nobetcilerlistesi);
            }
        }


    };

    private Eczane parseEczaneHtml(String htmlKaynak) {
        document = Jsoup.parse(htmlKaynak);
        Elements table = document.select("table.ilce-nobet-detay");
        Elements ilceDetay = table.select("caption>b");
        Eczane eczane = new Eczane();
        eczane.setTarih(ilceDetay.get(0).text());
        eczane.setIlceIsmi(ilceDetay.get(1).text());


        Elements eczaneDetayElemet = document.select("table.nobecti-eczane");
        List<EczaneDetay> eczaneDetayList = new ArrayList<>();
        for (Element el : eczaneDetayElemet) {

            EczaneDetay eczaneDetay = getEczaneDetay(el);

            if (eczaneDetay != null) {
                eczaneDetayList.add(eczaneDetay);
            }
        }

        eczane.setEczaneDetay(eczaneDetayList);
        Log.i("cevapppp",eczane.toString());
        return eczane;

    }

    public EczaneDetay getEczaneDetay(Element el) {
        String fax="",tel="",adres="",adresTarif="";
        EczaneDetay eczaneDetay = new EczaneDetay();
        Elements eczaneIsmıTag = el.select("thead");
        String eczaneIsmı = eczaneIsmıTag.select("div").attr("title");
        eczaneDetay.setEczaneIsmi(eczaneIsmı);

        Elements trTags = el.select("tbody>tr");
        Elements adresTags = trTags.select("tr#adres");
        adres = adresTags.select("label").get(1).text();
        eczaneDetay.setAdres(adres);


        Elements telTags = trTags.select("tr#Tel");
        tel = telTags.select("label").get(1).text();
        eczaneDetay.setTelefon(tel);

        Element faxTags = trTags.get(2);
        fax = faxTags.select("label").get(1).text();
        if(!fax.equals(""))
        {
            eczaneDetay.setFax(fax);
        }

        Element adresTarifTags = trTags.get(3);
        adresTarif = adresTarifTags.select("label").get(1).text();
        if(!adresTarif.equals(""))
        {
            eczaneDetay.setTarif(adresTarif);
        }







        return eczaneDetay;
    }



    private class SSLTolerentWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }

    }

    class JsBridge extends MainActivity {

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void htmlilceler(String str) {

            Message message = new Message();
            message.what = 3;
            message.obj = str;
            handler.sendMessage(message);
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void htmlilceNobetcileri(String str) {
            Message message = new Message();
            message.what = 4;
            message.obj = str;
            handler.sendMessage(message);
        }


    }

}
