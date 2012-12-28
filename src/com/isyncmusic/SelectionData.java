package com.isyncmusic;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectionData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// checkbox values; can be added/removed automatically when sub-item is ticked; rebuilt when collating with new index
	private ArrayList<String> selectartist;
	private ArrayList<String> selectalbum;
	private ArrayList<String> selectsong;
	// sync values and exclusion lists; only added and removed by user input; used to determine what to collate/exclude from collation
	private ArrayList<String> syncartist;
	private HashMap<String, ArrayList> syncalbum;
	private HashMap<String, ArrayList> syncsong;
	private HashMap<String, ArrayList> excludealbum;
	private HashMap<String, ArrayList> excludesong;
	public HashMap<String, Object> selectlist; // master list, current items that are selected for sync; rebuilt when collating with new index
	public SelectionData(){
		selectlist = new HashMap<String, Object>();
		selectartist = new ArrayList<String>();
		selectalbum = new ArrayList<String>();
		selectsong = new ArrayList<String>();
		syncartist = new ArrayList<String>();
		syncalbum = new HashMap<String, ArrayList>();
		syncsong = new HashMap<String, ArrayList>();
		excludealbum = new HashMap<String, ArrayList>();
		excludesong = new HashMap<String, ArrayList>();
	}
	public void clearSelectList(){
		// clear all values except what the user chose to sync; rebuilt via collatenewindex
		selectlist.clear();
		selectartist.clear();
		selectalbum.clear();
		selectsong.clear();
	}
	// sync lists; determines what must be updated (synced) with collation with new index
	// artists
	public void addArtistSync(String _artist){
		if (!syncartist.contains(_artist)){
			syncartist.add(_artist);
		}
	}
	public void removeArtistSync(String _artist){
		syncartist.remove(_artist);
	}
	public boolean isArtistSync(String _artist){
		if (syncartist.contains(_artist)){
			return true;
		} else {
			return false;
		}
	}
	public ArrayList<String> getArtistSync(){
		return syncartist;
	}
	// NOTE: The following functions perform similar tasks and should be combined into one using static integer vars to determine variable to write to 
	// albums
	public void addAlbumSync(String _album, String _artist){
		ArrayList temparr = null;
		if (syncalbum.containsKey(_artist)){
			temparr = syncalbum.get(_artist);
			if (!temparr.contains(_album)){
				temparr.add(_album);
			}
			syncalbum.remove(_artist);
		} else {
			temparr = new ArrayList<String>();
			temparr.add(_album);
		}
		syncalbum.put(_artist, temparr);
	}
	public void removeAlbumSync(String _album, String _artist){
		if (syncalbum.containsKey(_artist)){
			ArrayList temparr = syncalbum.get(_artist);
			temparr.remove(_album);
			syncalbum.remove(_artist);
			syncalbum.put(_artist, temparr);
		}
	}
	public void removeArtistAlbumSync(String _artist){
		syncalbum.remove(_artist);
	}
	public ArrayList getArtistAlbumSync(String _artist){
		return syncalbum.get(_artist);
	}
	public boolean isAlbumSync(String _album, String _artist){
		if (syncalbum.containsKey(_artist)){
			ArrayList temparr = syncalbum.get(_artist);
			if (temparr.contains(_album)){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	public HashMap<String, ArrayList> getAlbumSync(){
		return syncalbum;
	}
	// single songs
	public void addSongSync(String _song, String _artist){
		ArrayList temparr = null;
		if (syncsong.containsKey(_artist)){
			temparr = syncsong.get(_artist);
			temparr.add(_song);
			syncsong.remove(_artist);
		} else {
			temparr = new ArrayList<String>();
			temparr.add(_song);
		}
		syncsong.put(_artist, temparr);
	}
	public void removeSongSync(String _song, String _artist){
		if (syncsong.containsKey(_artist)){
			ArrayList temparr = syncsong.get(_artist);
			temparr.remove(_song);
			syncsong.remove(_artist);
			syncsong.put(_artist, temparr);
		}
	}
	public boolean isSongSync(String _song, String _artist){
		if (syncalbum.containsKey(_artist)){
			ArrayList temparr = syncalbum.get(_artist);
			if (temparr.contains(_song)){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	public void removeArtistSongSync(String _artist){
		syncsong.remove(_artist);
	}
	public HashMap<String, ArrayList> getSongSync(){
		return syncsong;
	}
	// album and song exclusions lists; when an artist is synced, and an album/song is unchecked, we use these to determine what must be excluded from the artist/album tracks
	// songs
	public void addSongExclude(String _song, String _artist){
		ArrayList temparr = null;
		if (excludesong.containsKey(_artist)){
			temparr = excludesong.get(_artist);
			temparr.add(_song);
			excludesong.remove(_artist);
		} else {
			temparr = new ArrayList<String>();
			temparr.add(_song);
		}
		excludesong.put(_artist, temparr);
	}
	public void removeSongExclude(String _song, String _artist){
		if (excludesong.containsKey(_artist)){
			ArrayList temparr = excludesong.get(_artist);
			temparr.remove(_song);
			excludesong.remove(_artist);
			if (!temparr.isEmpty()){
				excludesong.put(_artist, temparr);
			}
		}
	}
	public void removeArtistSongExclude(String _artist){
		excludesong.remove(_artist);
	}
	public HashMap<String, ArrayList> getSongExclude(){
		return excludesong;
	}
	// albums
	public void addAlbumExclude(String _album, String _artist){
		ArrayList temparr = null;
		if (excludealbum.containsKey(_artist)){
			temparr = excludealbum.get(_artist);
			if (!temparr.contains(_album)){
				temparr.add(_album);
			}
			excludealbum.remove(_artist);
		} else {
			temparr = new ArrayList<String>();
			temparr.add(_album);
		}
		excludealbum.put(_artist, temparr);
	}
	public void removeAlbumExclude(String _album, String _artist){
		if (excludealbum.containsKey(_artist)){
			ArrayList temparr = excludealbum.get(_artist);
			temparr.remove(_album);
			excludealbum.remove(_artist);
			if (!temparr.isEmpty()){
				excludealbum.put(_artist, temparr);
			}
		}
	}
	public boolean isAlbumExcluded(String _album, String _artist){
		if (excludealbum.containsKey(_artist)){
			ArrayList temparr = excludealbum.get(_artist);
			if (temparr.contains(_album)){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	public void removeArtistAlbumExclude(String _artist){
		excludealbum.remove(_artist);
	}
	public HashMap<String, ArrayList> getAlbumExclude(){
		return excludealbum;
	}
	// selection lists used to determine check-box values
	public void addToSelectionList(int _list, String _value){
		  switch (_list){
		  	case 1: selectartist.add(_value);
			  break;
		  	case 2: selectalbum.add(_value);
		  	  break;
		  	case 3: selectsong.add(_value);
		  	  break;
		  }
	  }
	public void removeFromSelectionList(int _list, String _value){
		  switch (_list){
		  	case 1: selectartist.remove(_value);
			  break;
		  	case 2: selectalbum.remove(_value);
		  	  break;
		  	case 3: selectsong.remove(_value);
		  	  break;
		  }
	}
	// used to find whether a value should be checked in the list
	public boolean valueexists(int _list, String _value){
		 switch (_list){
		  	case 1: if (selectartist.contains(_value)){ return true; }else{ return false; }
		  	case 2: if (selectalbum.contains(_value)){ return true; }else{ return false; }
		  	case 3: if (selectsong.contains(_value)){ return true; }else{ return false; }
		 }
		 return false;
	}
}
