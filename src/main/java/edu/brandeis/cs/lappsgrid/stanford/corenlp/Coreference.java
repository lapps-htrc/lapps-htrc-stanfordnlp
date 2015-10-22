package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ICoreference;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Coreference extends AbstractStanfordCoreNLPWebService implements
        ICoreference {

    public Coreference() {
        this.init(PROP_TOKENIZE,PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER, PROP_PARSE, PROP_CORERENCE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();

        // Prepare two containers, one for tokens and one for coreference chain
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");

        view.addContains(Uri.COREF,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "coreference:stanford");

        view.addContains(Uri.MARKABLE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "markable:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);

        // first iteration to populate token annotations
        int sid = 0;
        for (CoreMap sent : sents) {

            // first, populate tokenization view
            Map<String, String> tokenIndex = new HashMap<>();
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String tokenId = String.format("tk_%d_%d", sid, tid++);
                tokenIndex.put(token.word(), tokenId);
                newAnnotation(view, tokenId,
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
            }
            sid++;
        }
        // next iteration to populate mentions and markables

        Map<Integer, CorefChain> corefs = annotation.get(CorefChainAnnotation.class);
        for(Integer corefId : corefs.keySet()) {
            CorefChain coref =   corefs.get(corefId);
            List<CorefMention> mentions = coref.getMentionsInTextualOrder();
            if(mentions.size() <= 1)
                continue;
            ArrayList<String> mentionIds = new ArrayList<>();
            for (CorefChain.CorefMention mention : mentions) {
                CoreMap sent = sents.get(mention.sentNum - 1);
                List<CoreLabel> tokens = sent.get(TokensAnnotation.class);
                int mBegin = tokens.get(mention.startIndex - 1).beginPosition();
                int mEnd = tokens.get(mention.endIndex - 2).endPosition();
                Annotation mentionAnn = newAnnotation(view, "m_" + mention.mentionID,
                        Uri.MARKABLE, mBegin, mEnd);
                mentionAnn.addFeature("words", text.substring(mBegin, mEnd));
                mentionAnn.addFeature("sentenceIndex", Integer.toString(mention.sentNum - 1));
                ArrayList<String> targets = new ArrayList<>();
                for (int m = mention.startIndex; m < mention.endIndex; m++)
                    // stanford idx starts from 1, need to subtract 1 for each index
                    targets.add("tk_" + (mention.sentNum - 1) + "_" + (m - 1));
                mentionAnn.getFeatures().put("targets", targets);
                mentionIds.add("m_" + mention.mentionID);

            }

            // TODO 151017 current corefId will be the same as mentionID of representative,
            // should we use incremental ID starting from 0 (or 1) ?
            Annotation chain = newAnnotation(view, "coref_" + corefId, Uri.COREF);
            chain.addFeature("representative",
                    "m_" + coref.getRepresentativeMention().mentionID);
            chain.getFeatures().put("mentions", mentionIds);
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
}
