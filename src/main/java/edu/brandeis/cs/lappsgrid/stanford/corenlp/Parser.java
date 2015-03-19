package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class Parser extends AbstractStanfordCoreNLPWebService implements
		IParser {

	public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();
        JsonObj view  = json.newView();
        json.newContains(view, "Parser", "parser:stanford", this.getClass().getName() + ":" + VERSION);

        // NLP processing
        Annotation annotation = new Annotation(txt);
        snlp.annotate(annotation);
        List<CoreMap> list = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : list) {
            JsonObj ann = json.newAnnotation(view);
            int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            json.setStart(ann, start);
            json.setEnd(ann, end);
            json.setSentence(ann, sent.toString());
            for (Tree tree : sent.get(TreeAnnotation.class)) {
                StringWriter sw = new StringWriter();
                PrintWriter writer = new PrintWriter(sw);
                tree.printLocalTree(writer);
                json.setFeature(ann, "label", tree.label().value());
                json.setFeature(ann, "pattern", sw.toString());
            }
        }
        return json.toString();
    }
//
//	@Override
//	public Data execute(Data data) {
//
//        long discriminator = data.getDiscriminator();
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("parser:stanford");
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                JSONObject ann = json.newAnnotation();
//                int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                json.setStart(ann, start);
//                json.setEnd(ann, end);
//                json.setSentence(ann, sent.toString());
//                for (Tree tree : sent.get(TreeAnnotation.class)) {
//                    StringWriter sw = new StringWriter();
//                    PrintWriter writer = new PrintWriter(sw);
//                    tree.printLocalTree(writer);
//                    json.setFeature(ann, "label", tree.label().value());
//                    json.setFeature(ann, "pattern", sw.toString());
//                }
//            }
//            return DataFactory.json(json.toString());
//        } else if (discriminator == Types.TEXT) {
//
//            String textvalue = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization();
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("parser:stanford");
//            json.setTextValue(textvalue);
//
//            String pattern = parse(textvalue);
//
//            // NLP processing
//            Annotation annotation = new Annotation(json.getTextValue());
//            snlp.annotate(annotation);
//            List<CoreMap> list = annotation.get(SentencesAnnotation.class);
//            for (CoreMap sent : list) {
//                JSONObject ann = json.newAnnotation();
//                int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                json.setStart(ann, start);
//                json.setEnd(ann, end);
//                json.setSentence(ann, sent.toString());
//                for (Tree tree : sent.get(TreeAnnotation.class)) {
//                    StringWriter sw = new StringWriter();
//                    PrintWriter writer = new PrintWriter(sw);
//                    tree.printLocalTree(writer);
//                    json.setFeature(ann, "label", tree.label().value());
//                    json.setFeature(ann, "pattern", sw.toString());
//                }
//            }
//            return DataFactory.json(json.toString());
//        } else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//    }

	@Override
	public String parse(String docs) {
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);
		
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);  
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence1 : sentences) {
			for (Tree tree : sentence1.get(TreeAnnotation.class)) {				
				tree.printLocalTree(writer);
			}
		}
		// return null;
		return sw.toString();
	}

    @Override
    public String getMetadata() {
        return null;
    }
}
