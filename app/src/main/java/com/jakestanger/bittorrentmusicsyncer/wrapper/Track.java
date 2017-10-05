package com.jakestanger.bittorrentmusicsyncer.wrapper;

/**
 * @author Jake stanger
 * Track wrapper
 */
public class Track
{
	private String title;
	private String titleSort;
	private String summary;
	private String key;
	private String downloadURL;
	
	public Track(String title, String titleSort, String summary, String key, String downloadURL)
	{
		this.title = title;
		this.titleSort = titleSort;
		this.summary = summary;
		this.key = key;
		this.downloadURL = downloadURL.replace(" ", "%20").replace("'", "%27");
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getTitleSort()
	{
		return titleSort;
	}
	
	public void setTitleSort(String titleSort)
	{
		this.titleSort = titleSort;
	}
	
	public String getSummary()
	{
		return summary;
	}
	
	public void setSummary(String summary)
	{
		this.summary = summary;
	}
	
	public String getKey()
	{
		return key;
	}
	
	public void setKey(String key)
	{
		this.key = key;
	}
	
	public String getDownloadURL()
	{
		return downloadURL;
	}
	
	public void setDownloadURL(String downloadURL)
	{
		this.downloadURL = downloadURL;
	}
}