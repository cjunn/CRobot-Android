package com.crobot.core.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.crobot.core.resource.My;
import com.crobot.core.ui.core.SimpleUISupport;
import com.crobot.core.ui.core.UISupport;
import com.crobot.core.ui.core.UIValueSetter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TabPanel extends SimpleUISupport<LinearLayout, Integer> {
    private MyTabLayout myTabLayout;
    private MyViewPager myViewPager;

    public TabPanel(Context context, Map<String, String> attr) {
        super(context, attr);
    }


    @Override
    public <K1 extends View, E> void addChild(UISupport<K1, E> c) {
        super.addChild(c);
        if (c instanceof ViewPager) {
            addChild((ViewPager) c);
        }
    }

    private void addChild(ViewPager c) {
        myViewPager.addView(c);
    }

    @Override
    protected boolean isContainer() {
        return true;
    }

    @Override
    protected LinearLayout initView(Context context) {
        LinearLayout body = new LinearLayout(context);
        body.setOrientation(LinearLayout.VERTICAL);
        this.myTabLayout = new MyTabLayout(context);
        this.myViewPager = new MyViewPager(context);
        this.myTabLayout.setupWithViewPager(myViewPager);
        body.addView(myTabLayout);
        body.addView(myViewPager);
        return body;
    }

    @Override
    protected void setValue(LinearLayout view, Integer value) {
        TabLayout.Tab tabAt = this.myTabLayout.getTabAt(value);
        this.myTabLayout.selectTab(tabAt);
    }

    @Override
    protected void bindViewValueChange(LinearLayout linearLayout, UIValueSetter setter) {
        this.myTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                setter.apply(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public class MyTabLayout extends TabLayout {
        public MyTabLayout(@NonNull Context context) {
            super(context);
            this.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.height = dp2Px(this.getContext(), 35);
            this.setLayoutParams(params);
            this.setSelectedTabIndicatorColor(getResources().getColor(My.color.theme)); // 指示器颜色
        }

        public int dp2Px(Context context, float dpValue) {
            final float density = context.getResources().getDisplayMetrics().density;
            // 2. 换算公式：dp值 * 密度 + 0.5f（四舍五入，避免小数像素导致的精度丢失）
            return (int) (dpValue * density + 0.5f);
        }
    }

    public class MyViewPager extends androidx.viewpager.widget.ViewPager {
        private final List<ViewPager> mViews = new ArrayList<>();
        private final PagerAdapter adapter;

        public MyViewPager(@NonNull Context context) {
            super(context);
            this.adapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return mViews.size();
                }

                @Override
                public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                    return view == object;
                }

                @Override
                public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                    container.removeView(mViews.get(position).getView());
                }

                @NonNull
                @Override
                public Object instantiateItem(@NonNull ViewGroup container, int position) {
                    View view = mViews.get(position).getView();
                    container.addView(view);
                    return view;
                }

                @Nullable
                @Override
                public CharSequence getPageTitle(int position) {
                    ViewPager view = mViews.get(position);
                    String title = view.getTitle();
                    return title;
                }
            };
            this.setAdapter(this.adapter);
        }


        public void addView(ViewPager viewPager) {
            mViews.add(viewPager);
            this.adapter.notifyDataSetChanged();
            viewPager.setObserver(() -> this.adapter.notifyDataSetChanged());
        }
    }


}
