package com.isyncmusic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.*;
import org.json.simple.parser.ParseException;

/**
 *
 * @author michael TODO: Minimise on main window close TODO: Add server status
 * bar icons TODO: Method to count number of files within a directory TODO:
 */
public class ReadIndex {
	// global general and temp objects
    private JSONObject jsobject;
    //private JSONParser parser; was causing issues
    private String artist;
    private String album;
    // hashmap lookup tables
    private HashMap<String, ArrayList<String>> topdir;
    private HashMap<String, ArrayList<JSONObject>> fastalbumindex;
    // cached lists and values
    private ArrayList <String> allartistlist;
    private ArrayList <String> allalbumlist;
    private ArrayList <SongListModel> allsonglist;
    private String totalsize;
    // indicators
    private boolean indexread = false;
    public ReadIndex() {
        // init var; get music dir
        jsobject = new JSONObject();
        //parser = new JSONParser();
    }
    public boolean isIndexRead(){
    	return indexread;
    }
    public void readIndex() {
        try {
            // write into json object
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("/data/data/com.isyncmusic/files/ismsindex.json"));
            jsobject = (JSONObject) obj;
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			parsetohashmap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        indexread = true;
    }

    public void parsetohashmap() throws IOException {
        topdir = new HashMap<String, ArrayList<String>>();
        topdir = new ObjectMapper().readValue(jsobject.toString(), HashMap.class);
    }

    public JSONObject getJSON() {
        return jsobject;
    }
    
