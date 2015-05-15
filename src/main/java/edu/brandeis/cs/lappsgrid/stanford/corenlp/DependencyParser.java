package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.AbstractStanfordCoreNLPWebService;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class DependencyParser extends AbstractStanfordCoreNLPWebService implements
        IParser {

	public DependencyParser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();
        JsonObj view  = json.newView();
        json.newContains(view, "DependencyParser", "parser:stanford", this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation doc = new Annotation(txt);
        snlp.annotate(doc);
        Map<String, String> map = new HashMap<String, String>();

//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        try {
//            XMLOutputter.xmlPrint(doc, output, snlp);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new StanfordWebServiceException("XML Print ERROR.",e);
//        }
//        String xmlAnn = new String(output.toByteArray());
//        System.out.println(xmlAnn);

        List<CoreMap> list = doc.get(SentencesAnnotation.class);
        int cntSent = 0;
        for (CoreMap sent : list) {
            JsonObj ann = json.newAnnotation(view);
            int start = sent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            json.setId(ann, "dp" + cntSent++);
            json.setStart(ann, start);
            json.setEnd(ann, end);
            ann.put("type", "http://vocab.lappsgrid.org/DependencyStructure");


            JsonArr dependencies = new JsonArr();
            json.setSentence(ann, sent.toString());
            json.setFeature(ann, "dependencies", dependencies);

            SemanticGraph graph = sent.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);

            int cntEdge = 0;
            for(SemanticGraphEdge edge:graph.getEdgeSet()) {
                JsonObj dep = new JsonObj();
                dependencies.put(dep);
                dep.put("id", "dep"+cntEdge++);
                dep.put("type", "http://vocab.lappsgrid.org/Constituent");
                dep.put("label", edge.getRelation().toPrettyString());
                JsonObj feats = new JsonObj();
                dep.put("features", feats);
                feats.put("governor", edge.getGovernor().index());
                feats.put("dependent",edge.getDependent().index());
            }
        }
        return json.toString();
    }


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

}
