package edu.twitter.service;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spelling Correction service used to correct spelling mistakes in tweet message.
 */
public final class SpellingCorrectionService {
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
    public static String correctSpelling(final String tweetMessage) {
        try {
            final StringBuilder tweetStringBuilder = new StringBuilder();
            final JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
            final List<RuleMatch> matches = langTool.check(tweetMessage)
                    .stream()
                    .filter(l -> !l.getSuggestedReplacements().isEmpty())
                    .sorted(Comparator.comparingInt(RuleMatch::getFromPos))
                    .collect(Collectors.toList());
            int idx = 0;
            for (int i = 0; i < tweetMessage.length(); ) {
                if (idx < matches.size() && i == matches.get(idx).getFromPos()) {
                    i = matches.get(idx).getToPos();
                    tweetStringBuilder.append(matches.get(idx).getSuggestedReplacements().get(0));
                    ++idx;
                } else {
                    tweetStringBuilder.append(tweetMessage.charAt(i));
                    ++i;
                }
            }
            return tweetStringBuilder.toString();
        } catch (final Exception e) {
            LOGGER.warn("error in correcting tweet with message {} {}", tweetMessage, e.getMessage());
        }
        return tweetMessage;
    }
}
