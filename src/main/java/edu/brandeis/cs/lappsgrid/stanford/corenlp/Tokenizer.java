package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ITokenizer;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.ArrayList;
import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP 3.3.1 Tokenizer",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "token" }
)
public class Tokenizer extends AbstractStanfordCoreNLPWebService implements
        ITokenizer {

    public Tokenizer() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(),getVersion()),
                "tokenizer:stanford");

        // run stanford module
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        int sid = 0;
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                Annotation ann = view.newAnnotation(
                        String.format("%s%d_%d", TOKEN_ID, sid, tid), Uri.TOKEN,
                        token.beginPosition(), token.endPosition());
                tid++;
                ann.getFeatures().put("word", token.value());
            }
            sid++;
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    public String[] tokenize(String text) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);

        ArrayList<String> list = new ArrayList<String> ();

        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence1 : sentences) {
            for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
                // krim 150903: keeping only .value() is useless as it loses all offset info
                list.add(token.value());
            }
        }
        // return null;
        return list.toArray(new String[list.size()]);
    }

}
