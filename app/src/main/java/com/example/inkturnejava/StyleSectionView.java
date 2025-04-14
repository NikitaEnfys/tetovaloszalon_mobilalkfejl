package com.example.inkturnejava;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inkturnejava.R;
import com.example.inkturnejava.SalonAdapter;
import com.example.inkturnejava.StyleSalonListActivity;
import com.example.inkturnejava.Salon;

import java.util.List;

public class StyleSectionView extends LinearLayout {

    private TextView titleText;
    private RecyclerView recyclerView;
    private Button moreButton;
    private String currentStyle;

    public StyleSectionView(Context context) {
        super(context);
        init(context);
    }

    public StyleSectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StyleSectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_style_section, this, true);
        setOrientation(VERTICAL);
        titleText = findViewById(R.id.styleTitleText);
        recyclerView = findViewById(R.id.styleRecyclerView);
        moreButton = findViewById(R.id.moreButton);
    }

    public void setData(String style, List<Salon> salons, List<String> docIds){
        currentStyle = style;
        String label = style.substring(0, 1).toUpperCase() + style.substring(1).toLowerCase();
        titleText.setText(label + " stílus");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(new SalonAdapter(salons, docIds));

        moreButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StyleSalonListActivity.class);
            intent.putExtra("style", currentStyle);
            getContext().startActivity(intent);
        });
    }
}