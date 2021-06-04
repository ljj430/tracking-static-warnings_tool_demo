package edu.concordia.refactoring;

import gr.uom.java.xmi.diff.CodeRange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.*;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class RefactoringInfo {
    public static List<RefactoringFormat> getRefactoringInfo(String repoPath,String githubPath, String commit){
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        List<RefactoringFormat> refactoringInfo = new ArrayList<>();
        //args[0] = repoPath

        //args[1] = github path
        //args[2] = star commit
        //args[3] = end commit

        // return a
        try {
            Repository repo = gitService.cloneIfNotExists(
                    repoPath, githubPath);


            // start commit: 2a56db09571164d94ddab1ec6f4a8a766e615acc
            // end commit: 09936b57fc90b5a3c7fe530358a2c6a757c32839
//            miner.detectBetweenCommits(repo,startCommit,endCommit,
            miner.detectAtCommit(repo,commit,
                    new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
//                            System.out.println("Refactorings at " + commitId);
                            for (Refactoring ref : refactorings) {
//                                System.out.println("Refactorings at " + commitId);
//                                System.out.println(ref.getInvolvedClassesAfterRefactoring());



                                RefactoringFormat oneRefactoring = new RefactoringFormat(ref.getRefactoringType(),
                                        ref.leftSide(),ref.rightSide(), commitId,
                                        ref.getInvolvedClassesBeforeRefactoring(), ref.getInvolvedClassesAfterRefactoring());

//                                System.out.println(oneRefactoring.getRefactoringType());
//                                System.out.println("before\n " + oneRefactoring.getClassBefore());
//                                System.out.println("left\n " + oneRefactoring.getRefactoringLeft());
//                                System.out.println("after\n " + oneRefactoring.getClassAfter());
//                                System.out.println("right\n " + oneRefactoring.getRefactoringRight());
                                refactoringInfo.add(oneRefactoring);
                            }
                        }
                    });

        }
        catch(Exception ex){
            //todosomething
            System.out.println("error");
        }

        return refactoringInfo;
    }

    public static void main(String[] args){
        List<RefactoringFormat> forTest = new ArrayList<>();
//        String repoPath = "/Users/lijunjie/Desktop/Master/testProject/jclouds";
        String repoPath = "D:\\ThesisProject\\trackingProjects\\jclouds";
//        String githubPath = "https://github.com/jclouds/jclouds";
        String githubPath = "https://github.com/jclouds/jclouds";
//        String commit = "250722e";//1
//        String commit = "b88516f";//2
        String commit = "09936b57fc90b5a3c7fe530358a2c6a757c32839";//3
        forTest = getRefactoringInfo(repoPath,githubPath,commit);
        for(RefactoringFormat eachRefactoring: forTest){
//            if("MOVE_AND_INLINE_OPERATION" == eachRefactoring.getRefactoringType().toString()) {
            System.out.println("path:\n"+eachRefactoring.getRefactoringLeft().get(0).getFilePath());

            System.out.println(eachRefactoring.getRefactoringType());
            System.out.println("before\n " + eachRefactoring.getClassBefore());
            System.out.println("left\n " + eachRefactoring.getRefactoringLeft());
            System.out.println("after\n " + eachRefactoring.getClassAfter());
            System.out.println("right\n " + eachRefactoring.getRefactoringRight());


        }





    }

}