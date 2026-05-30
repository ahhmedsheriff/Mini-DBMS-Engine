package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Table implements Serializable
{
    private String name;
    private String[] columnsNames;
    private int pageCount;
    private int recordsCount;
    private ArrayList<String> trace;
    private ArrayList<String> indexedColumns;

    public Table(String name, String[] columnsNames)
    {
        super();
        this.name           = name;
        this.columnsNames   = columnsNames;
        this.trace          = new ArrayList<String>();
        this.indexedColumns = new ArrayList<String>();
        this.trace.add("Table created name:" + name + ", columnsNames:"
                + Arrays.toString(columnsNames));
    }

    @Override
    public String toString()
    {
        return "Table [name=" + name + ", columnsNames="
                + Arrays.toString(columnsNames) + ", pageCount=" + pageCount
                + ", recordsCount=" + recordsCount + "]";
    }

    private int getColumnIndex(String colName)
    {
        for (int i = 0; i < columnsNames.length; i++)
            if (columnsNames[i].equals(colName)) return i;
        return -1;
    }

    public String[] fixCond(String[] cols, String[] vals)
    {
        String[] res = new String[columnsNames.length];
        for (int i = 0; i < res.length; i++)
            for (int j = 0; j < cols.length; j++)
                if (columnsNames[i].equals(cols[j]))
                    res[i] = vals[j];
        return res;
    }

    private HashMap<Integer, ArrayList<String[]>> buildPageRecordsMap()
    {
        HashMap<Integer, ArrayList<String[]>> map = new HashMap<>();
        for (int i = 0; i < trace.size(); i++)
        {
            String entry = trace.get(i);
            if (!entry.startsWith("Inserted:")) continue;
            int s = entry.indexOf("at page number:") + "at page number:".length();
            int e = entry.indexOf(",", s);
            if (e < 0) e = entry.length();
            int pageNum;
            try { pageNum = Integer.parseInt(entry.substring(s, e).trim()); }
            catch (NumberFormatException ex) { continue; }
            int rs = entry.indexOf("[") + 1;
            int re = entry.indexOf("]");
            String[] record = entry.substring(rs, re).split(", ");
            if (!map.containsKey(pageNum))
                map.put(pageNum, new ArrayList<String[]>());
            map.get(pageNum).add(record);
        }
        return map;
    }

    public void insert(String[] record)
    {
        long startTime = System.currentTimeMillis();

        Page current = FileManager.loadTablePage(this.name, pageCount - 1);
        if (current == null || !current.insert(record))
        {
            current = new Page();
            current.insert(record);
            pageCount++;
        }
        FileManager.storeTablePage(this.name, pageCount - 1, current);

        for (int i = 0; i < indexedColumns.size(); i++)
        {
            String col = indexedColumns.get(i);
            int colIndex = getColumnIndex(col);
            if (colIndex < 0) continue;
            BitmapIndex bi = FileManager.loadTableIndex(this.name, col);
            if (bi != null)
            {
                bi.addRecord(record[colIndex]);
                FileManager.storeTableIndex(this.name, col, bi);
            }
        }

        recordsCount++;
        long stopTime = System.currentTimeMillis();
        this.trace.add("Inserted:" + Arrays.toString(record)
                + ", at page number:" + (pageCount - 1)
                + ", execution time (mil):" + (stopTime - startTime));
    }

    public ArrayList<String[]> select(String[] cols, String[] vals)
    {
        String[] cond  = fixCond(cols, vals);
        String tracer  = "Select condition:" + Arrays.toString(cols) + "->" + Arrays.toString(vals);
        ArrayList<ArrayList<Integer>> pagesResCount = new ArrayList<ArrayList<Integer>>();
        ArrayList<String[]> res = new ArrayList<String[]>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < pageCount; i++)
        {
            Page p = FileManager.loadTablePage(this.name, i);
            if (p == null) continue;
            ArrayList<String[]> pRes = p.select(cond);
            if (pRes.size() > 0)
            {
                ArrayList<Integer> pr = new ArrayList<Integer>();
                pr.add(i);
                pr.add(pRes.size());
                pagesResCount.add(pr);
                res.addAll(pRes);
            }
        }

        long stopTime = System.currentTimeMillis();
        tracer += ", Records per page:" + pagesResCount + ", records:" + res.size()
                + ", execution time (mil):" + (stopTime - startTime);
        this.trace.add(tracer);
        return res;
    }

    public ArrayList<String[]> select(int pageNumber, int recordNumber)
    {
        String tracer  = "Select pointer page:" + pageNumber + ", record:" + recordNumber;
        ArrayList<String[]> res = new ArrayList<String[]>();
        long startTime = System.currentTimeMillis();

        Page p = FileManager.loadTablePage(this.name, pageNumber);
        if (p != null)
        {
            ArrayList<String[]> pRes = p.select(recordNumber);
            if (pRes.size() > 0) res.addAll(pRes);
        }

        long stopTime = System.currentTimeMillis();
        tracer += ", total output count:" + res.size()
                + ", execution time (mil):" + (stopTime - startTime);
        this.trace.add(tracer);
        return res;
    }

    public ArrayList<String[]> select()
    {
        ArrayList<String[]> res = new ArrayList<String[]>();
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < pageCount; i++)
        {
            Page p = FileManager.loadTablePage(this.name, i);
            if (p != null) res.addAll(p.select());
        }

        long stopTime = System.currentTimeMillis();
        this.trace.add("Select all pages:" + pageCount + ", records:" + recordsCount
                + ", execution time (mil):" + (stopTime - startTime));
        return res;
    }

    public ArrayList<String[]> validateRecords()
    {
        ArrayList<String[]> missing = new ArrayList<String[]>();
        HashMap<Integer, ArrayList<String[]>> pageRecordsMap = buildPageRecordsMap();

        for (int i = 0; i < pageCount; i++)
        {
            Page p = FileManager.loadTablePage(this.name, i);
            if (p == null)
            {
                ArrayList<String[]> recs = pageRecordsMap.get(i);
                if (recs != null) missing.addAll(recs);
            }
        }

        this.trace.add("Validating records: " + missing.size() + " records missing.");
        return missing;
    }
