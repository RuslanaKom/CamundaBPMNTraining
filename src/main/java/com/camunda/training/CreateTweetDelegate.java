package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Component
public class CreateTweetDelegate implements JavaDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

//    private final TweeterService tweeterService;

//    public CreateTweetDelegate(TweeterService tweeterService) {
//        this.tweeterService = tweeterService;
//    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String content = (String) execution.getVariable("content");
        LOGGER.info("Publishing tweet: " + content);
       // long tweetId = tweeterService.postTweet(content);

            LOGGER.info("Publishing tweet: " + content);
            AccessToken accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
            twitter.setOAuthAccessToken(accessToken);
            Status status = twitter.updateStatus(content);
        // execution.setVariable("tweetId", tweetId);
    }
}
