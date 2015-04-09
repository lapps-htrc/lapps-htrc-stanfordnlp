package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ISplitter;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.util.ArrayList;
import java.util.List;

public class Splitter extends AbstractStanfordCoreNLPWebService implements
		ISplitter {

	public Splitter() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();

        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.SENTENCE,
                "splitter:stanford", this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);

        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : list) {
            JsonObj jsonann = json.newAnnotation(view);
            int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            json.setStart(jsonann, start);
            json.setEnd(jsonann, end);
            json.setSentence(jsonann, sent.toString());
            json.setLabel(jsonann, Discriminators.Uri.SENTENCE);
        }
        return json.toString();
    }

	@Override
	public String[] split(String docs) {
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);
		
		ArrayList<String> list = new ArrayList<String> ();
		
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence1 : sentences) {
			list.add(sentence1.toString());
		}
		return list.toArray(new String[list.size()]);
	}
}
