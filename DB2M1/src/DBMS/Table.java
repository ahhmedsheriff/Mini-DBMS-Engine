package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Table implements Serializable
{
	private static final long serialVersionUID = 1L;
    public String tableName;
    public String[] columnsNames;
    public int pagesCount;
    public int recordsCount;
    public ArrayList<String> traceLog;

    public Table(String tableName, String[] columnsNames)
    {
        this.tableName    = tableName;
        this.columnsNames = columnsNames;
        this.pagesCount   = 0;
        this.recordsCount = 0;
        this.traceLog     = new ArrayList<String>();
    }
}
