package com.jakestanger.bittorrentmusicsyncer.request;

import android.os.AsyncTask;
import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Jake stanger
 * TODO Write JavaDoc
 */
public class DownloadTorrent extends AsyncTask<List<Byte>, Integer, Void>
{
	@Override
	protected Void doInBackground(List<Byte>... bytes)
	{
		download(bytes[0]);
		return null;
	}
	
	private void download(List<Byte> data)
	{
		try
		{
			//byte[] responseByteArray = getTorrent(torrentURL);
			
			byte[] byteArray = new byte[data.size()];
			for (int index = 0; index < data.size(); index++)
				byteArray[index] = data.get(index);
			
			TorrentInfo torrentInfo = TorrentInfo.bdecode(byteArray);
			
			final SessionManager sessionManager = new SessionManager();
			final CountDownLatch signal = new CountDownLatch(1);
			
			sessionManager.addListener(new AlertListener() {
				@Override
				public int[] types()
				{
					return null;
				}
				
				@Override
				public void alert(Alert<?> alert)
				{
					switch (alert.type())
					{
						case ADD_TORRENT:
							System.out.println("Torrent added");
							((AddTorrentAlert) alert).handle().resume();
							break;
						case BLOCK_FINISHED:
							BlockFinishedAlert a = (BlockFinishedAlert) alert;
							int percentDone = (int) (a.handle().status().progress() * 100);
							
							publishProgress(percentDone);
							System.out.println(sessionManager.stats().totalDownload());
							break;
						case TORRENT_FINISHED:
							System.out.println("Torrent finished");
							signal.countDown();
							break;
					}
				}
			});
			
			sessionManager.start();
			sessionManager.download(torrentInfo, new File(Cache.getAppDir().getAbsolutePath() + "/" + Cache.getCurrentArtist()));
			
			try
			{
				signal.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			sessionManager.stop();
			
			
			//AlertListener listener = new AlertListener() {
			//	@Override
			//	public int[] types()
			//	{
			//		return new int[]{AlertType.SESSION_STATS.swig()};
			//	}
			//
			//	@Override
			//	public void alert(Alert<?> alert)
			//	{
			//		if(alert.type().equals(AlertType.SESSION_STATS)) sessionManager.postDhtStats();
			//		if(alert.type().equals(AlertType.DHT_STATS))
			//		{
			//			long nodes = sessionManager.stats().dhtNodes();
			//			if(nodes >= 10) //TODO Test this
			//			{
			//				System.out.println("DHT contains " + nodes + " nodes");
			//				signal.countDown();
			//			}
			//		}
			//	}
			//};
			
			//sessionManager.addListener(listener);
			//sessionManager.start();
			//sessionManager.postDhtStats();
			//
			//System.out.println("Waiting for nodes in DHT (10 seconds)...");
			//boolean ready = signal.await(5, TimeUnit.MINUTES);
			//if(!ready)
			//{
			//	System.out.println("Timeout");
			//	return;
			//}
			
			////Stop triggering DHT stats
			//sessionManager.removeListener(listener);
			
			//final AtomicInteger counter = new AtomicInteger(0);
			//for(int i = 0; i < 50; i++) //TODO Find out why 50
			//{
			//	final int index = i;
			//	Thread thread = new Thread()
			//	{
			//		@Override
			//		public void run()
			//		{
			//			byte[] data = sessionManager.fetchMagnet(torrentMagnet, 30);
			//			int count = counter.incrementAndGet();
			//			if(data != null) System.out.println("Success fetching magnet: " + index + "/" + count);
			//			else System.out.println("Failed to retrieve the magnet: " + index + "/" + count);
			//		}
			//	};
			//
			//	thread.start();
			//}
			
			//sessionManager.stop();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		System.out.println("Progress: " + values[0]);
	}
}
