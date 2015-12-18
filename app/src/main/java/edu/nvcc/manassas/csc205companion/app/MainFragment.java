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

package edu.nvcc.manassas.csc205companion.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.help.HelpActivity;
import edu.nvcc.manassas.csc205companion.io.Savelog;
import edu.nvcc.manassas.csc205companion.tools.ToolsActivity;

public class MainFragment extends Fragment
        implements ListView.OnItemClickListener {
    private static final String TAG = MainFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public static final int NumberOfTools = AppSettings.NumberOfTools;
    public static final int[] Types = ToolsActivity.Types;
    public static final int[] Type_NameId = ToolsActivity.Type_NameId;


    private static final String vacant = "";

    private ArrayList<String> mToolNames;
    private GridView mGridView;
    private GridAdapter mAdapter;


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Savelog.d(TAG, debug, "onCreate()");

        mToolNames = new ArrayList<>();
        for ( int id : Type_NameId ) {
            mToolNames.add(getString(id));
        }


        mAdapter = new GridAdapter(this);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Set the adapter
        mGridView = (GridView) view.findViewById(R.id.fragmentMain_grid);
        mGridView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mGridView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {
            case R.id.menu_info: {
                FragmentManager fm = getFragmentManager();
                LegalDialogFragment dialogFragment;
                dialogFragment = LegalDialogFragment.newInstance(R.raw.eula);
                dialogFragment.show(fm, LegalDialogFragment.dialogTag);

                return true;
            }
            case R.id.menu_help:
            {
                Intent intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (position>=0 && position<NumberOfTools) {
            Intent intent = new Intent(getActivity(), ToolsActivity.class);
            intent.putExtra(ToolsActivity.EXTRA_Type, Types[position]);
            startActivity(intent);
        }
        else {
            Toast.makeText(getActivity(), "No tool available", Toast.LENGTH_SHORT).show();
        }
    }





    public static class GridAdapter extends BaseAdapter {
        private Context appContext;
        MainFragment hostFragment;

        public GridAdapter(MainFragment hostFragment) {
            // This class of objects does not outlive its host, so no need to use weak references
            appContext = hostFragment.getActivity().getApplicationContext();
            this.hostFragment = hostFragment;
            Savelog.d(TAG, debug, "size of keypad = " + hostFragment.mToolNames.size());
        }

        public int getCount() {
            return hostFragment.mToolNames.size();
        }

        public Object getItem(int position) {
            return hostFragment.mToolNames.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String identifier = (String) getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) appContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                convertView = inflater.inflate(R.layout.griditem_tool, parent, false);
            }

            TextView labelView = (TextView) convertView.findViewById(R.id.gridItem_tool_name);
            if (identifier.length()==0) {
                labelView.setText(vacant);
            }
            else {
                labelView.setText(identifier);

            }
            return convertView;
        }
        public void cleanup() {
            hostFragment = null;
        }
    }


}
