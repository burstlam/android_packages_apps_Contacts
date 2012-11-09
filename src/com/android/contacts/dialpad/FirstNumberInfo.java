package com.android.contacts.dialpad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.android.contacts.dialpad.ShenduContactAdapter.Shendu_ContactItem;
/**
 * @Time 2012-9-14 
 * @author shutao shutao@shendu.com
 * @module : Contacts
 * @Project: ShenDu OS 2.0
 * Linked list search logic data classes
 */
public class FirstNumberInfo {
	
	public  int NUMBER = 0;
	public  int FIRST = 1;
	public  int PINYIN = 2; 
	
	public static class NodeShendu_ContactItem{
		public Shendu_ContactItem contactItem;
		public String number ;
		public int type;
	}
	
	private ArrayList<NodeShendu_ContactItem> timeArrayList0 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList1 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList2 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList3 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList4 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList5 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList6 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList7 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList8 = new ArrayList<NodeShendu_ContactItem>();
	private ArrayList<NodeShendu_ContactItem> timeArrayList9 = new ArrayList<NodeShendu_ContactItem>();
	
	private ArrayList<NodeShendu_ContactItem> timeArrayListAdd = new ArrayList<NodeShendu_ContactItem>();
	
	public ArrayList<NodeShendu_ContactItem> getTimeArrayListAdd() {
		return timeArrayListAdd;
	}

	public void setTimeArrayListAdd(
			ArrayList<NodeShendu_ContactItem> timeArrayListAdd) {
		this.timeArrayListAdd = timeArrayListAdd;
	}


	private HashMap<String, String> matchMap = new HashMap<String, String>();
	
	public ArrayList<NodeShendu_ContactItem> getTimeArrayList0() {
		return timeArrayList0;
	}

