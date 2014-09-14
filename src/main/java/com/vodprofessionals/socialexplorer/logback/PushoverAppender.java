package com.vodprofessionals.socialexplorer.logback;

import ch.qos.logback.classic.boolex.OnErrorEvaluator;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PushoverAppender extends AppenderBase<ILoggingEvent> {
    CloseableHttpClient         httpclient      = HttpClients.createDefault();
    HttpPost                    httpPost        = new HttpPost("https://api.pushover.net/1/messages.json");
    ObjectMapper                mapper          = new ObjectMapper();
    protected EventEvaluator<ILoggingEvent> eventEvaluator  = null;
    protected Layout<ILoggingEvent>         layout          = null;
    protected String            token           = null;
    protected String            user            = null;


    public void start() {
        if (eventEvaluator == null) {
            EventEvaluator<ILoggingEvent> onError = new OnErrorEvaluator();
            onError.setContext(getContext());
            onError.setName("onError");
            onError.start();
            this.eventEvaluator = onError;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        CloseableHttpResponse response = null;

        try {
            if (eventEvaluator.evaluate(eventObject)) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("token", token));
                nvps.add(new BasicNameValuePair("user", user));
                nvps.add(new BasicNameValuePair("message", layout.doLayout(eventObject)));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));

                response = httpclient.execute(httpPost);

                if (response.getStatusLine().getStatusCode() != 200) {
                    Map<String, Object> jsonMap = mapper.readValue(response.getEntity().getContent(), Map.class);
                    String request              = jsonMap.containsKey("request") ? jsonMap.get("request").toString() : "<empty>";
                    throw new IOException("Error pushing notification '" + request + "' to Pushback");
                }

                EntityUtils.consume(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setEvaluator(EventEvaluator<ILoggingEvent> eventEvaluator) {
        this.eventEvaluator = eventEvaluator;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public Layout<ILoggingEvent> getLayout() {
        return this.layout;
    }

    public void setPushoverToken(String token) {
        this.token = token;
    }

    public String getPushoverToken() {
        return this.token;
    }

    public void setPushoverUser(String user) {
        this.user = user;
    }

    public String getPushoverUser() {
        return this.user;
    }
}