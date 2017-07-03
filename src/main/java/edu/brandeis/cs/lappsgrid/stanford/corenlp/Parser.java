package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.stanford.nlp.ling.CoreAnnotations;
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

import java.util.*;

import static org.lappsgrid.discriminator.Discriminators.Uri;
import static org.lappsgrid.vocabulary.Features.PhraseStructure;
import static org.lappsgrid.vocabulary.Features.Token;

/**
 *
 * @author Chunqi SHI (shicq@cs.brandeis.edu)
 * @author Keigh Rim (krim@brandeis.edu)
 * @since 2014-03-25
 *
 */
@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP 3.3.1 Phrase Structure Parser",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "constituent", "token", "phrase-structure" }
)
public class Parser extends AbstractStanfordCoreNLPWebService {


    public Parser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();

        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");
        view.addContains(Uri.PHRASE_STRUCTURE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "syntacticparser:stanford");
        view.addContains(Uri.CONSTITUENT,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "syntacticparser:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        int sid = 0;
        for (CoreMap sent : sents) {

            Map<String, String> childToParent = new HashMap<>();

            // first, populate tokens
            Map<String, String> tokenIndex = new HashMap<>();
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String tokenId = String.format("%s%d_%d", TOKEN_ID, sid, tid++);
                tokenIndex.put(token.word(), tokenId);
                Annotation ann = view.newAnnotation(tokenId,
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature(Token.POS, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                ann.addFeature("word", token.value());
            }

            // then populate constituents.
            // constituents indexed left-right breadth-first
            // leaves are indexed by stanford (off by 1 from tokenIDs from above)
            int cid = 0;
            Annotation ps = view.newAnnotation(PS_ID + sid, Uri.PHRASE_STRUCTURE,
                    sent.get(CharacterOffsetBeginAnnotation.class),
                    sent.get(CharacterOffsetEndAnnotation.class));
            Tree root = sent.get(TreeAnnotation.class);
            root.indexLeaves(true);
            Queue<Tree> queue = new LinkedList<>();
            queue.add(root);
            List<String> allConstituents = new LinkedList<>();
            int nextNonTerminal = 1;
            while (!queue.isEmpty()) {
                Tree cur = queue.remove();
                if (cur.numChildren() != 0) {
                    String curID = String.format(
                            "%s%d_%d", CONSTITUENT_ID, sid, cid++);
                    allConstituents.add(curID);
                    String curLabel = cur.label().value();
                    Annotation constituent = view.newAnnotation(curID, Uri.CONSTITUENT);
                    constituent.setLabel(curLabel);
                    ArrayList<String> childrenIDs = new ArrayList<>();

                    for (Tree child : cur.getChildrenAsList()) {
                        queue.add(child);
                        String childID;
                        if (child.numChildren() > 0) {
                            childID = String.format("%s%d_%d", CONSTITUENT_ID, sid, nextNonTerminal++);
                        } else {
                            childID = String.format("%s%d_%d", TOKEN_ID, sid,
                                    ((CoreLabel) child.label()).index() - 1);
                        }
                        childToParent.put(childID, curID);
                        childrenIDs.add(childID);
                    }
                    constituent.getFeatures().put(Features.Constituent.CHILDREN, childrenIDs);
                    constituent.getFeatures().put(Features.Constituent.PARENT, childToParent.get(curID));
                }
            }
            sid++;
            ps.getFeatures().put("sentence", sent.toString());
            ps.getFeatures().put("penntree", root.pennString());
            ps.getFeatures().put(PhraseStructure.CONSTITUENTS,
                    allConstituents);
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

}
