package edu.concordia.tracking;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import edu.concordia.git.GitProxy;
import edu.concordia.refactoring.RefactoringFormat;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.refactoringminer.utils.RefactoringSet;

import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import javax.tools.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static edu.concordia.tracking.ViolationMatcher.initGitRepo;

public class TrackingUtils {

    public static ArrayList transformFiles2Package(List<DiffEntry> diffs, RevCommit parentCommit, RevCommit childCommit, GitProxy gitproxy) throws IOException, GitAPIException {
        HashSet oldPaths = new HashSet<String>();
        HashSet newPaths = new HashSet<String>();
        HashMap<String,DiffEntry> diffMap = new HashMap<String,DiffEntry>();
        for(DiffEntry d: diffs){
            String changePath = d.getChangeType().equals(DiffEntry.ChangeType.DELETE) ? d.getOldPath() : d.getNewPath();
            String oldPackage;
            String oldClassName;
            String newPackage;
            String newClassName;
            if(d.getChangeType().equals(DiffEntry.ChangeType.ADD)){
                String newSource = getSourceText(childCommit,changePath,gitproxy);
                newPackage = parseAndExtractPackagePath(newSource );
                newClassName = takeFileName(changePath);
                oldPackage = newPackage;
                oldClassName = newClassName;
                String oldSource = "";
            }

            else if(d.getChangeType() == DiffEntry.ChangeType.DELETE){
                String oldSource = getSourceText(parentCommit,changePath,gitproxy);
                oldPackage = parseAndExtractPackagePath( oldSource );
                oldClassName = takeFileName(changePath);
                String newSource = "";
                newPackage=oldPackage;
                newClassName=oldClassName;
            }
            else if(d.getChangeType() == DiffEntry.ChangeType.RENAME){
                String oldSource = getSourceText(parentCommit,d.getOldPath(),gitproxy);
                String newSource = getSourceText(childCommit,d.getNewPath(),gitproxy);
                oldPackage = parseAndExtractPackagePath( oldSource  );
                oldClassName = takeFileName(changePath);
                String np = parseAndExtractPackagePath(newSource);
                String nc = takeFileName(d.getNewPath());
                newPackage=np;
                newClassName=nc;
            }
            else{
                String oldSource = getSourceText(parentCommit,changePath,gitproxy);
                oldPackage = parseAndExtractPackagePath( oldSource  );
                oldClassName = takeFileName(changePath);
                String newSource = getSourceText(childCommit,changePath,gitproxy);
                String np = parseAndExtractPackagePath(newSource);
                String nc = takeFileName(d.getNewPath());
                newPackage=np;
                newClassName=nc;
            }
            String oldPackagePath = oldPackage+"."+oldClassName;
            String newPackagePath = newPackage+"."+newClassName;

//            System.out.println(d.getChangeType());
//            System.out.println(d.getOldPath());
//            System.out.println(d.getNewPath());
//            System.out.println(oldPackagePath);
//            System.out.println(newPackagePath);
//            System.out.println();

            oldPaths.add(oldPackagePath);
            newPaths.add(newPackagePath);
            diffMap.put(oldPackagePath, d);
        }
        ArrayList returnList = new ArrayList<>();
        returnList.add(oldPaths);
        returnList.add(newPaths);
        returnList.add(diffMap);
        return returnList;
    }

    public static String getSourceText(RevCommit commit, String path, GitProxy proxy) throws IOException {
        return proxy.getFileContent(commit,path);
    }

    public static String getSourceText(String commit, String path, GitProxy proxy) throws IOException {
        return proxy.getFileContent(commit,path);
    }

    public static String parseAndExtractPackagePath(String source){
        try{
            CompilationUnit cu = getCU(source);
            String packageName = cu.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
            return packageName;
        }
        catch (Exception e){

            CompilationUnit cu = getCU17(source);
//            System.out.println(cu.toString());
            return "";
        }
    }

    public static String parseAndExtractPackagePathScala(String source){
        String[] lines = source.split("\n");
        if(lines[0].startsWith("package")){
            String packageName = lines[0].replace("package","").replace(" ","");
            return packageName;
        }
        else{
            System.out.println("Did not find package of scala\n"+source);
            return null;
        }
    }

    public static CompilationUnit getCU(String source){
        JavaParser javaparser = new JavaParser();
        CompilationUnit cu = StaticJavaParser.parse(source);
//        CompilationUnit cu = StaticJavaParser.parse(source);

        return cu;
    }

    public static CompilationUnit getCU17(String source){
        JavaParser javaparser = new JavaParser();
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        CompilationUnit cu = StaticJavaParser.parse(source);
//        CompilationUnit cu = StaticJavaParser.parse(source);

        return cu;
    }

