package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.xerces.impl.io.UTF8Reader;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

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

	protected static final Logger logger = LoggerFactory
			.getLogger(AbstractStanfordCoreNLPWebService.class);

    static protected ConcurrentHashMap<String, StanfordCoreNLP> cache =
            new ConcurrentHashMap<String, StanfordCoreNLP>();

	public static final String PROP_TOKENIZE = "tokenize";
	public static final String PROP_SENTENCE_SPLIT = "ssplit";
	public static final String PROP_POS_TAG = "pos";
	public static final String PROP_LEMMA = "lemma";
	public static final String PROP_NER = "ner";
	public static final String PROP_PARSE = "parse";
	public static final String PROP_CORERENCE = "dcoref";
    public static final String PROP_KEY = "annotators";

	protected Properties props = new Properties();
	StanfordCoreNLP snlp = null;

    private String metadata;

    public AbstractStanfordCoreNLPWebService() {
        try {
            loadMetadata();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    protected static void putFeature(Map mapFeature, String key, Object obj) {
        if (key != null && obj != null) {
            mapFeature.put(key, obj.toString());
        }
    }

    protected void init(String ... tools) {
        props.clear();

        StringBuilder sb = new StringBuilder();
        for(String tool: tools) {
            sb.append(tool).append(" ");
        }
        props.put(PROP_KEY, sb.toString().trim());
        snlp = getCached(props);
    }

	protected void init(String toolList) {
		props.clear();
		props.put(PROP_KEY, toolList);
		snlp = getCached(props);
	}


    protected StanfordCoreNLP getCached (Properties props) {
        String key = props.getProperty(PROP_KEY);
        System.out.println("-----------------");
        System.out.println(key);
        StanfordCoreNLP val = cache.get(key);
        if (val == null) {
            val = new StanfordCoreNLP(props);
            cache.put(key, val);
        }
        return val;
    }



    @Override
    public String execute(String s) {
        return null;
    }
        /*
        LIFJsonSerialization json = null;
        try{
            s = s.trim();
            if (s.startsWith("{") && s.endsWith("}")) {
                json = new LIFJsonSerialization(s);
                if (json.getDiscriminator().equals(Discriminators.Uri.ERROR)) {
                    return json.toString();
                }
            } else {
                json = new LIFJsonSerialization();
                json.setText(s);
            }
            return execute(json);
        }catch(Throwable th) {
            json = new LIFJsonSerialization();
            StringWriter sw = new StringWriter();
            th.printStackTrace( new PrintWriter(sw));
            json.setError(th.toString(), sw.toString());
            System.err.println(sw.toString());
            return json.toString();
        }
    }


    public abstract String execute(LIFJsonSerialization json) throws StanfordWebServiceException;
    */

    public void loadMetadata() throws IOException {
        // get caller name using reflection
        String serviceName = this.getClass().getName();
        String resName = "/metadata/"+ serviceName +".json";
        logger.info("load resources:" + resName);
        InputStream inputStream = this.getClass().getResourceAsStream(resName);

        if (inputStream == null) {
            String message = "Unable to load metadata file for " + this.getClass().getName();
            logger.error(message);
            throw new IOException(message);
        } else {
            UTF8Reader reader = new UTF8Reader(inputStream);
            try {
                Scanner s = new Scanner(reader).useDelimiter("\\A");
                String metadataText = s.hasNext() ? s.next() : "";
                this.metadata = (new Data<>(Discriminators.Uri.META,
                        Serializer.parse(metadataText, ServiceMetadata.class))).asPrettyJson();
            } catch (Exception e) {
                String message = "Unable to parse json for " + this.getClass().getName();
                logger.error(message, e);
                this.metadata = (new Data<>(Discriminators.Uri.ERROR, message)).asPrettyJson();
            }
            reader.close();
        }
    }

    @Override
    public String getMetadata() {
        return this.metadata;
    }
}

