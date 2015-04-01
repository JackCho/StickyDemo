package me.ele.homedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by caoyubin on 15/3/31.
 */
public class SecondFragment extends Fragment{
    
    private static final String TAG = "tag";
    
    private int pos;
    
    public static SecondFragment newInstance(int pos) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putInt(TAG, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pos = getArguments().getInt(TAG);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        TextView textView = (TextView) view.findViewById(R.id.textview1);
        textView.setText("页面" + pos);
        
        switch (pos) {
            case 1:
                textView.setBackgroundColor(Color.RED);
                break;
            case 2:
                textView.setBackgroundColor(Color.GREEN);
                break;
            case 3:
                textView.setBackgroundColor(Color.BLUE);
                break;
        }
        
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_second, menu);
        menu.findItem(R.id.action_title).setTitle("页面" + pos);
    }
}