    public static String takeFileName(String path){
        return FilenameUtils.getBaseName(path);
    }

    public static ArrayList getChangedSourceFiles(List<DiffEntry> entries){
        ArrayList retList = new ArrayList <DiffEntry>(); // empty list

        for( DiffEntry diff : entries )
        {
            Boolean isSource;
            if(diff.getChangeType().equals(DiffEntry.ChangeType.ADD)){
                if(diff.getNewPath().toLowerCase().endsWith(".java") || diff.getNewPath().toLowerCase().endsWith(".scala")){
                    isSource = true;
                } else{
                    isSource = false;
                }
            }
            else if( diff.getOldPath().toLowerCase().endsWith(".java") || diff.getOldPath().toLowerCase().endsWith(".scala") ) {
                isSource = true;
            } else{
                isSource = false;
            }
            if(isSource){
                retList.add(diff);
            }
        }

        return retList;
    }
    public static Boolean isSameButDiffLoc(BugInstance pa,BugInstance ch){
        if(ch.getViolation().equals(pa.getViolation()) && ch.getSourcePath().equals(pa.getSourcePath()) && ch.getMethodName().equals(pa.getMethodName()) && ch.getFieldName().equals(pa.getFieldName())){
            return true;
        }
        else{
            return false;
        }
    }

    public static ArrayList<Edit> getOverlappingEditsParent(int paStart,int paEnd, List<Edit> edits){
        ArrayList<Edit> overlappingEdits = new ArrayList<Edit>();
        for(Edit e : edits){
//            System.out.println("Old Edit:"+e.getBeginA() +" "+ e.getEndA());
//            System.out.println("New Edit:"+e.getBeginB() +" "+ e.getEndB());
            if(isOverlappedParent(paStart,paEnd,e)){
                overlappingEdits.add(e);
            }
        }
        return overlappingEdits;
    }

    public static ArrayList<Edit> getOverlappingEditsChild(int chStart,int chEnd, List<Edit> edits){
        ArrayList<Edit> overlappingEdits = new ArrayList<Edit>();
        for(Edit e : edits){
            if(isOverlappedChild(chStart,chEnd,e)){
                overlappingEdits.add(e);
            }
        }
        return overlappingEdits;
    }

    public static Boolean isOverlappedParent(int start, int end, Edit e){
        int up = e.getBeginA();
        int bottom = e.getBeginA();
        return isOverlapped(start,end,up,bottom);
    }

    public static Boolean isOverlappedChild(int start, int end, Edit e){
        int up = e.getBeginB();
        int bottom = e.getBeginB();
        return isOverlapped(start,end,up,bottom);
    }

    public static Boolean isOverlapped(int start, int end, int up, int bottom){
        if(start>up && start>bottom){
            return false;
        }
        else if(end<up&& end<bottom){
            return false;
        }else
            return true;
    }

    public static Boolean hasEditedParent(int startLine, int endLine, ArrayList<Edit> edits){
        for(Edit e:edits){
            if(isOverlappedParent(startLine,endLine ,e)){
                return true;
            }
        }
        return false;
    }

    public static Boolean hasEditedChild(int startLine, int endLine, ArrayList<Edit> edits){
        for(Edit e:edits){
            if(isOverlappedChild(startLine,endLine ,e)){
                return true;
            }
        }
        return false;
    }

    public static Edit getMinimumEdit(ArrayList<Edit> edits){
        int paBegin = Integer.MAX_VALUE;
        int chBegin = Integer.MAX_VALUE;
        Edit editBegin = edits.get(0);
        for(Edit e : edits){
            if(e.getEndA()< paBegin && e.getEndB() < chBegin){
                paBegin = e.getEndA();
                chBegin = e.getEndB();
                editBegin = e;
            }
        }
        return editBegin;
    }

    public static Edit getLastEdit(int paStart , ArrayList<Edit> edits){
        Edit lastEdit = getMinimumEdit(edits);
        for(Edit e : edits){
            if(e.getEndA() < paStart && e.getEndA()> lastEdit.getEndA()){
                lastEdit = e;
            }
        }
        return lastEdit;
    }


//    public static String retrieveSourceCode(String classPath) throws IOException {
//        // Get the class name from the class path
//        String className = classPath.replace('.', '/') + ".java";
//
//        // Use Java Compiler API to retrieve the source code
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
//
//        try {
//            JavaFileObject javaFileObject = fileManager.getJavaFileForInput(
//                    StandardLocation.CLASS_PATH, classPath, JavaFileObject.Kind.SOURCE);
//            if (javaFileObject != null) {
//                byte[] fileBytes = Files.readAllBytes(Paths.get(javaFileObject.toUri()));
//                return new String(fileBytes, StandardCharsets.UTF_8);
//            } else {
//                throw new IOException("Source code not found for class: " + classPath);
//            }
//        } finally {
//            fileManager.close();
//        }
//    }








