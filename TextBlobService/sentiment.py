from flask import Flask, jsonify, request
from textblob import TextBlob
import base64
app = Flask(__name__)


@app.route('/TextBlob/classify', methods = ['POST'])
def sentiment():
    """
    classify a tweet using textblob.
    @input: post request body contains base64 encoded tweetMsg to be classified.
    @output: SAD or HAPPY label.
    """
    tweet = request.get_json()['tweetMsg']
    decoded_tweet = decode(tweet)
    text_blob = TextBlob(decoded_tweet)
    sentiment = text_blob.sentiment
    if(sentiment.polarity > 0):
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
    return encoded_tweet

def decode(tweet):
    """decode a tweet"""
    return base64.b64decode(tweet).decode('utf-8')

def encode(tweet):
    """encode a tweet"""
    return base64.b64encode(tweet.encode())


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

