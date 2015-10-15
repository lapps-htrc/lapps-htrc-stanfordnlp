package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.lappsgrid.discriminator.Discriminators.Uri;

public class Parser extends AbstractStanfordCoreNLPWebService implements
        IParser {
    static final String PSVOCAB = "http://vocab.lappsgrid.org/PhraseStructure";

    public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();

        // Prepare two containers, one for tokens and one for parse trees
        View tokens = container.newView();
        tokens.setId("v1");
        tokens.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");
        View parses = container.newView();
        parses.setId("v2");
        parses.addContains(PSVOCAB,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "syntacticparser:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        int sid = 0;
        int cid = 0;
        Map<String , String > map = new HashMap<>();
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        for (CoreMap sent : sents) {
            // populate tokens container
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                tokens.newAnnotation(String.format("tk_%d_%d", sid, tid), Uri.TOKEN,
                        token.beginPosition(), token.endPosition());
                tid++;
            }

            Annotation a = parses.newAnnotation(
                    "ps" + (sid), PSVOCAB,
                    sent.get(CharacterOffsetBeginAnnotation.class),
                    sent.get(CharacterOffsetEndAnnotation.class));
            Tree root = sent.get(TreeAnnotation.class);
            Queue<Tree> nodeQueue = new LinkedList<>();
            IntPair pair = root.getSpan();

            List<String> allConstituents = new LinkedList<>();

//            a.addFeature(Features.Constituent.START, String.valueOf(pair.elems()[0]));
//            a.addFeature(Features.Constituent.END, String.valueOf(pair.elems()[1]));
            // TODO 150903 add pennString as well, somehow
            // TODO continue from here to queue/deque children and put children names to parents' list of children

            while (!nodeQueue.isEmpty()) {
                Tree parent = nodeQueue.remove();
            }


            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String ner = token.ner();
                if (ner != null && !ner.equalsIgnoreCase("O")) {
                    Annotation a = view.newAnnotation(
                            "ne" + (++sid), Uri.??? token.beginPosition(), token.endPosition());
                    a.addFeature(Features.Token.WORD, token.value());
                    a.addFeature(Features.Token.NER, ner);
                }
            }
            sid++;
            a.addFeature(Features.PhraseStructure.CONSTITUENTS, allConstituents.toString());
        }

        // TODO 150903 LIF? JSONLD?
//        Data<Container> data = new Data<>(Uri.LIF, container);
        Data<Container> data = new Data<>(Uri.JSON_LD, container);
        return Serializer.toJson(data);
    }


    @Override
    public String parse(String docs) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(docs);
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
