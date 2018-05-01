package edu.twitter.service;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;
import java.util.List;

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
    public static Option<String> correctSpelling(String tweetMessage) {
        try {
            List<RuleMatch> matches = langTool.check(tweetMessage);
            StringBuilder tweetStringBuilder = new StringBuilder(tweetMessage);
            for (RuleMatch match : matches) {
                if (!match.getSuggestedReplacements().isEmpty()) {
                    tweetStringBuilder.replace(match.getFromPos(), match.getToPos(), match.getSuggestedReplacements().get(0));
                }
            }
            return Option.apply(tweetStringBuilder.toString());
        } catch (IOException e) {
            LOGGER.warn("error in correcting tweet with message {} {}", tweetMessage, e.getMessage());
        }
        return Option.empty();
    }
}
