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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.request.GetMagnetURL;
import com.jakestanger.bittorrentmusicsyncer.request.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.service.MusicService;
import com.jakestanger.bittorrentmusicsyncer.view.MediaControllerView;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;

import java.util.ArrayList;

import static com.jakestanger.bittorrentmusicsyncer.MainActivity.musicBound;
import static com.jakestanger.bittorrentmusicsyncer.MainActivity.musicService;
import static com.jakestanger.bittorrentmusicsyncer.MainActivity.playIntent;
import static com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData.Type.TRACK;

public class TrackActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener
{
	
	private MediaControllerView mediaController;
	
	private Handler handler = new Handler();
	
	private ArrayList<Track> trackQueue;
	private int queueIndex;
	
	private boolean firstTap = true;
	
	private String artistName, albumName;
	
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
		
		final TrackActivity instance = this;
		
		MenuItem menuItem = menu.findItem(R.id.download_album);
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem)
			{
				new GetMagnetURL(instance).getMagnetURL(artistName, albumName);
				//
				return true;
			}
		});
		
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_view);
		
		//Custom toolbar setup
		Toolbar toolbar = (Toolbar) findViewById(R.id.title_toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		final Intent intent = getIntent();
		artistName = intent.getStringExtra(MainActivity.ARTIST);
		albumName = intent.getStringExtra(AlbumActivity.ALBUM);
		setTitle(albumName);
		
		//Setup list
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		final RetrieveData retrieveData = new RetrieveData(this);
		retrieveData.showData(new JsonData(artistName, albumName, TRACK), listView, this);
		
		final TrackActivity instance = this;

		trackQueue = new ArrayList<>();
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				queueIndex = pos;

				Adapter adapter = parent.getAdapter();
				for(int i = 0; i < adapter.getCount(); i++)
					trackQueue.add((Track) adapter.getItem(i));

				musicService.startService(playIntent);
				
				musicService.getMediaPlayer().setOnPreparedListener(instance);
				
				Track track = trackQueue.get(queueIndex);
				musicService.setTracks(trackQueue);
				//musicService.playTrack(track);
				musicService.setTrackIndex(queueIndex);
				musicService.playQueue();
			}
		});
		
		mediaController = new MediaControllerView(this) {
			@Override
			public void hide() {}
		};

        //Show mediaController if service is already running.
        if(musicService != null)
            if(musicService.getMediaPlayer().getTrackInfo() != null)
                setupMediaController();
	}
	
	@Override
	public void onPrepared(MediaPlayer mediaPlayer)
	{
		mediaPlayer.seekTo(0);
		mediaPlayer.start();
		
		if(firstTap)
		{
			if(musicBound)
			{
				setupMediaController();
			}
		}
		else if (musicBound) mediaController.show(0);
	}



	private void setupMediaController()
	{
		mediaController.setMediaPlayer(musicService);
		mediaController.setAnchorView((ViewGroup) findViewById(R.id.media_controller_container));
		mediaController.show();
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
//		if(musicService.getMediaPlayer().isPlaying()) musicService.getMediaPlayer().stop();
//		musicService.getMediaPlayer().release();
//
//		if(musicBound)
//		{
//			unbindService(musicConnection);
//			musicBound = false;
//		}
	}
	
	@Override
	protected void onDestroy()
	{
		//stopService(playIntent);
		//getApplicationContext().unbindService(musicConnection);
		//musicService = null;
		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{
		//getApplicationContext().unbindService(musicConnection);
		Intent intent = new Intent(TrackActivity.this, AlbumActivity.class);
		intent.putExtra(MainActivity.ARTIST, artistName);
		startActivity(intent);
	}
}
