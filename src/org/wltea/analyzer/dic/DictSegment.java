/**
 * 
 */
package org.wltea.analyzer.dic;

import java.util.HashMap;
import java.util.Map;


/**
 * IK Analyzer v3.2
 * 字典子片断 字典匹配核心类
 * 该类采用 数组 结合 HashMap，实现词典存储，词语匹配
 * 
 * 当下属的页节点小等于3时，采用数组存储
 * 当下属的页节点大于3时，采用HashMap存储
 * @author 林良益
 *
 */
public class DictSegment {
	
	//公用字典表，存储汉字
	private static final Map<Character , Character> charMap = new HashMap<Character , Character>(16 , 0.95f);
	
	//数组大小上限
	private static final int ARRAY_LENGTH_LIMIT = 3;
	
	//当前节点上存储的字符
	private Character nodeChar;
	
	//Map存储结构
	private Map<Character , DictSegment> childrenMap;
	
	//数组方式存储结构
	private DictSegment[] childrenArray;
	
	//当前节点存储的Segment数目
	//storeSize <=ARRAY_LENGTH_LIMIT ，使用数组存储， storeSize >ARRAY_LENGTH_LIMIT ,则使用Map存储
	private int storeSize = 0;
	
	//当前DictSegment状态 ,默认 0 , 1表示从根节点到当前节点的路径表示一个词
	private int nodeState = 0;	
	
	public DictSegment(Character nodeChar){
		if(nodeChar == null){
			throw new IllegalArgumentException("参数为空异常，字符不能为空");
		}
		this.nodeChar = nodeChar;
	}

	public Character getNodeChar() {
		return nodeChar;
	}
	
	/*
	 * 判断是否有下一个节点
	 */
	public boolean hasNextNode(){
		return  this.storeSize > 0;
	}
	
	/**
	 * 匹配词段
	 * @param charArray
	 * @return Hit
	 */
	public Hit match(char[] charArray){
		return this.match(charArray , 0 , charArray.length);
	}
	
	/**
	 * 匹配词段
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 
	 */
	public Hit match(char[] charArray , int begin , int length){
		Character keyChar = new Character(charArray[begin]);
		DictSegment ds = null;
		
		//引用实例变量为本地变量，避免查询时遇到更新的同步问题
		DictSegment[] segmentArray = this.childrenArray;
		Map<Character , DictSegment> segmentMap = this.childrenMap;		
		
		//STEP1 在节点中查找keyChar对应的DictSegment
		if(segmentArray != null){
			//在数组中查找
			for(DictSegment seg : segmentArray){
				if(seg != null && seg.nodeChar.equals(keyChar)){
					//找到匹配的段
					ds = seg;
				}
			}
		}else if(segmentMap != null){
			//在map中查找
			ds = (DictSegment)segmentMap.get(keyChar);
		}
		//STEP2 找到DictSegment，判断词的匹配状态，是否继续递归，还是返回结果
		if(ds != null){			
			if(length > 1){
				//词未匹配完，继续往下搜索
				return ds.match(charArray, begin + 1 , length - 1);
			}else if (length == 1){
				Hit searchHit= new Hit();
				//搜索最后一个char
				if(ds.nodeState == 1){
					//添加HIT状态为完全匹配
					searchHit.setMatch();
				}
				if(ds.hasNextNode()){
					//添加HIT状态为前缀匹配
					searchHit.setPrefix();
				}
				return searchHit;
			}
			
		}		
		//STEP3 没有找到DictSegment， 将HIT设置为不匹配
		return new Hit();		
	}

	/**
	 * 加载填充词典片段
	 * @param charArray
	 */
	public void fillSegment(char[] charArray){
		this.fillSegment(charArray, 0 , charArray.length); 
	}
	
