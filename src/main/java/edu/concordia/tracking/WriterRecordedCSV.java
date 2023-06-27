package edu.concordia.tracking;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class WriterRecordedCSV {
    public static void appendCSV(String filePath, String[] row) throws Exception{

        File file = new File(filePath);
        if(!file.exists()){
            String[] header = {"path","commit","compilation time","Spotbugs detection time","PMD detection time","Spotbugs tracking time","PMD tracking time"};
            FileOutputStream fos= new FileOutputStream(filePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            CSVWriter w = new CSVWriter(osw);
            ArrayList<String[]> rows = new ArrayList<>();
            rows.add(header);
            rows.add(row);
            w.writeAll(rows);
            w.close();
        }
        else{
            CSVWriter w = new CSVWriter(new FileWriter(filePath,true));
            w.writeNext(row);
            w.close();
        }
    }

    public static void main(String[] augs) throws Exception {
        String[]  row = {"D:\\project","13456","1234","2345"};
        String filePath = "D:\\Git\\tmp\\test.csv";
        appendCSV(filePath,row);
    }
}
