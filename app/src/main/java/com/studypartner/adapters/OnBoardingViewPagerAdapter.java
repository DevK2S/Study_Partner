package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.items.OnBoardingItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class OnBoardingViewPagerAdapter extends PagerAdapter {
	
	final Context mContext;
	final List<OnBoardingItem> mListScreen;
	
	public OnBoardingViewPagerAdapter(Context mContext, List<OnBoardingItem> mListScreen) {
		this.mContext = mContext;
		this.mListScreen = mListScreen;
	}
	
	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layoutScreen = inflater.inflate(R.layout.on_boarding_screen_page_layout, container, false);
		
		ImageView image = layoutScreen.findViewById(R.id.onBoardingScreenItemImage);
		TextView title = layoutScreen.findViewById(R.id.onBoardingScreenItemTitle);
		TextView description = layoutScreen.findViewById(R.id.onBoardingScreenItemDescription);
		
		title.setText(mListScreen.get(position).getTitle());
		description.setText(mListScreen.get(position).getDescription());
		image.setImageResource(mListScreen.get(position).getImage());
		
		container.addView(layoutScreen);
		
		return layoutScreen;
	}
	
	@Override
	public int getCount() {
		return mListScreen.size();
	}
	
	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}
	
	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}
}
