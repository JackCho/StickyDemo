package me.ele.homedemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by caoyubin on 15/3/31.
 */
public class SecondFragment extends Fragment{
    
    private static final String TAG = "tag";
    
    public static SecondFragment newInstance(String tag) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putString(TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        
        String tag = getArguments().getString(TAG);
        
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        TextView textView = (TextView) view.findViewById(R.id.textview1);
        textView.setText(tag);
        
        return view;
    }
}
