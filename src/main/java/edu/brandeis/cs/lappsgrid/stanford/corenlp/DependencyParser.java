package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.*;

public class DependencyParser extends AbstractStanfordCoreNLPWebService implements
        IParser {

	public DependencyParser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
	}

    @Override
    public String execute(Container container) throws StanfordWebServiceException {
        String text = container.getText();
        View view = container.newView();
        view.addContains("http://vocab.lappsgrid.org/DependencyStructure", this.getClass().getName(), Version.getVersion());
        view.addContains(Discriminators.Uri.TOKEN , this.getClass().getName(), Version.getVersion());

//        // NLP processing
        Annotation doc = new Annotation(text);
        snlp.annotate(doc);
        Map<String, String> map = new HashMap<String, String>();
//
        List<CoreMap> list = doc.get(SentencesAnnotation.class);
        int cntSent = 0;
        for (CoreMap sent : list) {
            int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            org.lappsgrid.serialization.lif.Annotation ann = view.newAnnotation("dp" + cntSent,
                    "http://vocab.lappsgrid.org/DependencyStructure", start, end);

            ann.addFeature("sentence", sent.toString());
            SemanticGraph graph = sent.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//
            int cntEdge = 0;
            List<Map> dependencies = new ArrayList<>();
            for(SemanticGraphEdge edge:graph.getEdgeSet()) {
                Map dep = new LinkedHashMap();
                dep.put("id", "dep_"+cntSent+"_"+cntEdge++);
                dep.put("label", edge.getRelation().toString());
                Map feats = new LinkedHashMap();
                dep.put("features", feats);
                feats.put("governor", "tk" + cntSent + "_" + edge.getGovernor().index());
                feats.put("governor_word", edge.getGovernor().word());
                feats.put("dependent","tk" + cntSent + "_" + edge.getDependent().index());
                feats.put("dependent_word", edge.getDependent().word());
                
                dependencies.add(dep);
            }
            ann.getFeatures().put("dependencies", dependencies);

            int cntToken = 1;
            for (CoreLabel token : sent.get(CoreAnnotations.TokensAnnotation.class)) {
                 ann = view.newAnnotation("tk" + cntSent + "_" + cntToken++,
                         Discriminators.Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature("pos", token.tag());
                ann.addFeature("word", token.value());
            }
            cntSent ++;
        }

            Data<Container> data = new Data<>(Discriminators.Uri.LIF, container);
            return Serializer.toJson(data);
    }



	@Override
	public String parse(String docs) {
		Annotation annotation = new Annotation(docs);
		snlp.annotate(annotation);

        StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            for(SemanticGraphEdge edge : graph.getEdgeSet())
            {
                IndexedWord dep = edge.getDependent();
                IndexedWord gov = edge.getGovernor();
                GrammaticalRelation relation = edge.getRelation();
                String disc = relation.toString() + "("+gov.word()+"-"+gov.index()+","+dep.word()+"-"+dep.index()+")";
//                System.out.println(disc);
                sb.append(disc+"\n");
            }
		}
		// return null;
		return sb.toString();
	}

}
