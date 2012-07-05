/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.android.phone.location.PhoneLocation;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * @author shade, Danesh, pawitp
 */
class T9Search {

    // List sort modes
    private static final int NAME_FIRST = 1;
    private static final int NUMBER_FIRST = 2;

    // Phone number queries
    private static final String[] PHONE_PROJECTION = new String[] {Phone.NUMBER, Phone.CONTACT_ID, Phone.IS_SUPER_PRIMARY, Phone.TYPE, Phone.LABEL};
    private static final String PHONE_ID_SELECTION = Contacts.Data.MIMETYPE + " = ? ";
    private static final String[] PHONE_ID_SELECTION_ARGS = new String[] {Phone.CONTENT_ITEM_TYPE};
    private static final String PHONE_SORT = Phone.CONTACT_ID + " ASC";
    private static final String[] CONTACT_PROJECTION = new String[] {Contacts._ID, Contacts.DISPLAY_NAME, Contacts.TIMES_CONTACTED,Contacts.SORT_KEY_PRIMARY};
    private static final String CONTACT_QUERY = Contacts.HAS_PHONE_NUMBER + " > 0";
    private static final String CONTACT_SORT = Contacts._ID + " ASC";

    // Local variables
    private Context mContext;
    private int mSortMode;
    private String inputNumber;
    
    private ArrayList<ContactItem> mNameResults = new ArrayList<ContactItem>();
    private ArrayList<ContactItem> mNumberResults = new ArrayList<ContactItem>();
    private ArrayList<ContactItem> mPinyinResults = new ArrayList<ContactItem>();
    private Set<ContactItem> mAllResults = new LinkedHashSet<ContactItem>();
    private ArrayList<ContactItem> mContacts = new ArrayList<ContactItem>();
    private String mPrevInput;
    private static char[][] sT9Map;
    
    //contacts photo status
    private boolean photoisready=false;

    public T9Search(Context context) {
        mContext = context;
        
        getAll();
        
    }

    private void getAll() {
        if (sT9Map == null)
            initT9Map();

        Cursor contact = mContext.getContentResolver().query(Contacts.CONTENT_URI, CONTACT_PROJECTION, CONTACT_QUERY, null, CONTACT_SORT);
        Cursor phone = mContext.getContentResolver().query(Phone.CONTENT_URI, PHONE_PROJECTION, PHONE_ID_SELECTION, PHONE_ID_SELECTION_ARGS, PHONE_SORT);
        phone.moveToFirst();

        while (contact.moveToNext()) {
            long contactId = contact.getLong(0);
            if (phone.isAfterLast()) {
                break;
            }
            while (phone.getLong(1) == contactId) {
                String num = phone.getString(0);
                if(num.substring(0, 3).equals("+86")||num.substring(0, 3).equals("086")){
                	num=num.substring(3);
                }
                ContactItem contactInfo = new ContactItem();
                contactInfo.id = contactId;
                contactInfo.name = contact.getString(1);
                contactInfo.number = PhoneNumberUtils.formatNumber(num);
                Log.e("",contactInfo.number.substring(0, 3));
                
                Log.e("sort_key", contact.getString(3));
                contactInfo.pinyin=getPinyinFromSortKey(contact.getString(3));
                contactInfo.normalNumber = removeNonDigits(num);
                String s9 = PhoneLocation.getCityFromPhone(contactInfo.normalNumber);
				if (s9 != null){
					contactInfo.location=s9;
				}else{
					if(!contactInfo.normalNumber.substring(0, 1).equals("1")){
						contactInfo.location="本地";
					}
					
				}
                contactInfo.normalName = nameToNumber(contact.getString(1));
                contactInfo.normalPinyin=pinyinToNumber(contactInfo.pinyin);
                contactInfo.timesContacted = contact.getInt(2);
                contactInfo.isSuperPrimary = phone.getInt(2) > 0;
                contactInfo.groupType = Phone.getTypeLabel(mContext.getResources(), phone.getInt(3), phone.getString(4));
                
                mContacts.add(contactInfo);
                if (!phone.moveToNext()) {
                    break;
                }
            }
        }
        contact.close();
        phone.close();
    }
    
