package com.jakestanger.bittorrentmusicsyncer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jakestanger.bittorrentmusicsyncer.request.RetrieveData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;

public class MainActivity extends AppCompatActivity
{
	static final String ARTIST = "com.jakestanger.bittorentmusicsyncer.ARTIST";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
	}
	
	private void showArtist(String artist)
	{
		Intent intent = new Intent(this, AlbumActivity.class);
		intent.putExtra(ARTIST, artist);
		startActivity(intent);
	}
}
