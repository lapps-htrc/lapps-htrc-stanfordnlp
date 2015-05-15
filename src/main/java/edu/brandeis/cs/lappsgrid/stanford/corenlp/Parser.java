package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Parser extends AbstractStanfordCoreNLPWebService implements
		IParser {

	public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
	}

    @Override
    public String execute(LIFJsonSerialization json) throws StanfordWebServiceException {
        String txt = json.getText();
        JsonObj view  = json.newView();
        json.newContains(view, "Parser", "parser:stanford", this.getClass().getName() + ":" + Version.getVersion());
        // NLP processing
        Annotation doc = new Annotation(txt);
        snlp.annotate(doc);

        Map<String, String> map = new HashMap<String, String>();
        int cntConst = 0;

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

            json.setStart(ann, start);
            json.setEnd(ann, end);
            json.setId(ann, "ps" + cntSent++);
            ann.put("type", "http://vocab.lappsgrid.org/PhraseStructure");
            Tree root = sent.get(TreeAnnotation.class);
            Queue<Tree> queue = new LinkedList<Tree>();

            JsonArr constituents = new JsonArr();
            json.setSentence(ann, sent.toString());
            json.setFeature(ann, "penntree", root.pennString());
            json.setFeature(ann, "constituents", constituents);

//            Set<Constituent> constituentsets = root.constituents();
//            for(Constituent cons: constituentsets) {
//                System.out.println("start:" + cons.start());
//                System.out.println("end:" + cons.end());
//                System.out.println("value:" + cons.value());
//            }
            queue.add(root);
            while(!queue.isEmpty()) {
                Tree parent = queue.remove();
                JsonObj constituent = new JsonObj();
//                System.out.println("label:" + parent.label().value());
//                System.out.println("value:" + parent.value());
//                System.out.println("pennString:" + parent.pennString());
                constituents.put(constituent);
                String key = parent.pennString();
                String id = map.get(key);
                if(id == null) {
                    id = "cs" + cntConst++;
                    map.put(key, id);
                }
//                for(Constituent cons: constituentsets) {
//                    System.out.print("start:" + cons.start());
//                    System.out.print(" end:" + cons.end());
//                    System.out.println(" value:" + cons.label());
//                }
                System.out.println("-----------------------");

                constituent.put("id", id);
                constituent.put("type", "http://vocab.lappsgrid.org/Constituent");
                constituent.put("label", parent.label().value());
                JsonObj feature = new JsonObj();
                constituent.put("features", feature);
                feature.put("penntree", parent.pennString());
                List<Tree> childlist = parent.getChildrenAsList();
                if (childlist.size() > 0) {
                    JsonArr children = new JsonArr();
                    constituent.put("children", children);
                    for (Tree child : childlist) {
                        String childkey = child.pennString();
                        String childid = map.get(childkey);
                        if(childid == null) {
                            childid = "cs" + cntConst++;
                            map.put(childkey, childid);
                        }
                        children.put(childid);
                        queue.add(child);
                    }
                }
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
