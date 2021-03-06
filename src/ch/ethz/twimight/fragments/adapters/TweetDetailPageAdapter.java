package ch.ethz.twimight.fragments.adapters;


import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import ch.ethz.twimight.fragments.TweetDetailFragment;

public class TweetDetailPageAdapter extends FragmentPagerAdapter {
    
	ArrayList<Long> list;
	private static final String TAG = "ShowTweetPageAdapter";
	
	public TweetDetailPageAdapter(FragmentManager fm, ArrayList<Long> list){
		super(fm);
		this.list = list;
	}
	
	@Override
	public Fragment getItem(int pos) {
		long rowId = list.get(pos);
		Log.i(TAG, "rowId: " + rowId);
		return TweetDetailFragment.newInstance(rowId);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
}
