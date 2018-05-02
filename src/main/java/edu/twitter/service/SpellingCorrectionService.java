package edu.twitter.service;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;

/**
 * Spelling Correction service used to correct spelling mistakes in tweet message.
 */
public final class SpellingCorrectionService {


    private static final JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellingCorrectionService.class);

    /**
     * constructor.
     */
    private SpellingCorrectionService() {
    }


    /**
     * correct spelling mistakes in tweet.
     *
     * @param tweetMessage to be corrected.
     * @return tweetMessage after correcting spelling.
     */
    public static Option<String> correctSpelling(final String tweetMessage) {
        try {
            final StringBuilder tweetStringBuilder = new StringBuilder(tweetMessage);
            langTool.check(tweetMessage)
                    .stream()
                    .filter(matcher -> !matcher.getSuggestedReplacements().isEmpty())
                    .forEach(matcher ->
                            tweetStringBuilder.replace(matcher.getFromPos(),
                                    matcher.getToPos(), matcher.getSuggestedReplacements().get(0)));
            return Option.apply(tweetStringBuilder.toString());
        } catch (final Exception e) {
            LOGGER.warn("error in correcting tweet with message {} {}", tweetMessage, e.getMessage());
        }
        return Option.empty();
    }
}
