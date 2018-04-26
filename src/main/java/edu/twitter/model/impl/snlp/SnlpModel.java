package edu.twitter.model.impl.snlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.twitter.model.Label;
import edu.twitter.model.api.GenericModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

/**
 * Stanford nlp model.
 */
public class SnlpModel implements GenericModel {
    private static final String NAME = "SNLP";
    private static final Logger LOGGER = LoggerFactory.getLogger(SnlpModel.class);

    private final StanfordCoreNLP pipeline;

    /**
     * Constructor.
     */
    SnlpModel() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Label getLabel(final String tweetText) {
        //final String filteredTweet = TweetTextFilter$.MODULE$.filterTweet(tweetText).replaceAll("\\.", ",");
        final String filteredTweet = tweetText.replaceAll("\\.", ",");
        final Annotation annotation = pipeline.process(filteredTweet);
        final Optional<Label> label = annotation.get(CoreAnnotations.SentencesAnnotation.class)
                .stream()
                .map(this::transformCoreMap)
                .findFirst();
        if (!label.isPresent()) {
            LOGGER.warn("SNLP didn't return any sentiment for message={}", tweetText);
            throw new RuntimeException("Empty sentiment");
        }
        return label.get();
    }

    /**
     * transform core map to a label.
     *
     * @param coreMap target core map for transformation.
     * @return label.
     */
    private Label transformCoreMap(final CoreMap coreMap) {
        Tree tree = coreMap.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
        int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
        LOGGER.info("sentence={}, sentiment={}", coreMap, sentiment);
        if (sentiment < 2) {
            return Label.SAD;
        } else if (sentiment > 2) {
            return Label.HAPPY;
        } else {
            LOGGER.warn("Neutral tweet");
            throw new RuntimeException("Can't handle neutral tweets");
        }
    }
}
