package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.ISplitter;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
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

public class Splitter extends AbstractStanfordCoreNLPWebService implements
        ISplitter {

    public Splitter() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.SENTENCE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "splitter:stanford");
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        int id = -1;
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            int start = sent.get(CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CharacterOffsetEndAnnotation.class);
            newAnnotation(view, SENT_ID + (++id), Uri.SENTENCE, start, end);
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }


    @Override
    public String[] split(String docs) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(docs);
        snlp.annotate(annotation);

        ArrayList<String> list = new ArrayList<String> ();

        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence1 : sentences) {
            list.add(sentence1.toString());
        }
        return list.toArray(new String[list.size()]);
    }
}
