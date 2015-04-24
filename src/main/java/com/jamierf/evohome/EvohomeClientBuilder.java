package com.jamierf.evohome;

import static com.google.common.base.Preconditions.*;

import java.net.URI;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.jamierf.evohome.model.Location;
import com.sun.jersey.api.client.Client;

public class EvohomeClientBuilder {

	private final Client client;

	private URI apiRoot = EvohomeClient.DEFAULT_API_ROOT;
	private Predicate<Location> locationMatcher = Predicates.alwaysTrue();

	public EvohomeClientBuilder( final Client client ) {
		this.client = checkNotNull(client);
	}

	public EvohomeClientBuilder withLocation(final String name) {
		checkNotNull(name);
		locationMatcher = location -> location.getName().equalsIgnoreCase(name);
		return this;
	}

	public EvohomeClientBuilder withApiRoot(final URI apiRoot) {
		this.apiRoot = checkNotNull(apiRoot);
		return this;
	}

	public EvohomeClient build(final String username, final String password) {
		return new EvohomeClient(
				client,
				checkNotNull(username),
				checkNotNull(password),
				locationMatcher,
				apiRoot
		);
	}
}
