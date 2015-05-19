package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IPOSTagger;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.util.ArrayList;
import java.util.List;


public class POSTagger extends AbstractStanfordCoreNLPWebService implements
		IPOSTagger {

	public POSTagger() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();
        JsonObj view = json.newView();

        json.newContains(view, Discriminators.Uri.POS,
                "tagger:stanford", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");
        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        int cntsent = 0;
        for (CoreMap sent : list) {
            int cnttok = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                JsonObj ann = json.newAnnotation(view);
                json.setId(ann, "tk_"+cntsent+"_"+cnttok++);
                json.setStart(ann, token.beginPosition());
                json.setEnd(ann, token.endPosition());
                json.setWord(ann, token.value());
                json.setFeature(ann,"pos", token.get(PartOfSpeechAnnotation.class));
                json.setLabel(ann, Discriminators.Uri.POS);
            }
            cntsent++;
        }
        return json.toString();
    }


	@Override
	public String[] tag(String docs) {
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);
		
		ArrayList<String> list = new ArrayList<String> ();
		
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence1 : sentences) {
			for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
				String ps = token.get(PartOfSpeechAnnotation.class);
				list.add(ps);
			}
		}
		// return null;
		return list.toArray(new String[list.size()]);
	}


}
