/**
 * 
 */
package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

/**
 * 查询分析器
 * 针对IK Analyzer V3的优化实现
 * @author 林良益
 *
 */
public final class IKQueryParser {
	
	/**
	 * 优化query队列
	 * 减少Query表达式的嵌套
	 * @param queries
	 * @return
	 */
	private static Query optimizeQueries(List<Query> queries){	
		//生成当前branch 的完整query
		if(queries.size() == 0){
			return null;
		}else if(queries.size() == 1){
			return queries.get(0);
		}else{
			BooleanQuery mustQueries = new BooleanQuery();
			for(Query q : queries){
				mustQueries.add(q, Occur.MUST);
			}
			return mustQueries;
		}			
	}
	
	/**
	 * 单连续字窜（不带空格符）单Field查询分析
	 * @param field
	 * @param query
	 * @return
	 * @throws IOException
	 */
	private static Query _parse(String field , String query) throws IOException{
		if(field == null){
			throw new IllegalArgumentException("parameter \"field\" is null");
		}
		if(query == null || "".equals(query.trim())){
			return new TermQuery(new Term(field));
		}
		TokenBranch root = new TokenBranch(null);		
		//对查询条件q进行分词
		StringReader input = new StringReader(query.trim());
		IKSegmentation ikSeg = new IKSegmentation(input);
		for(Lexeme lexeme = ikSeg.next() ; lexeme != null ; lexeme = ikSeg.next()){
			//处理词元分支
			root.accept(lexeme);
		}
		return optimizeQueries(root.toQueries(field));	 
	}
	
	/**
	 * 单条件,单Field查询分析
	 * @param queryKeyword
	 * @return
	 * @throws IOException 
	 */
	public static Query parse(String field , String query) throws IOException{
		if(field == null){
			throw new IllegalArgumentException("parameter \"field\" is null");
		}
		String[] qParts = query.split("\\s");
		if(qParts.length > 1){			
			BooleanQuery resultQuery = new BooleanQuery();
			for(String q : qParts){		
				Query partQuery = _parse(field , q);
				if(partQuery != null && 
				          (!(partQuery instanceof BooleanQuery) || ((BooleanQuery)partQuery).getClauses().length>0)){
					resultQuery.add(partQuery, Occur.SHOULD); 
				}
			}
			return resultQuery;
		}else{
			return _parse(field , query);
		}
	}
	
	/**
	 * 多Field,单条件查询分析
	 * @param queryKeyword
	 * @return
	 * @throws IOException 
	 */
	public static Query parseMultiField(String[] fields , String query) throws IOException{
		if(fields == null){
			throw new IllegalArgumentException("parameter \"fields\" is null");
		}		
		BooleanQuery resultQuery = new BooleanQuery();		
		for(String field : fields){
			if(field != null){
				Query partQuery = parse(field , query);
				if(partQuery != null && 
				          (!(partQuery instanceof BooleanQuery) || ((BooleanQuery)partQuery).getClauses().length>0)){
					resultQuery.add(partQuery, Occur.SHOULD); 
				}
			}			
		}		
		return resultQuery;
	}
	
	/**
	 * 多Field,单条件,多Occur查询分析
	 * @param queryKeyword
	 * @return
	 * @throws IOException 
	 */
	public static Query parseMultiField(String[] fields , String query ,  BooleanClause.Occur[] flags) throws IOException{
		if(fields == null){
			throw new IllegalArgumentException("parameter \"fields\" is null");
		}
		if(flags == null){
			throw new IllegalArgumentException("parameter \"flags\" is null");
		}
		
		if (flags.length != fields.length){
		      throw new IllegalArgumentException("flags.length != fields.length");
		}		
		
		BooleanQuery resultQuery = new BooleanQuery();		
		for(int i = 0; i < fields.length; i++){
			if(fields[i] != null){
				Query partQuery = parse(fields[i] , query);
				if(partQuery != null && 
				          (!(partQuery instanceof BooleanQuery) || ((BooleanQuery)partQuery).getClauses().length>0)){
					resultQuery.add(partQuery, flags[i]); 
				}
			}			
		}		
		return resultQuery;
	}
	
