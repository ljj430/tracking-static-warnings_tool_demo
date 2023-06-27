package edu.concordia.tracking;

import com.google.common.collect.Iterators;
import edu.concordia.git.GitProxy;
import edu.concordia.refactoring.RefactoringFormat;
import edu.concordia.refactoring.RefactoringInfo;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


import static edu.concordia.tracking.HungarianMatrixConstructor.*;
import static edu.concordia.tracking.TrackedViolationWriter.writer;
import static edu.concordia.tracking.TrackingUtils.*;

public class ViolationMatcher {
    public static final int MATCHING_THRESHOLD = 3;
    protected static HashSet<BugInstance> paUnmatchedSet = new HashSet();
    protected static HashSet<BugInstance> chUnmatchedSet = new HashSet();
    protected static HashSet<BugInstance> paMatchedSet = new HashSet();
    protected static HashSet<BugInstance> chMatchedSet = new HashSet();
    protected static HashMap<String, BugInstance> chSourceMap = new HashMap();
    protected static Graph<BugInstanceCommit, DefaultWeightedEdge> hungarianMatrix = createGraph();
    protected static HashSet<BugInstanceCommit> paBugInstances = new HashSet<>();
    protected static HashSet<BugInstanceCommit> chBugInstances = new HashSet<>();
    protected static int flag = 1;
//    public static ArrayList matcher(ArrayList<BugInstance> paVios, ArrayList<BugInstance> chVios, String paCommit, String chCommit, GitProxy gitproxy, String savePath, String staticTool, String githubUrl) throws IOException, GitAPIException {
//        paUnmatchedSet = new HashSet();
//        chUnmatchedSet = new HashSet();
//        paMatchedSet = new HashSet();
//        chMatchedSet = new HashSet();
//        chSourceMap = new HashMap();
//        hungarianMatrix = createGraph();
//        paBugInstances = new HashSet<>();
//        chBugInstances = new HashSet<>();
//
//        paVios.forEach(value -> { paUnmatchedSet.add(value); });
//        chVios.forEach(value -> { chUnmatchedSet.add(value); });
//
//        RevCommit parentCommit = gitproxy.getCommitByHash(paCommit);
//        RevCommit childCommit = gitproxy.getCommitByHash(chCommit);
//        ArrayList diffs = new ArrayList(gitproxy.getChangedFiles(childCommit, parentCommit));
//        ArrayList sourceChanges = TrackingUtils.getChangedSourceFiles(diffs);
//
//        //        return 1 oldPaths
//        //        return 2 newPaths
//        //        return 3 diffMap
//        ArrayList diffMapwithPaCh = TrackingUtils.transformFiles2Package(sourceChanges,parentCommit,childCommit,gitproxy);
//
//        HashSet<String> parentChangedPaths = (HashSet) diffMapwithPaCh.get(0);
//        HashSet<String> childChangedPaths = (HashSet) diffMapwithPaCh.get(1);
//        HashMap<String, DiffEntry> diffMap = (HashMap<String, DiffEntry>) diffMapwithPaCh.get(2);
//        //String = package +class = parentChangedPaths = childChangedPaths
//
//        HashMap<String,String> paSource = getPaAllSource(gitproxy, parentChangedPaths , diffMap, paCommit);
//        HashMap<String,String> chSource = getChAllSource(gitproxy, childChangedPaths , diffMap, chCommit);
//
//
////refactoring        ArrayList<RefactoringFormat> refactoringInfo = new RefactoringInfo().getRefactoringInfo(repoPath,githubPath, commit);
//        for(BugInstance pa: paUnmatchedSet){
//
//            String paPath = pa.getClassPath();
//            //if class files are unchanged
//            if(!parentChangedPaths.contains(paPath)) {
//                //exact matching
//                BugInstance childMatched = exactMatch(pa, chUnmatchedSet);
//                paMatchedSet.add(pa);
//                chMatchedSet.add(childMatched);
//            }
//
//        }
//        paUnmatchedSet.removeAll(paMatchedSet);
//        chUnmatchedSet.removeAll(chMatchedSet);
//
//        //locaion and snippet
//        for(BugInstance pa: paUnmatchedSet){
//            DiffEntry d = diffMap.get(pa.getClassPath());
//
//            ArrayList<Edit> edits = new ArrayList(gitproxy.getEditList(d));
//            HashSet<BugInstance> locationMatched = locationMatch(pa,chUnmatchedSet,edits);
//            for(BugInstance ch:locationMatched){
//                BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
//                BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
//                paBugInstances.add(paCom);
//                chBugInstances.add(chCom);
//                hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
//            }
//
//            HashSet<BugInstance> snippetMatched = new HashSet();
//            HashSet<BugInstance> candidates = new HashSet();
//            for(BugInstance ch:chUnmatchedSet){
//                if(isSameButDiffLoc(pa,ch)){
//                    candidates.add(ch);
//                }
//            }
//            if(candidates.size()>0){
//                if(d.getChangeType() == DiffEntry.ChangeType.DELETE){
//                    continue;
//                }
//                else {
//                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()),paSource.get(d.getNewPath()));
//                    parentSnippet = parentSnippet.replace(" ","");
//                    for(BugInstance ca:candidates){
//                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()),chSource.get(d.getNewPath()));
//                        childSnippet = childSnippet.replace(" ","");
//                        if(childSnippet.equals(parentSnippet)){
//                            snippetMatched.add(ca);
//                        }
//                    }
//                }
//            }
//            for(BugInstance ch:snippetMatched){
//                BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
//                BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
//                paBugInstances.add(paCom);
//                chBugInstances.add(chCom);
//                hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
//            }
//        }
//        HashSet<DefaultWeightedEdge> matchedPairs = getHungarianMatchedEdge(hungarianMatrix,paBugInstances,chBugInstances);
//        for (DefaultWeightedEdge edge : matchedPairs) {
//            BugInstanceCommit paMatched = hungarianMatrix.getEdgeSource(edge);
//            BugInstanceCommit chMatched = hungarianMatrix.getEdgeTarget(edge);
//            paMatchedSet.add(paMatched.getBug());
//            paUnmatchedSet.remove(paMatched.getBug());
//            chMatchedSet.add(chMatched.getBug());
//            chUnmatchedSet.remove(chMatched.getBug());
//        }
//        System.out.println("The number of resolved warnings: "+paUnmatchedSet.size());
//        System.out.println("The number of newly-introduced warnings: "+chUnmatchedSet.size());
//        writer( paUnmatchedSet,  chUnmatchedSet, savePath, chCommit, staticTool);
//        return new ArrayList();
//    }


