[![Build Status](https://travis-ci.org/tarek-nawara/sentiment-analysis.svg?branch=master)](https://travis-ci.org/tarek-nawara/sentiment-analysis)
[![Coverage Status](https://coveralls.io/repos/github/tarek-nawara/sentiment-analysis/badge.svg?branch=master)](https://codecov.io/gh/tarek-nawara/sentiment-analysis)

# sentiment-analysis
Simple show case of consuming twitter api and performing sentiment analysis over tweets

### How to contribute
You need the following dependencies installed:
- [Gradle](https://gradle.org/)
- [ElasticSearch](https://www.elastic.co/downloads/elasticsearch)
- [Kibana](https://www.elastic.co/downloads/kibana)
- You need to get credentials to access `Twitter API` from here:
[Twitter-app](https://apps.twitter.com/) once you get your credentials create a file called `twitter4j.properties`
under `src/main/resources` and add your credentials in it, use the `template` under resources. 
### How to run it
After installing `Elasticsearch` just go to the bin directory and fire up `elasticsearch` or 
for windows `bin/elasticsearch`
after that you need to run `gradle run` 

### How to delete Elasticsearch index
use the following command in your terminal while Elasticsearch service is up, 
`curl -XDELETE 'http://localhost:9200/twitter'`
