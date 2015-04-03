package me.ele.homedemo;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
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

    private static final int THRESHOLD = 10;
    private int totalDeltaY = 0;
    private boolean isFixedUnderToolbar = false;
    private boolean isAnimating = false;

    @InjectView(R.id.sticky)
    protected TextView stickyView;
    @InjectView(R.id.recyclerview)
    protected RecyclerView recyclerView;

    private HeaderViewRecyclerAdapter adapter;
    private MyAdapter realAdapter;
    private LinearLayout footer;
    private View header;

    private Handler handler;

    private int count;

    public static FirstFragment newInstance(int count) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putInt("count", count);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        count = getArguments().getInt("count");

        handler = new Handler(Looper.getMainLooper());

        setHasOptionsMenu(true);

        stickyView.setText("Sticky");
        stickyView.setTranslationY(getOriginOffset());
        stickyView.setOnClickListener(clickListener);

        header = getActivity().getLayoutInflater().inflate(R.layout.head, null);
        realAdapter = new MyAdapter(getData());
        adapter = new HeaderViewRecyclerAdapter(realAdapter);
        adapter.addHeaderView(header);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(scrollListener);

        handler.post(new Runnable() {
            @Override
            public void run() {
                addFooter();
            }
        });

    }

    private void removeFooter() {
        adapter.removeFooterView(footer);
    }

    private void addFooter() {
        if (isFullScreen()) {
            return;
        }

        footer = new LinearLayout(getActivity());
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) footer.getLayoutParams();
        if (params == null) {
            params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, getFooterHeight());
        }
        footer.setLayoutParams(params);
        adapter.addFooterView(footer);
        adapter.notifyDataSetChanged();
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            totalDeltaY += dy;

            //防止动画执行过程中，影响sticky view的位置。bug:快速滑动有抖动
            if (totalDeltaY == 0 && stickyView.getTranslationY() != getOriginOffset()) {
                stickyView.animate().cancel();
                stickyView.setTranslationY(getOriginOffset());
            }

            if (isAnimating) {
                return;
            }

            //set sticky view's position
            int translationY = (getOriginOffset() - totalDeltaY) < 0 ? 0 : (getOriginOffset() - totalDeltaY);
            if (hasFixedLocation(translationY)) {
                translationY = getActionbarSize();
            }
            stickyView.setTranslationY(translationY);

            //当toolbar隐藏时下拉，动画显示toolbar，同时sticky view固定显示在toolbar之下
            if (dy < -THRESHOLD && showToolbar() && totalDeltaY > getHeaderHeight()) {
                isFixedUnderToolbar = true;
                isAnimating = true;
                stickyView.animate().translationY(getActionbarSize())
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
            if (dy > THRESHOLD && isFixedUnderToolbar && totalDeltaY >= getOriginOffset() && hideToolbar()) {
                isFixedUnderToolbar = false;
                isAnimating = true;
                stickyView.animate().translationY(0).setInterpolator(new DecelerateInterpolator())
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
            if (totalDeltaY == 0 && moveToolbar(0)) {
                isFixedUnderToolbar = false;
                return;
            }

            //当toolbar与sticky view相接时，同时移动
            if (translationY < getActionbarSize()) {
                if (dy < 0 && isToolbarTotalShown()) {
                    return;
                }

                moveToolbar(getActionbarSize() - translationY);
                isFixedUnderToolbar = false;
            }
        }


        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isToolbarTotalGone()) {
                return;
            }

            isAnimating = true;
            hideToolbar();
            int duration = computeScrollDuration(0, getOriginOffset() - totalDeltaY, 0, 0);
            recyclerView.smoothScrollBy(0, getOriginOffset() - totalDeltaY);
            stickyView.animate().translationY(0).setInterpolator(sQuinticInterpolator).setDuration(duration).setListener(new SimpleAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isAnimating = false;
                }
            }).start();
        }
    };

    private List<String> getData() {
        List<String> data = new ArrayList<>();

        for (int i = 1; i < count + 1; i++) {
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

        public void notifyDataSetChanged(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    private boolean hideToolbar() {
        return ((MainActivity) getActivity()).hideToolbar();
    }

    private boolean showToolbar() {
        return ((MainActivity) getActivity()).showToolbar();
    }

    private boolean moveToolbar(int offset) {
        return ((MainActivity) getActivity()).moveToolbar(offset);
    }

    private boolean isToolbarTotalShown() {
        return ((MainActivity) getActivity()).isToolbarTotalShown();
    }

    private boolean isToolbarTotalGone() {
        return ((MainActivity) getActivity()).isToolbarTotalGone();
    }

    private boolean hasFixedLocation(int translationY) {
        return isFixedUnderToolbar && translationY < getActionbarSize() && isToolbarTotalShown();
    }

    private int getOriginOffset() {
        return getActionbarSize() + dpToPx(100);
    }

    private int getActionbarSize() {
        return dpToPx(56);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (dp * displayMetrics.density + 0.5);
    }

    public void onEvent(FixUnderToolbarEvent event) {
        isFixedUnderToolbar = true;
        stickyView.setTranslationY(getActionbarSize());
    }


    public static final class FixUnderToolbarEvent {

    }

    private boolean isFullScreen() {
        return getEstimateHeight() > getOneScreenScrollHeight();
    }

    private int getEstimateHeight() {
        int totalHeight = getHeaderHeight();
        int itemCount = adapter.getItemCount();
        for (int i = 1; i < itemCount; i++) {
            View child = recyclerView.getChildAt(i);
            if (child != null) {
                totalHeight += child.getHeight();
            } else {
                totalHeight += recyclerView.getChildAt(1).getHeight();
            }
        }
        return totalHeight;
    }

    private int getOneScreenScrollHeight() {
        return getScreenHeight() + getHeaderHeight() - stickyView.getHeight();
    }

    private int getHeaderHeight() {
        return header.getHeight();
    }

    private int getFooterHeight() {
        return getOneScreenScrollHeight() - getEstimateHeight();
    }

    private int getScreenHeight() {
        return getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight();
    }

    private int computeScrollDuration(int dx, int dy, int vx, int vy) {
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final boolean horizontal = absDx > absDy;
        final int velocity = (int) Math.sqrt(vx * vx + vy * vy);
        final int delta = (int) Math.sqrt(dx * dx + dy * dy);
        final int containerSize = horizontal ? recyclerView.getWidth() : recyclerView.getHeight();
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            float absDelta = (float) (horizontal ? absDx : absDy);
            duration = (int) (((absDelta / containerSize) + 1) * 300);
        }
        return Math.min(duration, 2000);
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

}