    public void getAllPhoto() {
    	if(!photoisready){
    		Log.e("动作", "取得联系人头像开始！");
        	for(ContactItem item:mContacts){
        		Log.e("头像",item.name);
        		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, item.id);
                InputStream photoStream = Contacts.openContactPhotoInputStream(mContext.getContentResolver(), contactUri);
                if (photoStream != null) {
                    item.photo = BitmapFactory.decodeStream(photoStream);
                    try {
                        photoStream.close();
                    } catch (IOException e) {
                    }
                }
        	}
        	photoisready=true;
        	Log.e("动作", "取得联系人头像结束！");
    	}
    	
    }

    public static class T9SearchResult {

        private final ArrayList<ContactItem> mResults;
        private ContactItem mTopContact = new ContactItem();

        public T9SearchResult (final ArrayList<ContactItem> results, final Context mContext) {
            mTopContact = results.get(0);
            mResults = results;
            mResults.remove(0);
        }

        public int getNumResults() {
            return mResults.size() + 1;
        }

        public ContactItem getTopContact() {
            return mTopContact;
        }

        public ArrayList<ContactItem> getResults() {
            return mResults;
        }
    }

    public static class ContactItem {
        Bitmap photo;
        String name;
        String number;
        String[] pinyin;
        String normalNumber;
        String normalName;
        String[] normalPinyin;
        String location;
        int timesContacted;
        int nameMatchId;
        int numberMatchId;
        int pinyinMatchId;
        CharSequence groupType;
        long id;
        boolean isSuperPrimary;
    }

    public T9SearchResult search(String number) {
    	inputNumber=number;
        mNameResults.clear();
        mNumberResults.clear();
        mPinyinResults.clear();
        number = removeNonDigits(number);
        int pos = 0;
        mSortMode = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString("t9_sort", "1"));
        boolean newQuery = mPrevInput == null || number.length() <= mPrevInput.length();
        // Go through each contact
        for (ContactItem item : (newQuery ? mContacts : mAllResults)) {
            item.numberMatchId = -1;
            item.nameMatchId = -1;
            item.pinyinMatchId=-1;
            pos = item.normalNumber.indexOf(number);
            if (pos != -1) {
                item.numberMatchId = pos;
                mNumberResults.add(item);
            }
            pos = item.normalName.indexOf(number);
            if (pos != -1) {
                int last_space = item.normalName.lastIndexOf("0", pos);
                if (last_space == -1) {
                    last_space = 0;
                }
                item.nameMatchId = pos - last_space;
                mNameResults.add(item);
            }
            if(number.length()<item.normalPinyin.length||number.length()==item.normalPinyin.length){
            	//Log.e("Pinyin", item.pinyin[0]+item.pinyin[1]);
            	//Log.e("number",number);
            	boolean have=false;
            	for(int i=0;i<number.length();i++){
            		if(item.normalPinyin[i].subSequence(0, 1).equals(number.subSequence(i, i+1))){
            			have=true;
            		}else{
            			have=false;
            			i=number.length();
            		}
            	}
            	if(have){
            		item.pinyinMatchId=number.length();
            		mPinyinResults.add(item);
            	}
            	
            }
        }
        mAllResults.clear();
        mPrevInput = number;
        Collections.sort(mNumberResults, new NumberComparator());
        Collections.sort(mNameResults, new NameComparator());
        Collections.sort(mPinyinResults,new PinyinComparator());
        if (mNameResults.size() > 0 || mNumberResults.size() > 0||mPinyinResults.size()>0) {
            switch (mSortMode) {
            case NAME_FIRST:
            	mAllResults.addAll(mPinyinResults);
                mAllResults.addAll(mNameResults);
                mAllResults.addAll(mNumberResults);
                mAllResults.addAll(mPinyinResults);
                break;
            case NUMBER_FIRST:
                mAllResults.addAll(mNumberResults);
                mAllResults.addAll(mPinyinResults);
                mAllResults.addAll(mNameResults);
            }
            return new T9SearchResult(new ArrayList<ContactItem>(mAllResults), mContext);
        }
        return null;
    }

    public static class NameComparator implements Comparator<ContactItem> {
        @Override
        public int compare(ContactItem lhs, ContactItem rhs) {
            int ret = compareInt(lhs.nameMatchId, rhs.nameMatchId);
            if (ret == 0) ret = compareInt(rhs.timesContacted, lhs.timesContacted);
            if (ret == 0) ret = compareBool(rhs.isSuperPrimary, lhs.isSuperPrimary);
            return ret;
        }
    }
    
    public static class PinyinComparator implements Comparator<ContactItem> {
        @Override
        public int compare(ContactItem lhs, ContactItem rhs) {
        	int ret=compareInt(lhs.pinyin.length,rhs.pinyin.length);
        	if (ret == 0) ret=compareString(getAllPinyin(lhs.pinyin),getAllPinyin(rhs.pinyin));
            if (ret == 0) ret = compareInt(rhs.timesContacted, lhs.timesContacted);
            if (ret == 0) ret = compareBool(rhs.isSuperPrimary, lhs.isSuperPrimary);
            return ret;
        }
    }

    public static class NumberComparator implements Comparator<ContactItem> {
        @Override
        public int compare(ContactItem lhs, ContactItem rhs) {
            int ret = compareInt(lhs.numberMatchId, rhs.numberMatchId);
            if (ret == 0) ret = compareInt(rhs.timesContacted, lhs.timesContacted);
            if (ret == 0) ret = compareBool(rhs.isSuperPrimary, lhs.isSuperPrimary);
            return ret;
        }
    }
    
    public static int compareString (String lhs, String rhs) {
        return lhs.compareTo(rhs);
    }

    public static int compareInt (int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static int compareBool (boolean lhs, boolean rhs) {
        return lhs == rhs ? 0 : lhs ? 1 : -1;
    }

    private void initT9Map() {
        String[] t9Array = mContext.getResources().getStringArray(R.array.t9_map);
        sT9Map = new char[t9Array.length][];
        int rc = 0;
        for (String item : t9Array) {
            int cc = 0;
            sT9Map[rc] = new char[item.length()];
            for (char ch : item.toCharArray()) {
                sT9Map[rc][cc] = ch;
                cc++;
            }
            rc++;
        }
    }

    private static String nameToNumber(String name) {
        StringBuilder sb = new StringBuilder();
        int len = name.length();
        for (int i = 0; i < len; i++) {
            boolean matched = false;
            char ch = Character.toLowerCase(name.charAt(i));
            for (char[] row : sT9Map) {
                for (char a : row) {
                    if (ch == a) {
                        matched = true;
                        sb.append(row[0]);
                        break;
                    }
                }
                if (matched) {
                    break;
                }
            }
            if (!matched) {
                sb.append(sT9Map[0][0]);
            }
        }
        return sb.toString();
    }
    private static String[] pinyinToNumber(String[] pinyin){
    	String[] result = new String[pinyin.length];
    	for(int i=0;i<pinyin.length;i++){
    		result[i]=nameToNumber(pinyin[i]);
    		Log.e("pinyinToNumber", result[i]);
    	}
    	return result;
    }

    public static String removeNonDigits(String number) {
        int len = number.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = number.charAt(i);
            if ((ch >= '0' && ch <= '9') || ch == '*' || ch == '#' || ch == '+') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    protected class T9Adapter extends ArrayAdapter<ContactItem> {

        private ArrayList<ContactItem> mItems;
        private LayoutInflater mMenuInflate;
        //private ContactPhotoManager mPhotoLoader;

        public T9Adapter(Context context, int textViewResourceId, ArrayList<ContactItem> items, LayoutInflater menuInflate) {
            super(context, textViewResourceId, items);
            mItems = items;
            mMenuInflate = menuInflate;
            //mPhotoLoader = photoLoader;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            SharedPreferences ePrefs=PreferenceManager.getDefaultSharedPreferences(mContext);
            int colorMatched = ePrefs.getInt("matched_text_color", Color.BLUE);

            if (convertView == null) {
                convertView = mMenuInflate.inflate(R.layout.row, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.rowName);
                holder.number = (TextView) convertView.findViewById(R.id.rowNumber);
                holder.icon = (QuickContactBadge) convertView.findViewById(R.id.rowBadge);
                holder.pinyin=(TextView) convertView.findViewById(R.id.rowPinyin);
                holder.location=(TextView)convertView.findViewById(R.id.city_local);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ContactItem o = mItems.get(position);
            if (o.name == null) {
                holder.name.setText(mContext.getResources().getString(R.string.t9_add_to_contacts));
                holder.number.setVisibility(View.GONE);
                holder.icon.setImageResource(R.drawable.sym_action_add);
                holder.icon.assignContactFromPhone(o.number, true);
                holder.pinyin.setVisibility(View.GONE);
                holder.location.setText(PhoneLocation.getCityFromPhone(inputNumber));
            } else {
            	StringBuffer pinyinall = new StringBuffer();
                for(int i=0;i<o.pinyin.length;i++){
                	pinyinall.append(o.pinyin[i]);
                }
                holder.name.setText(o.name, TextView.BufferType.SPANNABLE);
                holder.number.setText(o.normalNumber, TextView.BufferType.SPANNABLE);
                holder.number.setVisibility(View.VISIBLE);
                holder.pinyin.setText(pinyinall.toString(),TextView.BufferType.SPANNABLE);
                holder.location.setText(o.location);
                
                if (o.nameMatchId != -1) {
                    Spannable s = (Spannable) holder.name.getText();
                    int nameStart = o.normalName.indexOf(mPrevInput);
                    s.setSpan(new ForegroundColorSpan(colorMatched),
                            nameStart, nameStart + mPrevInput.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.name.setText(s);
                }
                if (o.numberMatchId != -1) {
                    Spannable s = (Spannable) holder.number.getText();
                    int numberStart = o.numberMatchId;
                    s.setSpan(new ForegroundColorSpan(colorMatched),
                            numberStart, numberStart + mPrevInput.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.number.setText(s);
                }
                if(o.pinyinMatchId!=-1){
                	int[] pos=new int[o.pinyinMatchId];
                	Spannable s=(Spannable) holder.pinyin.getText();
                	for(int i=0;i<o.pinyinMatchId;i++){
                		if(i==0){
                			pos[i]=o.pinyin[i].length();
                			s.setSpan(new ForegroundColorSpan(colorMatched), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                		}else{
                			pos[i]=pos[i-1]+o.pinyin[i].length();
                			s.setSpan(new ForegroundColorSpan(colorMatched), pos[i-1], pos[i-1]+1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                		}
                		
                	}
                	holder.pinyin.setText(s);
                }
                if (o.photo != null)
                    holder.icon.setImageBitmap(o.photo);
                else
                    holder.icon.setImageResource(R.drawable.ic_contact_list_picture);

                holder.icon.assignContactFromPhone(o.number, true);
            }
            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView number;
            TextView pinyin;
            TextView location;
            QuickContactBadge icon;
        }

    }
    private static String[] getPinyinFromSortKey(String s){
    	if(s.getBytes().length==s.length()){
    		String[] result={s};
    		return result;
    	}else{
    		String[] str3=s.split(" ");
    		String[] result=new String[str3.length/2];
    		if(str3.length%2==0){
    			for(int i=0;i<str3.length;i++){
        			if(i%2==0){
        				String str1=str3[i].substring(0, 1);
        				String str2=str3[i].substring(1);
        				result[i/2]=str1.toUpperCase()+str2.toLowerCase();
        			}
        		}
    		}else{
    			for(int i=0;i<(str3.length-1);i++){
    				if(i%2==0){
        				String str1=str3[i].substring(0, 1);
        				String str2=str3[i].substring(1);
        				result[i/2]=str1.toUpperCase()+str2.toLowerCase();
        			}
    			}
    		}
    		
    		return result;
    	}
    }
    private static String getAllPinyin(String[] pinyin){
    	StringBuffer allPinyin=new StringBuffer();
    	for(int i=0;i<pinyin.length;i++){
    		allPinyin.append(pinyin[i]);
    	}
    	return allPinyin.toString();
    }

}
