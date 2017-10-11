package com.jakestanger.bittorrentmusicsyncer.request;

/**
 * @author Jake stanger
 * Values cache to avoid re-calculation.
 * Main purpose is to reduce network usage.
 * TODO cache JSON
 */
public class Cache
{
	private static String reachableURL;
	
	static public String getReachableURL()
	{
		return reachableURL;
	}
	
	static public void setReachableURL(String reachableURL)
	{
		Cache.reachableURL = reachableURL;
	}
}
