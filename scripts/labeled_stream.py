"""
This script will retreive tweets from twitter api, label them then store them 
in a json file. in order to run it you need to have a file named `twitter4j.properties`
in the same folder this file will hold you credentials in order to access the api

You can change the number of retreived tweets and the search query
in the main function.
"""

import re
import tweepy
import json
import uuid
from tweepy import OAuthHandler
from textblob import TextBlob
 
class TwitterClient(object):
    '''
    Generic Twitter Class for sentiment analysis.
    '''
    def __init__(self, consumer_key, consumer_secret, access_token, access_token_secret):
        '''
        Class constructor or initialization method.
        '''
        # attempt authentication
        try:
            # create OAuthHandler object
            self.auth = OAuthHandler(consumer_key, consumer_secret)
            # set access token and secret
            self.auth.set_access_token(access_token, access_token_secret)
            # create tweepy API object to fetch tweets
            self.api = tweepy.API(self.auth)
        except:
            print("Error: Authentication Failed")

    
    def is_valid(self, s):
        s = s.lower()
        return all(map(lambda i: i not in s, ['#', '@', 'http', 'rt', 'ftp']))


    def clean_tweet(self, tweet):
        """
        Remove all non ascii characters from the tweet
        """
        return ' '.join(filter(lambda s: self.is_valid(s), tweet.split(' ')))
    

    def get_tweet_sentiment(self, msg):
        '''
        Utility function to classify sentiment of passed tweet
        using textblob's sentiment method
        '''
        clean_msg = self.clean_tweet(msg)
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


    def get_tweets(self, query, count = 10):
        '''
        Main function to fetch tweets and parse them.
        '''
        # empty list to store parsed tweets
        tweets = []
 
        try:
            # call twitter api to fetch tweets
            fetched_tweets = self.api.search(q=query, lang='en', count = count)
            res = []
            for tweet in fetched_tweets:
                res.append(self.get_tweet_sentiment(tweet.text))
            return res

        except tweepy.TweepError as e:
            # print error (if any)
            print("Error : " + str(e))

def read_credentials():
    with open('twitter4j.properties') as f:
        return [s.split('=')[1].strip() for s in f.readlines()]


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
    # creating object of TwitterClient Class
    params = read_credentials()
    api = TwitterClient(params[0], params[1], params[2], params[3])
    # calling function to get tweets
    tweets = api.get_tweets(query='hello', count = 10)

    write_to_file(tweets, ''.join(['tweets-', str(uuid.uuid4()), '.json']))