	/**
	 * 加载填充词典片段
	 * @param charArray
	 * @param begin
	 * @param length
	 */
	public synchronized void fillSegment(char[] charArray , int begin , int length){
		//获取字典表中的汉字对象
		Character beginChar = new Character(charArray[begin]);
		Character keyChar = charMap.get(beginChar);
		//字典中没有该字，则将其添加入字典
		if(keyChar == null){
			charMap.put(beginChar, beginChar);
			keyChar = beginChar;
		}
		
		//搜索当前节点的存储，查询对应keyChar的keyChar，如果没有则创建
		DictSegment ds = lookforSegment(keyChar);
		//处理keyChar对应的segment
		if(length > 1){
			//词元还没有完全加入词典树
			ds.fillSegment(charArray, begin + 1, length - 1);
		}else if (length == 1){
			//已经是词元的最后一个char,设置当前节点状态为1，表明一个完整的词
			ds.nodeState = 1;
		}

	}
	
	/**
	 * 查找本节点下对应的keyChar的segment
	 * 如果没有找到，则创建新的segment
	 * @param keyChar
	 * @return
	 */
	private DictSegment lookforSegment(Character keyChar){
		
		DictSegment ds = null;

		if(this.storeSize <= ARRAY_LENGTH_LIMIT){
			//获取数组容器，如果数组未创建则创建数组
			DictSegment[] segmentArray = getChildrenArray();			
			//搜寻数组
			for(DictSegment segment : segmentArray){
				if(segment != null && segment.nodeChar.equals(keyChar)){
					//在数组中找到与keyChar对应的segment
					ds =  segment;
				}
			}			
			//遍历数组后没有找到对应的segment
			if(ds == null){
				//构造新的segment
				ds = new DictSegment(keyChar);				
				if(this.storeSize < ARRAY_LENGTH_LIMIT){
					//数组容量未满，使用数组存储
					segmentArray[this.storeSize] = ds;
				}else{
					//数组容量已满，切换Map存储
					//获取Map容器，如果Map未创建,则创建Map
					Map<Character , DictSegment> segmentMap = getChildrenMap();
					//将数组中的segment迁移到Map中
					migrate(segmentArray ,  segmentMap);
					//存储新的segment
					segmentMap.put(keyChar, ds);
					//释放当前的数组引用
					this.childrenArray = null;
				}
				//segment数目+1
				this.storeSize++;
			}			
			
		}else{
			//获取Map容器，如果Map未创建,则创建Map
			Map<Character , DictSegment> segmentMap = getChildrenMap();
			//搜索Map
			ds = (DictSegment)segmentMap.get(keyChar);
			if(ds == null){
				//构造新的segment
				ds = new DictSegment(keyChar);
				segmentMap.put(keyChar , ds);
				//当前节点存储segment数目+1
				this.storeSize ++;
			}
		}

		return ds;
	}
	
	
	/**
	 * 获取数组容器
	 * 线程同步方法
	 */
	private DictSegment[] getChildrenArray(){
		if(this.childrenArray == null){
			synchronized(this){
				if(this.childrenArray == null){
					this.childrenArray = new DictSegment[ARRAY_LENGTH_LIMIT];
				}
			}
		}
		return this.childrenArray;
	}
	
	/**
	 * 获取Map容器
	 * 线程同步方法
	 */	
	private Map<Character , DictSegment> getChildrenMap(){
		if(this.childrenMap == null){
			synchronized(this){
				if(this.childrenMap == null){
					this.childrenMap = new HashMap<Character , DictSegment>(ARRAY_LENGTH_LIMIT * 2,0.8f);
				}
			}
		}
		return this.childrenMap;
	}
	
	/**
	 * 将数组中的segment迁移到Map中
	 * @param segmentArray
	 */
	private void migrate(DictSegment[] segmentArray , Map<Character , DictSegment> segmentMap){
		for(DictSegment segment : segmentArray){
			if(segment != null){
				segmentMap.put(segment.nodeChar, segment);
			}
		}
	}
	
}
