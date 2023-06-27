# Tracking static warnings

## General Information
To run the tracking approach, you need to hava Java 17+, Maven 3.9+ installed and Python 3+.  

This tracking tool supports static tools:
* Spotbugs
* PMD
## Build a jar
    ./mvn clean compile assembly:single
    
## Configure git hooks
Copy `pre-push` and `config.yml` from templates into `<your-project>/.git/hooks`.
Edit to configure `config.yml`.

    local_repo_path: your project local path
    report_save_path: save directory path
    remote_repo_url: your project remote repo url 
    tracking_approach_jar_path:  the tracking approach jar path e.g., ./target/tracking-static-warnings-1.0-SNAPSHOT.jar
    static_tool: 
      name: static tool name
      compilation_command: Spotubgs only e.g., mvn clean install
      ruleset_path: PMD only 
## Usage
Once you put `pre-push` and `config.yml` in hooks, every time you push new code to repo, the Spotbugs/PMD will be run on your repo of the lastest two revisions and the tracking approach will identify the disappeared static warnings and newly-introduced static warnings by comparing the static warnings from two revisions. You can use `git push --no-verify` to skip the tracking process.
  

 