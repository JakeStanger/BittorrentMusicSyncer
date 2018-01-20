package com.jakestanger.bittorrentmusicsyncer.request;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import static com.android.volley.Request.Method.GET;

/**
 * @author Jake stanger
 * TODO Write JavaDoc
 */
public class GetMagnetURL
{
	
	//URL url = new URL("http://192.168.0.19/getTorrent?artist=" + strings[0] + "&album=" + strings[1]);
	RequestQueue queue;
	
	public GetMagnetURL(Context context)
	{
		queue = Volley.newRequestQueue(context);
	}
	
	public void getMagnetURL(final String artistName, String albumName)
	{
		try
		{
			Cache.setCurrentArtist(artistName);
			
			String baseURL = Cache.getReachableURL() != null ?
					Cache.getReachableURL() : new ReachableURLGetter().execute(Cache.URLs).get();
			
			String URL = baseURL + "/getTorrent?artist=" + URLEncoder.encode(artistName, "UTF-8") +
					"&album=" + URLEncoder.encode(albumName, "UTF-8");
			
			StringRequest stringRequest = new StringRequest(GET, URL, new Response.Listener<String>()
			{
				@Override
				public void onResponse(String response)
				{
					Cache.setCurrentMagnet(response);
					new FetchMagnet().execute(response);
				}
			},
					new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							error.printStackTrace();
						}
					}
			);
			
			queue.add(stringRequest);
		}
		catch (InterruptedException | ExecutionException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
