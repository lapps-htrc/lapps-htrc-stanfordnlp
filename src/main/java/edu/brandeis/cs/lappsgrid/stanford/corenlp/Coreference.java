package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ICoreference;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        json.newContains(view, Discriminators.Uri.TOKEN, "token:stanford",
                this.getClass().getName() + ":" + Version.getVersion());
        json.newContains(view, "http://vocab.lappsgrid.org/Markable", "markable:stanford",
                this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation doc = new Annotation(txt);
        snlp.annotate(doc);
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        try {
//            XMLOutputter.xmlPrint(doc, output, snlp);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new StanfordWebServiceException("XML Print ERROR.",e);
//        }
//        String xmlAnn = new String(output.toByteArray());
//        System.out.println(xmlAnn);

        List<CoreMap> listSent = doc.get(CoreAnnotations.SentencesAnnotation.class);
        int cntSent = 0;
        for (CoreMap sent : listSent) {
            int cntToken = 1;
            for (CoreLabel token : sent.get(CoreAnnotations.TokensAnnotation.class)) {
                JsonObj ann = json.newAnnotation(view);
                json.setId(ann, "tk_" + cntSent + "_" + cntToken++);
                json.setType(ann, Discriminators.Uri.TOKEN);
                json.setStart(ann, token.beginPosition());
                json.setEnd(ann, token.endPosition());
                json.setWord(ann, token.value());
                json.setFeature(ann, "pos", token.tag());
            }
            cntSent ++;
        }

        Map<Integer, CorefChain> corefMap = doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        for(Integer id : corefMap.keySet()) {
            CorefChain coref =   corefMap.get(id);
            List<CorefChain.CorefMention> cms = coref.getMentionsInTextualOrder();
            if(cms.size() <= 1)
                continue;
            JsonArr mentions = new JsonArr();
            for (CorefChain.CorefMention mention : cms) {
                JsonObj ann = json.newAnnotation(view);
                json.setId(ann, "m" + mention.mentionID);
                json.setType(ann, "http://vocab.lappsgrid.org/Markable");
                CoreMap tokens = listSent.get(mention.sentNum - 1);
                int begin = tokens.get(CoreAnnotations.TokensAnnotation.class).get(mention.startIndex - 1).beginPosition();
                int end = tokens.get(CoreAnnotations.TokensAnnotation.class).get(mention.endIndex - 2).endPosition();
                json.setStart(ann, begin);
                json.setEnd(ann, end);
                json.setFeature(ann, "words", txt.substring(begin, end));
                json.setFeature(ann,"sentenceIndex", mention.sentNum - 1);
                json.setFeature(ann,"targetStart", mention.startIndex);
                json.setFeature(ann,"targetEnd", mention.endIndex);
                JsonArr targets = new JsonArr();
                json.setFeature(ann, "targets",targets);
                for(int m = mention.startIndex; m < mention.endIndex; m ++)
                    targets.put("tk_" + mention.sentNum + "_" + m);
                mentions.put("m" + mention.mentionID);
            }
            JsonObj anncoref = json.newAnnotation(view);
            json.setId(anncoref, "coref" + id);
            json.setType(anncoref, Discriminators.Uri.COREF);
            CorefChain.CorefMention repre = coref.getRepresentativeMention();
            json.setFeature(anncoref, "representative","m" +repre.mentionID);
            json.setFeature(anncoref, "mentions", mentions);
        }
        return json.toString();
    }
}
