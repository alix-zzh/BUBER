package by.zhuk.buber.oauth;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class OAuthYandex implements OAuth {
    private static Logger logger = LogManager.getLogger(OAuthYandex.class);

    private static OAuthYandex instance;
    private static AtomicBoolean instanceCreated = new AtomicBoolean(false);
    private static ReentrantLock lock = new ReentrantLock();

    private String clientId = null;
    private String clientSecret = null;
    private String redirectUri = null;
    private String authUrl = null;
    private String tokenUrl = null;

    public static OAuthYandex getInstance() {
        if (!instanceCreated.get()) {
            lock.lock();
            try {
                if (!instanceCreated.get()) {
                    instance = new OAuthYandex();

                    instance.init();

                    instanceCreated.set(true);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    private void init() {
        final String YANDEX_PROPERTY = "properties/yandex";
        final String YANDEX_CLIENT_ID = "yandex.clientId";
        final String YANDEX_CLIENT_SECRET = "yandex.clientSecret";
        final String YANDEX_REDIRECT_URI = "yandex.redirectUri";
        final String YANDEX_AUTH_URI = "yandex.authUrl";
        final String YANDEX_TOKEN_URL = "yandex.tokenUrl";

        ResourceBundle resourceBundle;
        String authURI;

        try {
            resourceBundle = ResourceBundle.getBundle(YANDEX_PROPERTY);
            clientId = resourceBundle.getString(YANDEX_CLIENT_ID);
            clientSecret = resourceBundle.getString(YANDEX_CLIENT_SECRET);
            redirectUri = resourceBundle.getString(YANDEX_REDIRECT_URI);
            authURI = resourceBundle.getString(YANDEX_AUTH_URI);
            tokenUrl = resourceBundle.getString(YANDEX_TOKEN_URL);

        } catch (MissingResourceException e) {
            logger.log(Level.FATAL, "Hasn't found bundle for yandex");
            throw new RuntimeException("Hasn't found bundle for yandex");
        }
        try {
            authUrl = authURI + "authorize?client_id=" + clientId +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                    "&response_type=code" +
                    "&force_confirm=yes";
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARN, "Unknown problem in OAuthYandex");
        }
    }

    @Override
    public String takeEmail(JSONObject json) {
        return (String) json.getJSONArray("emails").get(0);
    }

    @Override
    public boolean hasError(String error) {
        return error != null && error.equals("invalid_request");
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String getAuthUrl() {
        return authUrl;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public String takeLoginInfoUrl(JSONObject json) {
        String token = json.getString("access_token");
        return "https://login.yandex.ru/info?format=json&oauth_token=" + token;
    }
}