package com.jamierf.evohome;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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

    private static final int TASK_STATUS_CHECK_DELAY_SECONDS = 2;
    private static final int TASK_STATUS_CHECK_TIMEOUT_SECONDS = 60;
    private static final int MAX_DEVICE_NAME_CACHE_SIZE = 100;

    private static final Logger LOG = LoggerFactory.getLogger(EvohomeClient.class);
    private static final int SESSION_EXPIRATION_HOURS = 24;

    private final Client client;
    private final ListeningExecutorService executor;
    private final UsernamePasswordCredentials credentials;
    private final long locationId;

    private final LoadingCache<UsernamePasswordCredentials, Session> sessionCache;
    private final LoadingCache<String, Device> deviceCache;

    public EvohomeClient(final Client client, final String username, final String password) {
        this.client = client;

        executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        credentials = new UsernamePasswordCredentials(username, password);

        sessionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(SESSION_EXPIRATION_HOURS, TimeUnit.HOURS)
                .build(new CacheLoader<UsernamePasswordCredentials, Session>() {
                    @Override
                    public Session load(final UsernamePasswordCredentials key) throws Exception {
                        return getSession(key.getUserName(), key.getPassword());
                    }
                });

        final Location location = Iterables.getFirst(getLocations(), null); // Only support the first location for now
        if (location == null) {
            throw new IllegalStateException("No locations found.");
        }
        locationId = location.getId();

        deviceCache = CacheBuilder.newBuilder()
                .maximumSize(MAX_DEVICE_NAME_CACHE_SIZE)
                .build(new CacheLoader<String, Device>() {
                    @Override
                    public Device load(final String key) {
                        final Optional<Device> device = Iterables.tryFind(location.getDevices(), input -> input.getName().equalsIgnoreCase(key));
                        if (!device.isPresent()) {
                            throw new IllegalArgumentException(String.format("Unknown device: %s", key));
                        }

                        return device.get();
                    }
                });
    }

    private Device getDevice(final String zone) {
        try {
            return deviceCache.get(zone);
        } catch (ExecutionException e) {
            LOG.warn("Failed to find device for " + zone, e);
            throw Throwables.propagate(e.getCause());
        }
    }

    private Session getSession() {
        try {
            return sessionCache.get(credentials);
        } catch (ExecutionException e) {
            LOG.warn("Failed to create session", e);
            throw Throwables.propagate(e);
        }
    }

    private Session getSession(final String username, final String password) {
        return client.resource(API_ROOT)
                .path(SESSION_API)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Session.class, new SessionRequest(username, password, APPLICATION_ID));
    }

    private Collection<Location> getLocations() {
        final Session session = getSession();
        return client.resource(API_ROOT)
                .path(LOCATIONS_API)
                .queryParam("userId", String.valueOf(session.getUser().getId()))
                .queryParam("allData", "True")
                .header(SESSION_ID_HEADER, session.getId())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Collection<Location>>() {
                });
    }

    public Collection<Device> getDevices() {
        final Optional<Location> result = Iterables.tryFind(getLocations(), input -> input != null && input.getId() == locationId);
        return result.isPresent() ? result.get().getDevices() : Collections.emptyList();
    }

    private Collection<Gateway> getGateways() {
        final Session session = getSession();
        return client.resource(API_ROOT)
                .path(GATEWAY_API)
                .queryParam("locationId", String.valueOf(locationId))
                .queryParam("allData", "False")
                .header(SESSION_ID_HEADER, session.getId())
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Collection<Gateway>>() {
                });
    }

    private TaskStatus getTaskStatus(final Task task) {
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
                .queryParam("locationId", String.valueOf(locationId))
                .header(SESSION_ID_HEADER, session.getId())
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(new GenericType<Collection<Task>>() {
                }, new QuickActionRequest(action, until)));
        return future(task);
    }

    public ListenableFuture<State> setTemperature(final String zone, final float temperature, final Optional<Date> until) {
        final Session session = getSession();
        final long deviceId = getDevice(zone).getId();
        final Task task = Iterables.getOnlyElement(client.resource(API_ROOT)
                .path(String.format(SET_TEMPERATURE_API, deviceId))
                .header(SESSION_ID_HEADER, session.getId())
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(new GenericType<Collection<Task>>() {
                }, new SetTemperatureRequest(temperature, until)));
        return future(task);
    }

    private ListenableFuture<State> future(final Task task) {
        return executor.submit(() -> await(task, TASK_STATUS_CHECK_DELAY_SECONDS, TASK_STATUS_CHECK_TIMEOUT_SECONDS));
    }

    private State await(final Task task, final int delay, final int timeout) throws InterruptedException {
        final long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeout);

        State state = null;
        while (System.currentTimeMillis() < endTime) {
            state = getTaskStatus(task).getState();
            if (state.isComplete()) {
                break;
            }

            TimeUnit.SECONDS.sleep(delay);
        }

        return state;
    }
}
