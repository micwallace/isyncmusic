package com.isyncmusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SectionIndexer;
 
public class ListViewFromArray extends ListActivity {
	public boolean indexstatus = false;
	public PublicResources  global;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		global = ((PublicResources)getApplicationContext());
		final ArrayList<String> arraylist = global.getListArray();
		// covert to linked list for indexing use
		LinkedList<String> linkedList = new LinkedList<String>();  
		linkedList.addAll(arraylist);
		setListAdapter(new IndexingAdaptor(this, linkedList));
		// setListAdapter(new ArrayAdapter<String>(this, R.layout.listview,arraylist));
		// init fast scrolling
		ListView lv = getListView(); 
        lv.setFastScrollEnabled(true);
		// set onclick listener
		OnclickAction();
	}
	@Override
	public void onBackPressed(){
		this.finish();
	}
	public void OnclickAction(){
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ArrayList<String> albumlist;
			    // Fetch artists albums or all albums?
				String clickedval =(String) parent.getItemAtPosition(position);
			    if(clickedval!="All"){
			    	albumlist = global.getReadIndex().getartistalbums(clickedval);
			    } else {
			    // get all albums
			    	// wait until the album index is ready
			    	indexstatus = global.getSongTaskstatus();
		    		if (!indexstatus){
		    			ProgressDialog dialog = ProgressDialog.show(ListViewFromArray.this, "","Pre-caching data...", true);
		    			while (indexstatus==false){
		    				try {
		    					Thread.sleep(500);
		    				} catch (InterruptedException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
		    				indexstatus = global.getTaskstatus();
		    				Log.w("isyncmusic", "Async task status: "+indexstatus);
		    			}
		    			dialog.dismiss();
		    		}
			    	albumlist = global.getReadIndex().getallalbumsfast();
			    }
			    // add "All" value to list
			    if (!albumlist.get(0).equals("All")){
			    	albumlist.add(0, "All");
			    }
				// pass array to global resource var used by list creator and set current artist for later use
				global.setListArray(albumlist);
				global.setCurrentArtist(clickedval);
				// open list view
				Intent myIntent2 = new Intent(ListViewFromArray.this, ListAlbums.class);
				startActivity(myIntent2);
				
			}
		});
	}
	  /** 
     * Section indexing adaptor
     */ 
    class IndexingAdaptor extends ArrayAdapter<String> implements 
              SectionIndexer { 
  
         HashMap<String, Integer> alphaIndexer;
         String[] sections;
  
         public IndexingAdaptor(Context context, LinkedList<String> items) { 
              super(context, R.layout.listview, items);
              alphaIndexer = new HashMap<String, Integer>();
              int size = items.size();
              alphaIndexer.put("0", 0);
              for (int x = 1; x < size; x++) {
                   String s = items.get(x);
                   // get the first letter of the store 
                   String ch = s.substring(0, 1); 
                   // convert to uppercase otherwise lowercase a -z will be sorted 
                   // after upper A-Z 
                   ch = ch.toUpperCase(); 
                   // HashMap will prevent duplicates 
                   alphaIndexer.put(ch, x); 
              }
              Set<String> sectionLetters = alphaIndexer.keySet(); 
              // create a list from the set to sort 
              ArrayList<String> sectionList = new ArrayList<String>( 
                        sectionLetters); 
              Collections.sort(sectionList);
              sections = new String[sectionList.size()];
              sectionList.toArray(sections);
         }
         public int getPositionForSection(int section) {
        	 if (section==0){
              return alphaIndexer.get(sections[section]);
        	 } else {
        		 return alphaIndexer.get(sections[section-1]); 
        	 }
         }
         public int getSectionForPosition(int position) { 
              return 0; 
         } 
         public Object[] getSections() { 
              return sections;
         } 
    } 
}

