package DBMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DBApp
{
    static int dataPageSize = 2;

    public static void createTable(String tableName, String[] columnsNames)
    {
        Table t = new Table(tableName, columnsNames);
        FileManager.storeTable(tableName, t);
    }

    public static void insert(String tableName, String[] record)
    {
        Table t = FileManager.loadTable(tableName);
        t.insert(record);
        FileManager.storeTable(tableName, t);
    }

    public static ArrayList<String[]> select(String tableName)
    {
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> res = t.select();
        FileManager.storeTable(tableName, t);
        return res;
    }

    public static ArrayList<String[]> select(String tableName, int pageNumber, int recordNumber)
    {
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> res = t.select(pageNumber, recordNumber);
        FileManager.storeTable(tableName, t);
        return res;
    }

    public static ArrayList<String[]> select(String tableName, String[] cols, String[] vals)
    {
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> res = t.select(cols, vals);
        FileManager.storeTable(tableName, t);
        return res;
    }

    public static String getFullTrace(String tableName)
    {
        Table t = FileManager.loadTable(tableName);
        return t.getFullTrace();
    }

    public static String getLastTrace(String tableName)
    {
        Table t = FileManager.loadTable(tableName);
        return t.getLastTrace();
    }

    public static ArrayList<String[]> validateRecords(String tableName)
    {
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> missing = t.validateRecords();
        FileManager.storeTable(tableName, t);
        return missing;
    }

    public static void recoverRecords(String tableName, ArrayList<String[]> missing)
    {
        Table t = FileManager.loadTable(tableName);
        t.recoverRecords(missing);
        FileManager.storeTable(tableName, t);
    }
//create bitmapindx method
    public static void createBitMapIndex(String tableName, String colName)
    {
        Table t = FileManager.loadTable(tableName);
        t.createBitMapIndex(colName);
        FileManager.storeTable(tableName, t);
    }

    public static String getValueBits(String tableName, String colName, String value)
    {
        Table t = FileManager.loadTable(tableName);
        return t.getValueBits(colName, value);
    }
//create slectindx 
    public static ArrayList<String[]> selectIndex(String tableName, String[] cols, String[] vals)
    {
        Table t = FileManager.loadTable(tableName);
        ArrayList<String[]> res = t.selectIndex(cols, vals);
        FileManager.storeTable(tableName, t);
        return res;
    }
//create main methodd
    public static void main(String[] args) throws IOException {

        FileManager.reset();

        String[] cols = {"id", "name", "major", "semester", "gpa"};
        createTable("student", cols);

        String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
        insert("student", r1);

        String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
        insert("student", r2);

        String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
        insert("student", r3);

        createBitMapIndex("student", "gpa");
        createBitMapIndex("student", "major");

        System.out.println(
            "Bitmap of the value of CS from the major index : "
            + getValueBits("student", "major", "CS")
        );

        System.out.println(
            "Bitmap of the value of 1.2 from the gpa index : "
            + getValueBits("student", "gpa", "1.2")
        );

        String[] r4 = {"4", "stud4", "CS", "9", "1.2"};
        insert("student", r4);

        String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
        insert("student", r5);

        System.out.println("After new insertions :");

        System.out.println(
            "Bitmap of the value of CS from the major index : "
            + getValueBits("student", "major", "CS")
        );

        System.out.println(
            "Bitmap of the valu of 1.2 frm the gpa index : "
            + getValueBits("student", "gpa", "1.2")
        );

        System.out.println(
            "Output of selection usng index when all columns of the slect conditions are indexed :"
        );

        ArrayList<String[]> result1 = selectIndex(
            "student",
            new String[]{"major", "gpa"},
            new String[]{"CS", "1.2"}
        );

        for (String[] array : result1) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println(
            "Last trace of the table : "
            + getLastTrace("student")
        );

        System.out.println("----------------------------------------");

        System.out.println(
            "Output of selection using index when only one column of the columns of the select conditions are indexed :"
        );

        ArrayList<String[]> result2 = selectIndex(
            "student",
            new String[]{"major", "semester"},
            new String[]{"CS", "5"}
        );

        for (String[] array : result2) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println(
            "Last trace of the table : "
            + getLastTrace("student")
        );

        System.out.println("----------------------------------------");

        System.out.println(
            "Output of selection using index when som of the column of the select condtions are indxed :"
        );

        ArrayList<String[]> result3 = selectIndex(
            "student",
            new String[]{"major", "semester", "gpa"},
            new String[]{"CS", "5", "0.9"}
        );

        for (String[] array : result3) {
            for (String str : array) {
                System.out.print(str + " ");
            }
            System.out.println();
        }

        System.out.println(
            "Last trace of the table : "
            + getLastTrace("student")
        );

        System.out.println("----------------------------------------");

        System.out.println("Full Trace of the table :");

        System.out.println(getFullTrace("student"));

        System.out.println("----------------------------------------");

        System.out.println("The trace of the Table Foldr :");

        System.out.println(FileManager.trace());
        FileManager.reset();
        createTable("student", cols);
        insert("student", r1);
        insert("student", r2);
        insert("student", r3);
        insert("student", r4);
        insert("student", r5);
        System.out.println(
            "File Manager trace befor deleting page : "
            + FileManager.trace()
        );
        String path = FileManager.class
            .getResource("FileManager.class")
            .toString();

        File directory = new File(
            path.substring(6, path.length() - 17)
            + File.separator
            + "Tables"
            + File.separator
            + "student"
            + File.separator
        );

        File[] contents = directory.listFiles();
        int[] pageDel = {0, 2};

        for (int i = 0; i < pageDel.length; i++) {
            contents[pageDel[i]].delete();
        }
        System.out.println(
            "File Manager trace after deletng pages : "
            + FileManager.trace()
        );
        ArrayList<String[]> tr = validateRecords("student");
        System.out.println("Missing record count : " + tr.size());
        recoverRecords("student", tr);
        System.out.println("----------------------------------------");
        System.out.println("Recovring the mising records.");
        tr = validateRecords("student");
        System.out.println("Missing record count : " + tr.size());
        System.out.println(
            "File Manger trace after recovring missing records : "
            + FileManager.trace()
        );
        System.out.println("----------------------------------------");
        System.out.println("Full trace of the table :");
        System.out.println(getFullTrace("student"));
    }
}