/*******************************************************************************
 * Copyright (c) 2011 ETH Zurich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Paolo Carta - Implementation
 *     Theus Hossmann - Implementation
 *     Dominik Schatzmann - Message specification
 ******************************************************************************/

package ch.ethz.twimight.net.twitter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import ch.ethz.twimight.R;
import ch.ethz.twimight.activities.LoginActivity;

/** 
 * Cursor adapter for a cursor containing users.
 */
public class DMAdapter extends SimpleCursorAdapter {
	
	Context context;
	 int flags;	
	
	static final String[] from = {TwitterUsers.COL_SCREENNAME, DirectMessages.COL_TEXT};
	static final int[] to = {R.id.showDMScreenName, R.id.showDMText};
	public static final String TAG = "DMAdapter";

	/** Constructor */
	public DMAdapter(Context context, Cursor c) {
		super(context, R.layout.dmrow, c, from, to);  
		this.context=context;
	}
	/**
	 * Asks the user if she wants to delete a dm.
	 */
	private void showDeleteDialog(final long tid, final long rowId){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure you want to delete your Direct Message?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Uri uri = Uri.parse("content://"+DirectMessages.DM_AUTHORITY+"/"+DirectMessages.DMS+"/"+ rowId );        	
		        	  
		        	   if ( tid != 0)
		        		   context.getContentResolver().update(uri, setDeleteFlag(flags), null, null);
		        	   else
		        		   context.getContentResolver().delete(uri,null,null );
		        	   
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	/**
	 * Adds the delete flag and returns the flags in a content value structure 
	 * to send to the content provider 
	 * @param flags
	 * @return
	 */
	private ContentValues setDeleteFlag(final int flags) {
		ContentValues cv = new ContentValues();
		cv.put(DirectMessages.COL_FLAGS, flags | DirectMessages.FLAG_TO_DELETE);		
		return cv;
	}

	/** This is where data is mapped to its view */
	@Override
	public void bindView(final View dmrow, Context context, Cursor cursor) {
		super.bindView(dmrow, context, cursor);
		
		// Find views by id
		long createdAt = cursor.getLong(cursor.getColumnIndex(DirectMessages.COL_CREATED));
		TextView dmCreatedAt = (TextView) dmrow.findViewById(R.id.dmCreatedAt);
		dmCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt));

			
		// Profile image
		ImageView picture = (ImageView) dmrow.findViewById(R.id.showDMProfileImage);
		if(!cursor.isNull(cursor.getColumnIndex(TwitterUsers.COL_PROFILEIMAGE))){
			byte[] bb = cursor.getBlob(cursor.getColumnIndex(TwitterUsers.COL_PROFILEIMAGE));
			picture.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));
		} else {
			picture.setImageResource(R.drawable.default_profile);
		}
		
		// any transactional flags?
		ImageView toPostInfo = (ImageView) dmrow.findViewById(R.id.dmToPost);
		flags = cursor.getInt(cursor.getColumnIndex(DirectMessages.COL_FLAGS));
		
		boolean toPost = (flags>0);
		if(toPost){
			toPostInfo.setImageResource(android.R.drawable.ic_dialog_alert);
			toPostInfo.getLayoutParams().height = 30;
		} else {
			toPostInfo.setImageResource(R.drawable.blank);
		}
		// can we delete the message		
		
		final ImageButton deleteButton = (ImageButton) dmrow.findViewById(R.id.showDMDelete);
		//deleteButton.setBackgroundColor(R.color.transparent);
				
		//if(Long.toString(cursor.getLong(cursor.getColumnIndex(DirectMessages.COL_SENDER))).equals(LoginActivity.getTwitterId(context))) {
		
			deleteButton.setOnClickListener(new OnButtonClickListener(cursor.getPosition(),cursor )  );			
		//} else 
		//	deleteButton.setVisibility(ImageButton.GONE);
				
	
		// DM background and disaster info
		LinearLayout rowLayout = (LinearLayout) dmrow.findViewById(R.id.dmUserRowLayout);		
		if(Long.toString(cursor.getLong(cursor.getColumnIndex(DirectMessages.COL_SENDER))).equals(LoginActivity.getTwitterId(context))) {
			
			if(cursor.getInt(cursor.getColumnIndex(DirectMessages.COL_ISDISASTER))>0)
				rowLayout.setBackgroundResource(R.drawable.disaster_tweet_background);
			else
				rowLayout.setBackgroundResource(R.drawable.own_tweet_background);
			
		} else {
			
			if(cursor.getInt(cursor.getColumnIndex(DirectMessages.COL_ISDISASTER))>0)
				rowLayout.setBackgroundResource(R.drawable.disaster_dm_background_receiver);
			else
				rowLayout.setBackgroundResource(R.drawable.normal_tweet_background);
		}
		
	}
	
	private class OnButtonClickListener implements OnClickListener {
	    private int position;
	    Cursor cursor;	    

	    public OnButtonClickListener(int position, Cursor c) {
	        super();
	        this.position=position;
	        this.cursor=c;
	        
	    }

	    @Override
	    public void onClick(View v) {
	        // position is an id.
	    	cursor.moveToPosition(position);
	    	int flags = cursor.getInt(cursor.getColumnIndex(DirectMessages.COL_FLAGS));
	    	Long tid = cursor.getLong(cursor.getColumnIndex(DirectMessages.COL_DMID));
	    	Long rowId = cursor.getLong(cursor.getColumnIndex("_id"));
	    	if((flags & Tweets.FLAG_TO_DELETE) == 0){	    		
				
				if (tid != null) {
					Log.i(TAG,"msg was published online");
					showDeleteDialog(tid,rowId );
				}
				else {
					Log.i(TAG,"msg was NOT published online");
					showDeleteDialog(0,rowId );		
				}
	    	}
	    }
	}
	
}