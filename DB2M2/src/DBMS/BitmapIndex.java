package DBMS;

import java.io.Serializable;
import java.util.HashMap;

public class BitmapIndex implements Serializable
{
    
    private HashMap<String, String> index;
    private int totalRecords;

    public BitmapIndex()
    {
        index = new HashMap<>();
        totalRecords = 0;
    }
//crete addrcord method
    public void addRecord(String value)
    {
        for (String key : index.keySet())
        {
            index.put(key, index.get(key) + "0");
        }

        if (!index.containsKey(value))
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < totalRecords; i++) sb.append('0');
            sb.append('1');
            index.put(value, sb.toString());
        }
        else
        {
            String bits = index.get(value);
            index.put(value, bits.substring(0, bits.length() - 1) + "1");
        }
        totalRecords++;
    }
    // create gtbits method
    public String getBits(String value)
    {
        if (!index.containsKey(value))
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < totalRecords; i++) sb.append('0');
            return sb.toString();
        }
        return index.get(value);
    }

    public int getTotalRecords()
    {
        return totalRecords;
    }
}