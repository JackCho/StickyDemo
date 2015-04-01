package me.ele.homedemo;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import me.ele.components.recyclerview.HeaderViewRecyclerAdapter;
import me.ele.omniknight.OKFragment;
import roboguice.inject.ContentView;

/**
 * Created by caoyubin on 15/3/31.
 */
@ContentView(R.layout.fragment_first)
public class FirstFragment extends OKFragment {

    private int totalDeltaY = 0;
    private boolean isFixedUnderToolbar = false;
    private boolean isAnimating = false;

    @InjectView(R.id.sticky)
    protected TextView mStickyView;
    @InjectView(R.id.recyclerview)
    protected RecyclerView recyclerView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mStickyView.setText("Sticky");
        mStickyView.setTranslationY(getOriginOffset());

        View head = getActivity().getLayoutInflater().inflate(R.layout.head, null);
        HeaderViewRecyclerAdapter adapter = new HeaderViewRecyclerAdapter(new MyAdapter(getData()));
        adapter.addHeaderView(head);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(scrollListener);
        
        mStickyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).hideToolbar();
                recyclerView.smoothScrollBy(0, getOriginOffset());
                mStickyView.animate().translationY(0).start();
            }
        });
    }
    
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            totalDeltaY += dy;

            if (isAnimating) {
                return;
            }
            
            //set sticky view's position
            int translationY = (getOriginOffset() - totalDeltaY) < 0 ? 0 : (getOriginOffset() - totalDeltaY);
            if (hasFixedLocation(translationY)) {
                translationY = getActionbarSize();
            }
            mStickyView.setTranslationY(translationY);

            //当toolbar隐藏时下拉，动画显示toolbar，同时sticky view固定显示在toolbar之下
            if (dy < -5 && ((MainActivity) getActivity()).showToolbar()) {
                isFixedUnderToolbar = true;
//                mStickyView.setTranslationY(getActionbarSize());
                isAnimating = true;
                mStickyView.animate().translationY(getActionbarSize())
                        .setInterpolator(new DecelerateInterpolator()).setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isAnimating = false;
                    }
                }).start();

                return;
            }

            //当sticky view固定显示在toolbar之下时上拉，隐藏toolbar，同时sticky view置顶
            if (dy > 5 && isFixedUnderToolbar && ((MainActivity) getActivity()).hideToolbar()) {
                isFixedUnderToolbar = false;
//                mStickyView.setTranslationY(0);
                isAnimating = true;
                mStickyView.animate().translationY(0).setInterpolator(new DecelerateInterpolator())
                        .setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                isAnimating = false;
                            }
                        }).start();

                return;
            }

            //当滑动到顶部，toolbar移动到顶部
            if (totalDeltaY == 0 && ((MainActivity) getActivity()).moveToolbar(0)) {
                isFixedUnderToolbar = false;
                return;
            }

            //当toolbar与sticky view相接时，同时移动
            if (translationY < getActionbarSize()) {
                if (dy < 0 && ((MainActivity) getActivity()).isToolbarShown()) {
                    return;
                }

                ((MainActivity) getActivity()).moveToolbar(getActionbarSize() - translationY);
                isFixedUnderToolbar = false;
            }
        }


        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    private List<String> getData() {
        List<String> data = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            data.add("测试数据" + i);
        }

        return data;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<String> list;

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            myViewHolder.textView.setText(list.get(i));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    private boolean hasFixedLocation(int translationY) {
        return isFixedUnderToolbar && translationY < getActionbarSize();
    }

    private int getOriginOffset() {
        return getActionbarSize() + dpToPx(100);
    }

    private int getActionbarSize() {
//        final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
//                new int[]{android.R.attr.actionBarSize});
//        try {
//            return (int) styledAttributes.getDimension(0, 0);
//        } finally {
//            styledAttributes.recycle();
//        }
        return dpToPx(56);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density + 0.5);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

    }

    public void onEvent(FixUnderToolbarEvent event) {
        isFixedUnderToolbar = true;
        mStickyView.setTranslationY(getActionbarSize());
    }


    public static final class FixUnderToolbarEvent {


    }
}
