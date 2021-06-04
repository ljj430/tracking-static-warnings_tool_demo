package edu.concordia.refactoring;

import gr.uom.java.xmi.diff.CodeRange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.refactoringminer.api.RefactoringType;

import java.util.List;
import java.util.Set;

public class RefactoringFormat {
    RefactoringType refactoringType;
    List<CodeRange> refactoringLeft;
    List<gr.uom.java.xmi.diff.CodeRange> refactoringRight;
    String commit;

    Set<ImmutablePair<String,String>> classBefore;
    Set<ImmutablePair<String,String>> classAfter;

    RefactoringFormat(RefactoringType refactoringType, List<gr.uom.java.xmi.diff.CodeRange> refactoringLeft,
                       List<gr.uom.java.xmi.diff.CodeRange> refactoringRight, String commit,
                       Set<org.apache.commons.lang3.tuple.ImmutablePair<String,String>> classBefore, Set<org.apache.commons.lang3.tuple.ImmutablePair<String,String>> classAfter){
        this.refactoringType = refactoringType;
        this.refactoringLeft = refactoringLeft;
        this.refactoringRight = refactoringRight;
        this.commit=commit;
        this.classBefore = classBefore;
        this.classAfter = classAfter;
    }
    public String toString(){
        return this.refactoringType.toString();
    }
    public void setRefactoringType(RefactoringType refactoringType) {
        this.refactoringType = refactoringType;
    }

    public void setRefactoringLeft(List<gr.uom.java.xmi.diff.CodeRange> refactoringLeft) {
        this.refactoringLeft = refactoringLeft;
    }

    public void setRefactoringRight(List<gr.uom.java.xmi.diff.CodeRange> refactoringRight) {
        this.refactoringRight = refactoringRight;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setClassBefore(Set<ImmutablePair<String,String>> classBefore) {
        this.classBefore = classBefore;
    }

    public void setClassAfter(Set<ImmutablePair<String,String>> classAfter) {
        this.classAfter = classAfter;
    }


    public RefactoringType getRefactoringType(){
        return this.refactoringType;
    }

    public List<CodeRange> getRefactoringLeft() {
        return this.refactoringLeft;
    }

    public List<gr.uom.java.xmi.diff.CodeRange> getRefactoringRight() {
        return this.refactoringRight;
    }

    public String getCommit() {
        return this.commit;
    }

    public Set<ImmutablePair<String,String>> getClassBefore() {
        return this.classBefore;
    }

    public Set<ImmutablePair<String,String>> getClassAfter() {
        return this.classAfter;
    }
}
