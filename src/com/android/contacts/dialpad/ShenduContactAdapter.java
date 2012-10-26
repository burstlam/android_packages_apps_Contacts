package com.android.contacts.dialpad;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.R;
import com.android.contacts.dialpad.T9Search.ContactItem;
import com.android.contacts.vcard.VCardService.MyBinder;
import com.android.phone.location.PhoneLocation;

/**
 * @Time 2012-9-14 
 * @author shutao shutao@shendu.com
 * @module : Contacts
 * @Project: ShenDu OS 2.0
 * Filterable Asynchronous parse Search data function
 */
public class ShenduContactAdapter extends BaseAdapter implements Filterable {
	
	private LayoutInflater mInflater;
	//Save the current search contact
	private ArrayList<Shendu_ContactItem> mContactinfoList = new ArrayList<Shendu_ContactItem>();
	//Save all your contacts
	private Set<Shendu_ContactItem> mOldInfoList = new LinkedHashSet<Shendu_ContactItem>();

	private ArrayList<Shendu_ContactItem> mInfoList = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
	
	//List of names, a list of numbers
	private List<String> mNameList = new ArrayList<String>();
	private List<String> mPhoneList  = new ArrayList<String>();
	//The name is converted to digital
	private List<String> mFirstNumberIndexs  = new ArrayList<String>();
	
	private ArrayList<String> mNameTONumberList =  new ArrayList<String>();
	private ArrayList<ArrayList<String>> mPinyinNumNames = new ArrayList<ArrayList<String>>();
	
	private ArrayList<Shendu_ContactItem> mStrangeCalls = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
	
    private ContactPhotoManager mPhotoLoader;
	
	private Context mContext;
	
    private static char[][] sT9Map;
    
    private SearchContactsListener mContactsListener;
    
    private Thread createDataThead;
    
    /**shutao 2012-10-26*/
    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
//			super.handleMessage(msg);
			Results results = (Results)msg.obj;
			if(results.constraint.equals(mPrevInput)){
//				log("publishResults =-===  "+constraint.toString()+"currentCharSequence"+currentCharSequence);
				/**shutao 2012-10-26*/
				mInfoList = (ArrayList<Shendu_ContactItem>) results.values;
				if (results.count > 0) {
					notifyDataSetChanged();
					mContactsListener.Contacts();
				} else {
					notifyDataSetInvalidated();
					mContactsListener.notContacts();
				}
			}
		}
    	
    };
    
    class Results {
    	ArrayList<Shendu_ContactItem> values;
    	String  constraint;
    	int count;
    }
    
    /** shutao 2012-9-14  */
    public interface SearchContactsListener{
    	public void notContacts();
    	public void Contacts();
    }
	
    // Phone number queries
    private static final String[] PHONE_PROJECTION = new String[] {Phone.NUMBER, Phone.CONTACT_ID, Phone.IS_SUPER_PRIMARY, Phone.TYPE, Phone.LABEL};
    private static final String PHONE_ID_SELECTION = Contacts.Data.MIMETYPE + " = ? ";
    private static final String[] PHONE_ID_SELECTION_ARGS = new String[] {Phone.CONTENT_ITEM_TYPE};
    private static final String PHONE_SORT = Phone.CONTACT_ID + " ASC";
    private static final String[] CONTACT_PROJECTION = new String[] {Contacts._ID, Contacts.DISPLAY_NAME, Contacts.TIMES_CONTACTED, Contacts.PHOTO_THUMBNAIL_URI
    	,Contacts.SORT_KEY_PRIMARY};
    private static final String CONTACT_QUERY = Contacts.HAS_PHONE_NUMBER + " > 0";
    private static final String CONTACT_SORT = Contacts._ID + " ASC";
	
	public ShenduContactAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
	    mPhotoLoader = ContactPhotoManager.getInstance(context);
       mPhotoLoader.preloadPhotosInBackground();
		if (mContactinfoList != null) {
			mContactinfoList.clear();
		}