    public void createFastAlbumIndex(){
    	fastalbumindex = new HashMap<String, ArrayList<JSONObject>>();
        Iterator<ArrayList<String>> it = topdir.values().iterator();
        while (it.hasNext()) {
            ArrayList<String> tempal = new ArrayList<String>();
            tempal = it.next();
            Iterator<String> it2 = tempal.iterator();
            while (it2.hasNext()) {
                Object tempobj = new Object();
                try {
                	JSONParser parser = new JSONParser();
                    tempobj = parser.parse(it2.next().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
                JSONObject tempjs = new JSONObject();
                tempjs = (JSONObject) (Object) tempobj;
                if (fastalbumindex.containsKey(tempjs.get("album").toString())){
                	ArrayList<JSONObject> tempaal = new ArrayList<JSONObject>();
                	ArrayList<JSONObject> previousal = fastalbumindex.get(tempjs.get("album").toString());
                	Iterator<JSONObject> it3 = previousal.iterator();
                	while (it3.hasNext()){
                		tempaal.add(it3.next());
                	}
                	tempaal.add(tempjs);
                	fastalbumindex.put(tempjs.get("album").toString(), tempaal);
                } else {
                	ArrayList<JSONObject> tempaal = new ArrayList<JSONObject>();
                	tempaal.add(tempjs);
                	fastalbumindex.put(tempjs.get("album").toString(), tempaal);
                }
            }
        }
    }

    public ArrayList<String> sortlistmodel(ArrayList<String> unsortedlist) {
        Collections.sort(unsortedlist);
        return unsortedlist; // now sorted ;)
    }

    public ArrayList<SongListModel> sortarraylistmodel(ArrayList<SongListModel> unsortedlist) {
        // SORTING NOT YET IMPLEMENTED AND MAY NEVER BE FOR SONGS; passthrough function
        return unsortedlist;
    }

    public ArrayList<String> getallartists() {

        allartistlist = new ArrayList<String>();
        // get the key strings of the top level json (artists); return as list object
        TreeSet<String> artisttm = new TreeSet<String>(topdir.keySet());
        /*for (String key : artisttm) {
            String value = topdir.get(key).toString();
        }*/
        Iterator<String> it = artisttm.iterator();
        while (it.hasNext()) {
            allartistlist.add(it.next());
            //it.remove(); // avoids a ConcurrentModificationException
        }
        return allartistlist;
    }
    public ArrayList<String> getallalbums() {
        ArrayList<String> finalal = new ArrayList<String>();
        // get the key strings of the top level json (artists); return as list object
        Iterator<ArrayList<String>> it = topdir.values().iterator();
        
        while (it.hasNext()) {
            ArrayList<String> tempal = new ArrayList<String>();
            tempal = it.next();
            Iterator<String> it2 = tempal.iterator();
            
            while (it2.hasNext()) {
                Object tempobj = new Object();
                try {
                	JSONParser parser = new JSONParser();
                    tempobj = parser.parse(it2.next().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
                JSONObject tempjs = new JSONObject();
                tempjs = (JSONObject) (Object) tempobj;

                if (!finalal.contains(tempjs.get("album"))) {
                    finalal.add(tempjs.get("album").toString());
                }
            }
        }
        finalal = sortlistmodel(finalal);
        return finalal;
    }
    
    public ArrayList<String> getallalbumsfast(){
    	if (allalbumlist == null){
    		allalbumlist = new ArrayList<String>();
    		// get the key strings of the top level json (artists); return as list object
    		TreeSet<String> artisttm = new TreeSet<String>(fastalbumindex.keySet());
    		/*for (String key : artisttm) {
    			String value = fastalbumindex.get(key).toString();
    		}*/
    		Iterator<String> it = artisttm.iterator();
    		while (it.hasNext()) {
    			allalbumlist.add(it.next());
    		}
    	}
    	return allalbumlist;
    }

    public ArrayList<SongListModel> getallsongs() {
    	// This is a redundant if; the async task running this indicates wheather the reasource is available
    	if (allsonglist == null){
        	genSongsArray();
    	}
        return allsonglist;
    }

    public ArrayList<SongListModel> genSongsArray() {
        ArrayList<SongListModel> finalal = new ArrayList<SongListModel>();
        Iterator<ArrayList<String>> it = topdir.values().iterator();
        
        while (it.hasNext()) {
            ArrayList<String> tempal = new ArrayList<String>();
            tempal = it.next();
            Iterator<String> it2 = tempal.iterator();
            
            while (it2.hasNext()) {
                Object tempobj = new Object();
                try {
                	JSONParser parser = new JSONParser();
                    tempobj = parser.parse(it2.next().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
                JSONObject tempjs = new JSONObject();
                tempjs = (JSONObject) (Object) tempobj;
                // create a song list from the JSON and add to list
                finalal.add(
                		new SongListModel(
                				(tempjs.get("id3song").equals("")) ? (tempjs.get("filename").toString()) : (tempjs.get("id3song").toString()), 
                						("/"+tempjs.get("artist").toString()+"/"+tempjs.get("album").toString()+"/"+tempjs.get("filename").toString()), 
                						tempjs.get("artist").toString(), 
                						tempjs.get("album").toString(), 
                						tempjs.get("filesize").toString()
                		)
                );
                
            }
            
        }
        // sorting
    	finalal = sortarraylistmodel(finalal);
    	allsonglist = finalal;
        return finalal;
    }

    public ArrayList<String> getartistalbums(String _artist) {
        artist = _artist;
        ArrayList<String> finalal = new ArrayList<String>();
        ArrayList<String> tempal = new ArrayList<String>();
        tempal = topdir.get(artist);
        Iterator<String> it = tempal.iterator();
        
        while (it.hasNext()) {
            Object tempobj = new Object();
            try {
            	JSONParser parser = new JSONParser();
                tempobj = parser.parse(it.next().toString());
            } catch (ParseException ex) {
                Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONObject tempjs = new JSONObject();
            tempjs = (JSONObject) (Object) tempobj;
            
            if (!finalal.contains(tempjs.get("album"))) {
                finalal.add(tempjs.get("album").toString());
            }
        }
        finalal = sortlistmodel(finalal);
        return finalal;
        // return albums from artists
    }

    public ArrayList<SongListModel> getartistalbumsongs(String _artist, String _album) {
        album = _album;
        artist = _artist;
        ArrayList<SongListModel> finalal = new ArrayList<SongListModel>();
        // get artist object from the array and iterate through to get the albums songs
            ArrayList<String> tempal = new ArrayList<String>();
            tempal = topdir.get(artist);
            Iterator<String> it1 = tempal.iterator();
            while (it1.hasNext()) {
                Object tempobj = new Object();
                JSONParser parser = new JSONParser();
                try {
                    tempobj = parser.parse(it1.next().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
                JSONObject tempjs = new JSONObject();
                tempjs = (JSONObject) (Object) tempobj;
                
                if (tempjs.get("album").equals(album)) {
                    finalal.add(
                    		new SongListModel(
                    				(tempjs.get("id3song").equals("")) ? (tempjs.get("filename").toString()) : (tempjs.get("id3song").toString()), 
                    				("/"+tempjs.get("artist").toString()+"/"+tempjs.get("album").toString()+"/"+tempjs.get("filename").toString()), 
                    				tempjs.get("artist").toString(), 
                    				tempjs.get("album").toString(), 
                    				tempjs.get("filesize").toString()
                    		)
                    );
                }
                
            }
        finalal = sortarraylistmodel(finalal);
        return finalal;
    }
    
    public ArrayList<SongListModel> getalbumsongsfast(String _artist, String _album){
    	 album = _album;
         artist = _artist;
         ArrayList<SongListModel> finalal = new ArrayList<SongListModel>();
         // get album object from the array and iterate through to get the albums songs
             ArrayList<JSONObject> tempal = new ArrayList<JSONObject>();
             tempal = fastalbumindex.get(album);
             Iterator<JSONObject> it1 = tempal.iterator();
             while (it1.hasNext()) {
                 Object tempobj = new Object();
                 try {
                	 JSONParser parser = new JSONParser();
                     tempobj = parser.parse(it1.next().toString());
                 } catch (ParseException ex) {
                     Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 JSONObject tempjs = new JSONObject();
                 tempjs = (JSONObject) (Object) tempobj;
                 finalal.add(
                		new SongListModel(
                				 (tempjs.get("id3song").equals("")) ? (tempjs.get("filename").toString()) : (tempjs.get("id3song").toString()), 
                			     ("/"+tempjs.get("artist").toString()+"/"+tempjs.get("album").toString()+"/"+tempjs.get("filename").toString()), 
                			     tempjs.get("artist").toString(), 
                			     tempjs.get("album").toString(), 
                			     tempjs.get("filesize").toString()
                		)
                );
             }
         finalal = sortarraylistmodel(finalal);
         return finalal;
    }
    	
    public ArrayList<SongListModel> getalbumsongs(String _artist, String _album) {
        album = _album;
        artist = _artist;
        ArrayList<SongListModel> finalal = new ArrayList<SongListModel>();
        // get the key strings of the top level json (artists); return as list object
        Iterator<ArrayList<String>> it = topdir.values().iterator();
        while (it.hasNext()) {
            ArrayList<String> tempal = new ArrayList<String>();
            tempal = it.next();
            Iterator<String> it2 = tempal.iterator();
            while (it2.hasNext()) {
                Object tempobj = new Object();
                try {
                	JSONParser parser = new JSONParser();
                    tempobj = parser.parse(it2.next().toString());
                } catch (ParseException ex) {
                    Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
                JSONObject tempjs = new JSONObject();
                tempjs = (JSONObject) (Object) tempobj;
                //System.out.print(tempjs.get("album"));
                if (tempjs.get("album").equals(album) && (tempjs.get("artist").equals(artist) || artist.equals("All"))) {
                    finalal.add(
                    	new SongListModel(
                    		(tempjs.get("id3song").equals("")) ? (tempjs.get("filename").toString()) : (tempjs.get("id3song").toString()), 
                    		("/"+tempjs.get("artist").toString()+"/"+tempjs.get("album").toString()+"/"+tempjs.get("filename").toString()), 
                    		tempjs.get("artist").toString(), 
                    		tempjs.get("album").toString(),
                    		tempjs.get("filesize").toString()
                    	)
                    );
                }
            }
        }
        finalal = sortarraylistmodel(finalal);
        return finalal;
    }

    public ArrayList<SongListModel> getartistsongs(String _artist) {
        artist = _artist;
        ArrayList<SongListModel> finalal = new ArrayList<SongListModel>();
        ArrayList<String> tempal = new ArrayList<String>();
        tempal = topdir.get(artist);
        Iterator<String> it = tempal.iterator();
        
        while (it.hasNext()) {
            Object tempobj = new Object();
            try {
            	JSONParser parser = new JSONParser();
                tempobj = parser.parse(it.next().toString());
            } catch (ParseException ex) {
                Logger.getLogger(ReadIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONObject tempjs = new JSONObject();
            tempjs = (JSONObject) (Object) tempobj;
            finalal.add(
            		new SongListModel(
            				(tempjs.get("id3song").equals("")) ? (tempjs.get("filename").toString()) : (tempjs.get("id3song").toString()), 
            				("/"+tempjs.get("artist").toString()+"/"+tempjs.get("album").toString()+"/"+tempjs.get("filename").toString()), 
            				tempjs.get("artist").toString(), 
            				tempjs.get("album").toString(), 
            				tempjs.get("filesize").toString()
            		)
            );
        }
        finalal = sortarraylistmodel(finalal);
        return finalal;
        // return albums from artists
    }

    public int countFiles() {
    	if (allsonglist == null){
        	genSongsArray();
    	}
        if (allsonglist != null) {
            return allsonglist.size();
        } else {
            return -1;
        }
    }

    public String getTotalFileSize() {
    	if (totalsize == null){
    		long total = 0;
    		if (allsonglist == null){
    			genSongsArray();
    		}
            for (int i = 0; i < allsonglist.size(); i++) {
                String size = allsonglist.get(i).getSize();
                //System.out.println(size);
                total += Integer.parseInt(size);
            }
            totalsize = humanReadableByteCount(total, false);
    	}
		return totalsize;
    }

    public int[] filterArtistFiles(String artist) {

        return null;
    }

    public int[] filterAlbumFiles(String artist, String album) {

        return null;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
