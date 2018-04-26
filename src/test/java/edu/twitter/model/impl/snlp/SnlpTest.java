package edu.twitter.model.impl.snlp;

import edu.twitter.model.Label;
import edu.twitter.model.api.GenericModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SnlpTest {

    @Test
    public void SnlpHappyTest() {
        final String tweetText = "I'm happy";
        final GenericModel model = new SnlpBuilder().build();
        final Label label = model.getLabel(tweetText);
        assertEquals(label, Label.HAPPY);
    }

    @Test
    public void SnlpSadTest() {
        final String tweetText = "John is bad";
        final GenericModel model = new SnlpBuilder().build();
        final Label label = model.getLabel(tweetText);
        assertEquals(label, Label.SAD);
    }

    @Test
    public void SnlpShouldHandleMultipleSentences() {
        try {
            final String tweetText = "I'm happy. Yesterday I was sad";
            final GenericModel model = new SnlpBuilder().build();
            model.getLabel(tweetText);
        } catch (final Exception e) {
            fail("Exception wasn't expected");
        }
    }
}
