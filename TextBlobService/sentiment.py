from flask import Flask, jsonify, request
from textblob import TextBlob
app = Flask(__name__)


@app.route('/TextBlob/classify', methods = ['POST'])
def sentiment():
    tweet = request.get_json()['tweetMsg']
    textBlob = TextBlob(tweet)
    sentiment = textBlob.sentiment
    if(sentiment.polarity > 0):
        return '"HAPPY"'
    else:
        return '"SAD"'


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

