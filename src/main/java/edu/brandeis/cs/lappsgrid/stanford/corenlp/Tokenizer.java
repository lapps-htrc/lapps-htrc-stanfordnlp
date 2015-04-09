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
        for (CoreMap sent : list) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                JsonObj ann = json.newAnnotation(view);
                json.setLabel(ann, Discriminators.Uri.TOKEN);
                json.setStart(ann, token.beginPosition());
                json.setEnd(ann, token.endPosition());
                json.setWord(ann, token.value());
            }
        }
        return json.toString();
    }

//	@Override
//	public Data execute(Data data) {
//        long discriminator = data.getDiscriminator();
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonTokenizerSerialization json = new JsonTokenizerSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tokenizer:stanford");
//
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
//                }
//            }
//            return DataFactory.json(json.toString());
//
//        } else if (discriminator == Types.TEXT) {
//            String text = data.getPayload();
//            JsonTokenizerSerialization json = new JsonTokenizerSerialization();
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tokenizer:stanford");
//            json.setTextValue(text);
//            // NLP processing
//            Annotation annotation = new Annotation(text);
//            snlp.annotate(annotation);
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
//                    JSONObject ann = json.newAnnotation();
//                    json.setStart(ann, token.beginPosition());
//                    json.setEnd(ann, token.endPosition());
//                    json.setWord(ann, token.value());
//                    json.setLemma(ann, token.lemma());
//                }
//            }
//            return DataFactory.json(json.toString());
//        } else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//	}





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
