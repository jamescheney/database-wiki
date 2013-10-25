package org.dbwiki.driver.rdbms;

import java.util.ArrayList;

import org.dbwiki.data.database.DatabaseAttributeNode;
import org.dbwiki.data.database.DatabaseElementNode;
import org.dbwiki.data.database.DatabaseGroupNode;
import org.dbwiki.data.database.DatabaseNode;

public class SizeOptimizer {
	private DatabaseNode mainNode;
	private int maxSize;
	private ArrayList<Integer> levelsSize;
	private int size;
	private int firstHiddenLevel=-1;
	public SizeOptimizer(DatabaseNode node, int maxSize) {
		this.mainNode=node;
		this.maxSize=maxSize;
		this.levelsSize=getDescCount(node,new ArrayList<Integer>(),0);
		countSize();
		if (maxSize<size)
			calculateLastLevel();
	}
	
	private void calculateLastLevel() {
		int curCnt=1;
		int i=0;
		while (curCnt<=maxSize && levelsSize.size()>i){
			firstHiddenLevel++;
			curCnt+=levelsSize.get(i);
			i++;
		}
	}
	public int getFirstHiddenLayer(){
		return firstHiddenLevel;
	}
	private void countSize(){
		
		for (int i=0; i<levelsSize.size(); i++)
			size=size+levelsSize.get(i);
		DatabaseNode node=mainNode;
		while (node.parent()!=null){
			size++;
			node=node.parent();
		}
		size++;
	}

	private ArrayList<Integer> getDescCount(DatabaseNode node,ArrayList<Integer> cnt,int lvl){
		if (cnt.size()<lvl+1)
			cnt.add(1);
		else
			cnt.set(lvl,cnt.get(lvl)+1);
		
		DatabaseElementNode element=null;
			if (node.isElement()) {
				element = (DatabaseElementNode)node;
			
				if (element.isAttribute()) {
					DatabaseAttributeNode attribute = (DatabaseAttributeNode)element;
					if (cnt.size()<lvl+2)
						cnt.add(lvl+1,attribute.value().size());
					else
						cnt.set(lvl+1,cnt.get(lvl+1)+attribute.value().size());
					
				} else {
					
					DatabaseGroupNode group = (DatabaseGroupNode)element;
					for (int iChild = 0; iChild < group.children().size(); iChild++) {
						cnt=getDescCount(group.children().get(iChild),cnt,lvl+1);
					}
				}
			}
			return  cnt;
	}
	

}
