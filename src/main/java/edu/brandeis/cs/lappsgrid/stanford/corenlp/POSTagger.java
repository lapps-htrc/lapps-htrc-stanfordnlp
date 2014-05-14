package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.util.IDGenerator;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IPOSTagger;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.json.JsonTaggerSerialization;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;
import org.lappsgrid.vocabulary.Metadata;

import org.json.JSONObject;

public class POSTagger extends AbstractStanfordCoreNLPWebService implements
		IPOSTagger {

	public POSTagger() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG);
	}

	@Override
	public long[] requires() {
		return TYPES_REQUIRES;
	}

	@Override
	public long[] produces() {
		return TYPES_PRODUCES;
	}

	
	@Override
	public Data configure(Data arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data execute(Data data) {


        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            String jsonstr = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("tagger:stanford");

            // NLP processing
            Annotation annotation = new Annotation(json.getTextValue());
            snlp.annotate(annotation);
            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
            for (CoreMap sent : list) {
                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                    JSONObject ann = json.newAnnotation();
                    json.setStart(ann, token.beginPosition());
                    json.setEnd(ann, token.endPosition());
                    json.setWord(ann, token.value());
                    json.setLemma(ann, token.lemma());
                    json.setCategory(ann, token.get(PartOfSpeechAnnotation.class));
                }
            }
            return DataFactory.json(json.toString());
        } else  if (discriminator == Types.TEXT) {
            String textvalue = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization();
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("tagger:stanford");
            json.setTextValue(textvalue);

            // NLP processing
            Annotation annotation = new Annotation(json.getTextValue());
            snlp.annotate(annotation);
            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
            for (CoreMap sent : list) {
                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                    JSONObject ann = json.newAnnotation();
                    json.setStart(ann, token.beginPosition());
                    json.setEnd(ann, token.endPosition());
                    json.setWord(ann, token.value());
                    json.setLemma(ann, token.lemma());
                    json.setCategory(ann, token.get(PartOfSpeechAnnotation.class));
                }
            }

            return DataFactory.json(json.toString());

        } else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
        }
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
