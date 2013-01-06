package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.contacts.R;
import com.android.contacts.list.ContactListItemView.PhotoPosition;
import com.android.contacts.list.ShenduContactPickAdapter.MemberWithoutRawContactId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ShenduPhoneNumberPickFragment extends ShenduPickFragment {
    
    public ShenduPhoneNumberPickFragment(){
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
    	 ShenduPhoneNumberPickAdapter adapter = new ShenduPhoneNumberPickAdapter(getActivity());
        adapter.setDisplayPhotos(true);
        adapter.setPhotoPosition(PhotoPosition.LEFT);
        adapter.setExcludedContactId(mExistedContactsIds);
        return adapter;
    }

    @Override
    protected void onItemClick(int position, long id) {
        return;
    }
    
    /**
     * Fetch the new Members Collection
     * @author Wang
     * @return new Members Collection
     * @date 2013-1-5
     * */
    public Collection<Parcelable> getNewMembers(){
        try {
        	ShenduPhoneNumberPickAdapter adapter = (ShenduPhoneNumberPickAdapter) getAdapter();
            return adapter.getNewMembers();
        } catch (ClassCastException e) {
            return null;
        }
    }
    

}
