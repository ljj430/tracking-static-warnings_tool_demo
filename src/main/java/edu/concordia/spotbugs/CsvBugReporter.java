package edu.concordia.spotbugs;

import com.opencsv.CSVWriter;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;


import javax.annotation.CheckForNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CsvBugReporter extends TextUIBugReporter {
    private String separator = System.getProperty("file.separator");
    public String outputPath = "";
    public List violationList = new ArrayList<String[]>();
    public String OTHER_CATEGORY_ABBREV = "X";
    public HashSet seenAlredy = new HashSet<BugInstance>();

    public CsvBugReporter(String outputPath){
        this.outputPath = outputPath;
        String[] header = new String[] {"Violation","Package","Class","Method","Field","StartLine","EndLine","SourcePath","Priority","Category","File"};
        this.violationList.add(header);
    }
    public void doReportBug(BugInstance ins){
        if(seenAlredy.add(ins)){
//            System.out.println("\n" + ins.toString());
            recordBugInstance(ins);
            notifyObservers(ins);
        }
    }

    public void finish() {
        File outFile = new File(this.outputPath);
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(this.outputPath));
            writer.writeAll(this.violationList);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @CheckForNull
    public BugCollection getBugCollection() {
        // TODO nothing to do
        return null;
    }

    public void observeClass(ClassDescriptor classDescriptor) {
        // TODO do we need to compute statistics?
    }

    public void recordBugInstance(BugInstance bugInstance){
        String priority = "";
        switch(bugInstance.getPriority()){
            case Priorities.EXP_PRIORITY:
                priority = "E";
                break;
            case Priorities.LOW_PRIORITY:
                priority = "L";
                break;
            case Priorities.NORMAL_PRIORITY:
                priority = "M";
                break;
            case Priorities.HIGH_PRIORITY:
                priority = "H";
                break;
            default:
                priority = "false";
        }

        BugPattern pattern = bugInstance.getBugPattern();
        String categoryAbbrev = "X";

        if(pattern != null){
            BugCategory bcat = DetectorFactoryCollection.instance().getBugCategory(pattern.getCategory());
            if(bcat != null){
                categoryAbbrev = bcat.getAbbrev();
            }
        }

        String bugAbbv = "";
        if(pattern != null){
            if(getUseLongBugCodes()){
                bugAbbv = pattern.getType();
            }
        }

        List<?> annotationList = bugInstance.getAnnotations();

        ClassAnnotation aClass = bugInstance.getPrimaryClass();
        FieldAnnotation aField = bugInstance.getPrimaryField();
        MethodAnnotation aMethod = bugInstance.getPrimaryMethod();
        SourceLineAnnotation aSourceLine = bugInstance.getPrimarySourceLineAnnotation();



        String sPackage= bugInstance.getPrimaryClass().getPackageName();
        String sSourceFile = bugInstance.getPrimaryClass().getSourceFileName();
        String sSourcePath = sPackage.replaceAll("\\.",separator) + separator + sSourceFile;
//        System.out.println("1:"+bugInstance.getPrimaryClass().getSourceFileName());
//        System.out.println("2:"+bugInstance.getPrimaryClass().getTopLevelClass());
//        System.out.println("3:"+bugInstance.getPrimaryClass().getDescription());
//        System.out.println("4:"+bugInstance.getPrimaryClass().getSlashedClassName());
//        System.out.println("5:"+bugInstance.getPrimaryClass().getSimpleClassName());
//        System.out.println("6:"+bugInstance.getPrimaryClass().getClassDescriptor().toResourceName());
//        System.out.println("7:"+bugInstance.getPrimaryClass().getClassDescriptor().getPackageName());
//        System.out.println("8:"+bugInstance.getPrimaryClass().getSlashedClassName());



        String sClass="";
        String sMethod = "";
        String sField = "";

        int iStartLine;
        int iEndLine;


        if(aClass == null) {
            sClass = "";
        }
        else{
//            sClass = aClass.getClassName();
            sClass = sSourceFile;

            if(sClass.endsWith(".java")){
                sClass = sClass.replace(".java","");
            }
            else if (sClass.endsWith(".scala")){
                sClass = sClass.replace(".scala","");

            }
        }

        if(aField == null) {
            sField = "";
        }
        else{
            sField = aField.getFieldName();
        }

        if(aMethod == null) {
            sMethod = "";
        }
        else{
            sMethod = aMethod.getMethodName();
        }


        if(aSourceLine == null) {
            iStartLine = -1;
            iEndLine = -1;
        }
        else{
            iStartLine = aSourceLine.getStartLine();
            iEndLine = aSourceLine.getEndLine();
        }

        String[] bugRow = new String[] {bugAbbv,sPackage,sClass,sMethod,sField,String.valueOf(iStartLine),String.valueOf(iEndLine),sSourcePath,priority,categoryAbbrev,sSourceFile};
        this.violationList.add(bugRow);
    }
}
