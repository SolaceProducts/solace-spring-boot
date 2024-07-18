package com.solace.it.util.semp.monitor;

import com.solace.it.util.semp.SempClientException;
import com.solace.it.util.semp.SempClientException.AuthenticationException;
import com.solace.it.util.semp.SempClientException.AuthorizationException;
import com.solace.it.util.semp.SempClientException.MissingResourceException;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solace.test.integration.semp.v2.monitor.ApiClient;
import com.solace.test.integration.semp.v2.monitor.ApiException;
import com.solace.test.integration.semp.v2.monitor.api.ClientProfileApi;
import com.solace.test.integration.semp.v2.monitor.api.MsgVpnApi;
import com.solace.test.integration.semp.v2.monitor.api.QueueApi;
import com.solace.test.integration.semp.v2.monitor.auth.HttpBasicAuth;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClient;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientConnection;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientConnectionsResponse;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientProfile;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientProfilesResponse;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientResponse;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientSubscription;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientSubscriptionsResponse;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnClientsResponse;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnQueueSubscription;
import com.solace.test.integration.semp.v2.monitor.model.MonitorMsgVpnQueueSubscriptionsResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Builder for entity that can perform administrator level monitoring tasks on a messaging broker
 */
public class BrokerMonitorBuilder {

  static final Logger logger = LoggerFactory.getLogger(BrokerMonitorBuilder.class);
  private final ApiClient theClient;

  public static BrokerMonitorBuilder create(SempV2Api sempV2Api) {
    return new BrokerMonitorBuilder(sempV2Api);
  }

  private BrokerMonitorBuilder(SempV2Api sempV2Api) {
    this.theClient = sempV2Api.monitor().getApiClient();
  }

  public BrokerMonitorBuilder withDebugLog() {
    this.theClient.setDebugging(true);
    return this;
  }

  public BrokerMonitorBuilder withBasicAuth(String userName, String password) {
    this.theClient.setUsername(userName);
    this.theClient.setPassword(password);
    return this;
  }

  public BrokerMonitor build() {
    return new BrokerMonitor(this.theClient);
  }

  /**
   * Entity that can perform administrator level monitoring tasks on a messaging broker
   */
  public static class BrokerMonitor {

    private final ApiClient theClient;

    private BrokerMonitor(final ApiClient theClient) {
      this.theClient = theClient;
    }

    public MessageVpnClients vpnClients() {
      return new MessageVpnClients(this.theClient);
    }

    public QueueClients queueClients() {
      return new QueueClients(this.theClient);
    }

    public MsgVpnClientProfiles clientProfiles() {
      return new MsgVpnClientProfiles(this.theClient);
    }
  }

  public static class QueueClients {

    private final QueueApi queueApi;

    public QueueClients(ApiClient theClient) {
      this.queueApi = new QueueApi(theClient);
    }

    public List<String> querySubscriptionsByOriginalClientUsername(String msgVpnName,
        String queueName) throws SempClientException {
      List<String> subscriptions = new LinkedList<>();
      final MonitorMsgVpnQueueSubscriptionsResponse response;
      try {
        response = queueApi.getMsgVpnQueueSubscriptions(msgVpnName, queueName, 10, null,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final List<MonitorMsgVpnQueueSubscription> subscriptionsList = response.getData();
        if (subscriptionsList != null) {
          subscriptionsList.forEach(s -> {
            if (s != null) {
              subscriptions.add(s.getSubscriptionTopic());
            }
          });
        }
      } catch (ApiException e) {
        return wrapAndRethrowException(e, "Query queue subscriptions");
      }

      return subscriptions;
    }

    private <T> T wrapAndRethrowException(ApiException e, String operation)
        throws SempClientException {
      final String userName = ((HttpBasicAuth) queueApi.getApiClient()
          .getAuthentication("basicAuth"))
          .getUsername();
      if (HttpStatus.NOT_FOUND.value() == e.getCode()) {
        throw new MissingResourceException(
            String.format("Can't find resource for %s ", userName), e);
      } else if (HttpStatus.UNAUTHORIZED.value() == e.getCode()) {
        throw new AuthenticationException(
            String.format("Invalid credentials provided for user %s to perform %s", userName,
                operation), e);
      } else if (HttpStatus.FORBIDDEN.value() == e.getCode()) {
        throw new AuthorizationException(
            String.format("User %s not authorized to perform %s", userName, operation), e);
      } else {
        throw new SempClientException(String.format("%s failed", operation), e);
      }
    }
  }

