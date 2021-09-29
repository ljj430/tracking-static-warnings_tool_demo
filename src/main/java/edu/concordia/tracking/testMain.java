package edu.concordia.tracking;

import edu.concordia.git.GitProxy;
import edu.concordia.spotbugs.CsvBugReporter;
import edu.umd.cs.findbugs.TextUIBugReporter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static edu.concordia.pmd.PMDRunner.runPMD;
import static edu.concordia.spotbugs.SpotbugsRunner.runSpotbugs;
import static edu.concordia.tracking.ViolationMatcher.initGitRepo;
import static edu.concordia.tracking.ViolationMatcher.matcherRefactoring;
import static edu.concordia.tracking.WriterRecordedCSV.appendCSV;

public class testMain {

    private static String OS = System.getProperty("os.name").toLowerCase();
    public static boolean IS_WINDOWS = (OS.contains("win"));
    public static boolean IS_MAC = (OS.contains("mac"));
    public static boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);

    public static void main(String[] args) throws Exception {
        //1.static tool 2. testedProjectPath 3. saveReportPath  4. if(PMD) ruleSetPath else ""
        //


        //0:set commits and paths
        ArrayList<String> paCommits = new ArrayList<>();
//        paCommits.add("1bb6b693fa875ea41d5fdf007558514020dd5352");
//        paCommits.add("d1f1ec3725c49917a540e7ff950ad45f42720d63");
//        paCommits.add("41fb13d42cc283cae98b1b0e94c8113ae4666c45");
//        paCommits.add("902823713c55ebca1d0d654bc5afe2dfccc2e122");
//        paCommits.add("febb4aeb458c48001ac1b64abf71d6f7b14a1f02");
//        paCommits.add("c48232ef20d7540d0e88ff49e887a34797392027");
//        paCommits.add("a2d75219440a42a6e66ae17f30ad46fc2269e2a3");
//        paCommits.add("194376632c537754f15c54724e84fb2f4ff71d1e");
//        paCommits.add("a02d0f0ee7b9e2833abc74848f0d9defa2e82cab");
//        paCommits.add("ddab4c0f9d52fd95be3e357e89f097cc9a9eca60");
        paCommits.add("69d56c24f654b7feb1fc3fcb8541f21ccde244b0");

        ArrayList<String> chCommits = new ArrayList<>();
//        chCommits.add("d1f1ec3725c49917a540e7ff950ad45f42720d63");
//        chCommits.add("eb1adfd4a19d2e78aa71782c30417ee7e8129f21");
//        chCommits.add("8b66978bc7655635e8237a944a1001af92d2d249");
//        chCommits.add("febb4aeb458c48001ac1b64abf71d6f7b14a1f02");
//        chCommits.add("ed222af89aa593f83f058592a60fc12bbda0b93b");
//        chCommits.add("a2d75219440a42a6e66ae17f30ad46fc2269e2a3");
//        chCommits.add("b1185b0522751d58869a04116335f0165e7938ae");
//        chCommits.add("64be4d92ea69eddf11a25bb9944638adbff3a39a");
//        chCommits.add("ddab4c0f9d52fd95be3e357e89f097cc9a9eca60");
//        chCommits.add("69d56c24f654b7feb1fc3fcb8541f21ccde244b0");
        chCommits.add("065d2f7d45e83b96586597e89334736292247b3e");


        String projectPath = "D:\\Git\\test\\forked\\druid";

        String githubURL = "https://github.com/ljj430/druid";
        String rulesetPath = "D:\\ThesisProject\\findbugsanalysis\\FixPatternMining\\ruleset\\rulesets.xml";
        String staticTool = "All";
        Path gitPath = Paths.get(projectPath, ".git");
        String gitProxyPath = gitPath.toString();
        GitProxy repoProxy = new GitProxy();
        Boolean initGit = initGitRepo(gitProxyPath);
        repoProxy.setURI(gitProxyPath);
        Git git = new Git(repoProxy.getRepository());
        for(int i = 0; i<paCommits.size();i++){
            int x;
            if(i ==0){
                x = 10;
            }
            else{
                x = 10;
            }

            String savePath = "D:\\Git\\tmp\\druid\\rerun";
            String rootPath = "D:\\Git\\tmp\\druid\\rerun";
            savePath = Paths.get(savePath,Integer.toString(x)).toString();
            String chCommit = chCommits.get(i);
            String paCommit = paCommits.get(i);
            //2. run trackng approach


//            String compilerCommand = "mvn clean package -DskipTests ";
//            String[] cmds = new String[]{};
//            String[] winStart = new String[]{"cmd","/c"};
//            cmds = new String[]{winStart[0],winStart[1], compilerCommand};
//
//            long staticTime = 0;
//            long startStatic = System.currentTimeMillis();
//
//            git.checkout().setName(chCommit).call();
//            //compilation
//            Runtime run = Runtime.getRuntime();
//            System.out.println("Compiling commit:"+chCommit + "...");
//
//            final Process p1 = run.exec(cmds, null, new File(projectPath));
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    BufferedReader br = new BufferedReader(
//                            new InputStreamReader(p1.getInputStream()));
//                    try {
//                        while (br.readLine() != null)
//
//                            System.out.println(br.readLine());
//                        br.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            BufferedReader br = null;
//            br = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//            p1.waitFor();
//
//            p1.destroy();
//
//            if(p1.waitFor()!=0){
//                if(p1.exitValue()==1){
//                    System.err.println("Error of compilation on "+chCommit);
//                }
//            }
//
//            //run spotbugs of pa
//            Path spotbugsOut = Paths.get(savePath, "Spotbugs_"+chCommit+".csv");
//            TextUIBugReporter reporter = new CsvBugReporter(spotbugsOut.toString());
//            runSpotbugs(projectPath, "" , reporter );
//
//            //run PMD o pa
//            Path savePMDPath = Paths.get(savePath, "PMD_"+chCommit+".csv");
//            runPMD(projectPath,rulesetPath,savePMDPath.toString());
//
//            //checkout ch
//            git.checkout().setName(paCommit).call();
//            //compilation ch
//
//            System.out.println("Compiling commit:"+paCommit + "...");
//            final Process p2 = run.exec(cmds, null, new File(projectPath));
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    BufferedReader br = new BufferedReader(
//                            new InputStreamReader(p2.getInputStream()));
//                    try {
//                        while (br.readLine() != null)
//                            System.out.println(br.readLine());
//                        br.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            br = null;
//            br = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
//            line = null;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//            p2.waitFor();
//            br.close();
//            p2.destroy();
//
//            if(p2.waitFor()!=0){
//                if(p2.exitValue()==1){
//                    System.err.println("Error of compilation on "+paCommit);
//                }
//            }
//
//            //runspotbugs
//            spotbugsOut = Paths.get(savePath, "Spotbugs_"+paCommit+".csv");
//            reporter = new CsvBugReporter(spotbugsOut.toString());
//            runSpotbugs(projectPath, "" , reporter );
//
//            //runPMD
//            savePMDPath = Paths.get(savePath, "PMD_"+paCommit+".csv");
//            runPMD(projectPath,rulesetPath,savePMDPath.toString());
//            git.checkout().setName(chCommit).call();
//
//            long endStatic = System.currentTimeMillis();
//            staticTime = endStatic - startStatic;

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

            ArrayList paViosPMD =  ViolationReader.Reader(PMDPaPath.toString());
            ArrayList chViosPMD =  ViolationReader.Reader(PMDChPath.toString());
            System.out.println("Start to track on PMD violations");
            matcherRefactoring(paViosPMD ,chViosPMD ,paCommit,chCommit,repoProxy,projectPath,savePath,"PMD",githubURL);

//            ArrayList paViosSpotbugs = ViolationReader.Reader(SpPaPath.toString());
//            ArrayList chViosSpotbugs = ViolationReader.Reader(SpChPath.toString());
//            System.out.println("Start to track on Spotbugs violations");
//            matcherRefactoring(paViosSpotbugs,chViosSpotbugs,paCommit,chCommit,repoProxy,projectPath,savePath,"Spotbugs",githubURL);

            long endMatching = System.currentTimeMillis();
            matchingTime = endMatching - startMatching;
//            String[] row = {projectPath,chCommit,Long.toString(staticTime), Long.toString(matchingTime)};
//            Path recordPath = Paths.get(rootPath,"record.csv");
//            appendCSV(recordPath.toString(),row);
            System.out.println(String.format("Finish:", i));
        }





//        String staticTool = "All";
//        String projectPath = "D:\\Git\\test\\forked\\guava";
//        String savePath = "D:\\Git\\tmp\\guava";
//        String rulesetPath = "D:\\ThesisProject\\findbugsanalysis\\FixPatternMining\\ruleset\\rulesets.xml";
//        String compilerCommand = "mvn clean package -DskipTests ";
//        String githubURL = "https://github.com/junjie-li-8/guava";
//
//        //1. run static analysis
//        String paCommit = "";
//        String chCommit = "";
//
//        Path gitPath = Paths.get(projectPath, ".git");
//        String gitProxyPath = gitPath.toString();
//        GitProxy repoProxy = new GitProxy();
//        Boolean initGit = initGitRepo(gitProxyPath);
//        repoProxy.setURI(gitProxyPath);
//        if (initGit) {
////            matcher(paVios,chVios,paCommit,chCommit,repoProxy,savePath);
//            Iterable<RevCommit> commits = repoProxy.getLogAll();
//            int count = 0;
//            for (RevCommit commit : commits) {
//                if (count == 0) {
//                    if (commit.getParentCount() > 1) {
//                        System.err.println("This commit has multiple parents");
//                        break;
//                    }
//                    chCommit = commit.name();
//                } else if (count == 1) {
//                    paCommit = commit.name();
//                }
//                count++;
//            }
//            System.out.println("post-commit:"+chCommit);
//            System.out.println("pre-commit:"+paCommit);
//
//            Git git = new Git(repoProxy.getRepository());
//
//
//            String[] cmds = new String[]{};
//            if(IS_WINDOWS) {
//                String[] winStart = new String[]{"cmd","/c"};
//                cmds = new String[]{winStart[0],winStart[1], compilerCommand};
//            }
//            else if(IS_MAC || IS_UNIX){
//                String[] unixStart = new String[]{"/bin/sh","-c"};
//                cmds = new String[]{unixStart[0],unixStart[1], compilerCommand};
//            }
//            else{
//                System.err.println("The system our approach does not support to compile files");
//            }
//            long staticTime = 0;
//            long startStatic = System.currentTimeMillis();
//            if(staticTool.equals("Spotbugs")){
//                git.checkout().setName(chCommit).call();
//                //compilation
//                Runtime run = Runtime.getRuntime();
//                System.out.println("Compiling commit:"+chCommit + "...");
//
//
//                final Process p1 = run.exec(cmds, null, new File(projectPath));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BufferedReader br = new BufferedReader(
//                                new InputStreamReader(p1.getInputStream()));
//                        try {
//                            while (br.readLine() != null)
//
//                                System.out.println(br.readLine());
//                            br.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                BufferedReader br = null;
//                br = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
//                String line = null;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//                p1.waitFor();
//                p1.destroy();
//
//                if(p1.waitFor()!=0){
//                    if(p1.exitValue()==1){
//                        System.err.println("Error of compilation on "+chCommit+" ...");
//                    }
//                }
//
//                //runspotbugs of pa
//                System.out.println("Finish compilation and start to run Spotbugs");
//                Path spotbugsOut = Paths.get(savePath, "Spotbugs_"+chCommit+".csv");
//                TextUIBugReporter reporter = new CsvBugReporter(spotbugsOut.toString());
//                runSpotbugs(projectPath, "" , reporter );
//
//                //checkout pa
//                git.checkout().setName(paCommit).call();
//                //compilation ch
//                System.out.println("Compiling commit:"+paCommit+" ...");
//
//                final Process p2 = run.exec(cmds, null, new File(projectPath));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BufferedReader br = new BufferedReader(
//                                new InputStreamReader(p2.getInputStream()));
//                        try {
//                            while (br.readLine() != null)
//
//                                System.out.println(br.readLine());
//                            br.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                br = null;
//                br = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
//                line = null;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//                p2.waitFor();
//                br.close();
//                p2.destroy();
//
//
//
//
//                if(p2.waitFor()!=0){
//                    if(p2.exitValue()==1){
//                        System.err.println("Error of compilation on "+paCommit);
//                    }
//                }
//
//                //runspotbugs
//                System.out.println("Finish compilation and start to run Spotbugs");
//                spotbugsOut = Paths.get(savePath, "Spotbugs_"+paCommit+".csv");
//                reporter = new CsvBugReporter(spotbugsOut.toString());
//                runSpotbugs(projectPath, "" , reporter );
//                git.checkout().setName(chCommit).call();
//
//
//            }
//            else if (staticTool.equals("PMD")){
//                git.checkout().setName(chCommit).call();
//                Path savePMDPath = Paths.get(savePath, "PMD_"+chCommit+".csv");
//                runPMD(projectPath,rulesetPath,savePMDPath.toString());
//                git.checkout().setName(paCommit).call();
//                savePMDPath = Paths.get(savePath, "PMD_"+paCommit+".csv");
//                runPMD(projectPath,rulesetPath,savePMDPath.toString());
//                git.checkout().setName(chCommit).call();
//
//            }
//            else if (staticTool.equals("All")){
//                git.checkout().setName(chCommit).call();
//                //compilation
//                Runtime run = Runtime.getRuntime();
//
//
//                System.out.println("Compiling commit:"+chCommit + "...");
//
//                final Process p1 = run.exec(cmds, null, new File(projectPath));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BufferedReader br = new BufferedReader(
//                                new InputStreamReader(p1.getInputStream()));
//                        try {
//                            while (br.readLine() != null)
//
//                                System.out.println(br.readLine());
//                            br.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                BufferedReader br = null;
//                br = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
//                String line = null;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//                p1.waitFor();
//
//                p1.destroy();
//
//                if(p1.waitFor()!=0){
//                    if(p1.exitValue()==1){
//                        System.err.println("Error of compilation on "+chCommit);
//                    }
//                }
//
//                //run spotbugs of pa
//                Path spotbugsOut = Paths.get(savePath, "Spotbugs_"+chCommit+".csv");
//                TextUIBugReporter reporter = new CsvBugReporter(spotbugsOut.toString());
//                runSpotbugs(projectPath, "" , reporter );
//
//                //run PMD o pa
//                Path savePMDPath = Paths.get(savePath, "PMD_"+chCommit+".csv");
//                runPMD(projectPath,rulesetPath,savePMDPath.toString());
//
//                //checkout ch
//                git.checkout().setName(paCommit).call();
//                //compilation ch
//
//                System.out.println("Compiling commit:"+paCommit + "...");
//                final Process p2 = run.exec(cmds, null, new File(projectPath));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BufferedReader br = new BufferedReader(
//                                new InputStreamReader(p2.getInputStream()));
//                        try {
//                            while (br.readLine() != null)
//                                System.out.println(br.readLine());
//                            br.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                br = null;
//                br = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
//                line = null;
//                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
//                }
//                p2.waitFor();
//                br.close();
//                p2.destroy();
//
//                if(p2.waitFor()!=0){
//                    if(p2.exitValue()==1){
//                        System.err.println("Error of compilation on "+paCommit);
//                    }
//                }
//
//                //runspotbugs
//                spotbugsOut = Paths.get(savePath, "Spotbugs_"+paCommit+".csv");
//                reporter = new CsvBugReporter(spotbugsOut.toString());
//                runSpotbugs(projectPath, "" , reporter );
//
//                //runPMD
//                savePMDPath = Paths.get(savePath, "PMD_"+paCommit+".csv");
//                runPMD(projectPath,rulesetPath,savePMDPath.toString());
//                git.checkout().setName(chCommit).call();
//            }
//            long endStatic = System.currentTimeMillis();
//            staticTime = endStatic - startStatic;
//
//            //2. run tracking approach
//            String SpotbugsPaReport = "Spotbugs_" + paCommit+".csv";
//            String SpotbugsChReport = "Spotbugs_" + chCommit+".csv";
//            Path SpPaPath = Paths.get(savePath,SpotbugsPaReport);
//            Path SpChPath = Paths.get(savePath,SpotbugsChReport);
//            String PMDPaReport = "PMD_" + paCommit+".csv";
//            String PMDChReport = "PMD_" + chCommit+".csv";
//            Path PMDPaPath = Paths.get(savePath,PMDPaReport);
//            Path PMDChPath = Paths.get(savePath,PMDChReport);
//
//            long matchingTime = 0;
//            long startMatching = System.currentTimeMillis();
//            if(staticTool.equals("Spotbugs")) {
//                ArrayList paVios = ViolationReader.Reader(SpPaPath.toString());
//                ArrayList chVios = ViolationReader.Reader(SpChPath.toString());
//                System.out.println("Start to track on Spotbugs violations");
//                matcherRefactoring(paVios,chVios,paCommit,chCommit,repoProxy,projectPath,savePath,staticTool,githubURL);
//            }
//            else if (staticTool.equals("PMD")){
//                ArrayList paVios = ViolationReader.Reader(PMDPaPath.toString());
//                ArrayList chVios = ViolationReader.Reader(PMDChPath.toString());
//                System.out.println("Start to track on PMD violations");
//                matcherRefactoring(paVios,chVios,paCommit,chCommit,repoProxy,projectPath,savePath,staticTool,githubURL);
//            }
//            else if (staticTool.equals("All")){
//                ArrayList paViosPMD =  ViolationReader.Reader(PMDPaPath.toString());
//                ArrayList chViosPMD =  ViolationReader.Reader(PMDChPath.toString());
//                System.out.println("Start to track on PMD violations");
//                matcherRefactoring(paViosPMD ,chViosPMD ,paCommit,chCommit,repoProxy,projectPath,savePath,"PMD",githubURL);
//
//                ArrayList paViosSpotbugs = ViolationReader.Reader(SpPaPath.toString());
//                ArrayList chViosSpotbugs = ViolationReader.Reader(SpChPath.toString());
//                System.out.println("Start to track on Spotbugs violations");
//                matcherRefactoring(paViosSpotbugs,chViosSpotbugs,paCommit,chCommit,repoProxy,projectPath,savePath,"Spotbugs",githubURL);
//            }
//            long endMatching = System.currentTimeMillis();
//            matchingTime = endMatching - startMatching;
//            String[] row = {projectPath,chCommit,Long.toString(staticTime), Long.toString(matchingTime)};
//            Path recordPath = Paths.get(savePath,"record.csv");
//            appendCSV(recordPath.toString(),row);
//
//        }

    }
}
