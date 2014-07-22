/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockerhieu.emojicon;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.rockerhieu.emojicon.emoji.Emojicon;
import com.rockerhieu.emojicon.emoji.People;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public class EmojiconGridFragment extends Fragment implements AdapterView.OnItemClickListener {
    private OnEmojiconClickedListener mOnEmojiconClickedListener;
    private Emojicon[] mData;
    private ViewPager mEmojiViewPager;
    private View mMainview;
    private Activity ac;
	private int mCurrentPage = 0;// 当前表情页

    protected static EmojiconGridFragment newInstance(Emojicon[] emojicons,int page_num) {
        EmojiconGridFragment emojiGridFragment = new EmojiconGridFragment();
        Bundle args = new Bundle();
        args.putSerializable("emojicons", emojicons);
        args.putInt("page_num", page_num);
        args.putInt("page_size", 21);
        emojiGridFragment.setArguments(args);
        return emojiGridFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.emojicon_grid, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainview=view;
		mEmojiViewPager = (ViewPager) mMainview.findViewById(R.id.emoji_child_pager);
//        GridView gridView = (GridView) view.findViewById(R.id.Emoji_GridView);
        mData = getArguments() == null ? People.DATA : (Emojicon[]) getArguments().getSerializable("emojicons");
        initFacePage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("emojicons", mData);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    	ac=activity;
        if (activity instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) activity;
        } else if(getParentFragment() instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(activity + " must implement interface " + OnEmojiconClickedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        mOnEmojiconClickedListener = null;
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnEmojiconClickedListener != null) {
            mOnEmojiconClickedListener.onEmojiconClicked((Emojicon) parent.getItemAtPosition(position));
        } 
        System.out.println("----------------");
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }
    private void initFacePage() {
		// TODO Auto-generated method stub
		List<View> lv = new ArrayList<View>();
		int page_num=getArguments().getInt("page_num");
		int page_size=getArguments().getInt("page_size");
		System.out.println("page_num:"+page_num+";page_size:"+page_size);
		for (int i = 0; i < page_num; ++i){
			  Emojicon[] emojicons;
			if (i==page_num-1) {

				emojicons=new Emojicon[mData.length-i*page_size];
				for (int j = 0; j < mData.length-i*page_size; j++) {
					emojicons[j]=mData[i*page_size+j];
				}
			}else{

				emojicons=new Emojicon[page_size];
				for (int j = 0; j < page_size; j++) {
					emojicons[j]=mData[i*page_size+j];
				}
			}
			lv.add(getGridView(emojicons));
		}
		EmojiPageAdeapter adapter = new EmojiPageAdeapter(lv);
		mEmojiViewPager.setAdapter(adapter);
		mEmojiViewPager.setCurrentItem(mCurrentPage);
		CirclePageIndicator indicator = (CirclePageIndicator) mMainview.findViewById(R.id.indicator);
		indicator.setViewPager(mEmojiViewPager);
		adapter.notifyDataSetChanged();
		indicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mCurrentPage = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// do nothing
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// do nothing
			}
		});

	}

	private GridView getGridView(Emojicon[] data) {
		// TODO Auto-generated method stub
		GridView gv = new GridView(ac);
		gv.setNumColumns(7); 
		gv.setSelector(android.R.color.transparent);// 屏蔽GridView默认点击效果
		gv.setBackgroundColor(Color.TRANSPARENT);
		gv.setCacheColorHint(Color.TRANSPARENT);
		gv.setHorizontalSpacing(1);
		gv.setVerticalSpacing(1);  
		gv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		gv.setGravity(Gravity.CENTER);


		gv.setAdapter(new EmojiAdapter(ac,data));
		gv.setOnTouchListener(forbidenScroll());
		gv.setOnItemClickListener(this);
		return gv;
	}
	// 防止乱pageview乱滚动
	private OnTouchListener forbidenScroll() {
		return new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		};
	}
}
