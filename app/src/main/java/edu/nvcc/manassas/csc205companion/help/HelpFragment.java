/*
 * Copyright (c) 2015,2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.nvcc.manassas.csc205companion.help;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class HelpFragment extends ListFragment {
    private static final String TAG = HelpFragment.class.getSimpleName() + "_class";
    private static boolean debug = AppSettings.defaultDebug;


    private static final int pad_label = 16;

    private ArrayList<Icon> mHelpList = null;
    private HelpListAdapter mHelpListAdapter = null;

    public static HelpFragment newInstance() {
        Bundle args = new Bundle();
        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Savelog.d(TAG, debug, "HelpFragment onCreate()");
        mHelpList = Icon.makeList();

        mHelpListAdapter = new HelpListAdapter(this, mHelpList);
        setRetainInstance(true);
        setHasOptionsMenu(true);

    } // end to implementing onCreate()




    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_help, parent, false);
        setListAdapter(mHelpListAdapter);
        ListView listview = (ListView) v.findViewById(android.R.id.list);
        listview.setClickable(false);
        return v;
    } // end to implementing onCreateView()


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHelpListAdapter!=null) {
            mHelpListAdapter.cleanup();
            mHelpListAdapter = null;
        }
        if (mHelpList!=null && mHelpList.size()>0) {
            mHelpList.clear();
        }

    }



    private static class HelpListAdapter extends ArrayAdapter<Icon> {
        HelpFragment hostFragment;
        public HelpListAdapter(HelpFragment hostFragment, ArrayList<Icon> helplist) {
            super(hostFragment.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, helplist);
            this.hostFragment = hostFragment;
        }
        @Override
        public boolean isEnabled(int position) {
            // make sure that this list is not clickable
            return false;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null) {
                convertView = hostFragment.getActivity().getLayoutInflater().inflate(R.layout.listitem_help, parent, false);
            }

            Icon item = getItem(position);

            TextView labelView = (TextView) convertView.findViewById(R.id.listItem_help_name_id);
            labelView.setText(item.getNameId());
            labelView.setCompoundDrawablesWithIntrinsicBounds(item.getIconId(),0,0,0);
            labelView.setCompoundDrawablePadding(pad_label);
            labelView.setClickable(false);


            TextView descriptionView = (TextView) convertView.findViewById(R.id.listItem_help_description_id);
            descriptionView.setText(item.getDescription());
            descriptionView.setClickable(false);

            convertView.setTag(position); // Add a tag for identification during test
            convertView.setClickable(false);
            convertView.setEnabled(false);

            return convertView;
        }
        void cleanup() {
            hostFragment = null;
        }
    }

}