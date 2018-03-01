package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.stanford.StanfordWebServiceException;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 *
 * @author Chunqi SHI (shicq@cs.brandeis.edu)
 * @author Keigh Rim (krim@brandeis.edu)
 * @since 2014-03-25
 *
 */
@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP 3.3.1 Sentence Splitter",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "sentence" }
)
public class Splitter extends AbstractStanfordCoreNLPWebService {

    public Splitter() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {

        String text = container.getText();
        View view = null;
        try {
            view = container.newView();
        } catch (LifException ignored) {
            // this never raises as newView() will check for duplicate view-id internally
        }
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
            Annotation ann = view.newAnnotation(SENT_ID + (++id), Uri.SENTENCE, start, end);
            ann.getFeatures().put("sentence", sent.toString());
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

}
