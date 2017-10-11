package com.jakestanger.bittorrentmusicsyncer.request;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jake stanger
 * Gets the list of URLs
 */
public class ReachableURLGetter extends AsyncTask<String, Void, String>
{
	@Override
	protected String doInBackground(String... URLs)
	{
		return getFirstReachableURL(URLs);
	}
	
	@Override
	protected void onPostExecute(String url)
	{
		super.onPostExecute(url);
		Cache.setReachableURL(url);
	}
	
	/**
	 * @return first reachable URL
	 * @param URLs an array of URL strings
	 */
	private String getFirstReachableURL(String[] URLs)
	{
		int i = 0;
		while(!isReachable(URLs[i])) i++;
		return URLs[i];
	}
	
	/**
	 * @param urlPath String URL
	 * @return is the URL reachable
	 */
	private boolean isReachable(String urlPath)
	{
		try
		{
			URL url = new URL(urlPath + "/test");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			int code = connection.getResponseCode();
			return code == 200;
		}
		catch (IOException e)
		{
			return false;
		}
	}
}
