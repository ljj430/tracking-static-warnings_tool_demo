package edu.concordia.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.RendererFactory;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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


        String inputPath ="D:\\Git\\test\\forked\\guava";
        String rulesetPath = "D:\\ThesisProject\\findbugsanalysis\\FixPatternMining\\ruleset\\rulesets.xml";
        String reportPath = "D:\\Git\\tmp\\695b_run.csv";
        runPMD(inputPath,rulesetPath,reportPath);

    }
}
