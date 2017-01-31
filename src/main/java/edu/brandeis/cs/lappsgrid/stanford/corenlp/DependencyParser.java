package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.IParser;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.ArrayList;
import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP 3.3.1 Dependency Parser",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "dependency", "dependency-structure", "token" }
)
public class DependencyParser extends AbstractStanfordCoreNLPWebService implements
        IParser {

    public DependencyParser() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_PARSE);
    }

    @Override
    public String execute(Container container) throws StanfordWebServiceException {
        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.DEPENDENCY_STRUCTURE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "dependency-parser:stanford");
        view.addContains(Uri.DEPENDENCY ,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "dependency-parser:stanford");
        view.addContains(Uri.TOKEN ,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:stanford");
        edu.stanford.nlp.pipeline.Annotation doc
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(doc);
        List<CoreMap> list = doc.get(SentencesAnnotation.class);
        int cntSent = 0;
        for (CoreMap sent : list) {
            int start = sent.get(CharacterOffsetBeginAnnotation.class);
            int end = sent.get(CharacterOffsetEndAnnotation.class);
            Annotation ann = view.newAnnotation(DS_ID + cntSent,
                    Uri.DEPENDENCY_STRUCTURE, start, end);

            ann.addFeature("sentence", sent.toString());
            SemanticGraph graph = sent.get(BasicDependenciesAnnotation.class);
            int cntEdge = 0;
            List<String> dependencies = new ArrayList<>();
            for(SemanticGraphEdge edge:graph.getEdgeSet()) {
                String id = String.format("%s%d_%d",
                        DEPENDENCY_ID, cntSent, cntEdge++);
                dependencies.add(id);

                Annotation dependency = view.newAnnotation(id, Uri.DEPENDENCY);
                dependency.setLabel(edge.getRelation().toString());
                // stanford indexing starts from 1, for consistency, we start from 0
                dependency.addFeature("governor",
                        makeTokenId(cntSent, edge.getGovernor().index() - 1));
                dependency.addFeature("governor_word", edge.getGovernor().word());
                dependency.addFeature("dependent",
                        makeTokenId(cntSent, edge.getDependent().index() - 1));
                dependency.addFeature("dependent_word", edge.getDependent().word());

            }
            ann.getFeatures().put("dependencies", dependencies);


            int cntToken = 0;
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                ann = view.newAnnotation(makeTokenId(cntSent, cntToken++),
                        Uri.TOKEN, token.beginPosition(), token.endPosition());
                ann.addFeature("pos", token.tag());
                ann.addFeature("word", token.value());
            }
            cntSent ++;
        }

        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    private String makeTokenId(int sid, int tid) {
        return String.format("%s%d_%d", TOKEN_ID, sid, tid);
    }

    @Override
    public String parse(String docs) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(docs);
        snlp.annotate(annotation);

        StringBuilder sb = new StringBuilder();
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            SemanticGraph graph = sentence.get(BasicDependenciesAnnotation.class);
            for(SemanticGraphEdge edge : graph.getEdgeSet())
            {
                IndexedWord dep = edge.getDependent();
                IndexedWord gov = edge.getGovernor();
                GrammaticalRelation relation = edge.getRelation();
                String disc = relation.toString()
                        + "("+gov.word()+"-"+gov.index()+","
                        +dep.word()+"-"+dep.index()+")";
//                System.out.println(disc);
                sb.append(disc+"\n");
            }
        }
        // return null;
        return sb.toString();
    }

}
