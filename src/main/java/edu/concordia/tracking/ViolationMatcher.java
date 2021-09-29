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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import static edu.concordia.tracking.HungarianMatrixConstructor.*;
import static edu.concordia.tracking.TrackedViolationWriter.writer;
import static edu.concordia.tracking.TrackingUtils.*;

public class ViolationMatcher {
    public static final int MATCHING_THRESHOLD = 3;
    protected static  HashSet<BugInstance> paUnmatchedSet = new HashSet();
    protected static  HashSet<BugInstance> chUnmatchedSet = new HashSet();
    protected static  HashSet<BugInstance> paMatchedSet = new HashSet();
    protected static  HashSet<BugInstance> chMatchedSet = new HashSet();
    protected static  HashMap<String, BugInstance> chSourceMap = new HashMap();
    protected static Graph<BugInstanceCommit, DefaultWeightedEdge> hungarianMatrix = createGraph();
    protected static HashSet<BugInstanceCommit> paBugInstances = new HashSet<>();
    protected static HashSet<BugInstanceCommit> chBugInstances = new HashSet<>();
    protected static int flag =1;
    public static ArrayList matcher(ArrayList<BugInstance> paVios, ArrayList<BugInstance> chVios, String paCommit, String chCommit, GitProxy gitproxy, String savePath, String staticTool, String githubUrl) throws IOException, GitAPIException {
        paUnmatchedSet = new HashSet();
        chUnmatchedSet = new HashSet();
        paMatchedSet = new HashSet();
        chMatchedSet = new HashSet();
        chSourceMap = new HashMap();
        hungarianMatrix = createGraph();
        paBugInstances = new HashSet<>();
        chBugInstances = new HashSet<>();

        paVios.forEach(value -> { paUnmatchedSet.add(value); });
        chVios.forEach(value -> { chUnmatchedSet.add(value); });

        RevCommit parentCommit = gitproxy.getCommitByHash(paCommit);
        RevCommit childCommit = gitproxy.getCommitByHash(chCommit);
        ArrayList diffs = new ArrayList(gitproxy.getChangedFiles(childCommit, parentCommit));
        ArrayList sourceChanges = TrackingUtils.getChangedSourceFiles(diffs);

        //        return 1 oldPaths
        //        return 2 newPaths
        //        return 3 diffMap
        ArrayList diffMapwithPaCh = TrackingUtils.transformFiles2Package(sourceChanges,parentCommit,childCommit,gitproxy);

        HashSet<String> parentChangedPaths = (HashSet) diffMapwithPaCh.get(0);
        HashSet<String> childChangedPaths = (HashSet) diffMapwithPaCh.get(1);
        HashMap<String, DiffEntry> diffMap = (HashMap<String, DiffEntry>) diffMapwithPaCh.get(2);
        //String = package +class = parentChangedPaths = childChangedPaths

        HashMap<String,String> paSource = getPaAllSource(gitproxy, parentChangedPaths , diffMap, paCommit);
        HashMap<String,String> chSource = getChAllSource(gitproxy, childChangedPaths , diffMap, chCommit);


//refactoring        ArrayList<RefactoringFormat> refactoringInfo = new RefactoringInfo().getRefactoringInfo(repoPath,githubPath, commit);
        for(BugInstance pa: paUnmatchedSet){

            String paPath = pa.getClassPath();
            //if class files are unchanged
            if(!parentChangedPaths.contains(paPath)) {
                //exact matching
                BugInstance childMatched = exactMatch(pa, chUnmatchedSet);
                paMatchedSet.add(pa);
                chMatchedSet.add(childMatched);
            }

        }
        paUnmatchedSet.removeAll(paMatchedSet);
        chUnmatchedSet.removeAll(chMatchedSet);

        //locaion and snippet
        for(BugInstance pa: paUnmatchedSet){
            DiffEntry d = diffMap.get(pa.getClassPath());

            ArrayList<Edit> edits = new ArrayList(gitproxy.getEditList(d));
            HashSet<BugInstance> locationMatched = locationMatch(pa,chUnmatchedSet,edits);
            for(BugInstance ch:locationMatched){
                BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
                BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
                paBugInstances.add(paCom);
                chBugInstances.add(chCom);
                hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
            }

            HashSet<BugInstance> snippetMatched = new HashSet();
            HashSet<BugInstance> candidates = new HashSet();
            for(BugInstance ch:chUnmatchedSet){
                if(isSameButDiffLoc(pa,ch)){
                    candidates.add(ch);
                }
            }
            if(candidates.size()>0){
                if(d.getChangeType() == DiffEntry.ChangeType.DELETE){
                    continue;
                }
                else {
                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()),paSource.get(d.getNewPath()));
                    parentSnippet = parentSnippet.replace(" ","");
                    for(BugInstance ca:candidates){
                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()),chSource.get(d.getNewPath()));
                        childSnippet = childSnippet.replace(" ","");
                        if(childSnippet.equals(parentSnippet)){
                            snippetMatched.add(ca);
                        }
                    }
                }
            }
            for(BugInstance ch:snippetMatched){
                BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
                BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
                paBugInstances.add(paCom);
                chBugInstances.add(chCom);
                hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
            }
        }
        HashSet<DefaultWeightedEdge> matchedPairs = getHungarianMatchedEdge(hungarianMatrix,paBugInstances,chBugInstances);
        for (DefaultWeightedEdge edge : matchedPairs) {
            BugInstanceCommit paMatched = hungarianMatrix.getEdgeSource(edge);
            BugInstanceCommit chMatched = hungarianMatrix.getEdgeTarget(edge);
            paMatchedSet.add(paMatched.getBug());
            paUnmatchedSet.remove(paMatched.getBug());
            chMatchedSet.add(chMatched.getBug());
            chUnmatchedSet.remove(chMatched.getBug());
        }
        System.out.println("The number of resolved warnings: "+paUnmatchedSet.size());
        System.out.println("The number of newly-introduced warnings: "+chUnmatchedSet.size());
        writer( paUnmatchedSet,  chUnmatchedSet, savePath, chCommit, staticTool);
        return new ArrayList();
    }


    public static ArrayList matcherRefactoring (ArrayList<BugInstance> paVios, ArrayList<BugInstance> chVios, String paCommit, String chCommit, GitProxy gitproxy, String repoPath, String savePath, String staticTool, String githubUrl) throws IOException, GitAPIException {
        paUnmatchedSet = new HashSet();
        chUnmatchedSet = new HashSet();
        paMatchedSet = new HashSet();
        chMatchedSet = new HashSet();
        chSourceMap = new HashMap();
        hungarianMatrix = createGraph();
        paBugInstances = new HashSet<>();
        chBugInstances = new HashSet<>();

        paVios.forEach(value -> { paUnmatchedSet.add(value); });
        chVios.forEach(value -> { chUnmatchedSet.add(value); });

        RevCommit parentCommit = gitproxy.getCommitByHash(paCommit);
        RevCommit childCommit = gitproxy.getCommitByHash(chCommit);
        ArrayList diffs = new ArrayList(gitproxy.getChangedFiles(childCommit, parentCommit));
        ArrayList sourceChanges = TrackingUtils.getChangedSourceFiles(diffs);

        //        return 1 oldPaths
        //        return 2 newPaths
        //        return 3 diffMap
        ArrayList diffMapwithPaCh = TrackingUtils.transformFiles2Package(sourceChanges,parentCommit,childCommit,gitproxy);

        HashSet<String> parentChangedPaths = (HashSet) diffMapwithPaCh.get(0);
        HashSet<String> childChangedPaths = (HashSet) diffMapwithPaCh.get(1);
        HashMap<String, DiffEntry> diffMap = (HashMap<String, DiffEntry>) diffMapwithPaCh.get(2);
        //String = package +class = parentChangedPaths = childChangedPaths

        HashMap<String,String> paSource = getPaAllSource(gitproxy, parentChangedPaths , diffMap, paCommit);
        HashMap<String,String> chSource = getChAllSource(gitproxy, childChangedPaths , diffMap, chCommit);


        ArrayList<RefactoringFormat> refactoringInfo = (ArrayList<RefactoringFormat>) new RefactoringInfo().getRefactoringInfo(repoPath,githubUrl, chCommit);

        int trackFlag = 0;
        System.out.println(parentChangedPaths);
        for(BugInstance pa: paUnmatchedSet){
            if(pa.getClassName().equals("ApplyFunctionExpr")){
                trackFlag = 1;
                System.out.println("Change the flag");
            }
            else{
                trackFlag = 0;
            }


            String paPath = pa.getClassPath();
            if(trackFlag == 1) {
                System.out.println(pa.getClassPath());
                System.out.println(pa.getClassName());
            }
            //if class files are unchanged
            if(!parentChangedPaths.contains(paPath)) {

                //exact matching
                BugInstance childMatched = exactMatch(pa, chUnmatchedSet);
                if(childMatched != null){
                    paMatchedSet.add(pa);
                    chMatchedSet.add(childMatched);
                }

//                if(trackFlag == 1){
//                    System.out.println(pa);
//                    System.out.println(childMatched);
//                }
            }

        }
        paUnmatchedSet.removeAll(paMatchedSet);
        chUnmatchedSet.removeAll(chMatchedSet);

        //locaion and snippet

        for(BugInstance pa: paUnmatchedSet){
            DiffEntry d = diffMap.get(pa.getClassPath());
            BugInstance paRefactoring = (BugInstance) pa.clone();
            ArrayList refactoringInfoRutrun = getPaRefactoring(paRefactoring,refactoringInfo);
            paRefactoring = (BugInstance) refactoringInfoRutrun.get(0);
            String sourcePath = (String) refactoringInfoRutrun.get(1);
            String refactoringType = (String) refactoringInfoRutrun.get(2);


            ArrayList<Edit> edits = new ArrayList(gitproxy.getEditList(d));

            if(d.getChangeType() != DiffEntry.ChangeType.RENAME){
                HashSet<BugInstance> locationMatched = locationMatch(paRefactoring,chUnmatchedSet,edits);
                for(BugInstance ch:locationMatched){
                    BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
                    BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
                    paBugInstances.add(paCom);
                    chBugInstances.add(chCom);
                    hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
                }
                if(trackFlag ==1 ){
                    System.out.println("location matched:"+ locationMatched.size());
                }
            }


            HashSet<BugInstance> snippetMatched = new HashSet();
            HashSet<BugInstance> candidates = new HashSet();
            for(BugInstance ch:chUnmatchedSet){
                if(isSameButDiffLoc(paRefactoring,ch)){
                    candidates.add(ch);
                }
            }
            if(candidates.size()>0){
                if(d.getChangeType() == DiffEntry.ChangeType.DELETE ||  d.getChangeType() == DiffEntry.ChangeType.RENAME){
                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()),paSource.get(d.getOldPath()));
                    parentSnippet = parentSnippet.replace(" ","");
                    for(BugInstance ca:candidates){
                        DiffEntry dCa = diffMap.get(ca.getClassPath());
                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()),chSource.get(dCa.getNewPath()));
                        childSnippet = childSnippet.replace(" ","");
//                        System.out.println("ca:\n"+childSnippet);
                        if(childSnippet.equals(parentSnippet)){
                            snippetMatched.add(ca);
                        }
                    }
                    if(trackFlag ==1 ){
                        System.out.println("sni matched:"+ snippetMatched.size());
                    }
                }
