package com.isyncmusic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import android.os.Environment;
import android.util.Log;

public class SelectionList {
	final String ROOTPATH = Environment.getExternalStorageDirectory() + "/Music/isyncedmusic/";
	public SelectionData selectdata;
	private String currentartist;
	private ReadIndex index;
	private ArrayList<SongListModel> dllist;
	private Long dlsize;
	private int selectcount = 0;
	private ArrayList<String> deletelist;

	public SelectionList(ReadIndex _index) {
		index = _index;
		File selectfile = new File(
				"/data/data/com.isyncmusic/files/selectionlist");
		if (selectfile.exists()) {
			// read current select object
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream("/data/data/com.isyncmusic/files/selectionlist");
				in = new ObjectInputStream(fis);
				selectdata = (SelectionData) in.readObject();
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// collate with new index if new index indicator == 1
		} else {
			// create new select object
			selectdata = new SelectionData();
		}
		deletelist = new ArrayList<String>();
	}
	
	public void clearSelectionData(){
		selectdata = new SelectionData();
		try {
			saveSelectionData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// nullify download list to provoke recalculation of list and size
		dllist = null;
		selectcount = 0;
	}
	// select all current artists for sync
	public void selectAll(){
		selectdata = new SelectionData();
		selectcount = 0;
		Iterator<String> it = index.getallartists().iterator();
		while (it.hasNext()){
			// user initiated set as true here (to add to sync lists)
			addartist(it.next().toString(), true);
		}
		try {
			saveSelectionData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCurrentArtist(String _cartist) {
		currentartist = _cartist;
	}
	// generates download list and also counts selected songs
	private void genDownloadList() {
		dllist = new ArrayList<SongListModel>();
		// reset download size
		dlsize = (long)0;
		// Iterate through selectlist and add non existant files to download
		// list
		Iterator it = selectdata.selectlist.values().iterator();
		while (it.hasNext()) {
			ArrayList<SongListModel> tempal = (ArrayList<SongListModel>) it.next();
			Iterator<SongListModel> it2 = tempal.iterator();
			while (it2.hasNext()) {
				SongListModel tempobj = (SongListModel) it2.next();
				// add to download list if it does not exist
				File abspath = new File(ROOTPATH+ tempobj.getRelPath());
				if (!abspath.exists()) {
					// add object to download list and size to download size
					dllist.add(tempobj);
					dlsize = dlsize + Long.valueOf(tempobj.getSize());
				}
				// increment select counter
				selectcount++;
			}
		}
	}
	public int getSelectCount(){
		if (dllist == null){
			genDownloadList();
		}
		return selectcount;
	}
	public ArrayList<SongListModel> getDownloadList(){
		if (dllist == null){
			genDownloadList();
		}
		return dllist;
	}
	
	public Long getDownloadSize(){
		if (dllist == null){
			genDownloadList();
		}
		return dlsize;
	}
	
	public int getDownloadCount(){
		if (dllist == null){
			genDownloadList();
		}
		return dllist.size();
	}
	
	public void addDownload(SongListModel newdl){
		if (dllist != null){
			File abspath = new File(ROOTPATH+ newdl.getRelPath());
			if (!abspath.exists()) {
				dllist.add(newdl);
				dlsize+=Long.valueOf(newdl.getSize());
			}
			selectcount++;
		}
	}

	public void removeDownloads(ArrayList<SongListModel> songlist){
		if (dllist != null){
			Iterator<SongListModel> it = songlist.iterator();
			while (it.hasNext()){
				removeDownload(it.next());
			}
		}
	}
	
	public void removeDownload(SongListModel olddl){
		if (dllist != null){
			Iterator<SongListModel> it = dllist.iterator();
			while (it.hasNext()){
				if (it.next().getRelPath().equals(olddl.getRelPath())){
					it.remove();
					dlsize-=Long.valueOf(olddl.getSize());
					break;
				}
			}
			selectcount--;
		}
	}

	public void saveSelectionData() throws IOException {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		fos = new FileOutputStream(
				"/data/data/com.isyncmusic/files/selectionlist");
		out = new ObjectOutputStream(fos);
		out.writeObject(selectdata);
		out.close();
	}

	public void collateWithNewIndex() {
		// clear current hashmap and checkbox list values
		selectdata.clearSelectList();
		// nullify download list to recalculate (including size)
	    dllist = null;
		// update artists
		Iterator<String> artistit = selectdata.getArtistSync().iterator();
		while (artistit.hasNext()) {
			addartist(artistit.next(), false);
		}
		// update albums
		Iterator<Entry<String, ArrayList>> albumit = selectdata.getAlbumSync()
				.entrySet().iterator();
		while (albumit.hasNext()) {
			Entry currentobj = albumit.next();
			if (!selectdata.isArtistSync(currentobj.getKey().toString())) {
				// set current artist, very important
				currentartist = currentobj.getKey().toString();
				Iterator<String> albumit2 = ((ArrayList<String>) currentobj
						.getValue()).iterator();
				while (albumit2.hasNext()) {
					addalbum(albumit2.next().toString(), false);
				}
			}
		}
		// remove exclusions
		// remove albums
		Iterator<Entry<String, ArrayList>> albumexit = selectdata
				.getAlbumExclude().entrySet().iterator();
		while (albumexit.hasNext()) {
			Entry currentobj = albumexit.next();
			// set current artist, very important
			currentartist = currentobj.getKey().toString();
			Iterator<String> albumexit2 = ((ArrayList<String>) currentobj
					.getValue()).iterator();
			while (albumexit2.hasNext()) {
				removealbum(albumexit2.next().toString(), false);
			}
		}
		// remove songs
		Iterator<Entry<String, ArrayList>> songexit = selectdata
				.getSongExclude().entrySet().iterator();
		while (songexit.hasNext()) {
			Entry currentobj = songexit.next();
			// songexit.remove();
			// set current artist, very important
			currentartist = currentobj.getKey().toString();
			Iterator<String> songexit2 = ((ArrayList<String>) currentobj
					.getValue()).iterator();
			while (songexit2.hasNext()) {
				removeSong(songexit2.next().toString(), false);
			}
		}
		// add single songs last; incase user has excluded album then selected
		// songs within that album
		Iterator<Entry<String, ArrayList>> songit = selectdata.getSongSync()
				.entrySet().iterator();
		while (songit.hasNext()) {
			Entry currentobj = songit.next();
			// set current artist, very important
			currentartist = currentobj.getKey().toString();
			// iterate through songs names and add to list
			Iterator<String> songit2 = ((ArrayList<String>) currentobj.getValue()).iterator();
			while (songit2.hasNext()) {
				addSong(songit2.next().toString(), false);
			}
		}
	}

	// DELETE list functions
	public boolean isDeletions() {
		if (deletelist.isEmpty()) {
			return false;
		} else {
			genFileDeletions();
			if (deletelist.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	public ArrayList<String> getDeletions(){
		return deletelist;
	}

	public void addDeletion(String relpath) {
		//if (!deletelist.contains(relpath)) {
			deletelist.add(relpath);
		//}
	}

	public void removeDeletion(String relpath) {
		deletelist.remove(relpath);
	}

	public void genFileDeletions() {
		Iterator<String> it = deletelist.iterator();
		while (it.hasNext()) {
			// replace any naughty windows backslashes, they just had to be
			// fucking different those bastards.
			String path = it.next().replaceAll("\\\\", "/");
			// check if file exsist, if not remove from list
			if (!(new File(ROOTPATH + path)).exists()) {
				it.remove();
			}
		}
	}

	public void runDeletions() {
		Iterator<String> it = deletelist.iterator();
		while (it.hasNext()) {
			File tempfile = new File(ROOTPATH + (it.next().replaceAll("\\\\", "/")));
			System.out.println(tempfile.toString());
			if (tempfile.exists()) {
				tempfile.delete();
				it.remove();
			}
		}
	}

	// ADD / REMOVE functions
	public void addartist(String _artist, boolean userinitiated) {
		String artist = _artist;
		// get the full list of artist songs and insert into hashmap
		ArrayList<SongListModel> songs = index.getartistsongs(artist);
		//Log.w("isyncmusic", songs.toString());
		selectdata.selectlist.put(artist, songs);
		// add values to lists
		selectdata.addToSelectionList(1, artist);
		// add albums and songs
		Iterator it = songs.iterator();
		while (it.hasNext()) {
			SongListModel song = (SongListModel) it.next();
			selectdata.addToSelectionList(3, song.toString());
			// Possible bug, album will be added to list multiple times
			selectdata.addToSelectionList(2, song.getAlbum());
			// remove possible entry in deletelist
			removeDeletion(song.getRelPath());
			// add entry in download list if file does not exist; SHOULD THIS BE UNDER USERINITIATED? Yes because these lists are rebuilt by genDownloadsLists, if the list is null function will not add to the list
			if (userinitiated) {
				File abspath = new File(ROOTPATH+ song.getRelPath());
				if (!abspath.exists()) {
					addDownload(song);
				}
			}
			//
		}
		// add to artist sync list
		if (userinitiated) {
			selectdata.addArtistSync(_artist);
			selectdata.removeArtistAlbumExclude(_artist);
			selectdata.removeArtistSongExclude(_artist);
		}
	}

	public void removeartist(String _name, boolean userinitiated) {
		String name = _name;
		// remove from master list
		selectdata.selectlist.remove(name);
		// remove values from lists
		selectdata.removeFromSelectionList(1, name);
		// remove albums and songs
		ArrayList<SongListModel> songs = index.getartistsongs(name);
		Iterator it = songs.iterator();
		while (it.hasNext()) {
			SongListModel song = (SongListModel) it.next();
			// remove from checkbox 'helper lists'
			selectdata.removeFromSelectionList(3, song.toString());
			selectdata.removeFromSelectionList(2, song.getAlbum());
			// add to deletelist
			addDeletion(song.getRelPath());
		}
		// remove possible entries in download list
		if (userinitiated) {
			removeDownloads(songs);
		}
		// remove artist from all sync lists and exclusion lists
		selectdata.removeArtistSync(name);
		selectdata.removeArtistAlbumSync(name);
		selectdata.removeArtistSongSync(name);
		selectdata.removeArtistAlbumExclude(name);
		selectdata.removeArtistSongExclude(name);
	}

	public void addalbum(String _name, boolean userinitiated) {
		String name = _name;
		ArrayList<SongListModel> currentsongs = null;
		// get the albums values as an array list
		ArrayList<SongListModel> songs = index.getartistalbumsongs(
				currentartist, name);
		// check if artist entry exists in the hashmap, act accordingly
		if (selectdata.selectlist.containsKey(currentartist)) {
			currentsongs = (ArrayList<SongListModel>) selectdata.selectlist
					.get(currentartist);
			// add together
			currentsongs.addAll(songs);
			// remove old entry
			selectdata.selectlist.remove(currentartist);
		} else {
			currentsongs = songs;
		}
		// add to hashmap
		selectdata.selectlist.put(currentartist, currentsongs);
		// refresh lists
		selectdata.addToSelectionList(2, name);
		if (!selectdata.valueexists(1, currentartist)) {
			selectdata.addToSelectionList(1, currentartist);
		}
		Iterator<SongListModel> it = songs.iterator();
		while (it.hasNext()) {
			SongListModel tempobj = it.next();
			selectdata.addToSelectionList(3, tempobj.toString());
			// remove possible entry in deletelist
			removeDeletion(tempobj.getRelPath());
			// add entry in download list if file does not exist
			if (userinitiated) {
				addDownload(tempobj);
			}
		}
		// remove from exclusion list if artist is synced else add to sync list,
		// like most of the music add/remove functions this section is not
		// executed during a collation to protect the user specified values
		if (userinitiated) {
			if (selectdata.isArtistSync(currentartist)) {
				selectdata.removeAlbumExclude(name, currentartist);
			} else {
				selectdata.addAlbumSync(name, currentartist);
			}
		}
		// this syncs the whole artist if all albums are checked; not wanted!
		/*
		 * if (index.getartistalbums(currentartist).size() ==
		 * selectdata.getArtistAlbumSync(currentartist).size()){
		 * selectdata.addArtistSync(currentartist); }
		 */
	}

	public void removealbum(String _name, boolean userinitiated) {
		String name = _name;
		// get the albums values as an array list
		ArrayList<SongListModel> songs = index.getartistalbumsongs(
				currentartist, name);
		// get the current list from the selection hashmap
		ArrayList<SongListModel> currentsongs = (ArrayList<SongListModel>) selectdata.selectlist
				.get(currentartist);
		// set finalsong arraylist
		ArrayList<SongListModel> finalsongs = new ArrayList<SongListModel>();
		// iterate through currentsongs and add all not found in 'songs' to
		// final list
		Iterator<SongListModel> it = currentsongs.iterator();
		boolean matchfound = false;
		while (it.hasNext()) {
			SongListModel songname = (SongListModel) it.next();
			Iterator<SongListModel> it2 = songs.iterator();
			matchfound = false;
			while (it2.hasNext()) {
				SongListModel albumsong = (SongListModel) it2.next();
				if (albumsong.toString().equals(songname.toString())) {
					// remove from song checkbox list
					selectdata.removeFromSelectionList(3, songname.toString());
					// set match found indicator to true
					matchfound = true;
					// add to deletelist
					addDeletion(songname.getRelPath());
					// remove possible entry in download list
					removeDownload(songname);
				}
				// remove from song exclusion and sync lists if user initiated
				if (userinitiated && albumsong.getAlbum().equals(name)) {
					selectdata.removeSongSync(albumsong.toString(),
							currentartist);
					selectdata.removeSongExclude(albumsong.toString(),
							currentartist);
				}
			}
			if (!matchfound) {
				// add to final hashmap if match not found
				finalsongs.add(songname);
			}
			// it.remove();
		}
		// remove from album and artist list if no songs left
		selectdata.removeFromSelectionList(2, name);
		// remove from hashmap
		selectdata.selectlist.remove(currentartist);
		if (finalsongs.size() > 0) {
			Log.w("isyncmusic", "Current artist songs: " + finalsongs.size());
			// insert in hashmap if values exist
			selectdata.selectlist.put(currentartist, finalsongs);
		} else {
			selectdata.removeFromSelectionList(1, currentartist);
		}
		// remove from sync/exclude lists if user initiated
		if (userinitiated) {
			if (selectdata.isArtistSync(currentartist)) {
				selectdata.addAlbumExclude(name, currentartist);
				// remove from song selection lists done above
			} else {
				selectdata.removeAlbumSync(name, currentartist);
			}
		}
	}

	public boolean isAlbumChecked(String _album) {
		if (selectdata.valueexists(2, _album)) {
			int valexists = 0;
			Log.w("isyncmusic", " " + currentartist);
			if (selectdata.selectlist.containsKey(currentartist)) {
				ArrayList tal = (ArrayList) selectdata.selectlist
						.get(currentartist);
				Iterator it = tal.iterator();
				while (it.hasNext()) {
					SongListModel tempitem = (SongListModel) it.next();
					if (tempitem.getAlbum().equals(_album)) {
						valexists = 1;
					}
				}
			}
			return valexists == 1 ? true : false;
		} else {
			return false;
		}
	}

	public boolean isSongChecked(String _song) {
		if (selectdata.valueexists(3, _song)) {
			int valexists = 0;
			Log.w("isyncmusic", " " + currentartist);
			if (selectdata.selectlist.containsKey(currentartist)) {
				ArrayList tal = (ArrayList) selectdata.selectlist
						.get(currentartist);
				Iterator it = tal.iterator();
				while (it.hasNext()) {
					SongListModel tempitem = (SongListModel) it.next();
					if (tempitem.toString().equals(_song)) {
						valexists = 1;
					}
				}
			}
			return valexists == 1 ? true : false;
		} else {
			return false;
		}
	}

	public void addSong(String _name, boolean userinitiated) {
		// get all current artist songs
		ArrayList<SongListModel> songs = index.getartistsongs(currentartist);
		Iterator it = songs.iterator();
		// find song values and add to array
		String songalbum = null;
		while (it.hasNext()) {
			SongListModel song = (SongListModel) it.next();
			if (song.toString().equals(_name)) {
				songalbum = song.getAlbum();
				selectdata.addToSelectionList(2, songalbum);
				ArrayList<SongListModel> templist;
				if (selectdata.selectlist.containsKey(currentartist)) {
					templist = (ArrayList<SongListModel>) selectdata.selectlist
							.get(currentartist);
					selectdata.selectlist.remove(currentartist);
				} else {
					templist = new ArrayList<SongListModel>();
				}
				templist.add(song);
				selectdata.selectlist.put(currentartist, templist);
				// remove possible entry in deletelist
				removeDeletion(song.getRelPath());
				// add entry in download list if file does not exist
				if (userinitiated) {
					addDownload(song);
				}
			}
		}
		if (!selectdata.valueexists(1, currentartist)) {
			selectdata.addToSelectionList(1, currentartist);
		}
		selectdata.addToSelectionList(3, _name);
		// add to sync song list; note: can't be added if album or artist is
		// already checked
		if (userinitiated) {
			if (selectdata.isArtistSync(currentartist)
					|| selectdata.isAlbumSync(songalbum, currentartist)) {
				if (selectdata.isAlbumExcluded(songalbum, currentartist)) {
					selectdata.addSongSync(_name, currentartist);
				} else {
					selectdata.removeSongExclude(_name, currentartist);
				}
			} else {
				selectdata.addSongSync(_name, currentartist);
			}
		}
	}

	public void removeSong(String _name, boolean userinitiated) {
		if (selectdata.selectlist.containsKey(currentartist)) {
			ArrayList<SongListModel> templist = new ArrayList<SongListModel>();
			ArrayList<SongListModel> currentlist = (ArrayList<SongListModel>) selectdata.selectlist
					.get(currentartist);
			Iterator it = currentlist.iterator();
			String songalbum = null;
			while (it.hasNext()) {
				SongListModel tempsong = (SongListModel) it.next();
				if (tempsong.toString().equals(_name)) {
					songalbum = tempsong.getAlbum();
					// add to deletelist
					addDeletion(tempsong.getRelPath());
					// remove possible entry in download list
					if (userinitiated) {
							removeDownload(tempsong);
					}
				} else {
					templist.add(tempsong);
				}
			}
			// replace hashmap values
			selectdata.selectlist.remove(currentartist);
			// check for remaining artist songs
			if (templist.size() > 0) {
				selectdata.selectlist.put(currentartist, templist);
				// check for remaining album songs
				int albumcount = 0;
				Iterator it2 = templist.iterator();
				while (it2.hasNext()) {
					SongListModel tempmodel = (SongListModel) it2.next();
					if (tempmodel.getAlbum().equals(songalbum)) {
						albumcount++;
					}
				}
				if (albumcount == 0) {
					selectdata.removeFromSelectionList(2, songalbum);
				}
			} else {
				selectdata.removeFromSelectionList(1, currentartist);
			}
			// remove from song lists
			selectdata.removeFromSelectionList(3, _name);
			// remove from sync list
			if (userinitiated) {
				if (selectdata.isArtistSync(currentartist)
						|| selectdata.isAlbumSync(songalbum, currentartist)) {
					if (selectdata.isAlbumExcluded(songalbum, currentartist)) {
						selectdata.removeSongSync(_name, currentartist);
					} else {
						selectdata.addSongExclude(_name, currentartist);
					}
				} else {
					selectdata.removeSongSync(_name, currentartist);
				}
			}
		}
	}

	public boolean isArtistSync() {
		if (selectdata.isArtistSync(currentartist)) {
			return true;
		}
		return false;
	}

	public boolean isAlbumSync(String _album) {
		if (selectdata.isAlbumSync(_album, currentartist)) {
			return true;
		}
		return false;
	}

	public boolean isAlbumExcluded(String _album) {
		if (selectdata.isAlbumExcluded(_album, currentartist)) {
			return true;
		}
		return false;
	}

	public boolean isArtistSongExcluded(String _song) {
		if (selectdata.getSongExclude().containsKey(currentartist)) {
			ArrayList<String> tempal = (ArrayList<String>) selectdata.getSongExclude().get(currentartist);
			Iterator<String> it = tempal.iterator();
			while (it.hasNext()) {
				if (it.next().equals(_song)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean doesArtistExclude() {
		if (doesArtistAlbumExclude(currentartist)
				|| doesArtistSongExclude(currentartist)) {
			return true;
		}
		return false;
	}

	public boolean doesArtistExclude(String _artist) {
		if (doesArtistAlbumExclude(_artist) || doesArtistSongExclude(_artist)) {
			return true;
		}
		return false;
	}

	public boolean doesArtistAlbumExclude(String artist) {
		if (selectdata.getAlbumExclude().containsKey(artist)) {
			return true;
		}
		return false;
	}

	public boolean doesArtistSongExclude(String artist) {
		if (selectdata.getSongExclude().containsKey(artist)) {
			return true;
		}
		return false;
	}

	public boolean doesAlbumExclude(String _album,
			ArrayList<SongListModel> albumsongs) { // arraylist used to get
													// album value for each
													// song; unfortunatly a
													// messy workaround; will
													// later add album value to
													// selectiondata data
		if (selectdata.getSongExclude().containsKey(currentartist)) {
			ArrayList<String> excludedsongs = selectdata.getSongExclude().get(currentartist);
			Iterator<SongListModel> it = albumsongs.iterator();
			while (it.hasNext()) {
				if (excludedsongs.contains(it.next().toString())) {
					return true;
				}
			}
		}
		return false;
	}
}