	/**
	 * 多Field多条件查询分析
	 * @param fields
	 * @param queries
	 * @return
	 * @throws IOException 
	 */
	public static Query parseMultiField(String[] fields , String[] queries) throws IOException{
		if(fields == null){
			throw new IllegalArgumentException("parameter \"fields\" is null");
		}				
		if(queries == null){
			throw new IllegalArgumentException("parameter \"queries\" is null");
		}				
		if (queries.length != fields.length){
		      throw new IllegalArgumentException("queries.length != fields.length");
		}
		BooleanQuery resultQuery = new BooleanQuery();		
		for(int i = 0; i < fields.length; i++){
			if(fields[i] != null){
				Query partQuery = parse(fields[i] , queries[i]);
				if(partQuery != null && 
				          (!(partQuery instanceof BooleanQuery) || ((BooleanQuery)partQuery).getClauses().length>0)){
					resultQuery.add(partQuery, Occur.SHOULD); 
				}
			}			
		}		
		return resultQuery;
	}

	/**
	 * 多Field,多条件,多Occur查询分析
	 * @param fields
	 * @param queries
	 * @param flags
	 * @return
	 * @throws IOException
	 */
	public static Query parseMultiField(String[] fields , String[] queries , BooleanClause.Occur[] flags) throws IOException{
		if(fields == null){
			throw new IllegalArgumentException("parameter \"fields\" is null");
		}				
		if(queries == null){
			throw new IllegalArgumentException("parameter \"queries\" is null");
		}
		if(flags == null){
			throw new IllegalArgumentException("parameter \"flags\" is null");
		}
		
	    if (!(queries.length == fields.length && queries.length == flags.length)){
	        throw new IllegalArgumentException("queries, fields, and flags array have have different length");
	    }

	    BooleanQuery resultQuery = new BooleanQuery();		
		for(int i = 0; i < fields.length; i++){
			if(fields[i] != null){
				Query partQuery = parse(fields[i] , queries[i]);
				if(partQuery != null && 
				          (!(partQuery instanceof BooleanQuery) || ((BooleanQuery)partQuery).getClauses().length>0)){
					resultQuery.add(partQuery, flags[i]); 
				}
			}			
		}		
		return resultQuery;
	}	
	/**
	 * 词元分支
	 * 当分词出现歧义时，采用词元分支容纳不同的歧义组合
	 * @author 林良益
	 *
	 */
	private static class TokenBranch{
		
		private static final int REFUSED = -1;
		private static final int ACCEPTED = 0;
		private static final int TONEXT = 1;
		
		//词元分支左边界
		private int leftBorder;
		//词元分支右边界
		private int rightBorder;
		//当前分支主词元
		private Lexeme lexeme;
		//当前分支可并入的词元分支
		private List<TokenBranch> acceptedBranchs;
		//当前分支的后一个相邻分支
		private TokenBranch nextBranch;
		
		TokenBranch(Lexeme lexeme){
			if(lexeme != null){
				this.lexeme = lexeme;
				//初始化branch的左右边界
				this.leftBorder = lexeme.getBeginPosition();
				this.rightBorder = lexeme.getEndPosition();
			}
		}
		
		public int getLeftBorder() {
			return leftBorder;
		}

		public int getRightBorder() {
			return rightBorder;
		}

		public Lexeme getLexeme() {
			return lexeme;
		}

		public List<TokenBranch> getAcceptedBranchs() {
			return acceptedBranchs;
		}

		public TokenBranch getNextBranch() {
			return nextBranch;
		}

		public int hashCode(){
			if(this.lexeme == null){
				return 0;
			}else{
				return this.lexeme.hashCode() * 37;
			}
		}
		
		public boolean equals(Object o){			
			if(o == null){
				return false;
			}		
			if(this == o){
				return true;
			}
			if(o instanceof TokenBranch){
				TokenBranch other = (TokenBranch)o;
				if(this.lexeme == null ||
						other.getLexeme() == null){
					return false;
				}else{
					return this.lexeme.equals(other.getLexeme());
				}
			}else{
				return false;
			}			
		}	
		
