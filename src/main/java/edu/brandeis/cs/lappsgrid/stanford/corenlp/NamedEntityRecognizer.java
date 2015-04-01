package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.INamedEntityRecognizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.util.List;

/**
 * 
 * The Language Application Grid: A Framework for Rapid Adaptation and Reuse
 * <p>
 * Lapps Grid project TODO.
 * <p>
 * @author Chunqi SHI (shicq@cs.brandeis.edu) <br> Jan 31, 2014 </br>
 *
 */
public class NamedEntityRecognizer extends AbstractStanfordCoreNLPWebService
		implements INamedEntityRecognizer {


	public NamedEntityRecognizer() {
		this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {

        String txt = json.getText();
        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.NE,
                "ner:stanford", this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : list) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String  ner = token.ner();
                if(ner != null && !ner.equalsIgnoreCase("O")) {
                    JsonObj ann = json.newAnnotation(view, Discriminators.Uri.NE);
                    json.setStart(ann, token.beginPosition());
                    json.setEnd(ann, token.endPosition());
                    json.setWord(ann, token.value());
                    json.setLemma(ann, token.lemma());
                    json.setCategory(ann, ner);
                }
            }
        }
        return json.toString();
    }

	@Override
	public String find(String docs) {		
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);
		
		StringBuffer sb = new StringBuffer();
		
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence1 : sentences) {
			for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
				String ne = token.get(NamedEntityTagAnnotation.class);
				if ( ne.equalsIgnoreCase("O") ){
					sb.append(token.value());	
				}
				else {
					sb.append("<").append(ne).append(">");
					sb.append(token.value());
					sb.append("</").append(ne).append(">");
				}
				sb.append(" ");
			}
		}
		// return null;
		return sb.substring(0, sb.length() - 1);
	}

}
