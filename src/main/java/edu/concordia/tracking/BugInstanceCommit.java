package edu.concordia.tracking;

import java.util.Objects;

public class BugInstanceCommit {
    private BugInstance bug;
    private String commit;

    public BugInstanceCommit(BugInstance bug, String commit) {
        this.bug = bug;
        this.commit = commit;
    }

    public BugInstance getBug() {
        return bug;
    }

    public void setBug(BugInstance bug) {
        this.bug = bug;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugInstanceCommit that = (BugInstanceCommit) o;
        return Objects.equals(bug, that.bug) &&
                Objects.equals(commit, that.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bug, commit);
    }

    @Override
    public String toString() {
        return "BugInstanceCommits{" +
                "bug=" + bug +
                ", commit='" + commit + '\'' +
                '}';
    }
}
