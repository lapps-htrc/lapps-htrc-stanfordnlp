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
        }
        return json.toString();
    }


//    @Override
//	public Data execute(Data data) {
//        long discriminator = data.getDiscriminator();
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonSplitterSerialization json = new JsonSplitterSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("splitter:stanford");
//
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//
//
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                JSONObject jsonann = json.newAnnotation();
//                int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                json.setStart(jsonann, start);
//                json.setEnd(jsonann, end);
//                json.setSentence(jsonann, sent.toString());
//            }
//            return DataFactory.json(json.toString());
//
//        } else if (discriminator == Types.TEXT)
//        {
//            String text = data.getPayload();
//            JsonSplitterSerialization json = new JsonSplitterSerialization();
//            json.setTextValue(text);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("splitter:stanford");
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//
//
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                JSONObject jsonann = json.newAnnotation();
//                int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                json.setStart(jsonann, start);
//                json.setEnd(jsonann, end);
//                json.setSentence(jsonann, sent.toString());
//            }
//
//            return DataFactory.json(json.toString());
//        }
//        else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//
//	}

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
