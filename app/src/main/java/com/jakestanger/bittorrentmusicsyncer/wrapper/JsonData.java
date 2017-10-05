package com.jakestanger.bittorrentmusicsyncer.wrapper;

/**
 * @author Jake stanger
 * JSON music data wrapper.
 */
public class JsonData
{
	private String artistName = "", albumName = "";
	private Type type;
	
	public JsonData(){}
	
	public JsonData(String artistName)
	{
		this.artistName = artistName;
	}
	
	public JsonData(String artistName, String albumName)
	{
		this.artistName = artistName;
		this.albumName = albumName;
	}
	
	public JsonData(String artistName, String albumName, Type type)
	{
		this.artistName = artistName;
		this.albumName = albumName;
		this.type = type;
	}
	
	public String getArtistName()
	{
		return artistName;
	}
	
	public void setArtistName(String artistName)
	{
		this.artistName = artistName;
	}
	
	public String getAlbumName()
	{
		return albumName;
	}
	
	public void setAlbumName(String albumName)
	{
		this.albumName = albumName;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public void setType(Type type)
	{
		this.type = type;
	}
	
	public enum Type {ARTIST, ALBUM, TRACK}
}