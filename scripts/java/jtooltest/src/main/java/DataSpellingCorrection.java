import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.language.AmericanEnglish;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSpellingCorrection {

    static JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());

    public static void main(String[] args) throws IOException {
        correctTweets();
    }

    private static void correctTweets() throws IOException {
        Stream<List<Tweet>> listStream = Files.walk(Paths.get("labeled-tweets"))
                .peek(System.out::println)
                .filter(p -> !p.toString().equals("labeled-tweets"))
                .filter(p -> !p.toString().equals("labeled-tweets/instructions.txt"))
                .map(p -> readFile(p.toString())
                        .stream()
                        .map(Main::correct)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));

        listStream.forEach(Main::writeToFile);
    }

    private static void writeToFile(List<Tweet> tweets) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> collect = tweets.stream()
                .map(p -> {
                    try {
                        return objectMapper.writeValueAsString(p);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        try {
            PrintWriter printWriter = new PrintWriter(new File("correct-tweets/tweet" + System.currentTimeMillis() + ".json"));
            for (String s : collect) {
                printWriter.println(s);
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Tweet> correct(Tweet tweet) {
        String input = tweet.getMsg();
        try {
            List<RuleMatch> matches = langTool.check(input);
            StringBuilder stringBuilder = new StringBuilder(input);
            for (RuleMatch match : matches) {
                if (!match.getSuggestedReplacements().isEmpty()) {
                    stringBuilder.replace(match.getFromPos(), match.getToPos(), match.getSuggestedReplacements().get(0));
                }
            }
            return Optional.of(new Tweet(stringBuilder.toString(), tweet.getLabel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static List<Tweet> readFile(String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Files.lines(Paths.get(fileName))
                    .map(line -> {
                        try {
                            return objectMapper.readValue(line, Tweet.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}

class Tweet {
    private String msg;
    private double label;

    public Tweet() {
    }

    public Tweet(String msg, double label) {
        this.msg = msg;
        this.label = label;
    }

    public String getMsg() {
        return msg;
    }

    public double getLabel() {
        return label;
    }
}
