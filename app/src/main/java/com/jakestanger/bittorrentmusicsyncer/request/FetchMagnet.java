package com.jakestanger.bittorrentmusicsyncer.request;

import android.os.AsyncTask;
import com.frostwire.jlibtorrent.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jake stanger
 * TODO Write JavaDoc
 */
public class FetchMagnet extends AsyncTask<String, Void, List<Byte>>
{
	private byte[] data;
	
	@Override
	protected List<Byte> doInBackground(String... strings)
	{
		final SessionManager sessionManager = new SessionManager();
		data = sessionManager.fetchMagnet(strings[0], 300);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(List<Byte> bytes)
	{
		List<Byte> returnData = new ArrayList<>();
		for(Byte entry : data) returnData.add(entry);
		
		super.onPostExecute(returnData);
		new DownloadTorrent().execute(returnData);
	}
}
