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
                tweet = json.loads(j)
                tweets_msg.append({'msg': tweet['msg'], 'label': tweet['label']})
            except ValueError:
                # You probably have bad JSON
                continue
    return tweets_msg




def get_all_files():
    """
    Load all files in the tweets directory
    """
    files = []
    for (_, _, filenames) in walk('labeled-tweets'):
        files.extend(filenames)
    return files
                     

def correct_tweet(labeled_tweet):
    """
    Create a json object representing labeled tweet
    """
    text_blob = TextBlob(labeled_tweet['msg'])
    correct = text_blob.correct()
    labeled_tweet['msg'] = str(correct)
    print(labeled_tweet)
    return labeled_tweet
    

def get_correct_tweets_from_file(path):
    """
    Read all tweets from file and label them
    """
    correct_tweets = []
    for msg in load_tweets_from_file(path):
        correct_tweets.append(correct_tweet(msg))
    return correct_tweets


def write_to_file(correct_tweets, path):
    """
    Write labeled tweets to a file
    """
    with open(path, 'w+') as outfile:
        for l_tweet in correct_tweets:
            json.dump(l_tweet, outfile)
            outfile.write('\n')

            
if __name__ == '__main__':
    files = get_all_files()
    source_files = [''.join(['labeled-tweets' , os.path.sep, filename]) for filename in files]
    target_files = [''.join(['correct-tweets' ,os.path.sep , filename]) for filename in files]
    for i in range(len(source_files)):
        print(source_files[i])
        print(target_files[i])
        correct_tweets = get_correct_tweets_from_file(source_files[i])
        write_to_file(correct_tweets, target_files[i])
    
