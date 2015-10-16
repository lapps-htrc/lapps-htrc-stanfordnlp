package edu.brandeis.cs.lappsgrid.stanford.corenlp;

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
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
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

    // TODO add these strings to vocab
    static final String PSVOCAB = "http://vocab.lappsgrid.org/PhraseStructure";
    static final String CONVOCAB = "http://vocab.lappsgrid.org/Constituent";
    static final String DELIMITER = ":";

    public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();

        // Prepare two containers, one for tokens and one for parse trees
        View tokenized = container.newView();
        tokenized.setId("v1");
        tokenized.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");

        View parsed = container.newView();
        parsed.setId("v2");
        parsed.addContains(PSVOCAB,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "syntacticparser:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        int sid = 0;
        for (CoreMap sent : sents) {

            // first, populate tokenization view
            Map<String, String> tokenIndex = new HashMap<>();
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String tokenId = String.format("tk_%d_%d", sid, tid++);
                tokenIndex.put(token.word(), tokenId);
                tokenized.newAnnotation(tokenId,
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
            }

            // then populate constituents view, using token ID map from above
            int cid = 0;
            Annotation ps = parsed.newAnnotation( "ps" + (sid), PSVOCAB,
                    sent.get(CharacterOffsetBeginAnnotation.class),
                    sent.get(CharacterOffsetEndAnnotation.class));
            Tree root = sent.get(TreeAnnotation.class);
            Queue<Tree> queue = new LinkedList<>();
            queue.add(root);
            List<String> allConstituents = new LinkedList<>();
            int nextNonTerminal = 1;
            while (!queue.isEmpty()) {
                Tree cur = queue.remove();
                if (cur.numChildren() != 0) {
                    String curID = "c_" + cid++;
                    allConstituents.add(curID);
                    String curLabel = cur.label().value();
                    Annotation constituent = parsed.newAnnotation(curID, CONVOCAB);
                    constituent.setLabel(curLabel);
                    ArrayList<String> childrenIDs = new ArrayList<>();

                    for (Tree child : cur.getChildrenAsList()) {
                        queue.add(child);
                        if (child.numChildren() > 0) {
                            childrenIDs.add("c_" + nextNonTerminal++);
                        } else {
                            childrenIDs.add(String.format("%s%s%s",
                                    tokenized.getId(),
                                    DELIMITER,
                                    tokenIndex.get(child.value())));
                        }
                    }
                    constituent.addFeature("children", childrenIDs.toString());
                }
            }
            sid++;
            ps.addFeature(Features.PhraseStructure.CONSTITUENTS,
                    allConstituents.toString());
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
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
