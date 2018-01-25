package com.jakestanger.bittorrentmusicsyncer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.request.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.view.MediaControllerView;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;

import static com.jakestanger.bittorrentmusicsyncer.MainActivity.ARTIST;
import static com.jakestanger.bittorrentmusicsyncer.MainActivity.musicService;

public class AlbumActivity extends AppCompatActivity
{
	static final String ALBUM = "com.jakestanger.bittorentmusicsyncer.ALBUM";
	
	private String name;

	private MediaControllerView mediaController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_view);

		//Custom toolbar setup
		Toolbar toolbar = (Toolbar) findViewById(R.id.title_toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		Intent intent = getIntent();
		
		//Restore bundle on back
		Bundle bundle = savedInstanceState != null ? savedInstanceState : intent.getExtras();
		
		name = bundle.getString(ARTIST);
		setTitle(name);
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		
		RetrieveData retrieveData = new RetrieveData(this);
		retrieveData.showData(new JsonData(name), listView, this);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id)
			{
				String entry = (String) adapterView.getItemAtPosition(pos);
				showAlbum(name, entry);
			}
		});

		mediaController = new MediaControllerView(this) {
			@Override
			public void hide() {}
		};

		//Show mediaController if service is already running.
		if(musicService != null && musicService.getMediaPlayer() != null)
			if(musicService.getMediaPlayer().getTrackInfo() != null)
				setupMediaController();
	}

	private void setupMediaController()
	{
		mediaController.setMediaPlayer(musicService);
		mediaController.setAnchorView((ViewGroup) findViewById(R.id.media_controller_container));
		mediaController.show();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(ARTIST, name);
	}
	
	private void showAlbum(String artist, String album)
	{
		Intent intent = new Intent(this, TrackActivity.class);
		intent.putExtra(ARTIST, artist);
		intent.putExtra(ALBUM, album);
		startActivity(intent);
	}
	
	@Override
	public boolean onSupportNavigateUp()
	{
		onBackPressed();
		return true;
	}

	/**
	 * Returns to parent screen on back button pressed.
	 * Without this, the album view returns to the track view,
	 * whereas the desired effect is to return to the artist view.
	 */
	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent(AlbumActivity.this, MainActivity.class);
		startActivity(intent);
	}
}
