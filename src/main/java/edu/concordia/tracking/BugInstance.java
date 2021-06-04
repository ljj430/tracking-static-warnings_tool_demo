package edu.concordia.tracking;

import java.util.Objects;

public class BugInstance implements Cloneable{
    private String className="";
    private String methodName="";
    private String fieldName="";
    private String startLine="-1";
    private String endLine="-1";
    private String classPath="";
    private String violation="";
    private Boolean isRefactoring=false;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
    }

    public String getEndLine() {
        return endLine;
    }

    public void setEndLine(String endLine) {
        this.endLine = endLine;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public Boolean getRefactoring() {
        return isRefactoring;
    }

    public void setRefactoring(Boolean refactoring) {
        isRefactoring = refactoring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugInstance that = (BugInstance) o;
        return Objects.equals(methodName, that.methodName) &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(startLine, that.startLine) &&
                Objects.equals(endLine, that.endLine) &&
                Objects.equals(classPath, that.classPath) &&
                Objects.equals(violation, that.violation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, fieldName, startLine, endLine, classPath, violation);
    }

    @Override
    public String toString() {
        return "BugInstance{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", startLine='" + startLine + '\'' +
                ", endLine='" + endLine + '\'' +
                ", classPath='" + classPath + '\'' +
                ", violation='" + violation + '\'' +
                ", isRefactoring=" + isRefactoring +
                '}';
    }
    @Override
    public Object clone() {
        BugInstance bi = null;
        try{
            bi = (BugInstance)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return bi;
    }
}
