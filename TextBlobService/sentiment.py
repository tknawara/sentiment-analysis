from flask import Flask, jsonify, request
from textblob import TextBlob
import base64
import re
app = Flask(__name__)


@app.route('/TextBlob/classify', methods = ['POST'])
def sentiment():
    """
    classify a tweet using textblob.
    @input: post request body contains base64 encoded tweetMsg to be classified.
    @output: SAD or HAPPY label.
    """
    tweet = request.get_json()['tweetMsg']
    decoded_tweet = clean_tweet(decode(tweet))
    text_blob = TextBlob(decoded_tweet)
    sentiment = text_blob.sentiment
    if(sentiment.polarity >= 0):
        return '"HAPPY"'
    else:
        return '"SAD"'
    
@app.route('/TextBlob/correct', methods = ['POST'])
def spelling_correct():
    """
    correct spelling in a tweet using textblob.
    @input: post request contains base64 encoded tweetMsg to be corrected.
    @ouput: bas64 encoded corrected tweet.
    """
    tweet = request.get_json()['tweetMsg']
    decoded_tweet = decode(tweet)
    text_blob = TextBlob(decoded_tweet)
    correction = text_blob.correct()
    encoded_tweet = encode(str(correction))
    return jsonify({'tweetMsg': encoded_tweet.decode('utf-8')})

def decode(tweet):
    """decode a tweet"""
    return base64.b64decode(tweet).decode('utf-8')

def encode(tweet):
    """encode a tweet"""
    return base64.b64encode(tweet.encode())

def clean_tweet(tweet):
    """ clean a tweet by removing links, special characters """
    tweet = re.sub("https?:\\/\\/\\S+\\b|www\\.(\\w+\\.)+\\S*", " ", tweet)
    tweet = re.sub("@\\w+", " ", tweet)
    tweet = re.sub("/", " / ", tweet)
    tweet = re.sub("[8:=;]['`\\-]?[)d]+|[)d]+['`\\-]?[8:=;]", " ", tweet)
    tweet = re.sub("[8:=;]['`\\-]?p+", " ", tweet)
    tweet = re.sub("[8:=;]['`\\-]?\\(+|\\)+['`\\-]?[8:=;]", " ", tweet)
    tweet = re.sub("[8:=;]['`\\-]?[\\/|l*]", " ", tweet)
    tweet = re.sub("<3", " ", tweet)
    tweet = re.sub("[-+]?[.\\d]*[\\d]+[:,.\\d]*", " ", tweet)
    tweet = re.sub("!", " ! ", tweet)
    tweet = re.sub(" can\\'t ", " can not ", tweet)
    tweet = re.sub(" won't ", " will not ", tweet)
    tweet = re.sub("n\\'t ", "not ", tweet)

    return tweet

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

