# Tracking static warnings
=================

## Usage
To run the tracking approach, you need to hava Java installed and Python 3 or later.

### Build a jar
    ./mvn clean install
    
### Configure Git Hooks
Copy the templates `pre-push` and `config.yml` into `<your-project>/.git/hooks`.
Configure `config.yml`.

    local_repo_path: your project local path
    report_save_path: save directory path
    remote_repo_url: your project remote repo url 
    tracking_jar_path:  the tracking approach jar path e.g., ./target/tracking-static-warnings-1.0-SNAPSHOT.jar
    static_tool: 
      name: static tool name
      compilation_command: Spotubgs only e.g., mvn clean install
      ruleset_path: PMD only 
##
Once you configure it, when your push code to repo, the Spotbugs/PMD will be run on your project of the lastest two revisions and the tracking approach will tell you the resolved static warnings and newly-introduced warnings.
  

 