//crete recoverecord method
    public void recoverRecords(ArrayList<String[]> missing)
    {
        ArrayList<Integer> missingPages = new ArrayList<Integer>();
        for (int i = 0; i < pageCount; i++)
        {
            Page p = FileManager.loadTablePage(this.name, i);
            if (p == null) missingPages.add(i);
        }

        HashMap<Integer, ArrayList<String[]>> pageRecordsMap = buildPageRecordsMap();
        for (int i = 0; i < missingPages.size(); i++)
        {
            int pageNum = missingPages.get(i);
            Page np = new Page();
            ArrayList<String[]> recs = pageRecordsMap.get(pageNum);
            if (recs != null)
                for (int j = 0; j < recs.size(); j++)
                    np.insert(recs.get(j));
            FileManager.storeTablePage(this.name, pageNum, np);
        }

        this.trace.add("Recovering " + missing.size() + " records in pages: " + missingPages);
    }
//create bitmapindx method
    public void createBitMapIndex(String colName)
    {
        long startTime = System.currentTimeMillis();

        int colIndex = getColumnIndex(colName);
        if (colIndex < 0) return;

        BitmapIndex bi = new BitmapIndex();
        for (int i = 0; i < pageCount; i++)
        {
            Page p = FileManager.loadTablePage(this.name, i);
            if (p == null) continue;
            ArrayList<String[]> records = p.select();
            for (int j = 0; j < records.size(); j++)
                bi.addRecord(records.get(j)[colIndex]);
        }

        FileManager.storeTableIndex(this.name, colName, bi);

        if (!indexedColumns.contains(colName))
            indexedColumns.add(colName);

        long stopTime = System.currentTimeMillis();
        this.trace.add("Index created for column:" + colName
                + ", execution time (mil):" + (stopTime - startTime));
    }

    public String getValueBits(String colName, String value)
    {
        BitmapIndex bi = FileManager.loadTableIndex(this.name, colName);
        if (bi == null) return "";
        return bi.getBits(value);
    }