    public static ArrayList matcherRefactoring(ArrayList<BugInstance> paVios, ArrayList<BugInstance> chVios, String paCommit, String chCommit, GitProxy gitproxy, String repoPath, String savePath, String staticTool, String githubUrl) throws IOException, GitAPIException {
        paUnmatchedSet = new HashSet();
        chUnmatchedSet = new HashSet();
        paMatchedSet = new HashSet();
        chMatchedSet = new HashSet();
        chSourceMap = new HashMap();
        hungarianMatrix = createGraph();
        paBugInstances = new HashSet<>();
        chBugInstances = new HashSet<>();

        paVios.forEach(value -> {
            paUnmatchedSet.add(value);
        });
        chVios.forEach(value -> {
            chUnmatchedSet.add(value);
        });

        RevCommit parentCommit = gitproxy.getCommitByHash(paCommit);
        RevCommit childCommit = gitproxy.getCommitByHash(chCommit);
        ArrayList diffs = new ArrayList(gitproxy.getChangedFiles(childCommit, parentCommit));
        ArrayList sourceChanges = TrackingUtils.getChangedSourceFiles(diffs);

        //        return 1 oldPaths
        //        return 2 newPaths
        //        return 3 diffMap
        ArrayList diffMapwithPaCh = TrackingUtils.transformFiles2Package(sourceChanges, parentCommit, childCommit, gitproxy);

        HashSet<String> parentChangedPaths = (HashSet) diffMapwithPaCh.get(0);
        HashSet<String> childChangedPaths = (HashSet) diffMapwithPaCh.get(1);
        HashMap<String, DiffEntry> diffMap = (HashMap<String, DiffEntry>) diffMapwithPaCh.get(2);
        //String = package +class = parentChangedPaths = childChangedPaths

        HashMap<String, String> paSource = getPaAllSource(gitproxy, parentChangedPaths, diffMap, paCommit);


        HashMap<String, String> chSource = getChAllSource(gitproxy, childChangedPaths, diffMap, chCommit);


        System.out.println(repoPath);
        System.out.println(paCommit);
        checkoutCommit(repoPath, paCommit);
        ArrayList paJavaFiles = (ArrayList) findAllJavaFiles(repoPath);

        checkoutCommit(repoPath, chCommit);
        ArrayList chJavaFiles = (ArrayList) findAllJavaFiles(repoPath);


        ArrayList<RefactoringFormat> refactoringInfo = (ArrayList<RefactoringFormat>) new RefactoringInfo().getRefactoringInfo(repoPath, githubUrl, chCommit);


        for (BugInstance pa : paUnmatchedSet) {
//            int trackFlag = 0;
//            if(pa.getClassName().equals("Splitter") && pa.getStartLine().equals("85") && pa.getEndLine().equals("85")){
//                trackFlag = 1;
//                System.out.println("Change the flag");
//            }
//            else{
//                trackFlag = 0;
//            }


            String paPath = pa.getClassPath();
//            if(trackFlag == 1) {
//                System.out.println(pa.getClassPath());
//                System.out.println(pa.getClassName());
//                System.out.println(pa);
//            }
            //if class files are unchanged
            if (!parentChangedPaths.contains(paPath)) {

                //exact matching
                BugInstance childMatched = exactMatch(pa, chUnmatchedSet);
                if (childMatched != null) {
                    paMatchedSet.add(pa);
                    chMatchedSet.add(childMatched);
                }

//                if(trackFlag == 1){
//                    System.out.println(childMatched);
//                }
            }

        }
        paUnmatchedSet.removeAll(paMatchedSet);
        chUnmatchedSet.removeAll(chMatchedSet);

        //locaion and snippet

        for (BugInstance pa : paUnmatchedSet) {
            int trackFlag = 0;
            if (pa.getSourcePath().equals("com/google/common/io/FileBackedOutputStreamTest.java") && pa.getStartLine().equals("85") && pa.getEndLine().equals("85") && pa.getViolation().equals("CloseResource")) {
                trackFlag = 1;
                System.out.println("Start to track");
            } else {
                trackFlag = 0;
            }


            DiffEntry d = diffMap.get(pa.getClassPath());

            BugInstance paRefactoring = (BugInstance) pa.clone();
            ArrayList refactoringInfoRutrun = getPaRefactoring(paRefactoring, refactoringInfo);
            paRefactoring = (BugInstance) refactoringInfoRutrun.get(0);
            String sourcePath = (String) refactoringInfoRutrun.get(1);
            String refactoringType = (String) refactoringInfoRutrun.get(2);
            ArrayList<Edit> edits = new ArrayList();
            try {
                edits = new ArrayList(gitproxy.getEditList(d));
            } catch (NullPointerException npe) {
                edits = new ArrayList();
            }

            HashSet<BugInstance> locationMatched = new HashSet<>();
            if (paRefactoring.isRefactoring() == true) {
                if (edits.size() == 0 || d.getChangeType() != DiffEntry.ChangeType.RENAME) {
                    if (trackFlag == 1) {
                        System.out.println(paRefactoring);
                    }
                    locationMatched = locationMatchRefactoring(paRefactoring, chUnmatchedSet, edits);
                    if (locationMatched.size() > 0) {
                        for (BugInstance ch : locationMatched) {
                            BugInstanceCommit paCom = new BugInstanceCommit(pa, paCommit);
                            BugInstanceCommit chCom = new BugInstanceCommit(ch, chCommit);
                            paBugInstances.add(paCom);
                            chBugInstances.add(chCom);
                            hungarianMatrix = addWeightedEdge(hungarianMatrix, paCom, chCom);
                        }
                    }

                    else{
                        // no location matched
                        if (d.getChangeType() != DiffEntry.ChangeType.ADD && d.getChangeType() != DiffEntry.ChangeType.DELETE) {
                            locationMatched = locationMatch(pa, chUnmatchedSet, edits);
                        }

                        if (locationMatched.size() > 0) {
                            for (BugInstance ch : locationMatched) {
                                BugInstanceCommit paCom = new BugInstanceCommit(pa, paCommit);
                                BugInstanceCommit chCom = new BugInstanceCommit(ch, chCommit);
                                paBugInstances.add(paCom);
                                chBugInstances.add(chCom);
                                hungarianMatrix = addWeightedEdge(hungarianMatrix, paCom, chCom);
                            }
                        }
                    }

                }
            } else {
                /// no refactoring
                if (d.getChangeType() != DiffEntry.ChangeType.ADD && d.getChangeType() != DiffEntry.ChangeType.DELETE) {
                    locationMatched = locationMatch(paRefactoring, chUnmatchedSet, edits);
                }
                if (locationMatched.size() > 0) {
                    for (BugInstance ch : locationMatched) {
                        BugInstanceCommit paCom = new BugInstanceCommit(pa, paCommit);
                        BugInstanceCommit chCom = new BugInstanceCommit(ch, chCommit);
                        paBugInstances.add(paCom);
                        chBugInstances.add(chCom);
                        hungarianMatrix = addWeightedEdge(hungarianMatrix, paCom, chCom);
                    }
                }
            }



            // snippet matching

            if (edits.size() == 0) {
                continue;
            }
            HashSet<BugInstance> snippetMatched = new HashSet();
            HashSet<BugInstance> candidates = new HashSet();
            for (BugInstance ch : chUnmatchedSet) {
                if (isSameButDiffLoc(paRefactoring, ch)) {
                    candidates.add(ch);
                }
            }

            if (candidates.size() > 0) {

                ///1 parefactoring vs ch
                if (d.getChangeType() == DiffEntry.ChangeType.DELETE || d.getChangeType() == DiffEntry.ChangeType.RENAME) {
                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()), paSource.get(d.getOldPath()));
                    parentSnippet = parentSnippet.replace(" ", "");

                    for (BugInstance ca : candidates) {
                        DiffEntry dCa = diffMap.get(ca.getClassPath());
                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()), chSource.get(dCa.getNewPath()));
                        childSnippet = childSnippet.replace(" ", "");
//                        System.out.println("ca:\n"+childSnippet);
                        if (childSnippet.equals(parentSnippet)) {
                            snippetMatched.add(ca);
                        }
                    }
                }
                else {

                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()), paSource.get(d.getNewPath()));
                    parentSnippet = parentSnippet.replace(" ", "");

                    for (BugInstance ca : candidates) {
                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()), chSource.get(d.getNewPath()));
                        childSnippet = childSnippet.replace(" ", "");
                        if (childSnippet.equals(parentSnippet)) {
                            snippetMatched.add(ca);
                        }
                    }
                }

                //handle multiple files

                if (snippetMatched.size() == 0) {
                    // check if it has multiple files
                    ArrayList paFileList = new ArrayList();
                    ArrayList chFileList = new ArrayList();
                    for (Iterator iter = paJavaFiles.iterator(); iter.hasNext(); ) {
                        String paJavaFile = iter.next().toString();
                        if (paJavaFile.contains(d.getOldPath())) {

                            paFileList.add(paJavaFile);
                        }
                    }

                    for (Iterator iter = chJavaFiles.iterator(); iter.hasNext(); ) {
                        String chJavaFile = iter.next().toString();
                        if (chJavaFile.contains(d.getNewPath())) {
                            chFileList.add(chJavaFile);
                        }
                    }
                    if (paFileList.size() > 1) {
                        ArrayList paSnippetList = new ArrayList<>();
                        checkoutCommit(repoPath, paCommit);
                        for (Iterator iter = paFileList.iterator(); iter.hasNext(); ) {
                            String paFile = iter.next().toString();
                            String paSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()), getFullSource(paFile)).replaceAll("\\s", "");
                            String paFull = getFullSource(paFile).replaceAll("\\s", "");
                            if (trackFlag == 1) {
                                System.out.println("paFile:" + paFile);
                                System.out.println(paSnippet);
                            }
                            if (!paSnippet.equals("")) {
                                paSnippetList.add(paSnippet);
                            }
                        }

                        checkoutCommit(repoPath, chCommit);
                        for (Iterator iterCa = candidates.iterator(); iterCa.hasNext(); ) {
                            BugInstance ca = (BugInstance) iterCa.next();
                            ArrayList chSnippetList = new ArrayList<>();
                            for (Iterator iter = chFileList.iterator(); iter.hasNext(); ) {
                                String chSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()), getFullSource(iter.next().toString())).replaceAll("\\s", "");
                                if (!chSnippet.equals("")) {
                                    chSnippetList.add(chSnippet);
                                }
                            }
                            Boolean snippetMatchedFlag = false;

                            for (Iterator iterPa = paSnippetList.iterator(); iterPa.hasNext(); ) {
                                String paSnippet = iterPa.next().toString();
                                for (Iterator iterCh = paSnippetList.iterator(); iterCh.hasNext(); ) {
                                    String chSnippet = iterCh.next().toString();
                                    if (paSnippet.equals(chSnippet)) {
                                        snippetMatchedFlag = true;
                                        break;
                                    }
                                }
                                if (snippetMatchedFlag == true) {
                                    snippetMatched.add(ca);
                                }
                            }
                        }
                    }
                }

            }



            //process matched pairs
            if (trackFlag == 1){
                System.out.println("snippet matches:"+snippetMatched.size());
            }
            for (BugInstance ch : snippetMatched) {
                BugInstanceCommit paCom = new BugInstanceCommit(pa, paCommit);
                BugInstanceCommit chCom = new BugInstanceCommit(ch, chCommit);
                paBugInstances.add(paCom);
                chBugInstances.add(chCom);
                hungarianMatrix = addWeightedEdge(hungarianMatrix, paCom, chCom);
            }



        }
        HashSet<DefaultWeightedEdge> matchedPairs = getHungarianMatchedEdge(hungarianMatrix, paBugInstances, chBugInstances);
        for (DefaultWeightedEdge edge : matchedPairs) {
            BugInstanceCommit paMatched = hungarianMatrix.getEdgeSource(edge);
            BugInstanceCommit chMatched = hungarianMatrix.getEdgeTarget(edge);
            paMatchedSet.add(paMatched.getBug());
            paUnmatchedSet.remove(paMatched.getBug());
            chMatchedSet.add(chMatched.getBug());
            chUnmatchedSet.remove(chMatched.getBug());
        }
        System.out.println("# resolved warnings:" + paUnmatchedSet.size());
        System.out.println("# newly-introduced warnings:" + chUnmatchedSet.size());
        writer(paUnmatchedSet, chUnmatchedSet, savePath, chCommit, staticTool);
        return new ArrayList();
    }

    public static BugInstance exactMatch(BugInstance pa, HashSet<BugInstance> childSet) {
        BugInstance output = new BugInstance();
        if (childSet.contains(pa)) {
            output = pa;
            return output;
        } else
            return null;

    }


    public static HashSet<BugInstance> locationMatchRefactoring(BugInstance pa, HashSet<BugInstance> childSet, ArrayList<Edit> edits) {
        int trackFlag = 0;

        if(pa.getSourcePath().equals("com/google/common/io/FileBackedOutputStreamAndroidIncompatibleTest.java") && pa.getStartLine().equals("35") && pa.getEndLine().equals("35") && pa.getViolation().equals("CloseResource")) {
            trackFlag = 1;
            System.out.println("Start to track in locationMatchRefactoring");
        }
        else{
            trackFlag = 0;
        }

        HashSet<BugInstance> matchedCandidate = new HashSet<>();

        for(BugInstance ch:childSet){

            if(isSameButDiffLoc(pa,ch) && Math.abs(Integer.parseInt(ch.getStartLine()) - Integer.parseInt(pa.getStartLine()))  <= MATCHING_THRESHOLD){
                matchedCandidate.add(ch);
            }
        }
        return matchedCandidate;
    }


    public static HashSet<BugInstance> locationMatch(BugInstance pa, HashSet<BugInstance> childSet, ArrayList<Edit> edits){
        int trackFlag = 0;

        if(pa.getSourcePath().equals("com/google/common/io/FileBackedOutputStreamAndroidIncompatibleTest.java") && pa.getStartLine().equals("35") && pa.getEndLine().equals("35") && pa.getViolation().equals("CloseResource")) {
            trackFlag = 1;
            System.out.println("Start to track in locationMatch");
        }
        else{
            trackFlag = 0;
        }


        HashSet<BugInstance> candidate1 = new HashSet<>();
        HashSet<BugInstance> candidate2 = new HashSet<>();
        HashSet<BugInstance> matchedCandidate = new HashSet<>();
        for(BugInstance ch:childSet){
            if(isSameButDiffLoc(pa,ch)){
                candidate1.add(ch);
            }
        }

        if (trackFlag == 1){
            System.out.println("location cadidates:"+candidate1.size());
        }
        //pa in edits
        if(hasEditedParent(Integer.parseInt(pa.getStartLine()),Integer.parseInt(pa.getEndLine()),edits)){
            ArrayList<Edit> matchingEdits = getOverlappingEditsParent(Integer.parseInt(pa.getStartLine()),Integer.parseInt(pa.getEndLine()),edits);
            for(BugInstance ca:candidate1){
                if(hasEditedChild(Integer.parseInt(ca.getStartLine()),Integer.parseInt(ca.getEndLine()),matchingEdits)){
                    candidate2.add(ca);
                }
            }

            for(BugInstance ca:candidate2){
                ArrayList<Edit> childEdits = getOverlappingEditsChild(Integer.parseInt(ca.getStartLine()),Integer.parseInt(ca.getEndLine()),matchingEdits);
                for(Edit e : childEdits){
                    if(Math.abs(Math.abs(Integer.parseInt(pa.getStartLine()) - e.getBeginA()) - Math.abs(Integer.parseInt(ca.getStartLine()) - e.getBeginB())) <= MATCHING_THRESHOLD){
                        matchedCandidate.add(ca);
                    }
                }
            }
        }
        //pa not in edits
        else{
            if (edits.size() == 0){
                for(BugInstance ca: candidate1){
                    if(Math.abs(Integer.parseInt(ca.getStartLine()) - Integer.parseInt(pa.getStartLine())) <= MATCHING_THRESHOLD){
                        matchedCandidate.add(ca);
                    }
                }
            }
            else{
                Edit editBegin = getMinimumEdit(edits);
                if(Integer.parseInt(pa.getEndLine()) < editBegin.getBeginA()){  //no diff before pa
                    for(BugInstance ca: candidate1){
                        if(Math.abs(Integer.parseInt(ca.getStartLine()) - Integer.parseInt(pa.getStartLine())) <= MATCHING_THRESHOLD){
                            matchedCandidate.add(ca);
                        }
                    }
                }
                else{
                    Edit lastEdit = getLastEdit(Integer.parseInt(pa.getStartLine()),edits);
                    for(BugInstance ca : candidate1){
                        if(Math.abs(Math.abs(Integer.parseInt(pa.getStartLine()) - lastEdit.getEndA()) - Math.abs(Integer.parseInt(ca.getStartLine()) - lastEdit.getEndB())) <= MATCHING_THRESHOLD){
                            matchedCandidate.add(ca);
                        }
                    }
                }
            }

        }

        return matchedCandidate;
    }

    public static void recordMatchedPair(BugInstance bugIns, HashSet<BugInstance> matchedSet,HashSet<BugInstance> unmatchedSet){
        matchedSet.add(bugIns);
        unmatchedSet.remove(bugIns);
    }

    public static void main(String[] args) throws IOException, GitAPIException {
//        String paPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/guava/45/PMD_33abc128648cf91c1f81b6964cb4268b399119f6.csv";
//        String chPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/guava/45/PMD_55c80ee0566e1145d0c3ed11a1cc78a5cdb4eb66.csv";
//        String paCommit = "33abc128648cf91c1f81b6964cb4268b399119f6";
//        String chCommit = "55c80ee0566e1145d0c3ed11a1cc78a5cdb4eb66";
//        String gitProxyPath = "/home/junjie/Desktop/tool_demo_StaticTracker/objects/fork/guava/.git";
////        gitProxyPath = "D:\\Git\\toyProject\\.git";
//        String savePath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/tmp/2";
//        String repoPath = "/home/junjie/Desktop/tool_demo_StaticTracker/objects/fork/guava";
//        String repoURL = "https://github.com/ljj430/guava";
//        String staticTool = "PMD";


        String paPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/jedis/1/PMD_8065385d1a079d424c23ae54a1ce6dbe4d0dfccc.csv";
        String chPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/jedis/1/PMD_0d405975ad042719b9e060033284bb354b472c91.csv";
        String paCommit = "8065385d1a079d424c23ae54a1ce6dbe4d0dfccc";
        String chCommit = "0d405975ad042719b9e060033284bb354b472c91";
        String gitProxyPath = "/home/junjie/Desktop/tool_demo_StaticTracker/objects/fork/jedis/.git";
//        gitProxyPath = "D:\\Git\\toyProject\\.git";
        String savePath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/tmp/2";
        String repoPath = "/home/junjie/Desktop/tool_demo_StaticTracker/objects/fork/jedis";
        String repoURL = "https://github.com/ljj430/jedis";
        String staticTool = "PMD";

        ArrayList paVios = ViolationReader.Reader(paPath);
        ArrayList chVios = ViolationReader.Reader(chPath);
        GitProxy repoProxy = new GitProxy();
        Boolean initGit = initGitRepo(gitProxyPath);
        repoProxy.setURI(gitProxyPath);
        if(initGit){
            matcherRefactoring(paVios,chVios,paCommit,chCommit,repoProxy,repoPath, savePath,staticTool,repoURL);
            Iterable<RevCommit> commits = repoProxy.getLogAll();
            int count = 0 ;
            for (RevCommit commit : commits) {
                if(count == 0){
                    if(commit.getParentCount()>1){
                        System.out.println("This commit has multiple parents");
                        break;
                    }
                    chCommit = commit.name();
                }
                else if(count == 1){
                    paCommit=commit.name();
                }
                count++;
            }

//            System.out.println(chCommit);
//            System.out.println(paCommit);
        }

    }

    public static Boolean initGitRepo(String repoPath) throws IOException {
        GitProxy proxy = new GitProxy();
        proxy.setURI(repoPath);
        if(!proxy.connect()){
            System.out.println("connect error");
            return false;
        }
        return true;
    }
}
