package com.jamierf.evohome;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.jamierf.evohome.api.QuickActionRequest;
import com.jamierf.evohome.api.SessionRequest;
import com.jamierf.evohome.api.SetTemperatureRequest;
import com.jamierf.evohome.model.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// https://github.com/watchforstock/evohome-client/
public class EvohomeClient {

    private static final String API_ROOT = "https://rs.alarmnet.com";

    private static final String SESSION_API = "/TotalConnectComfort/WebAPI/api/Session";
    private static final String LOCATIONS_API = "/TotalConnectComfort/WebAPI/api/locations";
    private static final String GATEWAY_API = "/TotalConnectComfort/WebAPI/api/gateways";
    private static final String TASK_STATUS_API = "/TotalConnectComfort/WebAPI/api/commTasks";
    private static final String SET_QUICK_ACTION_API = "/TotalConnectComfort/WebAPI/api/evoTouchSystems";
    private static final String SET_TEMPERATURE_API = "/TotalConnectComfort/WebAPI/api/devices/%s/thermostat/changeableValues/heatSetpoint";

    private static final String APPLICATION_ID = "91db1612-73fd-4500-91b2-e63b069b185c"; // the mobile app
    private static final String SESSION_ID_HEADER = "SessionId";

    private static final Logger LOG = LoggerFactory.getLogger(EvohomeClient.class);
    private static final int SESSION_EXPIRATION_HOURS = 24;

    private final Client client;
    private final TaskRunner taskRunner;
    private final UsernamePasswordCredentials credentials;
    private final Location location;

    private final LoadingCache<UsernamePasswordCredentials, Session> sessionCache;

    public EvohomeClient(final Client client, final String username, final String password, final String locationName) {
        this(client, username, password, location -> location.getName().equalsIgnoreCase(locationName));
    }

    public EvohomeClient(final Client client, final String username, final String password) {
        this(client, username, password, Predicates.alwaysTrue());
    }

    private EvohomeClient(final Client client, final String username, final String password,
                          final Predicate<Location> locationMatcher) {
        this.client = client;

        taskRunner = new TaskRunner(Executors.newCachedThreadPool(), this);
        credentials = new UsernamePasswordCredentials(username, password);

        sessionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(SESSION_EXPIRATION_HOURS, TimeUnit.HOURS)
                .build(new CacheLoader<UsernamePasswordCredentials, Session>() {
                    @Override
                    public Session load(final UsernamePasswordCredentials key) throws Exception {
                        return getSession(key.getUserName(), key.getPassword());
                    }
                });

        location = FluentIterable.from(getLocations().values())
                .firstMatch(locationMatcher)
                .orNull();
        if (location == null) {
            throw new IllegalStateException("No locations found.");
        }
    }

    public Optional<Device> getDevice(final String zone) {
        return Optional.fromNullable(getDevices().get(zone));
    }

    protected Session getSession() {
        try {
            return sessionCache.get(credentials);
        } catch (ExecutionException e) {
            LOG.warn("Failed to create session", e);
            throw Throwables.propagate(e.getCause());
        }
    }

    private Session getSession(final String username, final String password) {
        return client.resource(API_ROOT)
                .path(SESSION_API)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Session.class, new SessionRequest(username, password, APPLICATION_ID));
    }

    protected Map<String, Location> getLocations() {
        final Session session = getSession();
        final Collection<Location> locations = client.resource(API_ROOT)
                .path(LOCATIONS_API)
                .queryParam("userId", String.valueOf(session.getUser().getId()))
                .queryParam("allData", "True")
                .header(SESSION_ID_HEADER, session.getId())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Collection<Location>>() {
                });
        return Maps.uniqueIndex(locations, Location::getName);
    }

    public Map<String, Device> getDevices() {
        final Optional<Location> result = Optional.fromNullable(getLocations().get(location.getName()));
        final Collection<Device> devices = result.transform(Location::getDevices).or(Collections.emptyList());
        return Maps.uniqueIndex(devices, Device::getName);
    }

    public Map<String, Gateway> getGateways() {
        final Session session = getSession();
        final Collection<Gateway> gateways = client.resource(API_ROOT)
                .path(GATEWAY_API)
                .queryParam("locationId", String.valueOf(location.getId()))
                .queryParam("allData", "False")
                .header(SESSION_ID_HEADER, session.getId())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Collection<Gateway>>() {
                });
        return Maps.uniqueIndex(gateways, Gateway::getMac);
    }

    protected TaskStatus getTaskStatus(final Task task) {
        final Session session = getSession();
        return client.resource(API_ROOT)
                .path(TASK_STATUS_API)
                .queryParam("commTaskId", String.valueOf(task.getId()))
                .header(SESSION_ID_HEADER, session.getId())
                .accept(MediaType.APPLICATION_JSON)
                .get(TaskStatus.class);
    }

    public ListenableFuture<State> setQuickAction(final QuickAction action, final Optional<Date> until) {
        final Session session = getSession();
        final Task task = Iterables.getOnlyElement(client.resource(API_ROOT)
                .path(SET_QUICK_ACTION_API)
                .queryParam("locationId", String.valueOf(location.getId()))
                .header(SESSION_ID_HEADER, session.getId())
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(new GenericType<Collection<Task>>() {
                },
                        new QuickActionRequest(action, until)));
        return taskRunner.toFuture(task);
    }

    public ListenableFuture<State> setTemperature(final Device device, final Temperature temperature, final Optional<Date> until) {
        final Session session = getSession();
        final Task task = Iterables.getOnlyElement(client.resource(API_ROOT)
                .path(String.format(SET_TEMPERATURE_API, device.getId()))
                .header(SESSION_ID_HEADER, session.getId())
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(new GenericType<Collection<Task>>() {
                },
                        new SetTemperatureRequest(temperature, until)));
        return taskRunner.toFuture(task);
    }
}