//                else if(d.getChangeType() == DiffEntry.ChangeType.RENAME){
//                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()),paSource.get(d.getOldPath()));
//                    parentSnippet = parentSnippet.replace(" ","");
//                    for(BugInstance ca:candidates){
//                        DiffEntry dCa = diffMap.get(ca.getClassPath());
//                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()),chSource.get(dCa.getNewPath()));
//                        childSnippet = childSnippet.replace(" ","");
//                        if(childSnippet.equals(parentSnippet)){
//                            snippetMatched.add(ca);
//                        }
//                    }
//                }
                else {

                    String parentSnippet = getLineRange(Integer.parseInt(pa.getStartLine()), Integer.parseInt(pa.getEndLine()),paSource.get(d.getNewPath()));
                    parentSnippet = parentSnippet.replace(" ","");
                    for(BugInstance ca:candidates){
                        String childSnippet = getLineRange(Integer.parseInt(ca.getStartLine()), Integer.parseInt(ca.getEndLine()),chSource.get(d.getNewPath()));
                        childSnippet = childSnippet.replace(" ","");
                        if(childSnippet.equals(parentSnippet)){
                            snippetMatched.add(ca);
                        }
                    }
                }
            }
            for(BugInstance ch:snippetMatched){
                BugInstanceCommit paCom = new BugInstanceCommit(pa , paCommit);
                BugInstanceCommit chCom = new BugInstanceCommit(ch , chCommit);
                paBugInstances.add(paCom);
                chBugInstances.add(chCom);
                hungarianMatrix = addWeightedEdge(hungarianMatrix,paCom,chCom);
            }
        }
        HashSet<DefaultWeightedEdge> matchedPairs = getHungarianMatchedEdge(hungarianMatrix,paBugInstances,chBugInstances);
        for (DefaultWeightedEdge edge : matchedPairs) {
            BugInstanceCommit paMatched = hungarianMatrix.getEdgeSource(edge);
            BugInstanceCommit chMatched = hungarianMatrix.getEdgeTarget(edge);
            paMatchedSet.add(paMatched.getBug());
            paUnmatchedSet.remove(paMatched.getBug());
            chMatchedSet.add(chMatched.getBug());
            chUnmatchedSet.remove(chMatched.getBug());
        }
        System.out.println("# resolved warnings:"+paUnmatchedSet.size());
        System.out.println("# newly-introduced warnings:"+chUnmatchedSet.size());
        writer( paUnmatchedSet,  chUnmatchedSet, savePath, chCommit, staticTool);
        return new ArrayList();
    }

    public static BugInstance exactMatch(BugInstance pa, HashSet<BugInstance> childSet){
        BugInstance output = new BugInstance();
        if(childSet.contains(pa)){
            output = pa;
            return output;
        }
        else
            return null;

    }

    public static HashSet<BugInstance> locationMatch(BugInstance pa, HashSet<BugInstance> childSet, ArrayList<Edit> edits){
        HashSet<BugInstance> candidate1 = new HashSet<>();
        HashSet<BugInstance> candidate2 = new HashSet<>();
        HashSet<BugInstance> matchedCandidate = new HashSet<>();
        for(BugInstance ch:childSet){
            if(isSameButDiffLoc(pa,ch)){
                candidate1.add(ch);
            }
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

        return matchedCandidate;
    }

    public static void recordMatchedPair(BugInstance bugIns, HashSet<BugInstance> matchedSet,HashSet<BugInstance> unmatchedSet){
        matchedSet.add(bugIns);
        unmatchedSet.remove(bugIns);
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        String paPath = "D:\\Git\\tmp\\2a56db0.csv";
        String chPath = "D:\\Git\\tmp\\09936b5.csv";
        String paCommit = "2a56db09571164d94ddab1ec6f4a8a766e615acc";
        String chCommit = "09936b57fc90b5a3c7fe530358a2c6a757c32839";
        String gitProxyPath = "D:\\ThesisProject\\trackingProjects\\jclouds\\.git";
//        gitProxyPath = "D:\\Git\\toyProject\\.git";
        String savePath = "D:\\Git\\tmp";
        String repoPath = "D:\\ThesisProject\\trackingProjects\\jclouds";
        String repoURL = "https://github.com/jclouds/jclouds";
        String staticTool = "Spotbugs";

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

            System.out.println(chCommit);
            System.out.println(paCommit);
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
