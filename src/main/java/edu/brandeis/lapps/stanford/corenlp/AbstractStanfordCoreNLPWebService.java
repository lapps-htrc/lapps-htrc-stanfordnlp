package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.lappsgrid.discriminator.Discriminators.Uri;


/**
 * <a href="http://nlp.stanford.edu/software/corenlp.shtml" target="_blank">
 * Stanford Core NLP </a> provides a collection of available NLP tools,
 * including "tokenize, ssplit, pos, lemma, ner, parse, dcoref".
 *
 * <p>
 *
 * They are available through unique interface called annotation.
 *
 * @author shicq@cs.brandeis.edu
 *
 */

public abstract class AbstractStanfordCoreNLPWebService implements WebService {

    private static final Logger log = LoggerFactory
            .getLogger(AbstractStanfordCoreNLPWebService.class);

    private static ConcurrentHashMap<String, StanfordCoreNLP> cache =
            new ConcurrentHashMap<>();

    static final String PROP_TOKENIZE = "tokenize";
    static final String PROP_SENTENCE_SPLIT = "ssplit";
    static final String PROP_POS_TAG = "pos";
    static final String PROP_LEMMA = "lemma";
    static final String PROP_NER = "ner";
    static final String PROP_PARSE = "parse";
    static final String PROP_DEPPARSE = "depparse";
    static final String PROP_NATURALLOGIC = "natlog";
    static final String PROP_CORERENCE = "dcoref";
    static final String PROP_REALTION = "openie";
    static final String PROP_KEY = "annotators";
    static final String TOKEN_ID = "tk_";
    static final String SENT_ID = "s_";
    static final String CONSTITUENT_ID = "c_";
    static final String PS_ID = "ps_";
    static final String DEPENDENCY_ID = "dep_";
    static final String DS_ID = "ds_";
    static final String MENTION_ID = "m_";
    static final String COREF_ID = "coref_";
    static final String REL_ID = "rel_";
    static final String NE_ID = "ne_";


    private Properties props = new Properties();
    StanfordCoreNLP snlp = null;

    private String metadataString;

    /**
     * Default constructor only tries to load metadata.
     */
    AbstractStanfordCoreNLPWebService() {
        try {
            metadataString = loadMetadata();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get version from metadata
     */
    String getVersion() {
        String path = "/version.properties";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            log.error("version.properties file not found, version is UNKNOWN.");
            return "UNKNOWN";
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
            stream.close();
            return (String) properties.get("version");
        } catch (IOException e) {
            log.error("error loading version.properties, version is UNKNOWN.");
            return "UNKNOWN";
        }
    }

    /**
     * Initiate stanford NLP
     */
    void init(String... tools) {
        props.clear();
        String toolList;
        if (tools.length > 1) {
            StringBuilder sb = new StringBuilder();
            for(String tool: tools) {
                sb.append(tool).append(" ");
            }
            toolList = sb.toString();
        } else {
            toolList = tools[0];
        }
        props.put(PROP_KEY, toolList);
        snlp = getProcessor(props);
    }

    private StanfordCoreNLP getProcessor(Properties props) {
        String key = props.getProperty(PROP_KEY);
        log.info(String.format("Retrieving from cache: %s", key));
        StanfordCoreNLP val = cache.get(key);
        if (val == null) {
            val = new StanfordCoreNLP(props);
            cache.put(key, val);
            log.info(String.format("No cached found, newly cached: %s", key));
        }
        return val;
    }

    /**
     * This is default execute: takes a json, wrap it as a LIF, run modules
     */
    @Override
    public String execute(String input) {
        if (input == null)
            return null;
        // in case of Json
        Data data;

        try {
            data = Serializer.parse(input, Data.class);
            // Serializer#parse throws JsonParseException if input is not well-formed
        } catch (Exception e) {
            data = new Data();
            data.setDiscriminator(Uri.TEXT);
            data.setPayload(input);
        }

        final String discriminator = data.getDiscriminator();
        Container cont;

        switch (discriminator) {
            case Uri.ERROR:
                return input;
            case Uri.JSON_LD:
            case Uri.LIF:
                cont = new Container((Map) data.getPayload());
                // TODO: 5/9/18 what if the existing payload has different schema version?
                break;
            case Uri.TEXT:
                cont = new Container();
                // TODO: 5/9/18  fix url when it settles in
                cont.setSchema("http://vocab.lappsgrid.org/schema/container-schema-1.0.0.json");
                cont.setText((String) data.getPayload());
                cont.setLanguage("en");
                break;
            default:
                String message = String.format
                        ("Unsupported discriminator type: %s", discriminator);
                return new Data<>(Uri.ERROR, message).asJson();
        }

        try {
            return execute(cont);
        } catch (Throwable th) {
            th.printStackTrace();
            String message =
                    String.format("Error processing input: %s", th.toString());
            return new Data<>(Uri.ERROR, message).asJson();
        }
    }

    /**
     * This will be overridden for each module
     */
    public abstract String execute(Container json);

    abstract String loadMetadata();

    @Override
    public String getMetadata() {
        return this.metadataString;
    }

    ServiceMetadata setCommonMetadata() {
        ServiceMetadata commonMetadata = new ServiceMetadata();
        // TODO: 4/22/18 fix url when it settles in
        commonMetadata.setSchema("http://vocab.lappsgrid.org/schema/metadata-schema-1.1.0.json");
        commonMetadata.setVendor("http://www.cs.brandeis.edu/");
        commonMetadata.setLicense(Uri.APACHE2);
        commonMetadata.setVersion(this.getVersion());
        commonMetadata.setName(this.getClass().getName());

        IOSpecification required = new IOSpecification();
        required.addLanguage("en");
        required.setEncoding("UTF-8");
        required.addFormat(Uri.TEXT);
        required.addFormat(Uri.LIF);
        commonMetadata.setRequires(required);

        IOSpecification produces = new IOSpecification();
        produces.addLanguage("en");
        produces.setEncoding("UTF-8");
        produces.addFormat(Uri.LIF);
        commonMetadata.setProduces(produces);

        return commonMetadata;
    }
}

