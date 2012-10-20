package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.list.ShenduContactPickAdapter.MemberWithoutRawContactId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ShenduContactPickFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    private long[] mExistedContactsIds;
    
    public ShenduContactPickFragment(){
        setQuickContactEnabled(false);
        setPhotoLoaderEnabled(true);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.contact_list_content, null);
    }
    
    /**
     * Called when  onCreateView method is callbacked in fragment.
     * */
    @Override
    protected ContactEntryListAdapter createListAdapter() {
        ShenduContactPickAdapter adapter = new ShenduContactPickAdapter(getActivity());
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setExcludedContactId(mExistedContactsIds);
        return adapter;
    }

    @Override
    protected void onItemClick(int position, long id) {
        return;
    }
    
    /**
     * Set up mExistedRawContactsIds values.
     * @author Wang
     * @param ids The contacts id has in group 
     * @return
     * @date 2012-9-4
     * */
    public void setupExistedContactsIds(long[] ids){
        mExistedContactsIds = ids;
    }
    
    /**
     * Fetch the new Members Collection
     * @author Wang
     * @return new Members Collection
     * @date 2012-9-11
     * */
    public Collection<MemberWithoutRawContactId> getNewMembers(){
        try {
            ShenduContactPickAdapter adapter = (ShenduContactPickAdapter) getAdapter();
            return adapter.getNewMembers();
        } catch (ClassCastException e) {
            return null;
        }
    }
    

}
