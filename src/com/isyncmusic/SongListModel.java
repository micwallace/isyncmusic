package com.isyncmusic;

import java.io.Serializable;

public class SongListModel implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	 private String artist;
	 private String album;
	 private String size;
	 private String relpath;
	    public SongListModel(String _name, String _path, String _artist, String _album, String _size) {
	        this.name = _name;
	        this.relpath = _path;
	        artist = _artist;
	        album = _album;
	        size = _size;
	    }
	    @Override
	    public String toString() {
	        return this.name;
	    }
	    public String getRelPath() {
	        return this.relpath;
	    }
	    public String getArtist(){
	    	return artist;
	    }
	    public String getAlbum(){
	    	return album;
	    }
	    public String getSize(){
	    	return size;
	    }
}
