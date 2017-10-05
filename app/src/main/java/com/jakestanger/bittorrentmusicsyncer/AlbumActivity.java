package com.jakestanger.bittorrentmusicsyncer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.requests.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;

import static com.jakestanger.bittorrentmusicsyncer.MainActivity.ARTIST;

public class AlbumActivity extends AppCompatActivity
{
	static final String ALBUM = "com.jakestanger.bittorentmusicsyncer.ALBUM";
	
	private String name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_view);
		
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
}