//		getAll();
//		initSomeList();
	
	}

	/**SHUTAO 2012-10-16*/
	public synchronized void search(final String s){
		
//		createDataThead = new Thread(new Runnable() {
		
//		@Override
//		public void run() {
			// TODO Auto-generated method stub
		
		long time1 = System.currentTimeMillis();
		// TODO Auto-generated method stub
		Results results = new Results();
		ArrayList< Shendu_ContactItem > nameInitial = new ArrayList<Shendu_ContactItem>();
		ArrayList< Shendu_ContactItem > nameTopInitial = new ArrayList<Shendu_ContactItem>();
		ArrayList< Shendu_ContactItem > nameData = new ArrayList<Shendu_ContactItem>();
		ArrayList< Shendu_ContactItem > numberData = new ArrayList<Shendu_ContactItem>();
//		ArrayList<Shendu_ContactItem> result = new ArrayList<Shendu_ContactItem>();
		results.constraint =  s.toString();
	    String number = removeNonDigits(s);
		boolean newQuery = mPrevInput == null || number.length() <= mPrevInput.length();
//		log("performFiltering"+oldInfoList.size()+" phoneList.size()"+ phoneList.size());
		
				for(Shendu_ContactItem item : (newQuery ? mContactinfoList :mOldInfoList)){
					item.numberMatchId = -1;
					item.firstMathcId = -1;
					item.nameMatchId = -1;
					if(item.name != null){
						int pos = item.firstNumber.indexOf(number);
//						mNameTONumberList.get(i).indexOf(number);
						if (pos != -1) {
							
							item.firstMathcId = pos;
//							log("nameToNumList " + oldInfoList.get(i).nameMatchId);
							item.num = number.length();
//							result.add(oldInfoList.get(i));
//                        if(item.firstNumber.equals(number))
//                        nameTopInitial.add(item)	;
//							else
							nameInitial.add(item);
							
						}
						else 	/**shutao 2012-10-23*/
							if(item.pinyinNumber.contains(number)){
								item.pinyinMatchId = item.pinyinNumber.indexOf(number);
								item.num = number.length();
//							if(item.pinyinNumber.equals(number)){
//								 nameTopInitial.add(item);
//							}else
							     nameData.add(item);
							}
							else 
							{
								int index = item.number.indexOf(number);
								if (index !=-1 ) {
									item.nameMatchId = -1;
									item.numberMatchId = index;
//									log("phoneList " + mPhoneList.get(i)+"number"+number+"mOldInfoList.get(i)"+mOldInfoList.get(i).number);
									item.num = number.length();
									numberData.add(item);
//									result.add();
								}
							}
//						}else{
////							/**shutao 2012-9-26*/
////							if(/*matchNumberIndexOf01*/matchNumberIndexOf(i,number,mOldInfoList.get(i))){
//////								result.add(oldInfoList.get(i));
////								/**shutao 2012-9-26*/
////								if(mOldInfoList.get(i).nameMatchId == 0 &&
////										mOldInfoList.get(i).numberNums == mOldInfoList.get(i).name.length()){
////									 nameTopInitial.add(mOldInfoList.get(i));
////								}else
////								nameData.add(mOldInfoList.get(i));
////							}
//
						
					}else{
						int pos = item.number.indexOf(number);
						if (pos !=-1 ) {
							item.nameMatchId = -1;
							item.numberMatchId = pos;
//							log("phoneList " + mPhoneList.get(i)+"number"+number+"mOldInfoList.get(i)"+mOldInfoList.get(i).number);
							item.num = number.length();
							numberData.add(item);
//							result.add();
						}
					}
					 
				}

		mOldInfoList.clear();
		mPrevInput = s.toString();
		mOldInfoList.addAll(nameTopInitial);
		mOldInfoList.addAll(nameInitial);
		mOldInfoList.addAll(nameData);
		mOldInfoList.addAll(numberData);
//		log("performFiltering----------"+result.size());
		results.values = new ArrayList<Shendu_ContactItem>(mOldInfoList);
		results.count = mOldInfoList.size();
		nameTopInitial.clear();
		numberData.clear();
		nameInitial.clear();
		nameData.clear();
		Message msg = new Message();
		msg.obj = results;
		mHandler.sendMessage(msg);
		log("sech  ---  00091 time "+ (System.currentTimeMillis()-time1));
//		if(createDataThead!=null){
//		createDataThead.interrupt();
//		createDataThead = null;
//	}
//		}
//	});
//	createDataThead.start();
	}
	
	public void setSearchContactsListener(SearchContactsListener contactsListener){
		this.mContactsListener = contactsListener;
	}
    
	public synchronized void getAll() {
		long time1 = System.currentTimeMillis();
				  if (sT9Map == null)
			            initT9Map();

			        Cursor contact = mContext.getContentResolver().query(Contacts.CONTENT_URI, CONTACT_PROJECTION, CONTACT_QUERY, null, CONTACT_SORT);
			        Cursor phone = mContext.getContentResolver().query(Phone.CONTENT_URI, PHONE_PROJECTION, PHONE_ID_SELECTION, PHONE_ID_SELECTION_ARGS, PHONE_SORT);
			        phone.moveToFirst();
			        mNameList.clear();
			        mPhoneList.clear();
			        mNameTONumberList.clear();
			        mFirstNumberIndexs.clear();
					 mPinyinNumNames.clear();
			        if(mContactinfoList != null){
			        	mContactinfoList.clear();
			        }else{
			        	mContactinfoList = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
			        }

			        
			        while (contact.moveToNext()) {
			            long contactId = contact.getLong(0);
			            if (phone.isAfterLast()) {
			                break;
			            }
			            while (phone.getLong(1) == contactId) {
			                String num = phone.getString(0);
			                Shendu_ContactItem contactInfo = new Shendu_ContactItem();
			                contactInfo.id = contactId;
			                contactInfo.name = contact.getString(1);
			                contactInfo.number = removeNonDigits(num);
			                mNameList.add(contactInfo.name);
//			            	  contactInfo.pinYin = MyHanziToPinyin.getPinYin(contactInfo.name.replaceAll(",", ""));
//			            	  log("getall"+contactInfo.name+contactInfo.pinyin);
			                nameToPinYinAndNumber(contact.getString(4), contactInfo);
			                mPhoneList.add(contactInfo.number);
			                contactInfo.timesContacted = contact.getInt(2);
			                contactInfo.isSuperPrimary = phone.getInt(2) > 0;
			                contactInfo.groupType = Phone.getTypeLabel(mContext.getResources(), phone.getInt(3), phone.getString(4));
			                if (!contact.isNull(3)) {
			                    contactInfo.photo = Uri.parse(contact.getString(3));
			                }
			                mContactinfoList.add(contactInfo);
			                if (!phone.moveToNext()) {
			                    break;
			                }
			            }
			        }
			        contact.close();
			        phone.close();
			       getStrangeCallLogs();
			       mContactinfoList.addAll(mStrangeCalls);
//					mOldInfoList = mContactinfoList;
//					if(createDataThead!=null){
//						createDataThead.interrupt();
//						createDataThead = null;
//					}
				log("time1"+(System.currentTimeMillis() -time1 ));

    }
	
	private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final String FIRST_PINYIN_UNIHAN = "\u963F";
    private static final String LAST_PINYIN_UNIHAN = "\u84D9";
    private static final char FIRST_UNIHAN = '\u3400';
    /** shutao 2012-10-19*/
    public void nameToPinYinAndNumber(String name , Shendu_ContactItem contactInfo){
//    	 ArrayList<Boolean> isBoolean = new ArrayList<Boolean>();
    	 ArrayList<Integer> firstNumberIndexs = new ArrayList<Integer>();
     	 int nameLength = name.length();
    	 final StringBuilder sb = new StringBuilder();
    	 final StringBuilder sbNumber = new StringBuilder();
    	 final StringBuilder sbFirst = new StringBuilder();
    	 final StringBuilder hanzis = new StringBuilder(); 
    	 ArrayList<String> numberNum =  new ArrayList<String>();
    	 boolean isFirst = true;
    	 int cmp;
    	 int hanziNum = 0;
    	 for (int i = 0; i < nameLength; i++) {
    		 final char character = name.charAt(i);
    		 final String letter = Character.toString(character);
    		 cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
//    		 log("name = "+name);
    		 if(character == ' '){
    			 isFirst = true;
    			 if(hanzis.toString()!=null && !hanzis.toString().equals("")){
        			 numberNum.add(hanzis.toString());
//    				 log("hanzitustring = "+hanzis.toString());
    			 }
    			 hanzis.setLength(0);
    		 }else if (character < 256) {
    			 
    			 if(character>=65 && character <=90 || character >= 97 && character <= 122){
//    				 log("char == "+character);
    				 char num = LetterToNumber(character);
    				 sbNumber.append(num);
    				 hanzis.append(num);
    				 if(!isFirst){
    					 hanziNum++;
        				 sb.append((char)(character<97?(character+32):character));
    				 }else{
    					 hanziNum++;
//    					 isBoolean.add(true);
    					 firstNumberIndexs.add(hanziNum);
    					 sb.append(character);
    					 sbFirst.append(num);
    					 isFirst = false;
    				
    				 }
    				 
    			 }else if (character>=48 && character <=57){
    				 sbFirst.append(character);
    				 sb.append(character);
    				 sbNumber.append(character);
    				 hanzis.append(character);
    				 hanziNum ++ ;
    				 firstNumberIndexs.add(hanziNum);
//    				 isBoolean.add(true);
    			 }
    			 else{
    				 isFirst = false;
//    				 isBoolean.add(false);
    			 }
    		 }else if(character<FIRST_UNIHAN){
//    			 isBoolean.add(false);
    		 }else if(cmp < 0){
//    			 isBoolean.add(false);
    		 }else{
    			 cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
    			 if(cmp >0){
//    				 isBoolean.add(false);
    			 }
    		 }
    		
    	 }

//    	 log("pinYin ====="+hanzis.length()+"===="+hanzis.toString());
    	 if(hanzis.length()>0){
    		 numberNum.add(hanzis.toString());
    	 }
    	 contactInfo.pinYin = sb.toString();
    	 sb.setLength(0);
//    	 contactInfo.normalName = sbNumber.toString();
//        mNameTONumberList.add(sbNumber.toString());
    	 contactInfo.pinyinNumber = sbNumber.toString();
        sbNumber.setLength(0);
//        mFirstNumberIndexs.add(sbFirst.toString());
        contactInfo.firstNumber = sbFirst.toString()+sbNumber.toString();
        sbFirst.setLength(0);
//    	 contactInfo.isHanzis = isBoolean;
        if(contactInfo.firstNumberIndexs!=null){
        	contactInfo.firstNumberIndexs.clear();
        }
    	 contactInfo.firstNumberIndexs = firstNumberIndexs;
        mPinyinNumNames.add(numberNum);
    	
    }
    
    /**shutao 2012-10-19*/
    private static char LetterToNumber(char letter) {
    	letter=Character.toLowerCase(letter);
        char num = letter;
            boolean matched = false;
            for (char[] row : sT9Map) {
                for (char a : row) {
                    if (letter == a) {
                        matched = true;
                        num=row[0];
                        break;
                    }
                }
                if (matched) {
                    break;
                }
            }
        return num;
    }
	
    
    /**shutao 2012-10-25*/
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
	private void getStrangeCallLogs() {
		mStrangeCalls.clear();
		/** shutao 2012-9-27*/
		String selection = "_id in (select _id  from calls where "+CallLog.Calls.CACHED_NAME + " is null or "+CallLog.Calls.CACHED_NAME +"= \"\"  group by number having count (number) > 0)";
		Cursor cursor = mContext.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] { CallLog.Calls.NUMBER},
				selection, null, null);
		if (cursor == null) {
			return;
		}
