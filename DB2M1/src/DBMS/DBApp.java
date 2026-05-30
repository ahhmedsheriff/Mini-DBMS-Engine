package DBMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class DBApp
{
	static int dataPageSize = 2;
	
	public static void createTable(String tableName, String[] columnsNames)
	{
		Table t = new Table(tableName, columnsNames);
        t.traceLog.add("Table created name:" + tableName
                + ", columnsNames:" + Arrays.toString(columnsNames));
        FileManager.storeTable(tableName, t);
	}
	
	public static void insert(String tableName, String[] record)
	{
		long start = System.currentTimeMillis();

        Table t = FileManager.loadTable(tableName);

        int targetPage;
        Page p;

        if (t.pagesCount == 0)
        {
            p = new Page();
            targetPage = 0;
            t.pagesCount = 1;
        }
        else
        {
            targetPage = t.pagesCount - 1;
            p = FileManager.loadTablePage(tableName, targetPage);

            if (p.records.size() >= dataPageSize)
            {
                p = new Page();
                targetPage = t.pagesCount;
                t.pagesCount++;
            }
        }

        p.records.add(record);
        t.recordsCount++;

        long time = System.currentTimeMillis() - start;

        t.traceLog.add("Inserted:" + Arrays.toString(record)
                + ", at page number:" + targetPage
                + ", execution time (mil):" + time);

        FileManager.storeTablePage(tableName, targetPage, p);
        FileManager.storeTable(tableName, t);
	}
	
	public static ArrayList<String []> select(String tableName)
	{
		long start = System.currentTimeMillis();

        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (int i = 0; i < t.pagesCount; i++)
        {
            Page p = FileManager.loadTablePage(tableName, i);
            result.addAll(p.records);
        }

        long time = System.currentTimeMillis() - start;

        t.traceLog.add("Select all pages:" + t.pagesCount
                + ", records:" + result.size()
                + ", execution time (mil):" + time);

        FileManager.storeTable(tableName, t);
        return result;
		//return new ArrayList<String[]>();
	}
	
	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		 long start = System.currentTimeMillis();

	        Table t = FileManager.loadTable(tableName);
	        Page p = FileManager.loadTablePage(tableName, pageNumber);

	        ArrayList<String[]> result = new ArrayList<String[]>();
	        result.add(p.records.get(recordNumber));

	        long time = System.currentTimeMillis() - start;

	        t.traceLog.add("Select pointer page:" + pageNumber
	                + ", record:" + recordNumber
	                + ", total output count:1"
	                + ", execution time (mil):" + time);

	        FileManager.storeTable(tableName, t);
	        return result;
		//return new ArrayList<String[]>();
	}
	
	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		long start = System.currentTimeMillis();

        Table t = FileManager.loadTable(tableName);

        int[] colIndices = new int[cols.length];
        for (int c = 0; c < cols.length; c++)
        {
            colIndices[c] = -1;
            for (int k = 0; k < t.columnsNames.length; k++)
            {
                if (t.columnsNames[k].equals(cols[c]))
                {
                    colIndices[c] = k;
                    break;
                }
            }
        }

        ArrayList<String[]> result = new ArrayList<String[]>();
        int[] pageFrequency = new int[t.pagesCount];

        for (int i = 0; i < t.pagesCount; i++)
        {
            Page p = FileManager.loadTablePage(tableName, i);
            for (String[] record : p.records)
            {
                boolean match = true;
                for (int c = 0; c < cols.length; c++)
                {
                    if (colIndices[c] == -1
                            || !record[colIndices[c]].equals(vals[c]))
                    {
                        match = false;
                        break;
                    }
                }
                if (match)
                {
                    result.add(record);
                    pageFrequency[i]++;
                }
            }
        }

        ArrayList<String> pagesCounts = new ArrayList<String>();
        for (int i = 0; i < pageFrequency.length; i++)
        {
            if (pageFrequency[i] != 0)
                pagesCounts.add("[" + i + ", " + pageFrequency[i] + "]");
        }

        long time = System.currentTimeMillis() - start;

        t.traceLog.add("Select condition:"
                + Arrays.toString(cols) + "->" + Arrays.toString(vals)
                + ", Records per page:" + pagesCounts.toString()
                + ", records:" + result.size()
                + ", execution time (mil):" + time);

        FileManager.storeTable(tableName, t);
        return result;
		//return new ArrayList<String[]>();
	}
	
	public static String getFullTrace(String tableName)
	{
		 Table t = FileManager.loadTable(tableName);

	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < t.traceLog.size(); i++)
	        {
	            sb.append(t.traceLog.get(i));
	            sb.append("\n");
	        }
	        sb.append("Pages Count: " + t.pagesCount
	                + ", Records Count: " + t.recordsCount);

	        return sb.toString();
		//return "";
	}
	
	public static String getLastTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
        if (t.traceLog.isEmpty()) return "";
        return t.traceLog.get(t.traceLog.size() - 1);
		//return "";
	}
	
	
	public static void main(String []args) throws IOException
	{
		FileManager.reset();

        String[] cols = {"id", "name", "major", "semester", "gpa"};
        createTable("student", cols);

        insert("student", new String[]{"1", "stud1", "CS",   "5", "0.9"});
        insert("student", new String[]{"2", "stud2", "BI",   "7", "1.2"});
        insert("student", new String[]{"3", "stud3", "CS",   "2", "2.4"});
        insert("student", new String[]{"4", "stud4", "DMET", "9", "1.2"});
        insert("student", new String[]{"5", "stud5", "BI",   "4", "3.5"});
        
        System.out.println("Output of selecting the whole table content:");
        for (String[] r : select("student"))
            System.out.println(Arrays.toString(r));

        System.out.println("----------------------------------------");
        System.out.println("Output of selecting the output by position:");
        for (String[] r : select("student", 1, 1))
            System.out.println(Arrays.toString(r));

        System.out.println("----------------------------------------");
        System.out.println("Output of selecting the output by column condition:");
        for (String[] r : select("student",
                new String[]{"gpa"}, new String[]{"1.2"}))
            System.out.println(Arrays.toString(r));

        System.out.println("----------------------------------------");
        System.out.println("Full Trace of the table:");
        System.out.println(getFullTrace("student"));

        System.out.println("----------------------------------------");
        System.out.println("Last Trace of the table:");
        System.out.println(getLastTrace("student"));

        System.out.println("----------------------------------------");
        System.out.println("The trace of the Tables Folder:");
        System.out.println(FileManager.trace());

        FileManager.reset();
        System.out.println("----------------------------------------");
        System.out.println("The trace of the Tables Folder after resetting:");
        System.out.println(FileManager.trace());
    
		
	}
	
	
	
}
