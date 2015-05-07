package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ICoreference;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.util.List;
import java.util.Map;

public class Coreference extends AbstractStanfordCoreNLPWebService implements
		ICoreference {

	public Coreference() {
		this.init(PROP_TOKENIZE,PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER, PROP_PARSE, PROP_CORERENCE);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {

        String txt = json.getText();
        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.COREF,
                "coref:stanford", this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        Map<Integer, CorefChain> graph = annotation.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        for(Integer id : graph.keySet()) {
            CorefChain c =   graph.get(id);
            System.out.println("ClusterId: " + id);
            JsonObj anncoref = json.newAnnotation(view, Discriminators.Uri.COREF);
            json.setId(anncoref, "coref" + id);
            json.setLabel(anncoref, Discriminators.Uri.COREF);


            CorefChain.CorefMention repre = c.getRepresentativeMention();
            JsonObj annmentionrepr = json.newAnnotation(view, "Mention");
            json.setId(annmentionrepr, "mention" + repre.mentionID);
            json.setStart(annmentionrepr, repre.startIndex);
            json.setEnd(annmentionrepr, repre.endIndex);
            json.setLabel(annmentionrepr, "Mention");

            List<CorefChain.CorefMention> cms = c.getMentionsInTextualOrder();
            JsonArr mentions = new JsonArr();
            for (CorefChain.CorefMention mention : cms) {
                JsonObj annmention = json.newAnnotation(view, "Mention");
                json.setId(annmention, "mention" + mention.mentionID);
                json.setStart(annmention, mention.startIndex);
                json.setEnd(annmention, mention.endIndex);
                json.setLabel(annmention, "Mention");
                json.setWord(annmention, txt.substring(mention.startIndex, mention.endIndex));
                mentions.put("mention" + mention.mentionID);
            }
            System.out.println("");
            json.setFeature(anncoref, "representative",repre.mentionID);
            json.setFeature(anncoref, "mentions", mentions);
        }
        return json.toString();
    }
}
