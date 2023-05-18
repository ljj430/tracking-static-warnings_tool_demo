package edu.concordia.spotbugs;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.TextUIBugReporter;
import edu.umd.cs.findbugs.FindBugs2;

import java.util.ArrayList;
import java.util.List;
public class SpotbugsRunner {
    public static void runSpotbugs(String targetFiles, String libFiles, TextUIBugReporter reporter) throws Exception
//                                   List<String> filterFiles,
//                                   String releaseName, String projectName)
    {
        Project project = new Project();
        FindBugs2 findbugs = new FindBugs2();

        findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());

        project.addFile(targetFiles);
        project.addAuxClasspathEntry(libFiles);

        reporter.setPriorityThreshold(Priorities.LOW_PRIORITY);
        int rankThreshold = SystemProperties.getInt("findbugs.maxRank",BugRanker.VISIBLE_RANK_MAX);
        reporter.setRankThreshold(rankThreshold);
        reporter.setUseLongBugCodes(true);
        reporter.setReportHistory(false);

//        DetectorFactoryCollection detectorFactoryCollection = new DetectorFactoryCollection();

        findbugs.setRankThreshold(rankThreshold);
        findbugs.setBugReporter(reporter);
        findbugs.setProject(project);
        findbugs.setUserPreferences(project.getConfiguration());
        findbugs.setClassScreener(new ClassScreener());
        findbugs.setRelaxedReportingMode(false);
        findbugs.setAbridgedMessages(false);
        findbugs.setAnalysisFeatureSettings(FindBugs.DEFAULT_EFFORT);
        findbugs.setMergeSimilarWarnings(true);

        findbugs.finishSettings();
        System.out.println("Start to run Spotbugs");
        findbugs.execute();
    }
    public static void main(String args[]) throws Exception {

        String files = "";
        String targetFiles = "/home/junjie/Desktop/tool_demo_StaticTracker/objects/source/guava";
        String outputPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/tmp/violation_spotbugs.csv";
        TextUIBugReporter reporter = new CsvBugReporter(outputPath);
        runSpotbugs(targetFiles, files , reporter );

//        String files = "";
//        String targetFiles = "D:\\ThesisProject\\trackingProjects\\jclouds\\core\\target\\classes\\org\\jclouds";
//        String outputPath = "D:\\Git\\tmp\\spotbugsTmp.csv";
//        TextUIBugReporter reporter = new CsvBugReporter(outputPath);
//        runSpotbugs(targetFiles, files , reporter );
    }
}
