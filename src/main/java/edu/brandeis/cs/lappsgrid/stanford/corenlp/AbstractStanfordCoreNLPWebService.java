package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.io.IOUtils;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
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

	public AbstractStanfordCoreNLPWebService() {
//		this.init("tokenize, ssplit, pos, lemma, ner, parse, dcoref");
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
        LIFJsonSerialization json = null;
        try{
            s = s.trim();
            if (s.startsWith("{") && s.endsWith("}")) {
                json = new LIFJsonSerialization();
                json.setDiscriminator(s);
                json.setDiscriminator(Discriminators.Uri.TEXT);
            } else {
                json = new LIFJsonSerialization(s);
                if (json.getDiscriminator().equals(Discriminators.Uri.ERROR)) {
                    return json.toString();
                }
            }
            return execute(json);
        }catch(Throwable th) {
            json = new LIFJsonSerialization();
            StringWriter sw = new StringWriter();
            th.printStackTrace( new PrintWriter(sw));
            json.setError(th.getMessage(), sw.toString());
            return json.toString();
        }
    }

    public abstract String execute(LIFJsonSerialization json) throws StanfordWebServiceException;



    @Override
    public String getMetadata() {
        // get caller name using reflection
        String name = this.getClass().getName();
        //
        String resName = "/metadata/"+ name +".json";
//        System.out.println("load resources:" + resName);
        logger.info("load resources:" + resName);
        try {
            String meta = IOUtils.toString(this.getClass().getResourceAsStream(resName));
            JsonObj json = new JsonObj();
            json.put("discriminator", Discriminators.Uri.META);
            json.put("payload", new JsonObj(meta));
            return json.toString();
        }catch (Throwable th) {
            JsonObj json = new JsonObj();
            json.put("discriminator", Discriminators.Uri.ERROR);
            JsonObj error = new JsonObj();
            error.put("class", name);
            error.put("error", "NOT EXIST: "+resName);
            error.put("message", th.getMessage());
            StringWriter sw = new StringWriter();
            th.printStackTrace( new PrintWriter(sw));
            error.put("stacktrace", sw.toString());
            json.put("payload", error);
            return json.toString();
        }
    }
}
