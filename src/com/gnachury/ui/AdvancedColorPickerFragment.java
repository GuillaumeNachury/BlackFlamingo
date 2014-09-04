package com.gnachury.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.gnachury.blackflamingo.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;

public class AdvancedColorPickerFragment extends Fragment {

    private ColorPicker mColorPicker;
    private View mColorSquare;
    private Button mButton;
    private Bundle bundle;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.advanced_color_picker_fragment, null);
        bundle=getArguments();
       
        
        mColorPicker = (ColorPicker) view.findViewById(R.id.color_picker);
        mColorSquare = view.findViewById(R.id.color_square);
        mButton = (Button) view.findViewById(R.id.select_color_button);

        mColorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                mColorSquare.setBackgroundColor(color);
            }
        });
      
        int color = Color.argb(120, 255, 0, 0);
       
        mColorPicker.setColor(bundle.getInt("color"));
        mColorPicker.setOldCenterColor(color);

        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mColorPicker.setOldCenterColor(mColorPicker.getColor());
            }
        });

        return view;
    }
    

    
    
}