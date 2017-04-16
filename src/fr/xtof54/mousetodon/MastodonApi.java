package fr.xtof54.mousetodon;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class MastodonApi extends DefaultApi20 {
    private final String domain;

    protected MastodonApi(String domain) {
        this.domain = domain;
    }

    public static MastodonApi instance(String domain) {
        return new MastodonApi(domain);
    }

    @Override
    public String getAccessTokenEndpoint() {
        return String.format("https://%s/oauth/token", domain);
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return String.format("https://%s/oauth/authorize", domain);
    }
}

