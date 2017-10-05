package com.jakestanger.bittorrentmusicsyncer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.jakestanger.bittorrentmusicsyncer.R;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;

import java.util.ArrayList;

/**
 * @author Jake stanger
 * Custom track adapter
 * for passing Track object
 */
public class TrackAdapter extends ArrayAdapter<Track>
{
	private ArrayList<Track> data;
	Context context;
	
	private static class ViewHolder
	{
		TextView title;
	}
	
	public TrackAdapter(Context context, ArrayList<Track> data)
	{
		super(context, R.layout.item_track, data);
		this.data = data;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Track track = getItem(position);
		
		ViewHolder viewHolder;
		if(convertView == null)
		{
			viewHolder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(getContext());
			
			convertView = inflater.inflate(R.layout.item_track, parent, false);
			viewHolder.title = convertView.findViewById(R.id.track_title);
			
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.title.setText(track.getTitle());

		return convertView;
	}
}
