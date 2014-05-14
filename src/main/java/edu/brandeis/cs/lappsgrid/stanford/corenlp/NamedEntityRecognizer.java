package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.util.IDGenerator;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.INamedEntityRecognizer;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.json.JsonNERSerialization;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;
import org.lappsgrid.vocabulary.Metadata;

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
		this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG, PROP_LEMMA, PROP_NER);
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
	public Data execute(Data data) {

        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {

            String jsonstr = data.getPayload();
            JsonNERSerialization json = new JsonNERSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("ner:stanford");

            // NLP processing
            Annotation annotation = new Annotation(json.getTextValue());
            snlp.annotate(annotation);
            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
            for (CoreMap sent : list) {
                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                    String  ner = token.ner();
                    if(ner != null) {
                        JSONObject ann = json.newAnnotationWithType(ner);
                        json.setStart(ann, token.beginPosition());
                        json.setEnd(ann, token.endPosition());
                        json.setWord(ann, token.value());
                        json.setLemma(ann, token.lemma());
                        json.setCategory(ann, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                    }
                }
            }

            return DataFactory.json(json.toString());
        } else if (discriminator == Types.TEXT)
        {
            String text = data.getPayload();
            JsonNERSerialization json = new JsonNERSerialization();
            json.setTextValue(text);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("ner:stanford");

            // NLP processing
            Annotation annotation = new Annotation(json.getTextValue());
            snlp.annotate(annotation);
            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
            for (CoreMap sent : list) {
                for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                    String  ner = token.ner();
                    if(ner != null) {
                        JSONObject ann = json.newAnnotationWithType(ner);
                        json.setStart(ann, token.beginPosition());
                        json.setEnd(ann, token.endPosition());
                        json.setWord(ann, token.value());
                        json.setLemma(ann, token.lemma());
                        json.setCategory(ann, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                    }
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
