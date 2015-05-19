package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ITokenizer;
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

public class Tokenizer extends AbstractStanfordCoreNLPWebService implements
		ITokenizer {

	public Tokenizer() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();
        json.setDiscriminator(Discriminators.Uri.JSON_LD);
        JsonObj view = json.newView();

        json.newContains(view, Discriminators.Uri.TOKEN,
                "tokenizer:stanford", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");
        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        int cntsent = 0;
        for (CoreMap sent : list) {
            int cnttk = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                JsonObj ann = json.newAnnotation(view);
                json.setId(ann, "tk_"+cntsent+"_"+cnttk++);
                json.setType(ann, Discriminators.Uri.TOKEN);
                json.setStart(ann, token.beginPosition());
                json.setEnd(ann, token.endPosition());
                json.setWord(ann, token.value());
            }
            cntsent++;
        }
        return json.toString();
    }



	@Override
	public String[] tokenize(String docs) {
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);
		
		ArrayList<String> list = new ArrayList<String> ();
		
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence1 : sentences) {
			for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {				
				list.add(token.value());
			}
		}
		// return null;
		return list.toArray(new String[list.size()]);
	}

}
