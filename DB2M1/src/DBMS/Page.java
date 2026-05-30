package DBMS;

import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
	 private static final long serialVersionUID = 1L;
	    public ArrayList<String[]> records;

	    public Page()
	    {
	        this.records = new ArrayList<String[]>();
	    }
}
