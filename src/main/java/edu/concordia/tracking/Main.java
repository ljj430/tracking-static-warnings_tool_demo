package edu.concordia.tracking;

import edu.concordia.git.GitProxy;
import edu.concordia.spotbugs.CsvBugReporter;
import edu.umd.cs.findbugs.TextUIBugReporter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static edu.concordia.pmd.PMDRunner.runPMD;
import static edu.concordia.spotbugs.SpotbugsRunner.runSpotbugs;
import static edu.concordia.tracking.ViolationMatcher.initGitRepo;
import static edu.concordia.tracking.ViolationMatcher.matcherRefactoring;
import static edu.concordia.tracking.WriterRecordedCSV.appendCSV;

public class Main {
    private static String OS = System.getProperty("os.name").toLowerCase();
    public static boolean IS_WINDOWS = (OS.contains("win"));
    public static boolean IS_MAC = (OS.contains("mac"));
    public static boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);

    public static void main(String[] args) throws Exception {
        //1.static tool 2. testedProjectPath 3. saveReportPath  4. if(PMD) ruleSetPath else ""
        //
        if(args.length >5) {
//        if(true){
            String staticTool = args[0];
            String projectPath = args[1];
            String savePath = args[2];
            String rulesetPath = args[3];
            String compilerCommand = args[4];
            String githubURL = args[5];

            //1. run static analysis
            String paCommit = "";
            String chCommit = "";

            Path gitPath = Paths.get(projectPath, ".git");
            String gitProxyPath = gitPath.toString();
            GitProxy repoProxy = new GitProxy();
            Boolean initGit = initGitRepo(gitProxyPath);
            repoProxy.setURI(gitProxyPath);
            if (initGit) {
//            matcher(paVios,chVios,paCommit,chCommit,repoProxy,savePath);
                Iterable<RevCommit> commits = repoProxy.getLogAll();
                int count = 0;

                Repository repo = repoProxy.getRepository();

                for (RevCommit commit : commits) {
                    if (count == 0) {
                        RevCommit[] parents = commit.getParents();

                        if (commit.getParentCount() > 1 || parents == null) {
                            System.err.println("This commit has multiple parents");
                            break;
                        }
                        chCommit = commit.name();
                        paCommit = commit.getParent(0).name();
                    }
                    count++;
                }
                System.out.println("post-commit:"+chCommit);
                System.out.println("pre-commit:"+paCommit);

                Git git = new Git(repoProxy.getRepository());



                String[] cmds = new String[]{};
                if(IS_WINDOWS) {
                    String[] winStart = new String[]{"cmd","/c"};
                    cmds = new String[]{winStart[0],winStart[1], compilerCommand};
                }
                else if(IS_MAC || IS_UNIX){
                    String[] unixStart = new String[]{"/bin/sh","-c"};
                    cmds = new String[]{unixStart[0],unixStart[1], compilerCommand};
                }
                else{
                    System.err.println("The system our approach does not support to compile files");
                }
                long staticTime = 0;
                long startStatic = System.currentTimeMillis();
                if(staticTool.equals("Spotbugs")){
                    git.checkout().setName(chCommit).call();
                    //compilation
                    Runtime run = Runtime.getRuntime();
                    System.out.println("Compiling commit:"+chCommit + "...");


                    final Process p1 = run.exec(cmds, null, new File(projectPath));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(p1.getInputStream()));
                            try {
                                while (br.readLine() != null)

                                    System.out.println(br.readLine());
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    BufferedReader br = null;
                    br = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    p1.waitFor();
                    p1.destroy();

                    if(p1.waitFor()!=0){
                        if(p1.exitValue()==1){
                            System.err.println("Error of compilation on "+chCommit+" ...");
                        }
                    }

                    //runspotbugs of pa
                    System.out.println("Finish compilation and start to run Spotbugs");
                    Path spotbugsOut = Paths.get(savePath, "Spotbugs_"+chCommit+".csv");
                    TextUIBugReporter reporter = new CsvBugReporter(spotbugsOut.toString());
                    runSpotbugs(projectPath, "" , reporter );

                    //checkout pa
                    git.checkout().setName(paCommit).call();
                    //compilation ch
                    System.out.println("Compiling commit:"+paCommit+" ...");

                    final Process p2 = run.exec(cmds, null, new File(projectPath));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(p2.getInputStream()));
                            try {
                                while (br.readLine() != null)

                                    System.out.println(br.readLine());
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    br = null;
                    br = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
                    line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    p2.waitFor();
                    br.close();
                    p2.destroy();




                    if(p2.waitFor()!=0){
                        if(p2.exitValue()==1){
                            System.err.println("Error of compilation on "+paCommit);
                        }
                    }

                    //runspotbugs
                    System.out.println("Finish compilation and start to run Spotbugs");
                    spotbugsOut = Paths.get(savePath, "Spotbugs_"+paCommit+".csv");
                    reporter = new CsvBugReporter(spotbugsOut.toString());
                    runSpotbugs(projectPath, "" , reporter );
                    git.checkout().setName(chCommit).call();


                }
                else if (staticTool.equals("PMD")){
                    git.checkout().setName(chCommit).call();
                    Path savePMDPath = Paths.get(savePath, "PMD_"+chCommit+".csv");
                    runPMD(projectPath,rulesetPath,savePMDPath.toString());
                    git.checkout().setName(paCommit).call();
                    savePMDPath = Paths.get(savePath, "PMD_"+paCommit+".csv");
                    runPMD(projectPath,rulesetPath,savePMDPath.toString());
                    git.checkout().setName(chCommit).call();

                }
                else if (staticTool.equals("All")){
                    git.checkout().setName(chCommit).call();
                    //compilation
                    Runtime run = Runtime.getRuntime();


                    System.out.println("Compiling commit:"+chCommit + "...");

                    final Process p1 = run.exec(cmds, null, new File(projectPath));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(p1.getInputStream()));
                            try {
                                while (br.readLine() != null)

                                    System.out.println(br.readLine());
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    BufferedReader br = null;
                    br = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    p1.waitFor();

                    p1.destroy();

                    if(p1.waitFor()!=0){
                        if(p1.exitValue()==1){
                            System.err.println("Error of compilation on "+chCommit);
                        }
                    }

                    //run spotbugs of pa
                    Path spotbugsOut = Paths.get(savePath, "Spotbugs_"+chCommit+".csv");
                    TextUIBugReporter reporter = new CsvBugReporter(spotbugsOut.toString());
                    runSpotbugs(projectPath, "" , reporter );

                    //run PMD o pa
                    Path savePMDPath = Paths.get(savePath, "PMD_"+chCommit+".csv");
                    runPMD(projectPath,rulesetPath,savePMDPath.toString());

                    //checkout ch
                    git.checkout().setName(paCommit).call();
                    //compilation ch

                    System.out.println("Compiling commit:"+paCommit + "...");
                    final Process p2 = run.exec(cmds, null, new File(projectPath));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(p2.getInputStream()));
                            try {
                                while (br.readLine() != null)
                                    System.out.println(br.readLine());
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    br = null;
                    br = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
                    line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                    p2.waitFor();
                    br.close();
                    p2.destroy();

                    if(p2.waitFor()!=0){
                        if(p2.exitValue()==1){
                            System.err.println("Error of compilation on "+paCommit);
                        }
                    }

                    //runspotbugs
                    spotbugsOut = Paths.get(savePath, "Spotbugs_"+paCommit+".csv");
                    reporter = new CsvBugReporter(spotbugsOut.toString());
                    runSpotbugs(projectPath, "" , reporter );

                    //runPMD
                    savePMDPath = Paths.get(savePath, "PMD_"+paCommit+".csv");
                    runPMD(projectPath,rulesetPath,savePMDPath.toString());
                    git.checkout().setName(chCommit).call();
                }
                long endStatic = System.currentTimeMillis();
                staticTime = endStatic - startStatic;

                //2. run tracking approach
                String SpotbugsPaReport = "Spotbugs_" + paCommit+".csv";
                String SpotbugsChReport = "Spotbugs_" + chCommit+".csv";
                Path SpPaPath = Paths.get(savePath,SpotbugsPaReport);
                Path SpChPath = Paths.get(savePath,SpotbugsChReport);
                String PMDPaReport = "PMD_" + paCommit+".csv";
                String PMDChReport = "PMD_" + chCommit+".csv";
                Path PMDPaPath = Paths.get(savePath,PMDPaReport);
                Path PMDChPath = Paths.get(savePath,PMDChReport);

                long matchingTime = 0;
                long startMatching = System.currentTimeMillis();
                if(staticTool.equals("Spotbugs")) {
                    ArrayList paVios = ViolationReader.Reader(SpPaPath.toString());
                    ArrayList chVios = ViolationReader.Reader(SpChPath.toString());
                    System.out.println("Start to track on Spotbugs violations");
                    matcherRefactoring(paVios,chVios,paCommit,chCommit,repoProxy,projectPath,savePath,staticTool,githubURL);
                }
                else if (staticTool.equals("PMD")){
                    ArrayList paVios = ViolationReader.Reader(PMDPaPath.toString());
                    ArrayList chVios = ViolationReader.Reader(PMDChPath.toString());
                    System.out.println("Start to track on PMD violations");
                    matcherRefactoring(paVios,chVios,paCommit,chCommit,repoProxy,projectPath,savePath,staticTool,githubURL);
                }
                else if (staticTool.equals("All")){
                    ArrayList paViosPMD =  ViolationReader.Reader(PMDPaPath.toString());
                    ArrayList chViosPMD =  ViolationReader.Reader(PMDChPath.toString());
                    System.out.println("Start to track on PMD violations");
                    matcherRefactoring(paViosPMD ,chViosPMD ,paCommit,chCommit,repoProxy,projectPath,savePath,"PMD",githubURL);

                    ArrayList paViosSpotbugs = ViolationReader.Reader(SpPaPath.toString());
                    ArrayList chViosSpotbugs = ViolationReader.Reader(SpChPath.toString());
                    System.out.println("Start to track on Spotbugs violations");
                    matcherRefactoring(paViosSpotbugs,chViosSpotbugs,paCommit,chCommit,repoProxy,projectPath,savePath,"Spotbugs",githubURL);
                }
                long endMatching = System.currentTimeMillis();
                matchingTime = endMatching - startMatching;
                String[] row = {projectPath,chCommit,Long.toString(staticTime), Long.toString(matchingTime)};
                Path recordPath = Paths.get(savePath,"record.csv");
                appendCSV(recordPath.toString(),row);


            }
        }
        else{
            System.err.println("Args are less than 5, args are "+args.length);
        }

    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1)
                break;
            out.write((char) c);
        }
    }
}