//creat selctindex
    public ArrayList<String[]> selectIndex(String[] cols, String[] vals)
    {
        long startTime = System.currentTimeMillis();

        ArrayList<String> indexedCols    = new ArrayList<String>();
        ArrayList<String> indexedVals    = new ArrayList<String>();
        ArrayList<String> nonIndexedCols = new ArrayList<String>();
        ArrayList<String> nonIndexedVals = new ArrayList<String>();

        for (int i = 0; i < cols.length; i++)
        {
            if (indexedColumns.contains(cols[i]))
            {
                indexedCols.add(cols[i]);
                indexedVals.add(vals[i]);
            }
            else
            {
                nonIndexedCols.add(cols[i]);
                nonIndexedVals.add(vals[i]);
            }
        }

        ArrayList<String[]> result = new ArrayList<String[]>();

        if (indexedCols.isEmpty())
        {
            String[] cond = fixCond(cols, vals);
            for (int i = 0; i < pageCount; i++)
            {
                Page p = FileManager.loadTablePage(this.name, i);
                if (p != null) result.addAll(p.select(cond));
            }
        }
        else
        {
            BitmapIndex firstBI = FileManager.loadTableIndex(this.name, indexedCols.get(0));
            String resultBits   = firstBI.getBits(indexedVals.get(0));

            for (int i = 1; i < indexedCols.size(); i++)
            {
                BitmapIndex bi = FileManager.loadTableIndex(this.name, indexedCols.get(i));
                String bits    = bi.getBits(indexedVals.get(i));
                int len        = Math.min(resultBits.length(), bits.length());
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < len; j++)
                    sb.append((resultBits.charAt(j) == '1' && bits.charAt(j) == '1') ? '1' : '0');
                resultBits = sb.toString();
            }

            ArrayList<String[]> candidates = new ArrayList<String[]>();
            for (int i = 0; i < resultBits.length(); i++)
            {
                if (resultBits.charAt(i) != '1') continue;
                int pageNum = i / DBApp.dataPageSize;
                int recNum  = i % DBApp.dataPageSize;
                Page p = FileManager.loadTablePage(this.name, pageNum);
                if (p == null) continue;
                ArrayList<String[]> pageRecs = p.select();
                if (recNum < pageRecs.size()) candidates.add(pageRecs.get(recNum));
            }

            if (nonIndexedCols.isEmpty())
            {
                result = candidates;
            }
            else
            {
                String[] cond = fixCond(
                        nonIndexedCols.toArray(new String[0]),
                        nonIndexedVals.toArray(new String[0]));

                for (int i = 0; i < candidates.size(); i++)
                {
                    String[] record = candidates.get(i);
                    boolean match = true;
                    for (int j = 0; j < columnsNames.length; j++)
                    {
                        if (cond[j] != null && !cond[j].equals(record[j]))
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match) result.add(record);
                }
            }
        }

        long stopTime = System.currentTimeMillis();

        ArrayList<String> sortedIndexed    = new ArrayList<String>(indexedCols);
        ArrayList<String> sortedNonIndexed = new ArrayList<String>(nonIndexedCols);
        Collections.sort(sortedIndexed);
        Collections.sort(sortedNonIndexed);

        StringBuilder tracer = new StringBuilder();
        tracer.append("Select index condition:").append(Arrays.toString(cols))
              .append("->").append(Arrays.toString(vals));

        if (!indexedCols.isEmpty())
            tracer.append(", Indexed columns:").append(sortedIndexed.toString());

        if (!nonIndexedCols.isEmpty())
            tracer.append(", Non Indexed:").append(sortedNonIndexed.toString());

        tracer.append(", Final count: ").append(result.size());
        tracer.append(", execution time (mil):").append(stopTime - startTime);

        this.trace.add(tracer.toString());
        return result;
    }

    public String getFullTrace()
    {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < trace.size(); i++)
            res.append(trace.get(i)).append("\n");
        res.append("Pages Count: ").append(pageCount)
           .append(", Records Count: ").append(recordsCount)
           .append(", Indexed Columns: ").append(indexedColumns.toString());
        return res.toString();
    }
//create getlasttrac method
    public String getLastTrace()
    {
        return this.trace.get(this.trace.size() - 1);
    }
}