	public void setTimeArrayList0(ArrayList<NodeShendu_ContactItem> timeArrayList0) {
		this.timeArrayList0 = timeArrayList0;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList1() {
		return timeArrayList1;
	}

	public void setTimeArrayList1(ArrayList<NodeShendu_ContactItem> timeArrayList1) {
		this.timeArrayList1 = timeArrayList1;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList2() {
		return timeArrayList2;
	}

	public void setTimeArrayList2(ArrayList<NodeShendu_ContactItem> timeArrayList2) {
		this.timeArrayList2 = timeArrayList2;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList3() {
		return timeArrayList3;
	}

	public void setTimeArrayList3(ArrayList<NodeShendu_ContactItem> timeArrayList3) {
		this.timeArrayList3 = timeArrayList3;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList4() {
		return timeArrayList4;
	}

	public void setTimeArrayList4(ArrayList<NodeShendu_ContactItem> timeArrayList4) {
		this.timeArrayList4 = timeArrayList4;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList5() {
		return timeArrayList5;
	}

	public void setTimeArrayList5(ArrayList<NodeShendu_ContactItem> timeArrayList5) {
		this.timeArrayList5 = timeArrayList5;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList6() {
		return timeArrayList6;
	}

	public void setTimeArrayList6(ArrayList<NodeShendu_ContactItem> timeArrayList6) {
		this.timeArrayList6 = timeArrayList6;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList7() {
		return timeArrayList7;
	}

	public void setTimeArrayList7(ArrayList<NodeShendu_ContactItem> timeArrayList7) {
		this.timeArrayList7 = timeArrayList7;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList8() {
		return timeArrayList8;
	}

	public void setTimeArrayList8(ArrayList<NodeShendu_ContactItem> timeArrayList8) {
		this.timeArrayList8 = timeArrayList8;
	}

	public ArrayList<NodeShendu_ContactItem> getTimeArrayList9() {
		return timeArrayList9;
	}

	public void setTimeArrayList9(ArrayList<NodeShendu_ContactItem> timeArrayList9) {
		this.timeArrayList9 = timeArrayList9;
	}
	
	
	
	public ArrayList<NodeShendu_ContactItem> getNumberContactsItem(char number){
		switch(number){
		case 43:
		  return getTimeArrayListAdd();
		case 48:
		  return	getTimeArrayList0();
		case 49:
			return	getTimeArrayList1();
		case 50:
			return	getTimeArrayList2();
		case 51:
			return	getTimeArrayList3();
		case 52:
			return	getTimeArrayList4();
		case 53:
			return	getTimeArrayList5();
		case 54:
			return	getTimeArrayList6();
		case 55:
			return	getTimeArrayList7();
		case 56:
			return	getTimeArrayList8();
		case 57:
			return	getTimeArrayList9();
		}
		
		return null;
		
	}
	
	public void setNumberContactsItem(char number , NodeShendu_ContactItem contactItem){
		switch(number){
		case 43:
			timeArrayListAdd.add(contactItem);
			break;
		case 48:
			timeArrayList0.add(contactItem);
		   break;
		case 49:
			timeArrayList1.add(contactItem);
			break;
		case 50:
			timeArrayList2.add(contactItem);
			break;
		case 51:
			timeArrayList3.add(contactItem);
			break;
		case 52:
			timeArrayList4.add(contactItem);
			break;
		case 53:
			timeArrayList5.add(contactItem);
			break;
		case 54:
			timeArrayList6.add(contactItem);
			break;
		case 55:
			timeArrayList7.add(contactItem);
			break;
		case 56:
			timeArrayList8.add(contactItem);
			break;
		case 57:
			timeArrayList9.add(contactItem);
			break;
		}
		
	}
	private final int MAXNUMS = 100000;
	public  synchronized ArrayList<Shendu_ContactItem>  searchNumber(String  input , int count){
		matchMap.clear();
		long time = System.currentTimeMillis();
		ArrayList< Shendu_ContactItem > numberList = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
		ArrayList< Shendu_ContactItem > pinyinList = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
		char num = input.toCharArray()[0];
		
		int inputCount = input.length();
		ArrayList<NodeShendu_ContactItem> data = getNumberContactsItem(num);
		/**shutao 2012-11-8*/
		if( data==null || data.size()<1){
			return numberList;
		}
		int start = binSearchMin(data , 0 , data.size()-1 , input)+1;
		if(start == data.size()){
			start = data.size()-1;
		}
//		System.out.println("start="+start+"num = "+num);
		
//		int listSize = 0;
		if(start == -1){

			if(data.get(0).number.contains(input)){
				start = 0;
				for(int  index = start ; index < data.size()  ; index++){
//					int pos = data.get(index).number.indexOf(input);
//			        if(pos!=-1){
					NodeShendu_ContactItem itme = data.get(index);
					if(itme.number.contains(input) /*&& listSize < count*/ ){
						if(!matchMap.containsKey(itme.contactItem.number)){
							itme.contactItem.type = itme.type;
							if(itme.type == NUMBER){
								numberList.add(itme.contactItem);
							}else{
							   pinyinList.add(itme.contactItem);
							}
				        	itme.contactItem.num = inputCount;
				        	matchMap.put(itme.contactItem.number, "");
//				         	listSize ++;
						}
			        }else{
			        	break;
			        }
				}
			}else{
				pinyinList.addAll( numberList );
				return pinyinList;
			}
		}else{
			for(int  index = start ; index < data.size()  ; index++){
//				int pos = data.get(index).number.indexOf(input);
//		        if(pos!=-1){
				NodeShendu_ContactItem itme = data.get(index);
				if(itme.number.contains(input)/* && listSize < count*/){
					if(!matchMap.containsKey(itme.contactItem.number)){
						itme.contactItem.type = itme.type;
						if(itme.type == NUMBER){
							numberList.add(itme.contactItem);
						}else{
						   pinyinList.add(itme.contactItem);
						}
			        	itme.contactItem.num = inputCount;
			        	matchMap.put(itme.contactItem.number, "");
					}
		        }else{
		        	break;
		        }
			}
		}
		int pinyinSize = pinyinList.size();
		if(pinyinSize < count && MAXNUMS != count){
			for(int i = 0 ; i < numberList.size();i++){
				if( i >= (count-pinyinSize)){
					break;
				}
				pinyinList.add(numberList.get(i));
			}
			return pinyinList;
		}else if( pinyinSize > count  && MAXNUMS != count ){
			ArrayList< Shendu_ContactItem > arrayList = new ArrayList<ShenduContactAdapter.Shendu_ContactItem>();
			for(int i = 0 ; i < pinyinList.size();i++){
				if(i >= count){
					break;
				}
				arrayList.add(pinyinList.get(i));
			}
			return arrayList;
		}
		pinyinList.addAll( numberList );
		return pinyinList;
		
	}
	
	private boolean matchMin(ArrayList<NodeShendu_ContactItem>  data, int mid, String input){
		String before = data.get(mid).number;
		if (mid == data.size()-1){
			return before.indexOf(input) == 0;
		}
		String after = data.get(mid+1).number;
		return before.indexOf(input) != 0 && after.indexOf(input) ==0;
		//return false;
	}
	
	private int binSearchMin(ArrayList<NodeShendu_ContactItem>  data , int start , int end ,String input){
		int mid = (end - start) / 2 + start; 
		if (matchMin(data,mid,input)) {   
            return mid;   
        }   
		if (start >= end) {   
            return -1;   
        } else if(data.get(mid).number.compareTo(input) < 0){
        	 return binSearchMin(data, mid + 1, end, input);  
        }else if (data.get(mid).number.compareTo(input) >= 0) {   
            return binSearchMin(data, start, mid - 1, input);   
        }   
		return -1;
		
	}
	
//	private int binSearchMin(ArrayList<NodeShendu_ContactItem>  data,String input){
//		
//		int start = 0; 
//		int end = data.size()-1;
//		while( end > start+1 ){
//			int middle = ( start + end ) / 2;
//			if(matchMin(data,middle,input)){
//				return middle;
//			}
//			if(data.get( middle ).number.compareTo( input ) < 0){
//				start = middle;
//			}else if(data.get( middle ).number.compareTo( input ) >= 0){
//				end = middle;
//			}
//		}
//		return -1;		
//	}
	
	
	public  void  comparatorArraylist(){
		Comparator<NodeShendu_ContactItem> comparator = new Comparator<NodeShendu_ContactItem>(){
			@Override
			public int compare(NodeShendu_ContactItem lhs, NodeShendu_ContactItem rhs) {
				// TODO Auto-generated method stub
				return lhs.number.compareTo(rhs.number);
				//return 0;
			}};
		Collections.sort(timeArrayListAdd,comparator);
		Collections.sort(timeArrayList0,comparator);
		Collections.sort(timeArrayList1,comparator);
		Collections.sort(timeArrayList2,comparator);
		Collections.sort(timeArrayList3,comparator);
		Collections.sort(timeArrayList4,comparator);
		Collections.sort(timeArrayList5,comparator);
		Collections.sort(timeArrayList6,comparator);
		Collections.sort(timeArrayList7,comparator);
		Collections.sort(timeArrayList8,comparator);
		Collections.sort(timeArrayList9,comparator);
		
//		System.out.println("add size = " + timeArrayListAdd.size()+"list 1 = siaze = "+ timeArrayList1.size());
		
	}

	
	public void clearAll(){
		
		timeArrayList0.clear();
		timeArrayList1.clear();
		timeArrayList2.clear();
		timeArrayList3.clear();
		timeArrayList4.clear();
		timeArrayList5.clear();
		timeArrayList6.clear();
		timeArrayList7.clear();
		timeArrayList8.clear();
		timeArrayList9.clear();
		timeArrayListAdd.clear();
		
	}

}
