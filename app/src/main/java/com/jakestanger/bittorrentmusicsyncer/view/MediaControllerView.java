package com.jakestanger.bittorrentmusicsyncer.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.jakestanger.bittorrentmusicsyncer.R;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author Jake stanger
 * TODO Write JavaDoc
 */
public class MediaControllerView extends FrameLayout
{
	private static final String TAG = "MediaControllerView";
	
	private MediaPlayerControl  mPlayer;
	private Context mContext;
	private ViewGroup mAnchor;
	private View mRoot;
	private ProgressBar mProgress;
	private TextView mEndTime, mCurrentTime;
	private boolean             mShowing;
	private boolean             mDragging;
	private static final int    sDefaultTimeout = 3000;
	private static final int    FADE_OUT = 1;
	private static final int    SHOW_PROGRESS = 2;
	private boolean             mUseFastForward;
	private boolean             mFromXml;
	private boolean             mListenersSet;
	private View.OnClickListener mNextListener, mPrevListener;
	StringBuilder               mFormatBuilder;
	Formatter mFormatter;
	private ImageButton mPauseButton;
	private ImageButton         mFfwdButton;
	private ImageButton         mRewButton;
	private ImageButton         mNextButton;
	private ImageButton         mPrevButton;
	private Handler             mHandler = new MessageHandler(this);
	
	public MediaControllerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRoot = null;
		mContext = context;
		mUseFastForward = true;
		mFromXml = true;
		
