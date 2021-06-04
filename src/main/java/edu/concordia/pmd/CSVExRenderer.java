package edu.concordia.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import net.sourceforge.pmd.renderers.CSVRenderer;
import net.sourceforge.pmd.renderers.CSVWriter;
import net.sourceforge.pmd.renderers.ColumnDescriptor;
import org.apache.commons.lang3.StringUtils;
import net.sourceforge.pmd.renderers.ColumnDescriptor.Accessor;

import java.io.IOException;
import java.util.*;

public class CSVExRenderer extends AbstractIncrementingRenderer {

    private String separator;
    private String cr;

    private CSVWriter<RuleViolation> csvWriter;

    private static final String DEFAULT_SEPARATOR = ",";

    private static final Map<String, PropertyDescriptor<Boolean>> PROPERTY_DESCRIPTORS_BY_ID = new HashMap<String, PropertyDescriptor<Boolean>>();

    public static final String NAME = "csv";

    @SuppressWarnings("unchecked")
    private final ColumnDescriptor<RuleViolation>[] allColumns = new ColumnDescriptor[] {
    new ColumnDescriptor("violation", "Violation", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return rv.getRule().getName();
        }
    }), new ColumnDescriptor("package", "Package", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return rv.getPackageName();
        }

    }), new ColumnDescriptor("class", "Class", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            String className = rv.getClassName();
            int index = className.indexOf("$");
            if(index == -1){
                return className;
            }
            else{
                String subStr = className.substring(0,index);
                return subStr;
            }
//            return rv.getClassName();
        }
    }), new ColumnDescriptor("method", "Method", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return rv.getMethodName();
        }
    }), new ColumnDescriptor("field", "Field", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
//            System.out.println(rv.getVariableName());
//            if(rv.getVariableName() == null){
//                return "";
//            }
//            else{
//                return rv.getVariableName();
//            }
            return rv.getVariableName();
        }
    }), new ColumnDescriptor("startLine", "StartLine", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return Integer.toString(rv.getBeginLine());
        }
    }), new ColumnDescriptor("endLine", "EndLine", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return Integer.toString(rv.getEndLine());
        }

    }), new ColumnDescriptor("desc", "Description", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return StringUtils.replaceChars(rv.getDescription(), '\"', '\'');
        }

    }), new ColumnDescriptor("priority", "Priority", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return Integer.toString(rv.getRule().getPriority().getPriority());
        }
    }), new ColumnDescriptor("category", "Category", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return rv.getRule().getRuleSetName();
        }
    }), new ColumnDescriptor("path", "Path", new ColumnDescriptor.Accessor<RuleViolation>() {
        @Override
        public String get(int idx, RuleViolation rv, String cr) {
            return rv.getFilename();
        }
    }),
    };

    public CSVExRenderer(ColumnDescriptor<RuleViolation>[] columns, String theSeparator, String theCR) {
        super(NAME, "Comma-separated values tabular format.");

        separator = theSeparator;
        cr = theCR;

        for (ColumnDescriptor<RuleViolation> desc : columns) {
            definePropertyDescriptor(booleanPropertyFor(desc.id, desc.title));
        }
    }

    public CSVExRenderer() {
        super(NAME, "Comma-separated values tabular format.");

        separator = DEFAULT_SEPARATOR;
        cr = PMD.EOL;

        for (ColumnDescriptor<RuleViolation> desc : allColumns) {
            definePropertyDescriptor(booleanPropertyFor(desc.id, desc.title));
        }
    }

    private static PropertyDescriptor<Boolean> booleanPropertyFor(String id, String label) {

        PropertyDescriptor<Boolean> prop = PROPERTY_DESCRIPTORS_BY_ID.get(id);
        if (prop != null) {
            return prop;
        }

        prop = PropertyFactory.booleanProperty(id).defaultValue(true).desc("Include " + label + " column").build();
        PROPERTY_DESCRIPTORS_BY_ID.put(id, prop);
        return prop;
    }

    private List<ColumnDescriptor<RuleViolation>> activeColumns() {

        List<ColumnDescriptor<RuleViolation>> actives = new ArrayList();

        for (ColumnDescriptor<RuleViolation> desc : allColumns) {
            PropertyDescriptor<Boolean> prop = booleanPropertyFor(desc.id, null);
            if (getProperty(prop)) {
                actives.add(desc);
            }

        }
        return actives;
    }

    private CSVWriter<RuleViolation> csvWriter() {
        if (csvWriter != null) {
            return csvWriter;
        }

        csvWriter = new CSVWriter(activeColumns(), separator, cr);
        return csvWriter;
    }

    @Override
    public void start() throws IOException {
        csvWriter().writeTitles(getWriter());
    }

    @Override
    public String defaultFileExtension() {
        return "csv";
    }

    @Override
    public void renderFileViolations(Iterator<RuleViolation> violations) throws IOException {
        csvWriter().writeData(getWriter(), violations);
    }

    /**
     * We can't show any violations if we don't have any visible columns.
     *
     * @see PropertySource#dysfunctionReason()
     */
    @Override
    public String dysfunctionReason() {
        return activeColumns().isEmpty() ? "No columns selected" : null;
    }
}