//		log("cursor--getCount"+cursor.getCount());
		
		while (cursor.moveToNext()){
			Shendu_ContactItem r = new Shendu_ContactItem();
			r.number = cursor.getString(0);
			mPhoneList.add(r.number);
//			log("getStrangeCallLogs == "+r.number);
			r.name = null;
			mStrangeCalls.add(r);
		}
		cursor.close();

	}
	
	
//	   private  String nameToNumber(String name) {
//		   String [] names = name.split(",");
//		   StringBuilder sb = new StringBuilder();
//		   
//		   String [] numberNum =  new String[names.length]; ;
//		   
//		   StringBuilder first = new StringBuilder();
//		   for(int index = 0 ;index < names.length ; index++){
//			
//			   StringBuilder nums = new StringBuilder();
//			   String nameNum = names[index];
//			   int len = nameNum.length();
//		        for (int i = 0; i < len; i++) {
//		            boolean matched = false;
//		            char ch = Character.toLowerCase(nameNum.charAt(i));
//		            for (char[] row : sT9Map) {
//		                for (char a : row) {
//		                    if (ch == a) {
//		                        matched = true;
//		                        nums.append(row[0]);
//		                        break;
//		                    }
//		                }
//		                if (matched) {
//		                    break;
//		                }
//		            }
//		            if (!matched) {
//		            	nums.append(sT9Map[0][0]);
//		            }
//		        }
//		        
////		          log("subSequence"+(nums.toString().equals("")?"":nums.toString().subSequence(0, 1))+"nums"+nums.toString());
//		            first.append(nums.toString().equals("")?"":nums.subSequence(0, 1));
//		            sb.append(nums);
//		            numberNum[index] = nums.toString(); 
//		            nums.setLength(0);
//		            
//		   }
////		   mPinyinNumNames.add(numberNum);
//		   mFirstNumberIndexs.add(first.toString());
////	        log("nameToNumber"+sb.toString()+"first"+first.toString());
//	        return sb.toString();
//	    }
	
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


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mInfoList!=null?mInfoList.size():0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	class ViewHolder {
		QuickContactBadge imPhoto;
		TextView name;
		TextView number;
		TextView attribution;
		TextView pinYin;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.shendu_row, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.rowName);
            holder.number = (TextView) convertView.findViewById(R.id.rowNumber);
            holder.imPhoto = (QuickContactBadge) convertView.findViewById(R.id.rowBadge);
            holder.attribution = (TextView) convertView.findViewById(R.id.shendu_row_attribution);
            holder.pinYin = (TextView) convertView.findViewById(R.id.rowPinyin);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Shendu_ContactItem o = mInfoList.get(position);
        if (o.name == null) {
            holder.name.setText(o.number , TextView.BufferType.SPANNABLE);
            holder.number.setVisibility(View.GONE);
            holder.imPhoto.setImageResource(R.drawable.ic_contact_picture_holo_dark);
            holder.imPhoto.assignContactFromPhone(o.number, true);
        	  holder.pinYin.setVisibility(View.GONE);
            if (o.numberMatchId != -1) {
                Spannable s = (Spannable) holder.name.getText();
                int numberStart = o.numberMatchId;
//           	  log("getview yy== "+numberStart+"num"+(numberStart + o.num)+"name"+o.number);
                s.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
                        numberStart, numberStart + o.num, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.name.setText(s);
            }
        } else {
            holder.name.setText(o.name, TextView.BufferType.SPANNABLE);
            holder.number.setText(o.number , TextView.BufferType.SPANNABLE);
            /**shutao 2012-10-19*/
            holder.pinYin.setVisibility(View.VISIBLE);
            holder.pinYin.setText(o.pinYin ,TextView.BufferType.SPANNABLE);
            holder.number.setVisibility(View.VISIBLE);
            if (o.firstMathcId != -1) {
                Spannable s = (Spannable) holder.name.getText();
                Spannable sPinYin = (Spannable) holder.pinYin.getText();
                int sLeng = s.length(); 
                int nameStart = o.firstMathcId;
                int send = (nameStart + o.num) >= sLeng ?sLeng:(nameStart + o.num);
                
//                log("getview yy== "+nameStart+"num"+send+"name"+o.name+s.toString());
//                s.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
//                        nameStart, send , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                for(int index = nameStart ; index< o.firstNumberIndexs.size();index++){
                	if(index-nameStart == o.num){
                		break;
                	}
             	   int num =o.firstNumberIndexs.get(index);
//         		   log("o.firstNumberIndexs"+num+o.name);
         		   sPinYin.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
         				   num-1, num , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
//                holder.name.setText(s);
                holder.pinYin.setText(sPinYin);
            }else 
//            	if(o.nameMatchId != -1){
////            	 Spannable s = (Spannable) holder.name.getText();
//            	 Spannable sPinYin = (Spannable) holder.pinYin.getText();
////            	int sLeng = sPinYin.length(); 
////            	int send = (o.numberNums+nameStart) >= sLeng ?sLeng:(o.numberNums+nameStart);
////          	   sPinYin.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
////          			 nameStart, send , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
////                 log("getview ss== "+nameStart+"num"+ o.numberNums+"name"+o.name);
//
//
//                 for(int index = 0 ; index< o.pinyinNumberIndexs.size();index++){
//                 	int sPinyinStart = o.pinyinNumberIndexs.get(index);
//          		   sPinYin.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
//          				  sPinyinStart, sPinyinStart+1 , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                 }
//                 holder.pinYin.setText(sPinYin);
////                 holder.name.setText(s);
//            }else
            if (o.numberMatchId != -1) {
            	 holder.pinYin.setVisibility(View.GONE);
                Spannable s = (Spannable) holder.number.getText();
                int numberStart = o.numberMatchId;
                s.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
                        numberStart, numberStart + o.num, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.number.setText(s);
            }
            else if(o.pinyinMatchId != -1){
            	 Spannable sPinYin = (Spannable) holder.pinYin.getText();
            	 int sLeng = sPinYin.length();
            	 int nameStart = o.pinyinMatchId;
            	 int send = (o.num+nameStart) >= sLeng ?sLeng:(o.num+nameStart);
//            	 log("nameStart"+nameStart+"send = "+send);
        	     sPinYin.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.shendu_high_light)),
        			 nameStart, send , Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        	     holder.pinYin.setText(sPinYin);
            }
            if (o.photo != null)
                mPhotoLoader.loadDirectoryPhoto(holder.imPhoto, o.photo, true);
            else
                holder.imPhoto.setImageResource(ContactPhotoManager.getDefaultAvatarResId(false, true));
               
            holder.imPhoto.assignContactFromPhone(o.number, true);
        }
        
    	String city = PhoneLocation.getCityFromPhone(o.number);
		if(city != null){
			holder.attribution.setText(city);
		}else{
			holder.attribution.setText("");
		}
        return convertView;
    }

	
	private String mPrevInput = null;
	/***
	 * shutao 2012-9-14
	 * The following is added to the ListView filtering methods, 
	 * in accordance with the digital retrieval number, digital retrieve name.
	 */
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {


			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				// TODO Auto-generated method stub
			
				if(constraint.toString().equals(mPrevInput)){
//					log("publishResults =-===  "+constraint.toString()+"currentCharSequence"+currentCharSequence);
					/**shutao 2012-10-26*/
					mInfoList = (ArrayList<Shendu_ContactItem>) results.values;
					if (results.count > 0) {
						notifyDataSetChanged();
						mContactsListener.Contacts();
					} else {
						notifyDataSetInvalidated();
						mContactsListener.notContacts();
					}
				}
			}

			@Override
			protected FilterResults performFiltering(CharSequence s) {
				long time1 = System.currentTimeMillis();
				// TODO Auto-generated method stub
				FilterResults results = new FilterResults();
				ArrayList< Shendu_ContactItem > nameInitial = new ArrayList<Shendu_ContactItem>();
				ArrayList< Shendu_ContactItem > nameTopInitial = new ArrayList<Shendu_ContactItem>();
				ArrayList< Shendu_ContactItem > nameData = new ArrayList<Shendu_ContactItem>();
				ArrayList< Shendu_ContactItem > numberData = new ArrayList<Shendu_ContactItem>();
				ArrayList<Shendu_ContactItem> result = new ArrayList<Shendu_ContactItem>();
		
				mPrevInput = s.toString();
//				log("performFiltering"+oldInfoList.size()+" phoneList.size()"+ phoneList.size());
				String number = s.toString().replace(" ", "");
				number=number.toString().replace("-", "");
//				if (mOldInfoList != null && mOldInfoList.size() != 0) {
//					if ( mPhoneList != null) {
//			
//						for (int i = 0; i < mPhoneList.size(); i++) {
//							mOldInfoList.get(i).numberMatchId = -1;
//							mOldInfoList.get(i).firstMathcId = -1;
//							mOldInfoList.get(i).nameMatchId = -1;
//							if(mOldInfoList.get(i).name != null){
////								int pos = mFirstNumberIndexs.get(i).indexOf(number);
//////								mNameTONumberList.get(i).indexOf(number);
////								if (pos != -1) {
////									
////									mOldInfoList.get(i).firstMathcId = pos;
//////									log("nameToNumList " + oldInfoList.get(i).nameMatchId);
////									mOldInfoList.get(i).num = number.length();
//////									result.add(oldInfoList.get(i));
////	                            if(mFirstNumberIndexs.get(i).equals(number))
////	                            nameTopInitial.add(mOldInfoList.get(i))	;
////									else
////									nameInitial.add(mOldInfoList.get(i));
////									
////								}
////								else 	/**shutao 2012-10-23*/
//									if(mNameTONumberList.get(i).contains(number)){
//									mOldInfoList.get(i).pinyinMatchId = mNameTONumberList.get(i).indexOf(number);
//									mOldInfoList.get(i).num = number.length();
//									if(mNameTONumberList.get(i).equals(number)){
//										 nameTopInitial.add(mOldInfoList.get(i));
//									}else
//									     nameData.add(mOldInfoList.get(i));
//									}
//									else 
//									{
//										int index = mPhoneList.get(i).indexOf(number);
//										if (index !=-1 ) {
//											mOldInfoList.get(i).nameMatchId = -1;
//											mOldInfoList.get(i).numberMatchId = index;
////											log("phoneList " + mPhoneList.get(i)+"number"+number+"mOldInfoList.get(i)"+mOldInfoList.get(i).number);
//											mOldInfoList.get(i).num = number.length();
//											numberData.add(mOldInfoList.get(i));
////											result.add();
//										}
//									}
////								}else{
//////									/**shutao 2012-9-26*/
//////									if(/*matchNumberIndexOf01*/matchNumberIndexOf(i,number,mOldInfoList.get(i))){
////////										result.add(oldInfoList.get(i));
//////										/**shutao 2012-9-26*/
//////										if(mOldInfoList.get(i).nameMatchId == 0 &&
//////												mOldInfoList.get(i).numberNums == mOldInfoList.get(i).name.length()){
//////											 nameTopInitial.add(mOldInfoList.get(i));
//////										}else
//////										nameData.add(mOldInfoList.get(i));
//////									}
////
//								
//							}else{
//								int pos = mPhoneList.get(i).indexOf(number);
//								if (pos !=-1 ) {
//									mOldInfoList.get(i).nameMatchId = -1;
//									mOldInfoList.get(i).numberMatchId = pos;
////									log("phoneList " + mPhoneList.get(i)+"number"+number+"mOldInfoList.get(i)"+mOldInfoList.get(i).number);
//									mOldInfoList.get(i).num = number.length();
//									numberData.add(mOldInfoList.get(i));
////									result.add();
//								}
//							}
//							 
//						}
//					}
//				}
				
				result.addAll(nameTopInitial);
				result.addAll(nameInitial);
				result.addAll(nameData);
				result.addAll(numberData);
//				log("performFiltering----------"+result.size());
				results.values = result;
				results.count = result.size();
				nameTopInitial.clear();
				numberData.clear();
				nameInitial.clear();
				nameData.clear();
				log("sech  ---  1 time "+ (System.currentTimeMillis()-time1));
//				log("sech  ---  12 time "+ (System.currentTimeMillis()-time1));
//				log("sech  ---  13 time "+ (System.currentTimeMillis()-time1));
//				log("sech  ---  14 time "+ (System.currentTimeMillis()-time1));
//				log("sech  ---   1time "+ (System.currentTimeMillis()-time1));
//				log("sech  ---   time "+ (System.currentTimeMillis()-time1));
				return results;
			}
		};
		return filter;
	}
	
	public boolean numberMatch(String string, String s) {
		// TODO Auto-generated method stub
		if (null == string) return false;
		String dealStr = string.replace("-", "");
		dealStr = dealStr.replace(" ", "");
		if (dealStr.contains(s)) 
			return true;
		return false;
	}

	private synchronized boolean matchNumberIndexOf01(int index,String inputNumber,Shendu_ContactItem shendu_ContactItem){
		
		char[] inputNumbers = inputNumber.toCharArray();
		ArrayList<String> numbers = mPinyinNumNames.get(index);
		shendu_ContactItem.numberNums = 0;
	    int indexOne = -1;

		for(int inputIndex = 0 ,numberNum = 0 , numberIndex = 0 ; inputIndex <inputNumbers.length ; ){
			if(numberNum == numbers.size()){
    			shendu_ContactItem.numberNums = 0;
    			return false;
    		}
//			log("new"+"inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name +"cheng"+inputNumbers[inputIndex]+"--"+ numbers[numberNum]);
			if(numbers.get(numberNum).equals("")){
				numberNum++;
				numberIndex=0;
			}else
			
			if(inputNumbers[inputIndex] == numbers.get(numberNum).charAt(numberIndex)){
//				log("inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name +"cheng"+inputNumbers[inputIndex]+"--"+ numbers[numberNum].charAt(numberIndex));
				if(numberIndex == 0){
	    			++shendu_ContactItem.numberNums;
//	    	  		log("cheng"+indexOne+"numberNums"+shendu_ContactItem.numberNums);
		    	}
				inputIndex ++ ;
				numberIndex ++ ; 
				if(indexOne<0){
					indexOne = numberNum;
				}
				if( numberIndex == numbers.get(numberNum).length()){
		    		numberNum++;
		    		numberIndex=0;
		    	}
				
				
			}else{
//		  		log("inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name+"back");
				if( inputIndex==0 && numberIndex <= 1 ){
					numberNum ++ ;
					numberIndex = 0;
					if(numberIndex==0 && indexOne>=0){
//						log("duankai-----"+numberIndex);
						shendu_ContactItem.numberNums = 0;
		    			return false;
					}
				}else{
	    			shendu_ContactItem.numberNums = 0;
	    			return false;
	    		}
			}
			
		}
		
		shendu_ContactItem.nameMatchId = indexOne;
		
		return true;
		
	}
	
	
	
	/**
	 * shutao 2012-9-25
	 * @param index 
	 * @param number
	 * @param shendu_ContactItem
	 * @return Index input array to record the start and end positions
	 */
	private synchronized boolean matchNumberIndexOf(int index,String inputNumber,Shendu_ContactItem shendu_ContactItem){
		char[] inputNumbers = inputNumber.toCharArray();
		ArrayList<String> numbers = mPinyinNumNames.get(index);
		shendu_ContactItem.numberNums = 0;
		if(shendu_ContactItem.pinyinNumberIndexs!=null){
		shendu_ContactItem.pinyinNumberIndexs.clear();
		}
		String sName = "";
//		log("index  --- == "+index);
		int [] indexNums = new int[numbers.size()];
		for(int i=0 ;i<numbers.size();i++){
			sName = sName+numbers.get(i);
			indexNums[i]=sName.length();
		}
	    int indexOne = -1;
	    for(int inputIndex = 0 , numberNum = 0 , numberIndex = 0 ; inputIndex <inputNumbers.length ;){
	    	if(numberNum == numbers.size()){
    			shendu_ContactItem.numberNums = 0;
    			return false;
    		}
//	    	log("new"+"inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name +"cheng"+inputNumbers[inputIndex]+"--"+ numbers[numberNum]);
	    	if(inputNumbers[inputIndex] == numbers.get(numberNum).charAt(numberIndex)){
//	    		log("inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name +"cheng"+inputNumbers[inputIndex]+"--"+ numbers[numberNum].charAt(numberIndex));
	    		int num = numberNum==0?0:indexNums[numberNum-1];
//	    		log("num ====="+num);
	    		shendu_ContactItem.pinyinNumberIndexs.add(num+numberIndex);
	    		
	    		if(numberIndex == 0){
	    			++shendu_ContactItem.numberNums;
//	    	  		log("cheng"+indexOne+"numberNums"+shendu_ContactItem.numberNums);
		    		}
	    		numberIndex++;
	    		inputIndex++;
	    		if(indexOne<0){
					indexOne = numberNum;
				}
	    		if( numberIndex == numbers.get(numberNum).length()){
	    			numberNum++;
	    			numberIndex=0;
		    	}
	    	}else{
//	    		log("inputIndex = " + inputIndex + " numberNum = "+ numberNum + "numberIndex = " + numberIndex + shendu_ContactItem.name+"back");
	    		if(numberIndex==1){
	    			numberNum++;
	    			numberIndex=0;
	    		}else{
	    			shendu_ContactItem.numberNums = 0;
	    			return false;
	    		}
	    	}
	    }
	    
		shendu_ContactItem.nameMatchId = indexOne;
		
		return true;
		
	}
	

	
	private static boolean debug = false;

	private static void log(String msg) {
		if (debug)
			Log.i("1716", msg);
	}
	
    public static class Shendu_ContactItem {
        Uri photo;
        String name;
        String number;
        String normalNumber;
        String normalName;
        String pinYin;
        String firstNumber;
        String pinyinNumber;
        int timesContacted;
        int firstMathcId;
        int numberMatchId;
        int nameMatchId;
        int pinyinMatchId;
        int numberNums ;
        int num;
        CharSequence groupType;
        long id;
        ArrayList<Integer> firstNumberIndexs;
        ArrayList<Integer> pinyinNumberIndexs = new ArrayList<Integer>();
        boolean isSuperPrimary;
    }
}
