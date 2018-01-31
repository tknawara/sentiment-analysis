import json
import re
import os
from textblob import TextBlob
from os import walk
from pprint import pprint


def load_tweets_from_file(path):
    """
    Loads all the tweet messages from the given file
    """
    tweets_msg = []
    with open(path, 'r') as f:
        for line in f:
            try:
                j = line.split('|')[-1]
                tweets_msg.append(json.loads(j)['msg'])
            except ValueError:
                # You probably have bad JSON
                continue
    return tweets_msg


def clean_tweet(tweet):
    """
    Remove all non ascii characters from the tweet
    """
    return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(\w+:\/\/\S+)", " ", tweet).split())
    

def get_all_files():
    """
    Load all files in the tweets directory
    """
    files = []
    for (_, _, filenames) in walk('tweets'):
        files.extend(filenames)
    return files
                     

def label_tweet(msg):
    """
    Create a json object representing labeled tweet
    """
    clean_msg = clean_tweet(msg)
    analysis = TextBlob(clean_msg)
    polarity = analysis.sentiment.polarity
    if polarity > 0:
        polarity = 1.0
    elif polarity < 0:
        polarity = 0.0
    else:
        polarity = 'invalid'
    record = {'label': polarity, 'msg': clean_msg.lower() }
    return record
    

def get_labeled_tweets_from_file(path):
    """
    Read all tweets from file and label them
    """
    labeled_tweets = []
    for msg in load_tweets_from_file(path):
        labeled_tweets.append(label_tweet(msg))
    return labeled_tweets


def write_to_file(labeled_tweets, path):
    """
    Write labeled tweets to a file
    """
    with open(path, 'w+') as outfile:
        for l_tweet in labeled_tweets:
            if l_tweet['label'] != 'invalid':
                json.dump(l_tweet, outfile)
                outfile.write('\n')

            
if __name__ == '__main__':
    files = get_all_files()
    source_files = [''.join(['tweets' , os.path.sep, filename]) for filename in files]
    target_files = [''.join(['labeled-tweets' ,os.path.sep , filename]) for filename in files]
    for i in range(len(source_files)):
        labeled_tweets = get_labeled_tweets_from_file(source_files[i])
        write_to_file(labeled_tweets, target_files[i])
    
