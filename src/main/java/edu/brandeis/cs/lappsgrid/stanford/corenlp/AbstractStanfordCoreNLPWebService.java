package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.xerces.impl.io.UTF8Reader;
import org.lappsgrid.api.WebService;
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
import java.util.Scanner;
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

@org.lappsgrid.annotations.CommonMetadata(

        requires_encoding = "UTF-8",
        produces_encoding = "UTF-8",
        vendor = "http://www.cs.brandeis.edu/",
        license = "apache2",
        language = { "en" }
)
public abstract class AbstractStanfordCoreNLPWebService implements WebService {

    protected static final Logger log = LoggerFactory
            .getLogger(AbstractStanfordCoreNLPWebService.class);


    static protected ConcurrentHashMap<String, StanfordCoreNLP> cache =
            new ConcurrentHashMap<>();

    static final String PROP_TOKENIZE = "tokenize";
    static final String PROP_SENTENCE_SPLIT = "ssplit";
    static final String PROP_POS_TAG = "pos";
    static final String PROP_LEMMA = "lemma";
    static final String PROP_NER = "ner";
    static final String PROP_PARSE = "parse";
    static final String PROP_CORERENCE = "dcoref";
    static final String PROP_KEY = "annotators";

    static final String TOKEN_ID = "tk_";
    static final String SENT_ID = "s_";
    static final String CONSTITUENT_ID = "c_";
    static final String PS_ID = "ps_";
    static final String DEPENDENCY_ID = "dep_";
    static final String DS_ID = "ds_";
    static final String MENTION_ID = "m_";
    static final String COREF_ID = "coref_";
    static final String NE_ID = "ne_";


    private Properties props = new Properties();
    StanfordCoreNLP snlp = null;

    private String metadata;

    /**
     * Default constructor only tries to load metadata.
     */
    AbstractStanfordCoreNLPWebService() {
        try {
            loadMetadata();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get version from metadata
     */
    String getVersion() {
        return Version.getVersion();
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
        log.info(String.format("Retriveing from cache: %s", key));
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
        input = input.trim();  // remove the whitespace.
        // in case of Json
        Data data;

        try {
            data = Serializer.parse(input, Data.class);
            // Serializer#pase throws JsonParseException if input is not well-formed
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
                cont = new Container((Map) data.getPayload());
                break;
            case Uri.LIF:
                cont = new Container((Map) data.getPayload());
                break;
            case Uri.TEXT:
                cont = new Container();
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
    public abstract String execute(Container json)
            throws StanfordWebServiceException;

    private void loadMetadata() throws IOException {
        // get caller name using reflection
        String serviceName = this.getClass().getName();
        String resName = "/metadata/"+ serviceName +".json";
        log.info("load resources:" + resName);
        InputStream inputStream = this.getClass().getResourceAsStream(resName);

        if (inputStream == null) {
            String message = "Unable to load metadata file for " + this.getClass().getName();
            log.error(message);
            throw new IOException(message);
        } else {
            UTF8Reader reader = new UTF8Reader(inputStream);
            try {
                Scanner s = new Scanner(reader).useDelimiter("\\A");
                String metadataText = s.hasNext() ? s.next() : "";
                this.metadata = (new Data<>(Uri.META,
                        Serializer.parse(metadataText, ServiceMetadata.class))).asPrettyJson();
            } catch (Exception e) {
                String message = "Unable to parse json for " + this.getClass().getName();
                log.error(message, e);
                this.metadata = (new Data<>(Uri.ERROR, message)).asPrettyJson();
            }
            reader.close();
        }
    }

    @Override
    public String getMetadata() {
        return this.metadata;
    }

}

