package com.jakestanger.bittorrentmusicsyncer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import com.jakestanger.bittorrentmusicsyncer.request.Cache;
import com.jakestanger.bittorrentmusicsyncer.request.ReachableURLGetter;
import com.jakestanger.bittorrentmusicsyncer.view.MediaControllerView;
import com.jakestanger.bittorrentmusicsyncer.wrapper.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * @author Jake stanger
 * TODO Write JavaDoc
 */
public class MusicService extends Service implements MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener, MediaControllerView.MediaPlayerControl
{
	private MediaPlayer mediaPlayer;
	//private MediaControllerView mediaController;
	
	private ArrayList<Track> tracks;
	private int trackIndex;
	
	private final IBinder musicBind = new MusicBinder();

    @Override
	public void onCreate()
	{
		super.onCreate();
		trackIndex = 0;
		mediaPlayer = new MediaPlayer();

        MediaSession session = new MediaSession(this, "MusicService");
		session.setCallback(new MediaSessionCallback());
		
		initMusicPlayer();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}
	
	private void initMusicPlayer()
	{
		//Avoid playback stopping when device sleeps
		mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		
		//mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		
		
		
		//mediaPlayer.prepareAsync();
	}
	
	public void setTracks(ArrayList<Track> tracks)
	{
		this.tracks = tracks;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		//mediaPlayer.stop();
		mediaPlayer.release();
		return false;
	}
	
	public void playTrack(Track track)
	{
		try
		{
			String[] URLs = {"http://192.168.0.19/", "http://music.jakestanger.com/"}; //TODO integrate into class
			String baseURL = Cache.getReachableURL() != null ?
					Cache.getReachableURL() : new ReachableURLGetter().execute(URLs).get();
			
			Uri uri = Uri.parse(baseURL + track.getDownloadURL());
			
			mediaPlayer.reset();

			AudioAttributes.Builder attributes = new AudioAttributes.Builder();
			attributes.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
			attributes.setUsage(AudioAttributes.USAGE_MEDIA);
			
			mediaPlayer.setDataSource(this, uri);
			mediaPlayer.setAudioAttributes(attributes.build());
			mediaPlayer.prepareAsync();
			//mediaPlayer.seekTo(0);
			//mediaPlayer.start();
		}
		catch (InterruptedException | ExecutionException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public void playQueue()
	{
		playTrack(tracks.get(trackIndex));
	}
	
	public void setTrackIndex(int trackIndex)
	{
		this.trackIndex = trackIndex;
	}
	
	public void setTrack(Track track)
	{
		ArrayList<Track> tracks = new ArrayList<>();
		tracks.add(track);
		this.setTracks(tracks);
	}
	
	@Override
	public void onCompletion(MediaPlayer mediaPlayer)
	{
		if(trackIndex < tracks.size() - 1 && getCurrentPosition() > getDuration() / 2)
		{
			trackIndex++;
			playTrack(tracks.get(trackIndex));
		}
		else stopSelf();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(mediaPlayer.isPlaying()) mediaPlayer.stop();
		mediaPlayer.release();
	}
	
	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1)
	{
		return false;
	}
	
	public MediaPlayer getMediaPlayer()
	{
		return mediaPlayer;
	}
	
	public class MusicBinder extends Binder
	{
		public MusicService getService()
		{
			return MusicService.this;
		}
	}
	
	//--Media controller--
	@Override
	public void start()
	{
		mediaPlayer.start();
	}
	
	@Override
	public void pause()
	{
		mediaPlayer.pause();
	}
	
	@Override
	public int getDuration()
	{
		return mediaPlayer.isPlaying() ? mediaPlayer.getDuration() : 0;
	}
	
	@Override
	public int getCurrentPosition()
	{
		return mediaPlayer.getCurrentPosition();
	}
	
	@Override
	public void seekTo(int i)
	{
		mediaPlayer.seekTo(i);
	}
	
	@Override
	public boolean isPlaying()
	{
		return mediaPlayer.isPlaying();
	}
	
	@Override
	public int getBufferPercentage()
	{
		return 0; //TODO investigate
	}
	
	@Override
	public boolean canPause()
	{
		return true;
	}
	
	@Override
	public boolean canSkipBackward()
	{
		return trackIndex - 1 > 0;
	}
	
	@Override
	public boolean canSkipForward()
	{
		return trackIndex < tracks.size() - 1;
	}

	@Override
	public void nextTrack()
	{
		System.out.println("DFKLNH");
		if(canSkipForward())
		{
			trackIndex++;
			playTrack(tracks.get(trackIndex));
		}
	}

	@Override
	public void previousTrack()
	{
		if(canSkipBackward())
		{
			trackIndex--;
			playTrack(tracks.get(trackIndex));
		}
	}
}