  public static class MsgVpnClientProfiles {

    private final ClientProfileApi clientProfileApi;

    private MsgVpnClientProfiles(final ApiClient theClient) {
      this.clientProfileApi = new ClientProfileApi(theClient);
    }

    public MonitorMsgVpnClientProfile queryClientProfile(String msgVpnName, String clientName)
        throws SempClientException {
      try {
        MonitorMsgVpnClientProfilesResponse response = this.clientProfileApi
            .getMsgVpnClientProfiles(msgVpnName, 10, null, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final List<MonitorMsgVpnClientProfile> clProfiles = response.getData();
          final Optional<MonitorMsgVpnClientProfile> cp = clProfiles.stream()
              .filter(clientProfile -> {
                if (clientProfile != null && clientName.equals(
                    clientProfile.getClientProfileName())) {
                  return true;
                } else {
                  return false;
                }
              }).findFirst();

          try {
            Thread.sleep(6000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          if (cp.isPresent()) {
            return cp.get();
          } else {
            throw new MissingResourceException("Can't find resource");
          }
        } else {
          throw new MissingResourceException("Can't find resource");
        }
      } catch (ApiException e) {
        return wrapAndRethrowException(e, "Query client connections");
      }
    }

    private <T> T wrapAndRethrowException(ApiException e, String operation)
        throws SempClientException {
      final String userName = ((HttpBasicAuth) clientProfileApi.getApiClient()
          .getAuthentication("basicAuth"))
          .getUsername();

      if (HttpStatus.NOT_FOUND.value() == e.getCode()) {
        throw new MissingResourceException(
            String.format("Can't find resource for %s ", userName), e);
      } else if (HttpStatus.UNAUTHORIZED.value() == e.getCode()) {
        throw new AuthenticationException(
            String.format("Invalid credentials provided for user %s to perform %s", userName,
                operation), e);
      } else if (HttpStatus.FORBIDDEN.value() == e.getCode()) {
        throw new AuthorizationException(
            String.format("User %s not authorized to perform %s", userName, operation), e);
      } else {
        throw new SempClientException(String.format("%s failed", operation), e);
      }
    }
  }

  public static class MessageVpnClients {

    private final MsgVpnApi vpnApi;

    private MessageVpnClients(ApiClient apiClient) {
      this.vpnApi = new MsgVpnApi(apiClient);
    }

    public Collection<MonitorMsgVpnClientConnection> queryVpnClientConnections(String msgVpnName,
        String clientName) throws SempClientException {
      try {
        final MonitorMsgVpnClientConnectionsResponse response = this.vpnApi
            .getMsgVpnClientConnections(msgVpnName,
                clientName, 100, null, null,
                null);
        return response.getData();
      } catch (ApiException e) {
        return wrapAndRethrowException(e, "Query client connections");
      }
    }

    public List<String> querySubscriptionsByOriginalClientUsername(String msgVpnName,
        String originalClientUsername) throws SempClientException {
      final List<String> subscriptions = new LinkedList<>();
      try {
        final MonitorMsgVpnClientsResponse response = this.vpnApi
            .getMsgVpnClients(msgVpnName, 10, null, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);

        final List<MonitorMsgVpnClient> clients = response.getData();

        final Optional<MonitorMsgVpnClient> theClient = clients.stream().filter(client -> {
          if (client != null && originalClientUsername
              .equals(client.getOriginalClientUsername())) {
            return true;
          } else {
            return false;
          }
        }).findFirst();

        theClient.ifPresent(cl -> {
          try {
            final MonitorMsgVpnClientSubscriptionsResponse r = this.vpnApi
                .getMsgVpnClientSubscriptions(msgVpnName, cl.getClientName(), 10,
                    null, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            final List<MonitorMsgVpnClientSubscription> allSubscriptions = r.getData();
            allSubscriptions.forEach(s -> {
              subscriptions.add(s.getSubscriptionTopic());
            });
          } catch (ApiException e) {
            logger.warn("can't find any subscription", e);
          }
        });
      } catch (Exception e) {
        logger.warn("can't find any subscription", e);
        //
      }
      return subscriptions;
    }

    public MonitorMsgVpnClient queryVpnClientByClientName(String msgVpnName, String clientName)
        throws SempClientException {
      try {
        final MonitorMsgVpnClientResponse response = this.vpnApi
            .getMsgVpnClient(msgVpnName, clientName, null);
        return response.getData();
      } catch (ApiException e) {
        return wrapAndRethrowException(e, "Query vpn client");
      }
    }

    public List<MonitorMsgVpnClient> queryVpnClientsByClientName(String msgVpnName,
        String clientName) throws SempClientException {
      final Collection<MonitorMsgVpnClient> all = queryVpnClients(msgVpnName);
      List<MonitorMsgVpnClient> result = new LinkedList<>();
      for (MonitorMsgVpnClient c : all) {
        if (c != null && clientName.equals(c.getClientName())) {
          result.add(c);
        }
      }

      if (!result.isEmpty()) {
        return result;
      }
      throw new MissingResourceException(
          String.format("Can't find client with client name %s", clientName));
    }

    public List<MonitorMsgVpnClient> queryVpnClientsByUserName(String msgVpnName,
        String clientUsername)
        throws SempClientException {
      final Collection<MonitorMsgVpnClient> all = queryVpnClients(msgVpnName);
      List<MonitorMsgVpnClient> result = new LinkedList<>();
      for (MonitorMsgVpnClient c : all) {
        if (c != null && clientUsername.equals(c.getClientUsername())) {
          result.add(c);
        }
      }

      if (!result.isEmpty()) {
        return result;
      }
      throw new MissingResourceException(
          String.format("Can't find client with user name %s", clientUsername));
    }

    public List<MonitorMsgVpnClient> queryVpnClientsByClientUser(String msgVpnName,
        String clientUser) throws SempClientException {
      final Collection<MonitorMsgVpnClient> all = queryVpnClients(msgVpnName);
      List<MonitorMsgVpnClient> result = new LinkedList<>();
      for (MonitorMsgVpnClient c : all) {
        if (c != null && clientUser.equals(c.getUser())) {
          result.add(c);
        }
      }

      if (!result.isEmpty()) {
        return result;
      }
      throw new MissingResourceException(
          String.format("Can't find client with user %s", clientUser));
    }

    public Collection<MonitorMsgVpnClient> queryVpnClients(String msgVpnName)
        throws SempClientException {
      try {
        final MonitorMsgVpnClientsResponse response =
            this.vpnApi.getMsgVpnClients(msgVpnName, 10000, null, null, null);
        final Collection<MonitorMsgVpnClient> cl = response.getData();
        whenNotFound(cl);
        return cl;
      } catch (ApiException e) {
        return wrapAndRethrowException(e, "Query vpn client");
      }
    }

    private <T> T wrapAndRethrowException(ApiException e, String operation)
        throws SempClientException {
      // TBD if not found == no vpns or == bad request
      final String userName = ((HttpBasicAuth) vpnApi.getApiClient()
          .getAuthentication("basicAuth"))
          .getUsername();

      if (HttpStatus.NOT_FOUND.value() == e.getCode()) {
        throw new MissingResourceException(
            String.format("Can't find resource for %s ", userName), e);
      } else if (HttpStatus.UNAUTHORIZED.value() == e.getCode()) {
        throw new AuthenticationException(
            String.format("Invalid credentials provided for user %s to perform %s", userName,
                operation), e);
      } else if (HttpStatus.FORBIDDEN.value() == e.getCode()) {
        throw new AuthorizationException(
            String.format("User %s not authorized to perform %s", userName, operation), e);
      } else {
        throw new SempClientException(String.format("%s failed", operation), e);
      }
    }

    private void whenNotFound(Collection<?> collection) throws SempClientException {
      if (collection == null || collection.isEmpty()) {
        final String userName = ((HttpBasicAuth) vpnApi.getApiClient()
            .getAuthentication("basicAuth"))
            .getUsername();
        throw new MissingResourceException("Can't find resource");
      }
    }
  }
}