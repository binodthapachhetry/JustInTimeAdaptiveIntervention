/**
 * 
 */
package edu.neu.android.wocketslib.video.openyoutubeplayer;

public abstract class YouTubeId {
	protected String mId;
	
	public YouTubeId(String pId) {
		mId = pId;
	}
	
	public String getId() {
		return mId;
	}
}