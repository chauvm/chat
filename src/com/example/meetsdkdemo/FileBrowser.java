package com.example.meetsdkdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.moxtra.binder.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


/**
 * Created by jeffery on 14-7-8.
 */
public class FileBrowser{
    public interface OnFileSelectListener{
        void onFileSelected(String fileSelected);
    }

    String TAG = FileBrowser.class.getSimpleName();

    ArrayList<String> str = new ArrayList<String>();
    private Boolean firstLvl = true;
    private Item[] fileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "");
    private String mChosenFile;
    ListAdapter mAdapter;

    Context mContext;

    public FileBrowser(Context context){
        mContext = context;
    }

    public void selectFile(final OnFileSelectListener listener){
        loadFileList();
		new AlertDialog.Builder(mContext).setTitle("Choose your file")
				.setAdapter(mAdapter, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mChosenFile = fileList[which].file;
						File sel = new File(mPath + "/" + mChosenFile);
						if (sel.isDirectory()) {
							firstLvl = false;
							str.add(mChosenFile);
							fileList = null;
							mPath = new File(sel + "");
							loadFileList();
							dialog.dismiss();
							selectFile(listener);
							// Log.d(TAG, mPath.getAbsolutePath());
						}
						// Checks if 'up' was clicked
						else if (mChosenFile.equalsIgnoreCase("up")
								&& !sel.exists()) {
							String s = str.remove(str.size() - 1);
							mPath = new File(mPath.toString().substring(0,
									mPath.toString().lastIndexOf(s)));
							fileList = null;
							// if there are no more directories in the list,
							// then
							// its the first level
							if (str.isEmpty())
								firstLvl = true;
							loadFileList();
							dialog.dismiss();
							selectFile(listener);
							// Log.d(TAG, mPath.getAbsolutePath());
						}
						// File picked
						else {
							if (listener != null)
								listener.onFileSelected(sel.getAbsolutePath());
						}
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(listener != null)
                    listener.onFileSelected(null);
            }
        }).setNegativeButton("Cancel", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
                if(listener != null)
                    listener.onFileSelected(null);
			}
        	
        })
        .setCancelable(false)
        .show();
    }


    private void loadFileList() {
        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether mPath exists
        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = mPath.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], 0); //R.drawable.ic_launcher);

                // Convert into file mPath
                File sel = new File(mPath, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = 0; //R.drawable.directory_icon;
                    //Log.d("DIRECTORY", fileList[i].file);
                } else {
                    //Log.d("FILE", fileList[i].file);
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", 0); //R.drawable.ic_launcher);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "mPath does not exist");
        }

        mAdapter = new ArrayAdapter<Item>(mContext,
                android.R.layout.select_dialog_item, android.R.id.text1,
                fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * mContext.getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }
 }
