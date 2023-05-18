package edu.concordia.tracking;

import com.opencsv.CSVWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class TrackedViolationWriter {
    public static void writer(HashSet<BugInstance> pa, HashSet<BugInstance> ch, String path, String chCommit, String staticTool) throws IOException {
        String resolvedName = "Disappeared_"+staticTool+"_" + chCommit +".csv";
        String newName = "NewlyIntroduced_"+staticTool+"_" + chCommit +".csv";

        Path resolvedPath = Paths.get(path,resolvedName);
        Path newlyIntroPath = Paths.get(path,newName);

        FileOutputStream fos1= new FileOutputStream(resolvedPath.toString());
        OutputStreamWriter osw1 = new OutputStreamWriter(fos1);
        CSVWriter w1 = new CSVWriter(osw1);
        ArrayList<String[]> rowsResolved = new ArrayList<>();
        String[] header = new String[]{"Violation","Package","Class","Method","Field","StartLine","EndLine","SourcePath"};
        rowsResolved.add(header);
        for(BugInstance ins : pa){
            String classPath = ins.getClassPath();
            String[] tokens = classPath.split("\\.");
            String className = tokens[tokens.length-1];
            String packageName = classPath.replace("."+className,"");
            String[] row = new String[]{ins.getViolation(),packageName,className,ins.getMethodName(),ins.getFieldName(),ins.getStartLine(),ins.getEndLine(),ins.getSourcePath()};
            rowsResolved.add(row);
        }
        w1.writeAll(rowsResolved);
        w1.close();

        FileOutputStream fos2= new FileOutputStream(newlyIntroPath.toString());
        OutputStreamWriter osw2 = new OutputStreamWriter(fos2);
        CSVWriter w2 = new CSVWriter(osw2);
        ArrayList<String[]> rowsNew = new ArrayList<>();
        rowsNew.add(header);
        for(BugInstance ins : ch){
            String classPath = ins.getClassPath();
            String[] tokens = classPath.split("\\.");
            String className = tokens[tokens.length-1];
            String packageName = classPath.replace("."+className,"");
            String[] row = new String[]{ins.getViolation(),packageName,className,ins.getMethodName(),ins.getFieldName(),ins.getStartLine(),ins.getEndLine(),ins.getSourcePath()};
            rowsNew.add(row);
        }
        w2.writeAll(rowsNew);
        w2.close();
    }
}
