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
        PMDConfiguration config = new PMDConfiguration();
//        String[] pmdArgs={
//          "-d",inputPath,
//          "-R",rulesetPath,
//          "-f","xml",
//          "-r",reportPath
//        };
//        PMD.main(pmdArgs);

        config.setInputPaths(inputPath);
        config.setRuleSets(rulesetPath);
        config.setReportFormat("edu.concordia.pmd.CSVExRenderer");
        config.setReportFile(reportPath);
        Properties p = new Properties();
        RendererFactory.createRenderer("edu.concordia.pmd.CSVExRenderer",p);
        PMD.doPMD(config);
    }


    public static void main(String[] args) throws ClassNotFoundException {
//        String inputPath = "D:\\ThesisProject\\trackingProjects\\spring-boot";
//        String inputPath ="D:\\ThesisProject\\trackingProjects\\spring-boot\\buildSrc\\src\\main\\java\\org\\springframework\\boot\\build";
        String inputPath ="D:\\ThesisProject\\trackingProjects\\kafka\\generator\\src\\main\\java\\org\\apache\\kafka\\message";
        String rulesetPath = "D:\\ThesisProject\\findbugsanalysis\\FixPatternMining\\ruleset\\rulesets.xml";
        String reportPath = "D:\\Git\\tmp\\pmdReport.csv";
        runPMD(inputPath,rulesetPath,reportPath);
    }
}
