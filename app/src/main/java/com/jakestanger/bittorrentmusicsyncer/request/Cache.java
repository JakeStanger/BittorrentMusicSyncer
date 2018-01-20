package com.jakestanger.bittorrentmusicsyncer.request;

import java.io.File;

/**
 * @author Jake stanger
 * Values cache to avoid re-calculation.
 * Main purpose is to reduce network usage.
 * TODO cache JSON
 */
public class Cache
{
	//TODO Put into some config file somewhere
	public static final String[] URLs = {"http://192.168.0.19/", "http://music.jakestanger.com/"};
	
	private static String reachableURL;
	private static String currentMagnet;
	private static String currentArtist;
	
	private static File APP_DIR; //By default /data/user/0/com.jakestanger.bittorrentmusicsyncer/files
	
	static public String getReachableURL()
	{
		return reachableURL;
	}
	
	static public void setReachableURL(String reachableURL)
	{
		Cache.reachableURL = reachableURL;
	}
	
	public static void setAppDir(File appDir)
	{
		APP_DIR = appDir;
	}
	
	public static File getAppDir()
	{
		return APP_DIR;
	}
	
	public static String getCurrentMagnet()
	{
		return currentMagnet;
	}
	
	public static void setCurrentMagnet(String currentMagnet)
	{
		Cache.currentMagnet = currentMagnet;
	}
	
	public static String[] getURLs()
	{
		return URLs;
	}
	
	public static String getCurrentArtist()
	{
		return currentArtist;
	}
	
	public static void setCurrentArtist(String currentArtist)
	{
		Cache.currentArtist = currentArtist;
	}
}
