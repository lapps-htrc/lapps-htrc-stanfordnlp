package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IPOSTagger;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
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


public class POSTagger extends AbstractStanfordCoreNLPWebService implements
        IPOSTagger {

    public POSTagger() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG);
    }

    @Override
    public String execute(Container container)
            throws StanfordWebServiceException {

        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.POS,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tagger:stanford");
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);

        int sid = 0;
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int tid = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                Annotation a = view.newAnnotation(
                        "tk_" + sid + "_" + tid++, Uri.POS,
                        token.beginPosition(), token.endPosition());
                a.addFeature(Features.Token.PART_OF_SPEECH, token.get(PartOfSpeechAnnotation.class));
                a.addFeature(Features.Token.WORD, token.value());
            }
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }


    @Override
    public String[] tag(String docs) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(docs);
        snlp.annotate(annotation);

        ArrayList<String> list = new ArrayList<String> ();

        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence1 : sentences) {
            for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
                String ps = token.get(PartOfSpeechAnnotation.class);
                list.add(ps);
            }
        }
        // return null;
        return list.toArray(new String[list.size()]);
    }


}
