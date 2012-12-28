package com.isyncmusic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class SelectionListView extends ListActivity {
	public boolean indexstatus = false;
	public PublicResources global;
	public String inputtype = null;
	public int stage = 0;
	private ArrayList arraylist;
	private int currentscroll;
	private TextView spacestat;
	private StatTask stattask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectlistview);
		global = ((PublicResources) getApplicationContext());
		inputtype = getIntent().getExtras().get("inputtype").toString();
		stage = (Integer) getIntent().getExtras().get("stage");
		Log.w("isyncmusic", "input type: " + inputtype);
		// get arraylist according to input value
		if (inputtype.equals("objects")) {
			arraylist = global.getListObjectArray();
		} else {
			arraylist = global.getListArray();
		}
		// init fast scrolling
		ListView lv = getListView();
		lv.setFastScrollEnabled(true);
		// set device space/selected songs view vars
		stattask = new StatTask();
		spacestat = (TextView) findViewById(R.id.currentdevspace);
		// set onclick listener for list
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				OnclickAction(parent, view, position, id);
			}
		});
		// set onclick listener for buttons
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Settings Reverted",
						Toast.LENGTH_SHORT).show();
				Intent homeintent = new Intent(SelectionListView.this,
						MainActivity.class);
				homeintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(homeintent);
			}
		});
		Button btnView = (Button) findViewById(R.id.btnView);
		btnView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSelections();
			}
		});
		Button btnViewdl = (Button) findViewById(R.id.btnViewdl);
		btnViewdl.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDownloadList();
			}
		});
		Button btnClear = (Button) findViewById(R.id.btnclearselect);
		btnClear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// initiate confirmation dialog
				yesNoDialog("Are you sure you want to clear all your selections?", 0);
			}
		});
		Button btnSelectAll = (Button) findViewById(R.id.btnselectall);
		btnSelectAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// initiate confirmation dialog
				yesNoDialog("Are you sure you want to select all artists for sync?", 1);
			}
		});
	}
	// dialog used for clear and sync all buttons
	public void yesNoDialog(String query, final int action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(query)
				.setCancelable(true)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								if (action == 1) {
									final ProgressDialog progdialog = ProgressDialog.show(SelectionListView.this, "", ("Selecting all artists..."), true);
									Thread t = new Thread() {
										public void run() {
											global.getSelectList().selectAll();
											runOnUiThread(new Runnable() {
												public void run() {
													progdialog.cancel();
													onResume();
												}
											});
										}
									};
									t.start();
								} else {
									global.getSelectList().clearSelectionData();
									// restart activity
									onResume();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	// info dialog; informing the user that there is not enough space on the device
    public void infoDialog(String _title, String _text){
    	final Dialog edialog = new Dialog(this);
	    edialog.setContentView(R.layout.infodialog);
	    edialog.setTitle(_title);
	    TextView infotxt = (TextView) edialog.findViewById(R.id.infotext);
	    infotxt.setText(_text);
	    Button infobtn = (Button) edialog.findViewById(R.id.btninfook);
	    infobtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				edialog.cancel();
           }
	    });
	    edialog.show();
    }
	// Storage calculation
	public Long getDeviceSpace() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		long bytesfree = (long) stat.getBlockSize()
				* (long) stat.getBlockCount();
		return bytesfree;
	}

	public Long getPendingDLSize() {
		return global.getSelectList().getDownloadSize();
	}

	@Override
	public void onBackPressed() {
		if (!global.getSelectList().isDeletions()) {
			try {
				// check if there will be enough room for the selected songs
				if (spacestat.getText().toString().contains("-")){
					infoDialog("Insufficient space", "There is not enough space on the device for all the selected songs!\n\n Unselect some items before proceeding.");
				} else {
					global.getSelectList().saveSelectionData();
					Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
					this.finish();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(), "Save failed!",
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			
		} else {
			deleteDialog();
		}
	}

	private void deleteDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.deletedialog);
		dialog.setTitle("Pending Deletions");
		// get delete list and generate text
		String text = "";
		Iterator<String> it = global.getSelectList().getDeletions().iterator();
		while (it.hasNext()) {
			text += it.next() + "\n";
		}
		((TextView) dialog.findViewById(R.id.selecttext)).setText(text);
		dialog.show();

		Button keepbtn = (Button) dialog.findViewById(R.id.btnKeep);
		keepbtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
				SelectionListView.this.finish();
			}
		});

		Button deletebtn = (Button) dialog.findViewById(R.id.btnDelete);
		deletebtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				global.getSelectList().runDeletions();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "Files Deleted",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		currentscroll = getListView().getFirstVisiblePosition();
	}

	@Override
	public void onResume() {
		super.onResume();
		// set current artist in select model
		global.getSelectList().setCurrentArtist(global.getCurrentArtist());
		// refresh checkboxes
		ArrayList<ListItem> itemList = new ArrayList<ListItem>();
		Iterator it = arraylist.iterator();
		// feed the values into the custom list row object
		if (inputtype.equals("objects")) {
			while (it.hasNext()) {
				SongListModel templm = (SongListModel) it.next();
				itemList.add(new ListItem(templm.toString(), global
						.getSelectList().isSongChecked(templm.toString()),
						(stage == 3 ? false : isItemSync(templm.toString())),
						isItemExcluded(templm.toString()), templm.getRelPath()));
			}
		} else {
			while (it.hasNext()) {
				String itemname = it.next().toString();
				itemList.add(new ListItem(itemname, (stage == 2 ? global
						.getSelectList().isAlbumChecked(itemname) : global
						.getSelectList().selectdata
						.valueexists(stage, itemname)), (stage == 3 ? false
						: isItemSync(itemname)), isItemExcluded(itemname), ""));
			}
		}
		setListAdapter(new IndexingCheckboxAdaptor(this, itemList));
		Log.w("isyncmusic", " " + currentscroll);
		getListView().setSelectionFromTop(currentscroll, 0);
		// calculate space after download and update status
		updateStatText(); // will be completed but for the moment causing lag because of song list generation
	}

	public void OnclickAction(AdapterView<?> parent, View view, int position,
			long id) {
		ArrayList<String> albumlist;
		// Fetch artists albums
		String clickedval = (String) parent.getItemAtPosition(position)
				.toString();
		albumlist = global.getReadIndex().getartistalbums(clickedval);
		// pass array to global resource var used by list creator and set
		// current artist for later use
		global.setListArray(albumlist);
		global.setCurrentArtist(clickedval);
		// open list view
		Intent Intent2 = new Intent(SelectionListView.this,
				AlbumSelectionView.class);
		Intent2.putExtra("inputtype", "strings");
		Intent2.putExtra("stage", 2);
		startActivity(Intent2);
	}

	public void OnCheckAction(String listtxt, boolean checked) {
		System.out.println("Item " + listtxt + " was "
				+ (checked ? "checked" : "unchecked"));
		if (listtxt.equals("All")) {

		} else {
			if (checked) {
				global.getSelectList().addartist(listtxt, true);
			} else {
				global.getSelectList().removeartist(listtxt, true);
			}
		}
	}

	// checks whether the item is synced according to stage
	public boolean isItemSync(String itemname) {
		switch (stage) {
		case 1:
			global.getSelectList().setCurrentArtist(itemname);
			if (global.getSelectList().isArtistSync()) {
				return true;
			}
			break;
		case 2:
			if ((global.getSelectList().isArtistSync() && !global
					.getSelectList().isAlbumExcluded(itemname))
					|| global.getSelectList().isAlbumSync(itemname)) {
				return true;
			}
			break;
		}
		return false;
	}

	public boolean isItemExcluded(String itemname) {
		switch (stage) {
		case 1:
			global.getSelectList().setCurrentArtist(itemname);
			if (global.getSelectList().doesArtistExclude()) {
				return true;
			}
			break;
		case 2:
			if (global.getSelectList().isAlbumExcluded(itemname)
					|| global.getSelectList().doesAlbumExclude(
							itemname,
							global.getReadIndex().getartistalbumsongs(
									global.getCurrentArtist(), itemname))) {
				return true;
			}
			break;
		case 3:
			// TBC make function in selectlist to determine if song excluded
			if (global.getSelectList().isArtistSongExcluded(itemname)) {
				return true;
			}
			break;
		}
		return false;
	}
	
	String finaltxt = new String();
	public void showSelections() {
		final ProgressDialog dialog = ProgressDialog.show(
				SelectionListView.this, "", ("Processing data..."), true);
		Thread t = new Thread() {
			public void run() {
				// process data into string
				String selectlist = new String();
				Long grandsize = new Long(0);
				String txttab = "\u00A0\u00A0\u00A0\u00A0\u00A0";
				int grandcount = 0;
				Iterator it = global.getSelectList().selectdata.selectlist
						.entrySet().iterator();
				while (it.hasNext()) {
					Entry ce = (Entry) it.next();
					ArrayList temparray = (ArrayList) ce.getValue();
					Iterator it2 = temparray.iterator();
					String templist = new String();
					Long atotalsize = new Long(0);
					int atotal = 0;
					while (it2.hasNext()) {
						SongListModel to = (SongListModel) it2.next();
						Long songsize = Long.valueOf(to.getSize());
						templist += txttab + txttab + to.toString() + " "
								+ global.convertBytes(songsize, true) + "\r\n";
						atotalsize += songsize;
						atotal++;
					}
					String artistname = ce.getKey().toString();
					selectlist += artistname + " (" + atotal + " songs "
							+ global.convertBytes(atotalsize, true) + ")\r\n";
					if (global.getSelectList().selectdata
							.isArtistSync(artistname)) {
						selectlist += txttab + "Syncronizing all songs\r\n";
						// list excluded albums and songs
						if (global.getSelectList()
								.doesArtistExclude(artistname)) {
							if (global.getSelectList().doesArtistAlbumExclude(
									artistname)) {
								selectlist += txttab
										+ "Excluding the following albums:\r\n";
								Iterator<String> exalbumit = ((ArrayList<String>) global
										.getSelectList().selectdata
										.getAlbumExclude().get(artistname))
										.iterator();
								while (exalbumit.hasNext()) {
									selectlist += txttab + txttab
											+ exalbumit.next() + "\r\n";
								}
							}
							if (global.getSelectList().doesArtistSongExclude(
									artistname)) {
								selectlist += txttab
										+ "Excluding the following songs:\r\n";
								Iterator<String> exsongit = ((ArrayList<String>) global
										.getSelectList().selectdata
										.getSongExclude().get(artistname))
										.iterator();
								while (exsongit.hasNext()) {
									selectlist += txttab + txttab
											+ exsongit.next() + "\r\n";
								}
							}
						}
					} else {
						ArrayList temparr = global.getSelectList().selectdata
								.getArtistAlbumSync(artistname);
						if (temparr != null) {
							selectlist += txttab
									+ "Syncronizing following albums:\r\n";
							Iterator it3 = temparr.iterator();
							while (it3.hasNext()) {
								selectlist += txttab + txttab
										+ it3.next().toString() + "\r\n";
							}
						}
						// list excluded songs
						if (global.getSelectList().doesArtistSongExclude(
								artistname)) {
							selectlist += txttab
									+ "Excluding the following songs:\r\n";
							Iterator<String> exsongit = ((ArrayList<String>) global
									.getSelectList().selectdata
									.getSongExclude().get(artistname))
									.iterator();
							while (exsongit.hasNext()) {
								selectlist += txttab + txttab + exsongit.next()
										+ "\r\n";
							}
						}
					}
					selectlist += "\r\n" + txttab + "Selected songs:\r\n"
							+ templist + "\r\n";
					grandcount += atotal;
					grandsize += atotalsize;
				}
				finaltxt = "Grand total: " + grandcount + " songs ("
						+ global.convertBytes(grandsize, true) + ")\r\n\r\n"
						+ selectlist;
				
				runOnUiThread(new Runnable() {
					public void run() {
						Intent selectintent = new Intent(SelectionListView.this, SelectView.class);
						selectintent.putExtra("finaltxt", finaltxt);
						startActivity(selectintent);
						dialog.cancel();
					}
				});
			}
		};
		t.start();
	}
	
	public void updateStatText(){
		if (stattask.getStatus() == AsyncTask.Status.PENDING){
			stattask.execute();
		} else if (global.getSongTaskstatus() == true) {
			spacestat.setText(global.getSelectList().getSelectCount()+" of "+global.getReadIndex().countFiles()+" selected\n"+"Space after sync: "+ global.convertBytes((getDeviceSpace() - getPendingDLSize()),true));
		}
	}
	// sync task for song status text (generated required lists before letting updatespacestat run)
	public class StatTask extends AsyncTask<String,Long,Integer>{
		@Override
		protected Integer doInBackground(String... params) {
			// run getselectcount to trigger gendownloadlist to count selected songs then wait for index cache task for total songs count.
			global.getSelectList().getSelectCount();
			while (global.getSongTaskstatus() == false){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return 0;
		}
		@Override
		protected void onPostExecute(Integer _result) {
			// hide loader and update text
			(SelectionListView.this.findViewById(R.id.statloader)).setVisibility(View.GONE);
			spacestat.setVisibility(View.VISIBLE);
			updateStatText();
			
	    }
	}

	public void showDownloadList() {
		Intent dllintent = new Intent(SelectionListView.this,
				DownloadList.class);
		startActivity(dllintent);
	}

	/**
	 * Section indexing adaptor
	 */
	class IndexingCheckboxAdaptor extends ArrayAdapter<ListItem> implements
			SectionIndexer {
		public LayoutInflater inflater;
		HashMap<String, Integer> alphaIndexer;
		String[] sections;

		public IndexingCheckboxAdaptor(Context context,
				ArrayList<ListItem> objectlist) {
			super(context, R.layout.selectlistitem, R.id.listtxt, objectlist);
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// covert to linked list for indexing use
			LinkedList<String> items = new LinkedList<String>();
			Iterator it = objectlist.iterator();
			while (it.hasNext()) {
				items.add(it.next().toString());
			}
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
			if (section == 0) {
				return alphaIndexer.get(sections[section]);
			} else {
				return alphaIndexer.get(sections[section - 1]);
			}
		}

		public int getSectionForPosition(int position) {
			return 0;
		}

		public Object[] getSections() {
			return sections;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Contact to display
			ListItem planet = (ListItem) this.getItem(position);
			// System.out.println(String.valueOf(position));
			// The child views in each row.
			CheckBox checkBox;
			TextView textView;
			ImageView syncicon;
			ImageView excludeicon;
			// Create a new row view
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.selectlistitem, parent,
						false);
				// Find the child views.
				textView = (TextView) convertView.findViewById(R.id.listtxt);
				checkBox = (CheckBox) convertView.findViewById(R.id.listcb);
				syncicon = (ImageView) convertView.findViewById(R.id.listsync);
				excludeicon = (ImageView) convertView
						.findViewById(R.id.listexclude);
				// Optimization: Tag the row with it's child views, so we don't
				// have to
				// call findViewById() later when we reuse the row.
				convertView.setTag(new ListViewHolder(textView, checkBox,
						syncicon, excludeicon));
				// If CheckBox is toggled, update the Contact it is tagged with.
				checkBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						// if item is being checked, check if there's enough space
						CheckBox cb = (CheckBox) v;
						if (cb.isChecked() && spacestat.getText().toString().contains("-")){
							cb.setChecked(false);
							infoDialog("Insufficient Space", "There is not enough space on the device for all the selected songs!\n\n Unselect some items before proceeding.");
						} else {
							ListItem contact = (ListItem) cb.getTag();
							contact.setChecked(cb.isChecked());
							// update select list
							OnCheckAction(contact.getListTxt(), cb.isChecked());
							// update info icons
							// get tag for viewholder access
							RelativeLayout container = (RelativeLayout) v.getParent();
							ListViewHolder viewHolder = (ListViewHolder) container.getTag();
							switch (stage) {
							// song items can only have exclude icon
							case 3:
								if (isItemExcluded(contact.getListTxt())) {
									contact.setExclude(true);
									viewHolder.getExclude().setVisibility(View.VISIBLE);
								} else {
									contact.setExclude(false);
									viewHolder.getExclude().setVisibility(View.GONE);
								}
								break;
							default:
								boolean itemsync = isItemSync(contact.getListTxt());
								boolean itemexclude = isItemExcluded(contact.getListTxt());
								if (itemsync || itemexclude) {
									if (itemsync) {
										contact.setSync(true);
										viewHolder.getSync().setVisibility(View.VISIBLE);
									} else {
										contact.setSync(false);
										viewHolder.getSync().setVisibility(View.GONE);
									}
									if (isItemExcluded(contact.getListTxt())) {
										contact.setExclude(true);
										viewHolder.getExclude().setVisibility(View.VISIBLE);
									} else {
										contact.setExclude(false);
										viewHolder.getExclude().setVisibility(View.GONE);
									}
								} else {
									contact.setExclude(false);
									contact.setSync(false);
									viewHolder.getSync().setVisibility(View.GONE);
									viewHolder.getExclude().setVisibility(View.GONE);
								}
								break;
							}
							// calculate space after download and update status
							updateStatText();
						}
					}
				});
			}
			// Reuse existing row view
			else {
				// Because we use a ViewHolder, we avoid having to call
				// findViewById().
				ListViewHolder viewHolder = (ListViewHolder) convertView
						.getTag();
				checkBox = viewHolder.getCheckBox();
				textView = viewHolder.getTextView();
				syncicon = viewHolder.getSync();
				excludeicon = viewHolder.getExclude();
			}
			// Tag the CheckBox with the Contact it is displaying, so that we
			// can
			// access the Contact in onClick() when the CheckBox is toggled.
			checkBox.setTag(planet);

			// Display Item data
			checkBox.setChecked(planet.isChecked());
			textView.setText(planet.getListTxt());
			syncicon.setVisibility((planet.isSync() ? View.VISIBLE : View.GONE));
			excludeicon.setVisibility((planet.isExclude() ? View.VISIBLE
					: View.GONE));
			return convertView;
		}
	}

	public class ListItem {
		private String listtxt = "";
		private String relpath = "";
		private boolean listcb = false;
		private boolean listsync = false;
		private boolean listexclude = false;

		public ListItem(String _listtxt, boolean _listcb, boolean _listsync,
				boolean _listexclude, String _relpath) {
			super();
			this.listtxt = _listtxt;
			this.listcb = _listcb;
			this.listsync = _listsync;
			this.listexclude = _listexclude;
			this.relpath = _relpath;
		}

		public String getListTxt() {
			return listtxt;
		}

		public String toString() {
			return listtxt;
		}

		public boolean isChecked() {
			return listcb;
		}

		public void setChecked(boolean checked) {
			this.listcb = checked;
		}

		public void toggleChecked() {
			listcb = !listcb;
		}

		public boolean isSync() {
			return listsync;
		}

		public void setSync(boolean checked) {
			this.listsync = checked;
		}

		public boolean isExclude() {
			return listexclude;
		}

		public void setExclude(boolean checked) {
			this.listexclude = checked;
		}

		public String getRelPath() {
			return relpath;
		}
	}

	private class ListViewHolder {
		private CheckBox checkBox;
		private TextView textView;
		private ImageView syncicon;
		private ImageView excludeicon;

		public ListViewHolder() {
		}

		public ListViewHolder(TextView textView, CheckBox checkBox,
				ImageView _syncicon, ImageView _excludeicon) {
			this.checkBox = checkBox;
			this.textView = textView;
			this.syncicon = _syncicon;
			this.excludeicon = _excludeicon;
		}

		public CheckBox getCheckBox() {
			return checkBox;
		}

		public void setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
		}

		public ImageView getExclude() {
			return this.excludeicon;
		}

		public void setExclude(ImageView _excludeicon) {
			this.excludeicon = _excludeicon;
		}

		public ImageView getSync() {
			return this.syncicon;
		}

		public void setSync(ImageView _syncicon) {
			this.syncicon = _syncicon;
		}

		public TextView getTextView() {
			return textView;
		}

		public void setTextView(TextView textView) {
			this.textView = textView;
		}
	}

}
// async task for updating selection size and number of songs.

