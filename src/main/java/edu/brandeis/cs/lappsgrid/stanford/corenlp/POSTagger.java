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

        json.newContains(view, Discriminators.Uri.TOKEN,
                "tagger:stanford", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");

        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : list) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                JsonObj ann = json.newAnnotation(view);
                json.setStart(ann, token.beginPosition());
                json.setEnd(ann, token.endPosition());
                json.setWord(ann, token.value());
                json.setLemma(ann, token.lemma());
                json.setCategory(ann, token.get(PartOfSpeechAnnotation.class));
            }
        }
        return json.toString();
    }


//	@Override
//	public Data execute(Data data) {
//
//
//        long discriminator = data.getDiscriminator();
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tagger:stanford");
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
//                    JSONObject ann = json.newAnnotation();
//                    json.setStart(ann, token.beginPosition());
//                    json.setEnd(ann, token.endPosition());
//                    json.setWord(ann, token.value());
//                    json.setLemma(ann, token.lemma());
//                    json.setCategory(ann, token.get(PartOfSpeechAnnotation.class));
//                }
//            }
//            return DataFactory.json(json.toString());
//        } else  if (discriminator == Types.TEXT) {
//            String textvalue = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization();
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tagger:stanford");
//            json.setTextValue(textvalue);
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
//                    JSONObject ann = json.newAnnotation();
//                    json.setStart(ann, token.beginPosition());
//                    json.setEnd(ann, token.endPosition());
//                    json.setWord(ann, token.value());
//                    json.setLemma(ann, token.lemma());
//                    json.setCategory(ann, token.get(PartOfSpeechAnnotation.class));
//                }
//            }
//
//            return DataFactory.json(json.toString());
//
//        } else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//	}

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
