/**
 * 
 */
package org.wltea.analysis.help.statistics;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wltea.analysis.DictSegment;
import org.wltea.analysis.Dictionary;
import org.wltea.analysis.Hit;

import junit.framework.TestCase;

/**
 * 主词典统计分析工具类
 * @author 林良益
 * 
 */
public class DictionaryTester extends TestCase {
	
	public void testMainDicEncoding(){
		int count = 0;
        InputStream is = DictionaryTester.class.getResourceAsStream(Dictionary.PATH_DIC_MAIN);
		try {
			
			String theWord = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"), 512);
			do {
				theWord = br.readLine();
				if (theWord != null) {
					theWord = theWord.trim();
                    /*Test Logging*/
                    System.out.println(theWord);
				}
				count++;
			} while (theWord != null && count < 20);
			
		} catch (IOException ioe) {
			System.err.println("主词典库载入异常.");
			ioe.printStackTrace();
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void testMainDictMemoryConsume(){
        InputStream is = DictionaryTester.class.getResourceAsStream(Dictionary.PATH_DIC_MAIN);
        System.out.println(new Date() + " before load dictionary");
        DictSegment _root_ = new DictSegment((char)0);
        try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        System.out.println(new Date() + " loading dictionary");
		try {
			String theWord = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			do {
				theWord = br.readLine();
				if (theWord != null) {
					_root_.fillSegment(theWord.toCharArray());
				}
			} while (theWord != null);
			System.out.println(new Date() + " after load dictionary");

	
		} catch (IOException ioe) {
			System.err.println("主词典库载入异常.");
			ioe.printStackTrace();
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
        try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}	
	
	public void testCountWordHeader(){
		FileOutputStream fos = null;
		Map<String , Integer> wordMap = new HashMap<String ,Integer>();
        InputStream is = DictionaryTester.class.getResourceAsStream(Dictionary.PATH_DIC_MAIN);
        
		try {
			fos = new FileOutputStream("D:/testCountWordHeader.txt");
			String theWord = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			do {
				theWord = br.readLine();
				if (theWord != null) {
					theWord = theWord.trim();
					String key = theWord.substring(0,1);
					Integer c = wordMap.get(key);
					if(c == null){
						wordMap.put(key, new Integer(1));
					}else{
						wordMap.put(key, ++c);
					}
				}
			} while (theWord != null);
			
			int countOnlyOne = 0;
			int countMorethan64 = 0;
			Set<String> it = wordMap.keySet();
			for(String key : it){
				Integer c = wordMap.get(key);
				if(c == 1){
					countOnlyOne ++;
				}
				if(c > 64){
					countMorethan64 ++;
				}
				
				fos.write((key + " : " + c + "\r\n").getBytes());
			}
			fos.write(("Total : " + wordMap.size() + "\r\n").getBytes());
			fos.write(("OnlyOneCount : " + countOnlyOne + "\r\n").getBytes());
			fos.write(("MoreThen64Count : " + countMorethan64 + "\r\n").getBytes());
			fos.flush();
			
		} catch (IOException ioe) {
			System.err.println("主词典库载入异常.");
			ioe.printStackTrace();
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fos != null){
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void testSurNameDicEncoding(){
		int count = 0;
        InputStream is = DictionaryTester.class.getResourceAsStream(Dictionary.PATH_DIC_SURNAME);
		try {
			
			String theWord = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			do {
				theWord = br.readLine();
				if (theWord != null) {
					theWord = theWord.trim();
                    /*Test Logging*/
                    System.out.println(theWord);
				}
				count++;
			} while (theWord != null && count < 20);
			
		} catch (IOException ioe) {
			System.err.println("主词典库载入异常.");
			ioe.printStackTrace();
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void testDictSegmentSearch(){
        InputStream is = DictionaryTester.class.getResourceAsStream(Dictionary.PATH_DIC_SURNAME);
        System.out.println(new Date() + " before load dictionary");

        DictSegment _root_ = new DictSegment((char)0);
        List<String> allWords = new ArrayList<String>();
        
        System.out.println(new Date() + " loading dictionary");
		try {
			String theWord = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			do {
				theWord = br.readLine();
				if (theWord != null) {
					allWords.add(theWord.trim());
					_root_.fillSegment(theWord.trim().toCharArray());
				}
			} while (theWord != null);
			System.out.println(new Date() + " after load dictionary");

	
		} catch (IOException ioe) {
			System.err.println("主词典库载入异常.");
			ioe.printStackTrace();
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
        try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println(new Date() + " begin march");
		long begintime = System.currentTimeMillis();
		Hit hit = null;
		int umCount = 0;
		int mCount = 0;
		for(String word : allWords){			
			hit = _root_.match(word.toCharArray());
			if(hit.isUnmatch()){
				System.out.println(word);
				umCount++;
			}else{
				mCount++;
			}
		}
		System.out.println(new Date() + " finish march , cost " + (System.currentTimeMillis() - begintime ) + " millseconds");
		System.out.println("Match words : " + mCount + " Unmatch words : " + umCount);
	}
}
