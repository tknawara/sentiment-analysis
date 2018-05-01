from flask import Flask, jsonify, request
from textblob import TextBlob
import base64
app = Flask(__name__)


@app.route('/TextBlob/classify', methods = ['POST'])
def sentiment():
    tweet = request.get_json()['tweetMsg']
    decodedTweet = base64.b64decode(tweet).decode('utf-8')
    textBlob = TextBlob(decodedTweet)
    sentiment = textBlob.sentiment
    if(sentiment.polarity > 0):
        return '"HAPPY"'
    else:
        return '"SAD"'


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

