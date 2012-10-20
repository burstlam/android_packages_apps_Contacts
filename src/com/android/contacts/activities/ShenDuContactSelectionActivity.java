
package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ShenduContactPickAdapter.MemberWithoutRawContactId;
import com.android.contacts.list.ShenduContactPickFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * An activity for selection operating . For example add new members into
 * specified group.
 * 
 * @author Wang
 * @date 2012-9-2
 */
public class ShenDuContactSelectionActivity extends ContactsActivity implements
        OnNavigationListener, OnClickListener {
    public enum Option {
        Normal, SelectAll;
    }

    protected ContactEntryListFragment<?> mListFragment;
    private static final String EXCLUED_RAWCONTACTS_IDS_KEY = "exclued_ids";
    private View mSpinnerView;
    private TextView mSpinnerLine1View;
    private OptionsDropdownPopup mDropdown;
    private String[] mOptionsStringArray;
    private Option mOption = Option.Normal;
    private static final int SELECT_ALL_INDEX = 0;
    private static final int DESELECT_ALL_INDEX = 1;
    private OptionChangedListener mOptionChangedListener;
    private static final int PICK_ACTION = 100;
    private boolean isPickMode = false;

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ContactEntryListFragment<?>) {
            mListFragment = (ContactEntryListFragment<?>) fragment;
            setupActionListener();
        }
    }

    public void setOptionChangedListener(OptionChangedListener listener) {
        this.mOptionChangedListener = listener;
    }

    private void setupActionListener() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupOptionsStringArray();
        setContentView(R.layout.shendu_contact_pick);
        configureActionBar();
        resolveIntent();
        configureListFragment();
    }

    private void setupOptionsStringArray() {
        Resources res = getResources();
        mOptionsStringArray = new String[] {
                res.getString(R.string.select_all), res.getString(R.string.deselect_all)
        };

    }

    private void configureListFragment() {
        ShenduContactPickFragment fragment = new ShenduContactPickFragment();
        long[] ids = getIntent().getLongArrayExtra(EXCLUED_RAWCONTACTS_IDS_KEY);
        log(" ids ==>" + ids);
        fragment.setupExistedContactsIds(ids);
        mListFragment = fragment;
        // mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        // mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    /**
     * Resolve intent
     * 
     * @author Wang
     * @date 2012-9-4
     */
    private void resolveIntent() {
        if(PICK_ACTION ==getIntent().getIntExtra("from", 0)){
            isPickMode = true;
        }else{
            isPickMode = false;
        }
    }

    /**
     * configure ActionBar with custom UI.
     * 
     * @author Wang
     * @date 2012-9-3
     */
    private void configureActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.shendu_contact_pick_actionbar,
                    null);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                            ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
            /*Spinner part*/
            mSpinnerView = customActionBarView.findViewById(R.id.options_spinner);
            mSpinnerLine1View = (TextView) customActionBarView.findViewById(R.id.spinner_line_1);
            updateSelectedCountInSpinner(0);
            mDropdown = new OptionsDropdownPopup(this);
            mDropdown.setAdapter(new OptionAdapter(this));
            mSpinnerView.setOnClickListener(this);
            /*Done part*/
            View doneItem = customActionBarView.findViewById(R.id.save_menu_item);
            doneItem.setOnClickListener(this);
            /*Cancel Part*/
            View cancelItem = customActionBarView.findViewById(R.id.cancel_menu_item);
            cancelItem.setOnClickListener(this);
        }
    }

    /**
     * Update text content of spinner
     * @author Wang
     * @param selectedCount Count of how many items has been selected.
     * @date 2012-9-10
     */
    public void updateSelectedCountInSpinner(int count) {
        if (mSpinnerLine1View == null)
            return;
        String text = String.format(getResources().getString(R.string.number_of_items_selected), count);
        mSpinnerLine1View.setText(text);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.options_spinner:
                mDropdown.show();
                break;
            case R.id.save_menu_item:
                Intent data = getIntent();
                if (isPickMode) {
                    data.putExtra("data", getNewMembersContactIdArray());
                }else{
                    data.putParcelableArrayListExtra("data", getNewMembers());
                }
                setResult(0, data);
                this.finish();
                break;
            case R.id.cancel_menu_item:
                this.finish();
                break;
           
        }
        
    }
    
    /**
     * Get New Members
     * @author Wang
     * @date 2012-9-10
     */
    private ArrayList<MemberWithoutRawContactId> getNewMembers(){
        if(mListFragment != null && mListFragment instanceof ShenduContactPickFragment){
            ShenduContactPickFragment fragment = (ShenduContactPickFragment) mListFragment;
            Collection<MemberWithoutRawContactId> clt = fragment.getNewMembers();
            ArrayList<MemberWithoutRawContactId> list = new ArrayList<MemberWithoutRawContactId>(clt.size());
            Iterator<MemberWithoutRawContactId> it = clt.iterator();
            while(it.hasNext()){
                list.add(it.next());
            }
            return list;
        }
        return null;
    }
    
    /**
     * Get ContactId Array of New Members 
     * @author Wang
     * @date 2012-9-18
     */
    private long[] getNewMembersContactIdArray(){
        if(mListFragment != null && mListFragment instanceof ShenduContactPickFragment){
            ShenduContactPickFragment fragment = (ShenduContactPickFragment) mListFragment;
            Collection<MemberWithoutRawContactId> clt = fragment.getNewMembers();
            long[] array = new long[clt.size()];
            Iterator<MemberWithoutRawContactId> it = clt.iterator();
            int i = 0;
            while(it.hasNext()){
                array[i] = it.next().getContactId();
                i++;
            }
            return array;
        }
        return null;
    }

    /**
     * Change Option State
     * @author Wang
     * @param state The state will be changed.
     * @date 2012-9-10
     */
    public void changeOptionState(Option state) {
        if(state == null) return;
        mOption = state;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    // Based on Spinner.DropdownPopup
    private class OptionsDropdownPopup extends ListPopupWindow {
        public OptionsDropdownPopup(Context context) {
            super(context);
            setAnchorView(mSpinnerView);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);
            setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    switch (mOption) {
                        case Normal:
                            mOption = Option.SelectAll;
                            if(mOptionChangedListener != null) mOptionChangedListener.onOptionChanged(mOption);
                            break;

                        case SelectAll:
                            mOption = Option.Normal;
                            if(mOptionChangedListener != null) mOptionChangedListener.onOptionChanged(mOption);
                            break;
                    }
                    dismiss();
                }
            });
        }

        @Override
        public void show() {
            setWidth(getResources().getDimensionPixelSize(R.dimen.shendu_custom_bar_spinner_width));
            setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
            super.show();
            // List view is instantiated in super.show(), so we need to do this
            // after...
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    private class OptionAdapter extends BaseAdapter {

        private Context mContext;

        public OptionAdapter(Context ctx) {
            mContext = ctx;
        }

        @Override
        public int getCount() {
            if (mOptionsStringArray == null)
                return 0;
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.shendu_actionbar_drop_text,
                    null);
            final TextView spinnerText = (TextView) v.findViewById(R.id.text1);
            switch (mOption) {
                case Normal:
                    spinnerText.setText(mOptionsStringArray[SELECT_ALL_INDEX]);
                    break;
                case SelectAll:
                    spinnerText.setText(mOptionsStringArray[DESELECT_ALL_INDEX]);
                    break;
            }
            return spinnerText;
        }

    }

    public static interface OptionChangedListener {
        public void onOptionChanged(Option op);
    }

    private static final boolean debug = false;

    private static void log(String msg) {
        msg = "ShenduSelectionAct  --> " + msg;
        if (debug)
            Log.i("shenduGroup", msg);
    }


}
