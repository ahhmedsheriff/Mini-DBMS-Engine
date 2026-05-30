package DBMS;

import java.io.Serializable;

public class DenseIndexBlock implements Serializable
{
	String[] keys;
	int[] pageNums;
	int[] recNums;
	int size;
	int maxSize;

	public DenseIndexBlock(int maxSize)
	{
		this.maxSize = maxSize;
		keys = new String[maxSize];
		pageNums = new int[maxSize];
		recNums = new int[maxSize];
		size = 0;
	}
//add mthod
	public boolean add(String key, int pageNum, int recNum)
	{
		if (size >= maxSize)
			return false;
		keys[size] = key;
		pageNums[size] = pageNum;
		recNums[size] = recNum;
		size++;
		return true;
	}

	public int getSize()
	{
		return size;
	}

	public String getKey(int i)
	{
		return keys[i];
	}

	public int getPageNumber(int i)
	{
		return pageNums[i];
	}

	public int getRecordNumber(int i)
	{
		return recNums[i];
	}
//to striing 
	public String toString()
	{
		String result = "[";
		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				result += ", ";
			result += "(" + keys[i] + ", r" + recNums[i] + "@p" + pageNums[i] + ")";
		}
		result += "]";
		return result;
	}
}