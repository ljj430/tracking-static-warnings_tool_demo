package edu.concordia.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class GitProxy {
    private String uri = "";
    private Boolean isConnected = false;

    private static Repository repository = null;

    private DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

    public Boolean connect() throws IOException {
        if(this.isConnected){
            return false;
        }
        if(this.uri == null){
            return false;
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder.setGitDir(new File(this.uri)).readEnvironment().findGitDir().build();
        if(this.testConnection()){
            this.isConnected = true;
            df.setRepository(getRepository());
            return true;
        }
        else{
            this.isConnected = false;
            return false;
        }
    }

    public Iterable<RevCommit> getLogAll() throws IOException, GitAPIException {
        return (new Git(repository).log().all().call());
    }

    public RevCommit  getCommitByHash(String hash) throws IOException {
        ObjectId obj=repository.resolve(hash);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(obj);
        return commit;
    }

    public String getFileContent(RevCommit commit, String path) throws IOException {
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(path));

        if(!treeWalk.next()){
            return null;
        }
        String resultingPath = treeWalk.getPathString();

        if(!path.equals(resultingPath)){
            return null;
        }
        ObjectId objectID = treeWalk.getObjectId(0);
        ObjectLoader loader = repository.open(objectID);

        String content = new String(loader.getBytes());
        return content;
    }

    public String getFileContent(String hash, String path) throws IOException {
        return getFileContent(getCommitByHash(hash),path);
    }

    public List<DiffEntry> getChangedFiles(RevCommit current,RevCommit parent) throws IOException {
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), current.getTree());

        return diffs;
    }

    public List<Edit> getEditList(DiffEntry d) throws IOException {
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        FileHeader header = df.toFileHeader(d);
        return header.toEditList();
    }

    public Boolean testConnection() throws IOException {
        if (repository == null) {
            return false;
        }
        ObjectId head = repository.resolve(Constants.HEAD);
        if (head == null) {
            return false;
        }
        return true;
    }


    public void setURI(String in){
        this.uri=in;
    }

    public String getUri() {
        return uri;
    }

    public Repository getRepository() {
        return repository;
    }

    public void disconnect(){
        repository.close();
    }


}
