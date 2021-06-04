package edu.concordia.tracking;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import edu.umd.cs.findbugs.BugPattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ViolationReader {
    public static ArrayList<BugInstance> Reader(String reportPath) throws IOException {
        Reader reader = new FileReader(reportPath);
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        List<String[]> list = new ArrayList<>();
        list = csvReader.readAll();
        reader.close();
        csvReader.close();
        ArrayList buginstanceList = new ArrayList();
        for(String[] row : list){
//            for(String e : row){
//                System.out.format("%s\t",e);
//            }
            BugInstance ins = new BugInstance();
            ins.setViolation(row[0]);
            ins.setClassPath(row[1]+"."+row[2]);
            ins.setClassName(row[2]);
            ins.setMethodName(row[3]);
            ins.setFieldName(row[4]);
            ins.setStartLine(row[5]);
            ins.setEndLine(row[6]);
            buginstanceList.add(ins);
        }

        return buginstanceList;
    }

    public static void main(String[] args) throws IOException {
        String PMDReport = "D:\\Git\\tmp\\pmdReport.csv";
        String SpotbugsReport = "D:\\Git\\tmp\\bugReport.csv";
        List a = Reader(SpotbugsReport);
    }
}