		Log.i(TAG, TAG);
	}
	
	public MediaControllerView(Context context, boolean useFastForward) {
		super(context);
		mContext = context;
		mUseFastForward = useFastForward;
		
		Log.i(TAG, TAG);
	}
	
	public MediaControllerView(Context context) {
		this(context, true);
		
		Log.i(TAG, TAG);
	}
	
	@Override
	public void onFinishInflate() {
		if (mRoot != null)
			initControllerView(mRoot);
	}
	
	public void setMediaPlayer(MediaPlayerControl player) {
		mPlayer = player;
		updatePausePlay();
	}
	
	/**
	 Устанавливает якорь на ту вьюху на которую вы хотите разместить контролы
	 Это может быть например VideoView или SurfaceView
	 */
	public void setAnchorView(ViewGroup view) {
		mAnchor = view;
		
		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		
		removeAllViews();
		View v = makeControllerView();
		addView(v, frameParams);
	}
	
	/**
	 Создает вьюху которая будет находится поверх вашего VideoView или другого контролла
	 */
	protected View makeControllerView() {
		LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRoot = inflate.inflate(R.layout.media_controller, null);
		
		initControllerView(mRoot);
		
		return mRoot;
	}
	
	private void initControllerView(View v) {
		mPauseButton = v.findViewById(R.id.pause);
		if (mPauseButton != null) {
			mPauseButton.requestFocus();
			mPauseButton.setOnClickListener(mPauseListener);
		}
		
		mFfwdButton = v.findViewById(R.id.ffwd);
		if (mFfwdButton != null) {
			mFfwdButton.setOnClickListener(mFfwdListener);
			if (!mFromXml) {
				mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
			}
		}
		
		mRewButton = v.findViewById(R.id.rew);
		if (mRewButton != null) {
			mRewButton.setOnClickListener(mRewListener);
			if (!mFromXml) {
				mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
			}
		}
		
		// По дефолту вьюха будет спрятана, и показываться будет после события onTouch()
		mNextButton = v.findViewById(R.id.next);
		if (mNextButton != null && !mFromXml && !mListenersSet) {
			mNextButton.setVisibility(View.GONE);
		}
		mPrevButton = v.findViewById(R.id.prev);
		if (mPrevButton != null && !mFromXml && !mListenersSet) {
			mPrevButton.setVisibility(View.GONE);
		}
		
		mProgress = v.findViewById(R.id.mediacontroller_progress);
		if (mProgress != null) {
			if (mProgress instanceof SeekBar) {
				SeekBar seeker = (SeekBar) mProgress;
				seeker.setOnSeekBarChangeListener(mSeekListener);
			}
			mProgress.setMax(1000);
		}
		
		mEndTime = v.findViewById(R.id.time);
		mCurrentTime = v.findViewById(R.id.time_current);
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
		
		installPrevNextListeners();
	}
	
	/**
	 Показывает контроллер на экране
	 Он будет убран через 3 секуунды
	 */
	public void show() {
		show(sDefaultTimeout);
	}
	
	/**
	 * Отключить паузу или seek button, если поток не может быть приостановлена
	 * Это требует интерфейс управления MediaPlayerControlExt
	 */
	private void disableUnsupportedButtons() {
		if (mPlayer == null) {
			return;
		}
		
		try {
			if (mPauseButton != null && !mPlayer.canPause()) {
				mPauseButton.setEnabled(false);
			}
			if (mRewButton != null && !mPlayer.canSeekBackward()) {
				mRewButton.setEnabled(false);
			}
			if (mFfwdButton != null && !mPlayer.canSeekForward()) {
				mFfwdButton.setEnabled(false);
			}
		} catch (IncompatibleClassChangeError ex) {
			//выводите в лог что хотите из ex
		}
	}
	
	/**
	 * Показывает контроллы на экране
	 * Он исчезнет автоматически после своего таймаута
	 */
	public void show(int timeout) {
		if (!mShowing && mAnchor != null) {
			setProgress();
			if (mPauseButton != null) {
				mPauseButton.requestFocus();
			}
			disableUnsupportedButtons();
			
			FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM
			);
			
			mAnchor.addView(this, tlp);
			mShowing = true;
		}
		updatePausePlay();
		
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		
		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(msg, timeout);
		}
	}
	
	public boolean isShowing() {
		return mShowing;
	}
	
	/**
	 * Удаляем контроллы с экрана.
	 */
	public void hide() {
		if (mAnchor == null) {
			return;
		}
		
		try {
			mAnchor.removeView(this);
			mHandler.removeMessages(SHOW_PROGRESS);
		} catch (IllegalArgumentException ex) {
			Log.w("MediaController", "already removed");
		}
		mShowing = false;
	}
	
	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;
		
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours   = totalSeconds / 3600;
		
		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}
	
	private int setProgress() {
		if (mPlayer == null || mDragging) {
			return 0;
		}
		
		int position = mPlayer.getCurrentPosition();
		int duration = mPlayer.getDuration();
		if (mProgress != null) {
			if (duration > 0) {
				// use long to avoid overflow
				long pos = 1000L * position / duration;
				mProgress.setProgress( (int) pos);
			}
			int percent = mPlayer.getBufferPercentage();
			mProgress.setSecondaryProgress(percent * 10);
		}
		
		if (mEndTime != null)
			mEndTime.setText(stringForTime(duration));
		if (mCurrentTime != null)
			mCurrentTime.setText(stringForTime(position));
		
		return position;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		show(sDefaultTimeout);
		return true;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		show(sDefaultTimeout);
		return false;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mPlayer == null) {
			return true;
		}
		
		int keyCode = event.getKeyCode();
		final boolean uniqueDown = event.getRepeatCount() == 0
				&& event.getAction() == KeyEvent.ACTION_DOWN;
		if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			if (uniqueDown) {
				doPauseResume();
				show(sDefaultTimeout);
				if (mPauseButton != null) {
					mPauseButton.requestFocus();
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
			if (uniqueDown && !mPlayer.isPlaying()) {
				mPlayer.start();
				updatePausePlay();
				show(sDefaultTimeout);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
				|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
			if (uniqueDown && mPlayer.isPlaying()) {
				mPlayer.pause();
				updatePausePlay();
				show(sDefaultTimeout);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
			return super.dispatchKeyEvent(event);
		} else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
			if (uniqueDown) {
				hide();
			}
			return true;
		}
		
		show(sDefaultTimeout);
		return super.dispatchKeyEvent(event);
	}
	
	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
			show(sDefaultTimeout);
		}
	};
	
	public void updatePausePlay() {
		if (mRoot == null || mPauseButton == null || mPlayer == null) {
			return;
		}
		
		if (mPlayer.isPlaying()) {
			mPauseButton.setImageResource(R.drawable.pause_black);
		} else {
			mPauseButton.setImageResource(R.drawable.play_black);
		}
	}
	
	private void doPauseResume() {
		if (mPlayer == null) {
			return;
		}
		
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
		} else {
			mPlayer.start();
		}
		updatePausePlay();
	}
	
	private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			show(3600000);
			
			mDragging = true;
			mHandler.removeMessages(SHOW_PROGRESS);
		}
		
		public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
			if (mPlayer == null) {
				return;
			}
			
			if (!fromUser) {
				return;
			}
			
			long duration = mPlayer.getDuration();
			long newPos = (duration * progress) / 1000L;
			mPlayer.seekTo( (int) newPos);
			if (mCurrentTime != null)
				mCurrentTime.setText(stringForTime( (int) newPos));
		}
		
		public void onStopTrackingTouch(SeekBar bar) {
			mDragging = false;
			setProgress();
			updatePausePlay();
			show(sDefaultTimeout);
			
			mHandler.sendEmptyMessage(SHOW_PROGRESS);
		}
	};
	
	@Override
	public void setEnabled(boolean enabled) {
		if (mPauseButton != null) {
			mPauseButton.setEnabled(enabled);
		}
		if (mFfwdButton != null) {
			mFfwdButton.setEnabled(enabled);
		}
		if (mRewButton != null) {
			mRewButton.setEnabled(enabled);
		}
		if (mNextButton != null) {
			mNextButton.setEnabled(enabled && mNextListener != null);
		}
		if (mPrevButton != null) {
			mPrevButton.setEnabled(enabled && mPrevListener != null);
		}
		if (mProgress != null) {
			mProgress.setEnabled(enabled);
		}
		disableUnsupportedButtons();
		super.setEnabled(enabled);
	}
	
	private View.OnClickListener mRewListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mPlayer == null) {
				return;
			}
			
			int pos = mPlayer.getCurrentPosition();
			pos -= 5000; // милисекунд
			mPlayer.seekTo(pos);
			setProgress();
			
			show(sDefaultTimeout);
		}
	};
	
	private View.OnClickListener mFfwdListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mPlayer == null) {
				return;
			}
			
			int pos = mPlayer.getCurrentPosition();
			pos += 15000; // милисекунд
			mPlayer.seekTo(pos);
			setProgress();
			
			show(sDefaultTimeout);
		}
	};
	
	private void installPrevNextListeners() {
		if (mNextButton != null) {
			mNextButton.setOnClickListener(mNextListener);
			mNextButton.setEnabled(mNextListener != null);
		}
		
		if (mPrevButton != null) {
			mPrevButton.setOnClickListener(mPrevListener);
			mPrevButton.setEnabled(mPrevListener != null);
		}
	}
	
	public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
		mNextListener = next;
		mPrevListener = prev;
		mListenersSet = true;
		
		if (mRoot != null) {
			installPrevNextListeners();
			
			if (mNextButton != null && !mFromXml) {
				mNextButton.setVisibility(View.VISIBLE);
			}
			if (mPrevButton != null && !mFromXml) {
				mPrevButton.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public interface MediaPlayerControl {
		void    start();
		void    pause();
		int     getDuration();
		int     getCurrentPosition();
		void    seekTo(int pos);
		boolean isPlaying();
		int     getBufferPercentage();
		boolean canPause();
		boolean canSeekBackward();
		boolean canSeekForward();
	}
	
	private static class MessageHandler extends Handler
	{
		private final WeakReference<MediaControllerView> mView;
		
		MessageHandler(MediaControllerView view) {
			mView = new WeakReference<>(view);
		}
		@Override
		public void handleMessage(Message msg) {
			MediaControllerView view = mView.get();
			if (view == null || view.mPlayer == null) {
				return;
			}
			
			int pos;
			switch (msg.what) {
				case FADE_OUT:
					view.hide();
					break;
				case SHOW_PROGRESS:
					pos = view.setProgress();
					if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
						msg = obtainMessage(SHOW_PROGRESS);
						sendMessageDelayed(msg, 1000 - (pos % 1000));
					}
					break;
			}
		}
	}
}