    public static String getLineRange(int start, int end, String source){
        int counter = 1;
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(source);
        while(scanner.hasNextLine() && counter <= end){
            if(start <= counter){
                sb.append(scanner.nextLine()+"\n");
            }
            else{
                scanner.nextLine();
            }
            counter+=1;
        }
        scanner.close();
        return sb.toString();
    }


    ///git checkout checkout

    public static void checkoutCommit(String repoPath, String commitId) throws GitAPIException {
        Path gitPath = Paths.get(repoPath, ".git");
        String gitProxyPath = gitPath.toString();
        GitProxy repoProxy = new GitProxy();
        repoProxy.setURI(gitProxyPath);
        Git git = new Git(repoProxy.getRepository());

        git.checkout().setName(commitId).call();
//        try {
//            Repository repository = openRepository(repoPath);
//            Git git = new Git(repository);
//
//            CheckoutCommand checkoutCommand = git.checkout();
//            checkoutCommand.setName(commitId);
//            checkoutCommand.call();
//
//            git.close();
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }


    }

    public static List<File> findAllJavaFiles(String folderPath) {
        List<File> javaFiles = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            findJavaFiles(folder, javaFiles);
        } else {
            System.out.println("Invalid folder path: " + folderPath);
        }

        return javaFiles;
    }

    private static void findJavaFiles(File folder, List<File> javaFiles) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                } else if (file.isDirectory()) {
                    findJavaFiles(file, javaFiles);
                }
            }
        }
    }


    public static String getFullSource(String filePath) throws IOException {

        Path file = Paths.get(filePath);
        StringBuilder sourceCode = new StringBuilder();
        for (String line : Files.readAllLines(file)) {
            sourceCode.append(line).append(System.lineSeparator());
        }
        return sourceCode.toString();
    }

    public static HashMap<String, String> getPaAllSource(GitProxy gitproxy, HashSet<String> changedPaths, HashMap<String,DiffEntry> diffMap, String commit) throws IOException {
        HashMap<String, String> allSource = new HashMap();


        for(String path: changedPaths){
            DiffEntry d = diffMap.get(path);
            String changePath = d.getChangeType().equals(DiffEntry.ChangeType.DELETE) ? d.getOldPath() : d.getNewPath();
            if(d.getChangeType().equals(DiffEntry.ChangeType.RENAME)){
                changePath = d.getOldPath();
            }
            allSource.put(changePath,gitproxy.getFileContent(commit,changePath));
        }




        return allSource;
    }

    public static HashMap<String, String> getChAllSource(GitProxy gitproxy, HashSet<String> changedPaths, HashMap<String,DiffEntry> diffMap, String commit) throws IOException {
        HashMap<String, String> allSource = new HashMap();


        for(String path: changedPaths){
            try {
                DiffEntry d = diffMap.get(path);
                String changePath = d.getChangeType().equals(DiffEntry.ChangeType.DELETE) ? d.getOldPath() : d.getNewPath();
                if (d.getChangeType().equals(DiffEntry.ChangeType.RENAME)) {
                    changePath = d.getNewPath();
                }
                allSource.put(changePath, gitproxy.getFileContent(commit, changePath));
            }
            catch (NullPointerException npe){
                ;
            }
        }
        return allSource;
    }
