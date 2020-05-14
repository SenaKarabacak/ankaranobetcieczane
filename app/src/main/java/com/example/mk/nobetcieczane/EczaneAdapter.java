package com.example.mk.nobetcieczane;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mk.nobetcieczane.Models.EczaneDetay;

import java.util.List;



public class EczaneAdapter extends BaseAdapter {
    List<EczaneDetay> list;
    Context context;
    Activity activity;

    public EczaneAdapter(List<EczaneDetay> list, Context context,Activity activity) {
        this.list = list;
        this.context = context;
        this.activity =activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.layout,parent,false);

        TextView eczaneIsim, eczaneAdres, eczaneTel, eczaneFax, eczaneAdresTarfi;
        Button aramaYap;
        ImageView eczn;
        eczaneIsim = (TextView) convertView.findViewById(R.id.eczaneIsmi);
        eczaneAdres = (TextView) convertView.findViewById(R.id.eczaneAdres);
        eczaneTel = (TextView) convertView.findViewById(R.id.eczaneTelefon);
        eczaneFax = (TextView) convertView.findViewById(R.id.eczaneFax);
        eczaneAdresTarfi = (TextView) convertView.findViewById(R.id.eczaneAdresTarifi);

        aramaYap = (Button) convertView.findViewById(R.id.aramaYapButon);
        eczaneIsim.setText(list.get(position).getEczaneIsmi());
        eczaneAdres.setText(list.get(position).getAdres());
        eczaneTel.setText(list.get(position).getTelefon());
        eczaneFax.setText(list.get(position).getFax());
        eczaneAdresTarfi.setText(list.get(position).getTarif());
        aramaYap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ıntent = new Intent();
                ıntent.setData(Uri.parse("tel:"+list.get(position).getTelefon()));
                activity.startActivity(ıntent);
        }
        });
        return convertView;
    }
}
