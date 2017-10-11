package com.jakestanger.bittorrentmusicsyncer.request;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jakestanger.bittorrentmusicsyncer.adapter.TrackAdapter;
import com.jakestanger.bittorrentmusicsyncer.wrapper.JsonData;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * @author Jake stanger
 * Handles server data retrieval
 */
public class RetrieveData
{
	private static final String[] URLs = {"http://192.168.0.19/", "http://music.jakestanger.com/"};
	
	private static RequestQueue queue;
	
	private ArrayAdapter adapter;
	
	public RetrieveData(Context context)
	{
		queue = Volley.newRequestQueue(context);
	}
	
	public void showData(final JsonData jsonData, final ListView view, final Context context)
	{
		try
		{
			String baseURL = Cache.getReachableURL() != null ?
					Cache.getReachableURL() : new ReachableURLGetter().execute(URLs).get();
			String URL = baseURL + "/json?artist=" + URLEncoder.encode(jsonData.getArtistName(), "UTF-8") +
					"&album=" + URLEncoder.encode(jsonData.getAlbumName(), "UTF-8");
			
			StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>()
			{
				@Override
				public void onResponse(String response)
				{
					try
					{
						JSONArray data = new JSONArray(response);
						
						if(jsonData.getType() != JsonData.Type.TRACK)
						{
							ArrayList<String> names = new ArrayList<>();
							
							for (int i = 0; i < data.length(); i++)
								names.add(((JSONObject) data.get(i)).getString("title"));
							
							adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names);
						}
						else
						{
							ArrayList<Track> tracks = new ArrayList<>();
							for (int i = 0; i < data.length(); i++)
							{
								JSONObject trackData = (JSONObject) data.get(i);
								
								String title = trackData.getString("title");
								String titleSort = trackData.getString("titleSort");
								String summary = trackData.getString("summary");
								String key = trackData.getString("key");
								String downloadURL = trackData.getString("downloadURL");
								
								tracks.add(new Track(title, titleSort, summary, key, downloadURL));
							}
							
							adapter = new TrackAdapter(context, tracks);
							
						}
						
						view.setAdapter(adapter);
						adapter.notifyDataSetChanged();
						
					}
					catch (JSONException e)
					{
						e.printStackTrace(); //TODO Add error popup
					}
				}
			},
					new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError e)
						{
							e.printStackTrace(); //TODO Add error popup
						}
					});
			
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			
			queue.add(stringRequest);
		}
		catch(InterruptedException | ExecutionException | UnsupportedEncodingException e)
		{
			e.printStackTrace(); //TODO Add error popup
		}
	}
}
