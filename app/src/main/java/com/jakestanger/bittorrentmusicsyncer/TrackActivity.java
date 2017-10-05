package com.jakestanger.bittorrentmusicsyncer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.requests.Cache;
import com.jakestanger.bittorrentmusicsyncer.requests.ReachableURLGetter;
import com.jakestanger.bittorrentmusicsyncer.requests.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData.Type.TRACK;

public class TrackActivity extends AppCompatActivity
{
	MediaPlayer player = new MediaPlayer();
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tracklist_menu, menu);
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_view);
		
		//Custom toolbar setup
		Toolbar toolbar = (Toolbar) findViewById(R.id.track_view_toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		Intent intent = getIntent();
		final String artistName = intent.getStringExtra(MainActivity.ARTIST);
		final String albumName = intent.getStringExtra(AlbumActivity.ALBUM);
		setTitle(albumName);
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		final RetrieveData retrieveData = new RetrieveData(this);
		retrieveData.showData(new JsonData(artistName, albumName, TRACK), listView, this);
		
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				//Track track = retrieveData.getTrackAdapter().getItem(pos);
				Track track = (Track) parent.getAdapter().getItem(pos);
				playTrack(track);
			}
		});
	}
	
	private void playTrack(Track track)
	{
		try
		{
			String[] URLs = {"http://192.168.0.19/", "http://music.jakestanger.com/"}; //TODO integrate into class
			String baseURL = Cache.getReachableURL() != null ?
					Cache.getReachableURL() : new ReachableURLGetter().execute(URLs).get();
			
			Uri uri = Uri.parse(baseURL + track.getDownloadURL());
			
			//player = new MediaPlayer();
			if(player.isPlaying())
			{
				player.reset();
			}
			
			player.setDataSource(this, uri);
			player.prepare();
			player.start();
		}
		catch (InterruptedException | ExecutionException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
