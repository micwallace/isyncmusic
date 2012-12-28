package com.isyncmusic;
import java.util.ArrayList;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PublicResources extends Application {

	  private ReadIndex readindex;
	  private SelectionList selectlist;
	  
	  private String IPaddress = null;
	  private String currentartist = null;
	  
	  private ArrayList<String> listarray = null;
	  private ArrayList<SongListModel> listobjectarray = null;
	  
	  private ArrayList<SongListModel> currentdllist = null;
	  private ArrayList<SongListModel> selectdllist = null;
	  private ArrayList<SongListModel> completedllist = null;
	  
	  private boolean albumindexstatus = false;
	  private boolean songliststatus = false;
	  public PublicResources(){
	  }
	  public SharedPreferences getPrefs(){
		  return PreferenceManager.getDefaultSharedPreferences(PublicResources.this);
	  }
	  public ReadIndex getReadIndex() {
		  if (readindex == null) {
			  readindex = new ReadIndex();
		  } else {
			  return readindex;
		  }
		  return readindex;
	  }
	  public SelectionList getSelectList() {
		  if (selectlist == null) {
			  selectlist = new SelectionList(getReadIndex());
		  }
		  return selectlist;
	  }
	  // The following three functions are used by the download service and associated views to determine the current download set:
	  // This is to; 1: Prevent sync view from updating DL list (possibly causing more entries to show that will never download)
	  // 2: keep track of current downloads. 3: Keep track of user specified downloads.
	  // single song download marker/list
	  public void setSelectDL(ArrayList<SongListModel> _list){
		  selectdllist = _list;
	  }
	  public void clearSelectDL(){
		  selectdllist = null;
	  }
	  public ArrayList<SongListModel> getSelectDL(){
		  return selectdllist;
	  }
	  public boolean isSetSelectDL(){
		  return (selectdllist==null?false:true);
	  }
	  // current/pending download list 
	  public void setCurrentDL(ArrayList<SongListModel> dllist){
		  currentdllist = dllist;
	  }
	  public ArrayList<SongListModel> getCurrentDL(){
		  return currentdllist;
	  }
	  public void clearCurrentDL(){
		  currentdllist = null;
	  }
	  // completed download list (will eventually be stored in selection data for more persistance)
	  public void putCompletedDL(SongListModel completedl){
		  if (completedllist==null){
			  completedllist = new ArrayList<SongListModel>();
		  }
		  completedllist.add(completedl);
		  currentdllist.remove(completedl);
	  }
	  public ArrayList<SongListModel> getCompleteDL(){
		  return completedllist;
	  }
	  public void clearCompletedDL(){
		  completedllist = null;
	  }
	  // current connected ip address, actually IP socket (port included); updated by ippicker. Used by all download functions
	  public String getIPAddress() {
		  if (IPaddress==null){
			  setIPAddress("0");
		  }
		  return IPaddress;
	  }
	  public void setIPAddress(String _IPAddress){
		  IPaddress = _IPAddress;
	  }
	  // arrays for list view use; should eventually migrate these into intent bundles
	  public ArrayList<String> getListArray() {
          return listarray;
	  }
	  public void setListArray(ArrayList<String> _listarray){
		  listarray = _listarray;
	  }
	  public ArrayList<SongListModel> getListObjectArray() {
          return listobjectarray;
	  }
	  public void setListObjectArray(ArrayList<SongListModel> _listobjectarray){
		  listobjectarray = _listobjectarray;
	  }
	  // get current artist, used by list views and select list to determine artist of selected album/song
	  public String getCurrentArtist(){
		  return currentartist;
	  }
	  public void setCurrentArtist(String _currentartist){
		  currentartist = _currentartist;
	  }
	  // album cached index task status
	  public boolean getTaskstatus(){
		  return albumindexstatus;
	  }
	  public void setTaskStatus(Boolean _status){
		  albumindexstatus = _status;
	  }
	  public boolean getSongTaskstatus(){
		  return songliststatus;
	  }
	  public void setSongTaskStatus(Boolean _status){
		  songliststatus = _status;
	  }
	  // global general functions
	  public String convertBytes(long bytes, boolean si) {
		  	String stringbytes = String.valueOf(bytes);
		  	boolean negative = false;
		  	if (stringbytes.contains("-")){
		  		negative = true;
		  		stringbytes = stringbytes.replace("-", "");
		  		bytes = Long.valueOf(stringbytes);
		  	}

		    int unit = si ? 1000 : 1024;
		    if (bytes < unit) return bytes + " B";
		    int exp = (int) (Math.log(bytes) / Math.log(unit));
		    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		    return (negative?"-":"")+String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	  }
}