		/**
		 * 组合词元分支
		 * @param _lexeme
		 * @return 返回当前branch能否接收词元对象
		 */
		boolean accept(Lexeme _lexeme){
			
			/*
			 * 检查新的lexeme 对当前的branch 的可接受类型
			 * acceptType : REFUSED  不能接受
			 * acceptType : ACCEPTED 接受
			 * acceptType : TONEXT   由相邻分支接受 
			 */			
			int acceptType = checkAccept(_lexeme);			
			switch(acceptType){
			case REFUSED:
				// REFUSE 情况
				return false;
				
			case ACCEPTED : 
				if(acceptedBranchs == null){
					//当前branch没有子branch，则添加到当前branch下
					acceptedBranchs = new ArrayList<TokenBranch>(2);
					acceptedBranchs.add(new TokenBranch(_lexeme));					
				}else{
					boolean acceptedByChild = false;
					//当前branch拥有子branch，则优先由子branch接纳
					for(TokenBranch childBranch : acceptedBranchs){
						acceptedByChild = childBranch.accept(_lexeme) || acceptedByChild;
					}
					//如果所有的子branch不能接纳，则由当前branch接纳
					if(!acceptedByChild){
						acceptedBranchs.add(new TokenBranch(_lexeme));
					}					
				}
				//设置branch的最大右边界
				if(_lexeme.getEndPosition() > this.rightBorder){
					this.rightBorder = _lexeme.getEndPosition();
				}
				break;
				
			case TONEXT : 
				//把lexeme放入当前branch的相邻分支
				if(this.nextBranch == null){
					//如果还没有相邻分支，则建立一个不交叠的分支
					this.nextBranch = new TokenBranch(null);
				}
				this.nextBranch.accept(_lexeme);
				break;
			}

			return true;
		}
		
		/**
		 * 将分支数据转成Query逻辑
		 * @return
		 */
		List<Query> toQueries(String fieldName){			
			List<Query> queries = new ArrayList<Query>(1);			
 			//生成当前branch 的query
			if(lexeme != null){
				queries.add(new TermQuery(new Term(fieldName , lexeme.getLexemeText())));
			}			
			//生成child branch 的query
			if(acceptedBranchs != null && acceptedBranchs.size() > 0){
				if(acceptedBranchs.size() == 1){
					Query onlyOneQuery = optimizeQueries(acceptedBranchs.get(0).toQueries(fieldName));
					if(onlyOneQuery != null){
						queries.add(onlyOneQuery);
					}					
				}else{
					BooleanQuery orQuery = new BooleanQuery();
					for(TokenBranch childBranch : acceptedBranchs){
						Query childQuery = optimizeQueries(childBranch.toQueries(fieldName));
						if(childQuery != null){
							orQuery.add(childQuery, Occur.SHOULD);
						}
					}
					if(orQuery.getClauses().length > 0){
						queries.add(orQuery);
					}
				}
			}			
			//生成nextBranch的query
			if(nextBranch != null){				
				queries.addAll(nextBranch.toQueries(fieldName));
			}
			return queries;	
		}
		
		/**
		 * 判断指定的lexeme能否被当前的branch接受
		 * @param lexeme
		 * @return 返回接受的形式
		 */
		private int checkAccept(Lexeme _lexeme){
			int acceptType = 0;
			
			if(_lexeme == null){
				throw new IllegalArgumentException("parameter:lexeme is null");
			}
			
			if(null == this.lexeme){//当前的branch是一个不交叠（ROOT）的分支
				if(this.rightBorder > 0  //说明当前branch内至少有一个lexeme
						&& _lexeme.getBeginPosition() >= this.rightBorder){
					//_lexeme 与 当前的branch不相交
					acceptType = TONEXT;
				}else{
					acceptType = ACCEPTED;
				}				
			}else{//当前的branch是一个有交叠的分支
				
				if(_lexeme.getBeginPosition() < this.lexeme.getBeginPosition()){
					//_lexeme 的位置比 this.lexeme还靠前（这种情况不应该发生）
					acceptType = REFUSED;
				}else if(_lexeme.getBeginPosition() >= this.lexeme.getBeginPosition()
							&& _lexeme.getBeginPosition() < this.lexeme.getEndPosition()){
					// _lexeme 与 this.lexeme相交
					acceptType = REFUSED;
				}else if(_lexeme.getBeginPosition() >= this.lexeme.getEndPosition()
							&& _lexeme.getBeginPosition() < this.rightBorder){
					//_lexeme 与 this.lexeme 不相交， 但_lexeme 与 当前的branch相交
					acceptType = ACCEPTED;
				}else{//_lexeme.getBeginPosition() >= this.rightBorder
					//_lexeme 与 当前的branch不相交
					acceptType=  TONEXT;
				}
			}
			return acceptType;
		}
	
	}
}
