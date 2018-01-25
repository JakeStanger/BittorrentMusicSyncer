package com.jakestanger.bittorrentmusicsyncer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.request.Cache;
import com.jakestanger.bittorrentmusicsyncer.request.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.service.MusicService;
import com.jakestanger.bittorrentmusicsyncer.view.MediaControllerView;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;

public class MainActivity extends AppCompatActivity
{
	static final String ARTIST = "com.jakestanger.bittorentmusicsyncer.ARTIST";

	static MusicService musicService;
	static Intent playIntent;
	static boolean musicBound;

	private MediaControllerView mediaController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_view);

		//Custom toolbar setup
		Toolbar toolbar = (Toolbar) findViewById(R.id.title_toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		RetrieveData retrieveData = new RetrieveData(this);
		retrieveData.showData(new JsonData(), listView, this);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id)
			{
				String entry = (String) adapterView.getItemAtPosition(pos);
				showArtist(entry);
			}
		});

		mediaController = new MediaControllerView(this) {
			@Override
			public void hide() {}
		};

		//Show mediaController if service is already running.
		if(musicService != null && musicService.getMediaPlayer() != null)
			if(musicService.getMediaPlayer().getTrackInfo() != null )
				setupMediaController();
		
		Cache.setAppDir(this.getApplicationContext().getFilesDir());
		
		try
		{
			//GetMagnet.download();
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
	}

	private void setupMediaController()
	{
		mediaController.setMediaPlayer(musicService);
		mediaController.setAnchorView((ViewGroup) findViewById(R.id.media_controller_container));
		mediaController.show();
	}
	
	private void showArtist(String artist)
	{
		Intent intent = new Intent(this, AlbumActivity.class);
		intent.putExtra(ARTIST, artist);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
}
