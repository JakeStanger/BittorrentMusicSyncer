package com.jakestanger.bittorrentmusicsyncer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.request.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.service.MusicService;
import com.jakestanger.bittorrentmusicsyncer.view.MediaControllerView;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;

import java.util.ArrayList;

import static com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData.Type.TRACK;

public class TrackActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener
{
	private MusicService musicService;
	private Intent playIntent;
	private boolean musicBound;
	
	private MediaControllerView mediaController;
	
	private Handler handler = new Handler();
	
	private ArrayList<Track> trackQueue;
	
	private boolean firstTap = true;
	
	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service)
		{
			MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
			musicService = binder.getService();
			musicService.setTracks(trackQueue);
			
			musicBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName componentName)
		{
			musicBound = false;
		}
	};
	
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
		
		final Intent intent = getIntent();
		final String artistName = intent.getStringExtra(MainActivity.ARTIST);
		final String albumName = intent.getStringExtra(AlbumActivity.ALBUM);
		setTitle(albumName);
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		final RetrieveData retrieveData = new RetrieveData(this);
		retrieveData.showData(new JsonData(artistName, albumName, TRACK), listView, this);
		
		final TrackActivity instance = this;
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				musicService.startService(playIntent);
				
				musicService.getMediaPlayer().setOnPreparedListener(instance);
				
				Track track = (Track) parent.getAdapter().getItem(pos);
				musicService.setTrack(track);
				musicService.playTrack(track);
			}
		});
		
		mediaController = new MediaControllerView(this) {
			@Override
			public void hide() {}
		};
	}
	
	@Override
	public void onPrepared(MediaPlayer mediaPlayer)
	{
		mediaPlayer.start();
		
		if(firstTap)
		{
			if(musicBound) //TODO wait for mediaplayer to be ready
			{
				mediaController.setMediaPlayer(musicService);
				mediaController.setAnchorView((ViewGroup) findViewById(R.id.media_controller_container));
				mediaController.show();
			}
		}
		else if (musicBound) mediaController.show(0);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if(playIntent == null)
		{
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		mediaController.hide();
		musicService.getMediaPlayer().stop();
		musicService.getMediaPlayer().release();
		
		if(musicBound)
		{
			unbindService(musicConnection);
			musicBound = false;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		stopService(playIntent);
		getApplicationContext().unbindService(musicConnection);
		musicService = null;
		super.onDestroy();
	}
}
