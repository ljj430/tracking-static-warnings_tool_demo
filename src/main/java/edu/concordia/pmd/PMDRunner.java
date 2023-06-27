package edu.concordia.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.RendererFactory;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import edu.concordia.pmd.CSVExRenderer;
public class PMDRunner {

    public static void runPMD(String inputPath, String rulesetPath, String reportPath) throws ClassNotFoundException {

//        String[] pmdArgs = {
//                "-d", inputPath,
//                "-R", rulesetPath,
//                "-f","edu.concordia.pmd.CSVExRenderer",
//                "-r",reportPath
//        };
//        int i = PMD.run(pmdArgs);
//        System.out.println("status:"+ i );
        //PMD.main(pmdArgs);


        System.out.println("Start to run PMD");
        PMDConfiguration config = new PMDConfiguration();
        config.setInputPaths(inputPath);
        config.setRuleSets(rulesetPath);
        config.setReportFormat("edu.concordia.pmd.CSVExRenderer");
        config.setReportFile(reportPath);
        Properties p = new Properties();
        RendererFactory.createRenderer("edu.concordia.pmd.CSVExRenderer",p);
        PMD.doPMD(config);

//        PMDConfiguration config = new PMDConfiguration();
//        config.setInputPaths(inputPath);
//        config.setRuleSets(rulesetPath);
//        config.setReportFormat("csv");
//        config.setReportFile(reportPath);
//        PMD.doPMD(config);




    }


    public static void main(String[] args) throws ClassNotFoundException {
//        String inputPath = "D:\\ThesisProject\\trackingProjects\\spring-boot";
//        String inputPath ="D:\\ThesisProject\\trackingProjects\\spring-boot\\buildSrc\\src\\main\\java\\org\\springframework\\boot\\build";


        String inputPath ="/home/junjie/Desktop/tool_demo_StaticTracker/objects/fork/guava";
        String rulesetPath = "/home/junjie/Desktop/tool_demo_StaticTracker/ruleset/rulesets.xml";
        String reportPath = "/home/junjie/Desktop/tool_demo_StaticTracker/results/tmp/PMD_a394b2aea64d90c4cdc3696a82978b460ffbb914.csv";
        runPMD(inputPath,rulesetPath,reportPath);

    }
}