//temproly
    public static ArrayList getPaRefactoring(BugInstance pa,ArrayList<RefactoringFormat> refactoringInfo){
        String sourcePath = "";
        String refactoringType="";
        HashSet<String> classRefactoringSet = new HashSet<String>(Arrays.asList("MOVE_CLASS","RENAME_CLASS","MOVE_RENAME_CLASS","EXTRACT_SUPERCLASS","EXTRACT_CLASS","EXTRACT_SUBCLASS"));
        HashSet<String> methodRefactoringSet = new HashSet<String>(Arrays.asList("EXTRACT_OPERATION", "RENAME_METHOD","MOVE_OPERATION","PULL_UP_OPERATION","PUSH_DOWN_OPERATION",
                "MOVE_AND_INLINE_OPERATION","EXTRACT_AND_MOVE_OPERATION","MOVE_AND_RENAME_OPERATION"));
        HashSet<String> fieldRefactoringSet = new HashSet<String>(Arrays.asList("RENAME_ATTRIBUTE","RENAME_VARIABLE","RENAME_PARAMETER","MOVE_ATTRIBUTE","REPLACE_VARIABLE_WITH_ATTRIBUTE",
                "MOVE_RENAME_ATTRIBUTE","PULL_UP_ATTRIBUTE","PUSH_DOWN_ATTRIBUTE"));
        for(RefactoringFormat re:refactoringInfo){
            if(fieldRefactoringSet.contains(re.getRefactoringType().toString())){
                ArrayList fieldList = fieldRefactoringDetect(pa,re);
                pa = (BugInstance) fieldList.get(0);
                sourcePath = (String) fieldList.get(1);
                refactoringType = (String) fieldList.get(2);
            }
            else if (methodRefactoringSet.contains(re.getRefactoringType().toString())){
                ArrayList methodList = methodRefactoringDetect(pa,re);
                pa = (BugInstance) methodList.get(0);
                sourcePath = (String) methodList.get(1);
                refactoringType = (String) methodList.get(2);
            }
            else if (classRefactoringSet.contains(re.getRefactoringType().toString())){
                ArrayList classList = classRefactoringDetect(pa,re);
                pa = (BugInstance) classList.get(0);
                sourcePath = (String) classList.get(1);
                refactoringType = (String) classList.get(2);
            }
            if(pa.isRefactoring() && !refactoringType.equals("")){
                break;
            }
        }
        ArrayList returnList = new ArrayList(Arrays.asList(pa, sourcePath,refactoringType));
        return returnList;
    }



    public static ArrayList classRefactoringDetect(BugInstance pa,RefactoringFormat ref){
        String sourcePath = "";
        String refactoringType = "";
        String classPath = getClassPath(ref.getRefactoringLeft().get(0).getFilePath());
        String newClassPath = getClassPath(ref.getRefactoringRight().get(0).getFilePath());
        int newStart = 0;
        int newEnd = 0;
        int oldStart = ref.getRefactoringLeft().get(0).getStartLine();
        int oldEnd = ref.getRefactoringLeft().get(0).getEndLine();



        if(pa.getClassPath().equals(classPath) && (Integer.parseInt(pa.getStartLine())>=oldStart
                && Integer.parseInt(pa.getEndLine())<= oldEnd || oldEnd < 0 )){
            newStart = Integer.parseInt(pa.getStartLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();
            newEnd = Integer.parseInt(pa.getEndLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();


//            System.out.println("pa.classpath:"+pa.getClassPath());
//            System.out.println("refactoring get:"+newClassPath);
//            System.out.println(pa.getClassPath().equals(classPath));
//            System.out.println("pa.start"+pa.getStartLine());
//            System.out.println("re old start"+ref.getRefactoringLeft().get(0).getStartLine());
//            System.out.println("re new start"+ref.getRefactoringRight().get(0).getStartLine());
//            System.out.println("pa.end"+pa.getEndLine());
//            System.out.println("re old end"+ref.getRefactoringLeft().get(0).getEndLine());
//            System.out.println("re new end"+ref.getRefactoringRight().get(0).getEndLine());
//            System.out.println();

            pa.setRefactoring(true);
            pa.setClassPath(newClassPath);
            pa.setStartLine(String.valueOf(newStart));
            pa.setEndLine(String.valueOf(newEnd));
            refactoringType = ref.getRefactoringType().toString();
            sourcePath = pathDotToSlash(getClassPath(ref.getRefactoringRight().get(0).getFilePath()));
            pa.setSourcePath(sourcePath);
        }
//        System.out.println("pa after set:\n"+pa);
        return new ArrayList(Arrays.asList(pa,sourcePath,refactoringType));
    }

    public static ArrayList methodRefactoringDetect(BugInstance pa,RefactoringFormat ref){

        String sourcePath = "";
        String refactoringType = "";
        //pa is a field refactoirng
        int newStart = 0;
        int newEnd = 0;
        int oldStart = ref.getRefactoringLeft().get(0).getStartLine();
        int oldEnd = ref.getRefactoringLeft().get(0).getEndLine();
        String methodName = getMethodName(ref.getRefactoringLeft().get(0).getCodeElement());
        String classPath = getClassPath(ref.getRefactoringLeft().get(0).getFilePath());

        if(pa.getMethodName().equals(methodName) && pa.getClassPath().equals(classPath) && Integer.parseInt(pa.getStartLine())>=oldStart
        && Integer.parseInt(pa.getEndLine())<= oldEnd){
            newStart = Integer. parseInt(pa.getStartLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();
            newEnd = Integer. parseInt(pa.getEndLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();
            pa.setRefactoring(true);
            pa.setMethodName(methodName);
            pa.setStartLine(String.valueOf(newStart));
            pa.setEndLine(String.valueOf(newEnd));
            sourcePath = pathDotToSlash(getClassPath(ref.getRefactoringRight().get(0).getFilePath()));
            pa.setSourcePath(sourcePath);
            refactoringType = ref.getRefactoringType().toString();
        }
        return new ArrayList(Arrays.asList(pa,sourcePath,refactoringType));
    }

    public static ArrayList fieldRefactoringDetect(BugInstance pa,RefactoringFormat ref){
        String sourcePath = "";
        String refactoringType = "";
        //pa is a field refactoirng
        int newStart = 0;
        int newEnd = 0;
        String fieldName = getFieldName(ref.getRefactoringLeft().get(0).getCodeElement());
        String classPath = getClassPath(ref.getRefactoringLeft().get(0).getFilePath());

        if(pa.getFieldName().equals(fieldName) && pa.getClassPath().equals(classPath)){
            newStart = Integer. parseInt(pa.getStartLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();
            newEnd = Integer. parseInt(pa.getEndLine()) -
                    ref.getRefactoringLeft().get(0).getStartLine() + ref.getRefactoringRight().get(0).getStartLine();
            pa.setRefactoring(true);
            pa.setFieldName(fieldName);
            pa.setStartLine(String.valueOf(newStart));
            pa.setEndLine(String.valueOf(newEnd));
            sourcePath = pathDotToSlash(getClassPath(ref.getRefactoringRight().get(0).getFilePath()));
            pa.setSourcePath(sourcePath);
            refactoringType = ref.getRefactoringType().toString();
        }
        return new ArrayList(Arrays.asList(pa,sourcePath,refactoringType));
    }

    //"src/test/java/org/jclouds/openstack/FindSecurityGroupOrCreateTest.java" -> "org.jclouds.openstack.FindSecurityGroupOrCreateTest"
    public static String getClassPath(String raw){
        String[] token = raw.split("/");
        String tmp = "";
        for (int i = token.length - 1; i >= 0; i--) {
            if (token[i].equals("java")) {
                break;
            } else if (token[i].contains(".java")) {
                tmp = token[i].replace(".java", "");
            } else if ("com".equals(token[i])) {
                tmp = token[i] + "." + tmp;
                break;
            } else if ("org".equals(token[i])) {
                tmp = token[i] + "." + tmp;
                break;
            } else {
                tmp = token[i] + "." + tmp;
            }
        }

        return tmp;
    }

    public static String  pathDotToSlash(String classPath){
        String filePath = classPath.replace('.', '/') + ".java";
        return filePath;
    }
    //"public xxx( "
    public static String getMethodName(String raw){
        String tmp = raw.replace("public ","");
        tmp = tmp.replace("private ","");
        tmp = tmp.replace("protected ","");

        String[] tokens = tmp.split("\\(");
        tmp = tokens[0];
        tmp = tmp.replace(" ","");
        return tmp;
    }

    //"public xxx : "
    public static String getFieldName(String raw){
        String tmp = raw.replace("public ","");
        tmp = tmp.replace("private ","");
        tmp = tmp.replace("protected ","");

        String[] tokens = tmp.split(":");
        tmp = tokens[0];
        tmp = tmp.replace(" ","");
        return tmp;
    }

    public static void main(String[] args){
//        String path = "D:\\ThesisProject\\Mutant_Test_Tools\\Jmutator\\Jmutator";
//        String packageName = takeFileName(path);
        String sc = "public class PackageInfoExtractor {\n" +
                "    private void configureAsciidoctorTask(Project project, AbstractAsciidoctorTask asciidoctorTask) {\n" +
                "        asciidoctorTask.configurations(EXTENSIONS_CONFIGURATION_NAME);\n" +
                "        configureCommonAttributes(project, asciidoctorTask);\n" +
                "        configureOptions(asciidoctorTask);\n" +
                "        configureForkOptions(asciidoctorTask);\n" +
                "        asciidoctorTask.baseDirFollowsSourceDir();\n" +
                "        createSyncDocumentationSourceTask(project, asciidoctorTask);\n" +
                "        if (asciidoctorTask instanceof AsciidoctorTask task) {\n" +
                "            boolean pdf = task.getName().toLowerCase().contains(\"pdf\");\n" +
                "            String backend = (!pdf) ? \"spring-html\" : \"spring-pdf\";\n" +
                "            task.outputOptions((outputOptions) -> outputOptions.backends(backend));\n" +
                "        }\n" +
                "    }\n" +
                "}";
        System.out.println(getCU(sc).toString());

//        System.out.println(packageName);
    }
}

