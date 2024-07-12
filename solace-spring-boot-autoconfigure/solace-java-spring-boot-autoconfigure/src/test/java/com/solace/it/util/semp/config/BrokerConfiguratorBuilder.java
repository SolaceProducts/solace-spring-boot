package com.solace.it.util.semp.config;

import com.solace.it.util.semp.SempClientException;
import com.solace.it.util.semp.SempClientException.AuthenticationException;
import com.solace.it.util.semp.SempClientException.AuthorizationException;
import com.solace.it.util.semp.SempClientException.MissingResourceException;
import com.solace.test.integration.semp.v2.SempV2Api;
import com.solace.test.integration.semp.v2.config.ApiClient;
import com.solace.test.integration.semp.v2.config.ApiException;
import com.solace.test.integration.semp.v2.config.api.AuthenticationOauthProfileApi;
import com.solace.test.integration.semp.v2.config.api.AuthorizationGroupApi;
import com.solace.test.integration.semp.v2.config.api.CertAuthorityApi;
import com.solace.test.integration.semp.v2.config.api.ClientUsernameApi;
import com.solace.test.integration.semp.v2.config.api.MsgVpnApi;
import com.solace.test.integration.semp.v2.config.api.QueueApi;
import com.solace.test.integration.semp.v2.config.auth.HttpBasicAuth;
import com.solace.test.integration.semp.v2.config.model.ConfigCertAuthority;
import com.solace.test.integration.semp.v2.config.model.ConfigCertAuthorityResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpn;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpn.AuthenticationBasicTypeEnum;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAclProfile;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAclProfilesResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthenticationOauthProfile;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthenticationOauthProfileResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthorizationGroup;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnAuthorizationGroupResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnClientUsername;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnClientUsernameResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueue;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueue.AccessTypeEnum;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueue.PermissionEnum;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueueResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueueSubscription;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueueSubscriptionResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnQueuesResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigMsgVpnsResponse;
import com.solace.test.integration.semp.v2.config.model.ConfigSempMetaOnlyResponse;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Builder for entity that can perform administrator level configuration tasks on a messaging
 * broker
 */
public class BrokerConfiguratorBuilder {

  static final Logger logger = LoggerFactory.getLogger(BrokerConfiguratorBuilder.class);

  private final ApiClient theClient;

  public static BrokerConfiguratorBuilder create(SempV2Api sempV2Api) {
    return new BrokerConfiguratorBuilder(sempV2Api);
  }

  private BrokerConfiguratorBuilder(SempV2Api sempV2Api) {
    this.theClient = sempV2Api.config().getApiClient();
  }

  /**
   * Enables http request level logging.
   *
   * @return
   */
  public BrokerConfiguratorBuilder withDebugLog() {
    this.theClient.setDebugging(true);
    return this;
  }

  public BrokerConfiguratorBuilder withBasicAuth(String userName, String password) {
    this.theClient.setUsername(userName);
    this.theClient.setPassword(password);
    return this;
  }

  public BrokerConfigurator build() {
    return new BrokerConfigurator(this.theClient);
  }

  static <T> T wrapAndRethrowException(ApiException e, String operation, ApiClient apiClient)
      throws SempClientException {
    final String userName = ((HttpBasicAuth) apiClient
        .getAuthentication("basicAuth")).getUsername();
    if (HttpStatus.NOT_FOUND.value() == e.getCode()) {
      throw new MissingResourceException(e.getMessage());
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

  /**
   * Entity that can perform administrator level configuration tasks on a messaging broker
   */
  public static class BrokerConfigurator {

    private final ApiClient theClient;

    private BrokerConfigurator(final ApiClient theClient) {
      this.theClient = theClient;
    }

    public MessageVpns vpns() {
      return new MessageVpns(this.theClient);
    }

    public CertAuthorities certAuthorities() {
      return new CertAuthorities(this.theClient);
    }

    public Queues queues() {
      return new Queues(this.theClient);
    }
  }

  public static class CertAuthorities {

    private final CertAuthorityApi certAuthorityApi;
    private final ApiClient apiClient;

    private CertAuthorities(ApiClient apiClient) {
      this.certAuthorityApi = new CertAuthorityApi(apiClient);
      this.apiClient = apiClient;
    }

    public void setupCertAuthority(String certAuthorityName, String certContent) {
      final ConfigCertAuthority ca = new ConfigCertAuthority();
      ca.certAuthorityName(certAuthorityName).certContent(certContent);
      try {
        final ConfigCertAuthorityResponse response = this.certAuthorityApi
            .createCertAuthority(ca, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return;
        } else {
          throw new SempClientException(
              String.format("CertAuthority %s could not be upploaded, response code: %s",
                  certAuthorityName, response.getMeta().getResponseCode()));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Query VPNs", apiClient);
      }
    }
  }

  public static class MessageVpns {

    private final String VPN_CREATE_FAIL = "Message vpn %s could not be created";
    private final String VPN_UPDATE_FAIL = "Message vpn %s could not be updated";
    private final String VPN_DELETE_FAIL = "Message vpn %s could not be deleted";

    private final MsgVpnApi vpnApi;
    private final ApiClient apiClient;

    private MessageVpns(ApiClient apiClient) {
      this.vpnApi = new MsgVpnApi(apiClient);
      this.apiClient = apiClient;
    }

    /**
     * Create Message VPN from a given entity
     *
     * @param vpn entity with vpn properties to be created
     * @return instance of freshly created message vpn
     */
    public ConfigMsgVpn createVpn(ConfigMsgVpn vpn) {
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.createMsgVpn(vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return response.getData();
        } else {
          throw new SempClientException(String.format(VPN_CREATE_FAIL, vpn.getMsgVpnName()));
        }
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder.wrapAndRethrowException(e, "Create VPN", this.apiClient);
      }
    }

    /**
     * updates Message VPN from a given entity
     *
     * @param vpn entity with vpn properties to be updated
     * @return instance of freshly updated message vpn
     */
    public ConfigMsgVpn updateVpn(ConfigMsgVpn vpn) {
      try {
        final ConfigMsgVpnResponse response = this.vpnApi
            .updateMsgVpn(vpn.getMsgVpnName(), vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return response.getData();
        } else {
          throw new SempClientException(String.format(VPN_UPDATE_FAIL, vpn.getMsgVpnName()));
        }
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder.wrapAndRethrowException(e, "Update VPN", this.apiClient);
      }
    }

    /**
     * Create Message VPN with default settings and no oauth
     *
     * @param msgVpnName name of the vpn to be created
     * @return instance of freshly created message vpn
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public ConfigMsgVpn createVpn(String msgVpnName) {
      return createVpn(msgVpnName, 50L, false);
    }

    public ConfigMsgVpn createVpn(String msgVpnName, Long msgSpool,
        boolean internalBasicAuthEnabled) {
      ConfigMsgVpn vpn = new ConfigMsgVpn().enabled(true)
          .msgVpnName(msgVpnName)
          .maxMsgSpoolUsage(msgSpool)
          .authenticationBasicType(internalBasicAuthEnabled ? AuthenticationBasicTypeEnum.INTERNAL
              : AuthenticationBasicTypeEnum.NONE);
      return createVpn(vpn);
    }

    /**
     * Creates basic vpn copy with disabled extended services like MQTT, Rest, AMQP
     *
     * @param from
     * @param to
     * @return
     */
    public ConfigMsgVpn copyVpn(String from, String to) {
      ConfigMsgVpn vpn = queryVpn(from);
      vpn.msgVpnName(to)
          .serviceAmqpPlainTextEnabled(false)
          .serviceAmqpTlsEnabled(false)
          .serviceMqttPlainTextEnabled(false)
          .serviceMqttTlsEnabled(false)
          .serviceMqttWebSocketEnabled(false)
          .serviceMqttTlsWebSocketEnabled(false)
          .serviceRestIncomingPlainTextEnabled(false)
          .serviceRestIncomingTlsEnabled(false);
      return createVpn(vpn);
    }

    /**
     * Deletes message vpn with a given name
     *
     * @param msgVpnName name of the vpn
     */
    public void deleteVpn(String msgVpnName) {
      try {
        final ConfigSempMetaOnlyResponse response = this.vpnApi.deleteMsgVpn(msgVpnName);
        if (HttpStatus.OK.value() != response.getMeta().getResponseCode()) {
          throw new SempClientException(String.format(VPN_DELETE_FAIL, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    /**
     * Disables message vpn
     *
     * @param msgVpnName name of the vpn to be disabled
     */
    public void disableVpn(String msgVpnName) {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.enabled(false);
      updateVpn(vpn);
    }

    /**
     * Queries all vpns on a broker
     *
     * @return collection with all vpns on a broker
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public Collection<ConfigMsgVpn> queryAllVpns() throws SempClientException {
      try {
        final ConfigMsgVpnsResponse response = this.vpnApi
            .getMsgVpns(null, null, null, null, null);
        final Collection<ConfigMsgVpn> cl = response.getData();
        whenNotFound(cl);
        return cl;
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Query VPNs", this.apiClient);
      }
    }

    public ConfigMsgVpn queryVpn(String msgVpnName) throws SempClientException {
      try {
        final ConfigMsgVpnResponse response = this.vpnApi
            .getMsgVpn(msgVpnName, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final ConfigMsgVpn cl = response.getData();
          return cl;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be found", msgVpnName));
        }
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Query VPNs", this.apiClient);
      }
    }

    /**
     * Enables internal basic auth on for the given vpn
     *
     * @param msgVpnName name of the vpn
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public void enableBasicAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationBasicType(AuthenticationBasicTypeEnum.INTERNAL);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Basic auth enabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    /**
     * Disables internal basic auth on for the given vpn
     *
     * @param msgVpnName name of the vpn
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public void disableBasicAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationBasicType(AuthenticationBasicTypeEnum.NONE);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Basic auth disabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    /**
     * Enables internal client auth on for the given vpn
     *
     * @param msgVpnName name of the vpn
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public void enableClientCertAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationClientCertEnabled(true);
      vpn.authenticationClientCertValidateDateEnabled(true);
      vpn.authenticationClientCertAllowApiProvidedUsernameEnabled(true);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Client certificate auth enabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    /**
     * Disables internal client auth on for the given vpn
     *
     * @param msgVpnName name of the vpn
     * @throws SempClientException thrown if something goes wrong; original exception is returned
     *                             wrapped
     */
    public void disableClientCertAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationClientCertEnabled(false);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Basic auth disabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void enableKerberosAuth(String msgVpnName, boolean allowApiProvidedUsername)
        throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.authenticationKerberosEnabled(true);
      vpn.authenticationKerberosAllowApiProvidedUsernameEnabled(allowApiProvidedUsername);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Kerberos auth enabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void disableKerberosAuth(String msgVpnName)
        throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.authenticationKerberosEnabled(false);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Kerberos auth disabled for vpn:  {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Message vpn %s could not be updated", msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public Collection<ConfigMsgVpnAclProfile> queryAclProfile(String msgVpnName)
        throws SempClientException {
      try {
        final ConfigMsgVpnAclProfilesResponse response = this.vpnApi
            .getMsgVpnAclProfiles(msgVpnName, null, null, null, null, null);
        final Collection<ConfigMsgVpnAclProfile> cl = response.getData();
        whenNotFound(cl);
        return cl;
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Query AlcProfiles", this.apiClient);
      }
    }

    private void whenNotFound(Collection<?> collection)
        throws SempClientException {
      if (collection == null || collection.isEmpty()) {
        final String userName = ((HttpBasicAuth) vpnApi
            .getApiClient()
            .getAuthentication("basicAuth"))
            .getUsername();
        throw new MissingResourceException("Can't find resource");
      }
    }

    public void enableOAuthAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationOauthEnabled(true);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("OAuth authentication enabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(String.format(VPN_UPDATE_FAIL, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void disableOAuthAuth(String msgVpnName) throws SempClientException {
      final ConfigMsgVpn vpn = queryVpn(msgVpnName);
      vpn.setAuthenticationOauthEnabled(false);
      try {
        final ConfigMsgVpnResponse response = this.vpnApi.updateMsgVpn(msgVpnName, vpn, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("OAuth authentication disabled for vpn: {}", msgVpnName);
          return;
        } else {
          throw new SempClientException(String.format(VPN_UPDATE_FAIL, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public ConfigMsgVpnClientUsername createClientUsername(String msgVpnName,
        String clientUsername) {
      try {
        final ClientUsernameApi clientUsernameApi = new ClientUsernameApi(this.apiClient);
        final ConfigMsgVpnClientUsername msgVpnClientUsername = new ConfigMsgVpnClientUsername()
            .clientUsername(clientUsername)
            .clientProfileName("default")
            .aclProfileName("default")
            .enabled(true);
        ConfigMsgVpnClientUsernameResponse response = clientUsernameApi.createMsgVpnClientUsername(
            msgVpnName, msgVpnClientUsername, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return response.getData();
        } else {
          throw new SempClientException("MsgVpnClientUsername Creation Failed");
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void deleteClientUsername(String msgVpnName, String clientUsername) {
      try {
        final ClientUsernameApi clientUsernameApi = new ClientUsernameApi(this.apiClient);
        final ConfigSempMetaOnlyResponse response = clientUsernameApi.deleteMsgVpnClientUsername(
            msgVpnName, clientUsername);
        if (HttpStatus.OK.value() != response.getMeta().getResponseCode()) {
          throw new SempClientException(
              String.format("Could not delete Client Username %s for Message VPN %s",
                  clientUsername, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public ConfigMsgVpnAuthenticationOauthProfile createOAuthProfile(String msgVpnName,
        ConfigMsgVpnAuthenticationOauthProfile profile) {
      try {
        AuthenticationOauthProfileApi oAuthProfileApi = new AuthenticationOauthProfileApi(
            this.apiClient);
        ConfigMsgVpnAuthenticationOauthProfileResponse response = oAuthProfileApi.createMsgVpnAuthenticationOauthProfile(
            msgVpnName, profile, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return response.getData();
        } else {
          throw new SempClientException("AuthenticationOauthProfileApi Creation Failed");
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void deleteOAuthProfile(String msgVpnName, String oAuthProfileName) {
      try {
        AuthenticationOauthProfileApi oAuthProfileApi = new AuthenticationOauthProfileApi(
            this.apiClient);
        final ConfigSempMetaOnlyResponse response = oAuthProfileApi.deleteMsgVpnAuthenticationOauthProfile(
            msgVpnName, oAuthProfileName);
        if (HttpStatus.OK.value() != response.getMeta().getResponseCode()) {
          throw new SempClientException(
              String.format("Could not be delete OAuth Profile %s for Message VPN %s",
                  oAuthProfileName, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public ConfigMsgVpnAuthorizationGroup createAuthorizationGroup(String msgVpnName,
        ConfigMsgVpnAuthorizationGroup request) {
      try {
        AuthorizationGroupApi authorizationGroupApi = new AuthorizationGroupApi(this.apiClient);
        ConfigMsgVpnAuthorizationGroupResponse response = authorizationGroupApi.createMsgVpnAuthorizationGroup(
            msgVpnName, request, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          return response.getData();
        } else {
          throw new SempClientException("AuthenticationOauthProfileApi Creation Failed");
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }

    public void deleteAuthorizationGroup(String msgVpnName, String authorizationGroupName) {
      try {
        AuthorizationGroupApi authorizationGroupApi = new AuthorizationGroupApi(this.apiClient);
        final ConfigSempMetaOnlyResponse response = authorizationGroupApi.deleteMsgVpnAuthorizationGroup(
            msgVpnName, authorizationGroupName);
        if (HttpStatus.OK.value() != response.getMeta().getResponseCode()) {
          throw new SempClientException(
              String.format("Could not be delete Authorization Group %s for Message VPN %s",
                  authorizationGroupName, msgVpnName));
        }
      } catch (ApiException e) {
        throw new SempClientException(e);
      }
    }
  }

  /**
   * Api to create/update queues and topic subscriptions
   *
   * @exclude
   */
  public static class Queues {

    private final QueueApi queueApi;
    private final ApiClient apiClient;

    private Queues(ApiClient apiClient) {
      this.queueApi = new QueueApi(apiClient);
      this.apiClient = apiClient;
    }

    public void createQueue(String msgVpnName, String queueName, boolean exclusive)
        throws SempClientException {
      createQueue(msgVpnName, queueName, exclusive, PermissionEnum.DELETE);
    }

    public void createPartitionedQueue(String msgVpnName, String queueName, int partitionsCount)
        throws SempClientException {
      createQueue(msgVpnName, queueName, false, PermissionEnum.DELETE, partitionsCount);
    }

    public void updatePartitionCount(String msgVpnName, String queueName, int newPartitionsCount)
        throws SempClientException {
      final ConfigMsgVpnQueue queue = queryQueue(msgVpnName, queueName);
      queue.setPartitionCount(newPartitionsCount);
      updateQueue(queue);
    }

    public void createQueue(String msgVpnName, String queueName, boolean exclusive,
        PermissionEnum permission) throws SempClientException {
      createQueue(msgVpnName, queueName, exclusive, permission, 0);
    }

    public void createQueue(String msgVpnName, String queueName, boolean exclusive,
        PermissionEnum permission, int partitionsCount) throws SempClientException {
      final ConfigMsgVpnQueue queue = new ConfigMsgVpnQueue();
      queue.setQueueName(queueName);
      queue.setEgressEnabled(true);
      queue.setIngressEnabled(true);
      queue.setPermission(permission);
      queue.setMaxBindCount(10000L);
      queue.setMaxMsgSpoolUsage(1500L);

      if (exclusive) {
        queue.setAccessType(AccessTypeEnum.EXCLUSIVE);
      } else {
        queue.setAccessType(AccessTypeEnum.NON_EXCLUSIVE);
      }

      if (partitionsCount > 0) {
        queue.setPartitionCount(partitionsCount);
      }

      try {
        final ConfigMsgVpnQueueResponse response = this.queueApi
            .createMsgVpnQueue(msgVpnName, queue, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Queue {} created in vpn {}", queueName, msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Queue %s could not be created", msgVpnName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Creation of a queue", this.apiClient);
      }
    }

    public void updateQueue(ConfigMsgVpnQueue q) throws SempClientException {
      try {
        final ConfigMsgVpnQueueResponse response = this.queueApi
            .updateMsgVpnQueue(q.getMsgVpnName(), q.getQueueName(), q, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Queue {} updated in vpn {}", q.getQueueName(), q.getMsgVpnName());
          return;
        } else {
          throw new SempClientException(
              String.format("Queue %s could not be updated", q.getMsgVpnName()));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Update of a queue", this.apiClient);
      }
    }

    private ConfigMsgVpnQueue queryQueue(String msgVpnName, String queueName)
        throws SempClientException {
      try {
        final ConfigMsgVpnQueueResponse response = this.queueApi.getMsgVpnQueue(msgVpnName,
            queueName, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final ConfigMsgVpnQueue vpnQueue = response.getData();
          return vpnQueue;
        } else {
          throw new SempClientException(
              String.format("Queue %s could not be found", msgVpnName));
        }
      } catch (ApiException e) {
        return BrokerConfiguratorBuilder.wrapAndRethrowException(e, "Creation of a queue",
            this.apiClient);
      }
    }

    public void addSubscriptionToQueue(String msgVpnName, String queueName,
        String subscriptionTopic)
        throws SempClientException {
      final ConfigMsgVpnQueueSubscription subscription = new ConfigMsgVpnQueueSubscription();
      subscription.setMsgVpnName(msgVpnName);
      subscription.setQueueName(queueName);
      subscription.setSubscriptionTopic(subscriptionTopic);

      try {
        final ConfigMsgVpnQueueSubscriptionResponse response = this.queueApi
            .createMsgVpnQueueSubscription(msgVpnName, queueName, subscription, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Subscription is {} created for the queue {} in vpn {}", subscriptionTopic,
              queueName, msgVpnName);
          return;
        } else {
          throw new SempClientException(
              String.format("Subscription %s could not be created", msgVpnName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Creation of a subscription", this.apiClient);
      }
    }

    public void disableEgressOnQueue(String msgVpnName, String queueName) {
      try {
        final ConfigMsgVpnQueuesResponse response = this.queueApi
            .getMsgVpnQueues(msgVpnName, 10, null, null, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final List<ConfigMsgVpnQueue> queues = response.getData();
          ConfigMsgVpnQueue q = queues.stream().filter(queue -> {
            if (queueName.equals(queue.getQueueName())) {
              return true;
            }
            return false;
          }).findFirst().orElseThrow(() -> {
            return new SempClientException(
                String.format("Can't shutdown the queue %s", msgVpnName));
          });

          q.setEgressEnabled(false);
          this.queueApi.updateMsgVpnQueue(msgVpnName, queueName, q, null, null);
        } else {
          throw new SempClientException(
              String.format("Can't shutdown the queue %s", queueName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Shutdown of the queue " + queueName, this.apiClient);
      }
    }

    public void reenableEgressOnQueue(String msgVpnName, String queueName) {
      try {
        final ConfigMsgVpnQueuesResponse response = this.queueApi
            .getMsgVpnQueues(msgVpnName, 10, null, null, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final List<ConfigMsgVpnQueue> queues = response.getData();
          ConfigMsgVpnQueue q = queues.stream().filter(queue -> {
            if (queueName.equals(queue.getQueueName())) {
              return true;
            }
            return false;
          }).findFirst().orElseThrow(() -> {
            return new SempClientException(
                String.format("Can't shutdown the queue %s", msgVpnName));
          });
          q.setEgressEnabled(true);
          this.queueApi.updateMsgVpnQueue(msgVpnName, queueName, q, null, null);
        } else {
          throw new SempClientException(
              String.format("Can't shutdown the queue %s", queueName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Shutdown of the queue " + queueName, this.apiClient);
      }
    }

    public void disableIngressOnQueue(String msgVpnName, String queueName) {
      try {
        final ConfigMsgVpnQueuesResponse response = this.queueApi
            .getMsgVpnQueues(msgVpnName, 10, null, null, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final List<ConfigMsgVpnQueue> queues = response.getData();
          ConfigMsgVpnQueue q = queues.stream().filter(queue -> {
            if (queueName.equals(queue.getQueueName())) {
              return true;
            }
            return false;
          }).findFirst().orElseThrow(() -> {
            return new SempClientException(
                String.format("Can't shutdown the queue %s", msgVpnName));
          });
          q.setIngressEnabled(false);
          this.queueApi.updateMsgVpnQueue(msgVpnName, queueName, q, null, null);
        } else {
          throw new SempClientException(
              String.format("Can't shutdown the queue %s", queueName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Shutdown of the queue " + queueName, this.apiClient);
      }
    }

    public void reenableIngressOnQueue(String msgVpnName, String queueName) {
      try {
        final ConfigMsgVpnQueuesResponse response = this.queueApi
            .getMsgVpnQueues(msgVpnName, 10, null, null, null, null);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          final List<ConfigMsgVpnQueue> queues = response.getData();
          ConfigMsgVpnQueue q = queues.stream().filter(queue -> {
            if (queueName.equals(queue.getQueueName())) {
              return true;
            }
            return false;
          }).findFirst().orElseThrow(() -> {
            return new SempClientException(
                String.format("Can't shutdown the queue %s", msgVpnName));
          });
          q.setIngressEnabled(true);
          this.queueApi.updateMsgVpnQueue(msgVpnName, queueName, q, null, null);
        } else {
          throw new SempClientException(
              String.format("Can't shutdown the queue %s", queueName));
        }
      } catch (ApiException e) {
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Shutdown of the queue " + queueName, this.apiClient);
      }
    }

    public void deleteQueue(String msgVpnName, String queueName) {
      try {
        final ConfigSempMetaOnlyResponse response = this.queueApi
            .deleteMsgVpnQueue(msgVpnName, queueName);
        if (HttpStatus.OK.value() == response.getMeta().getResponseCode()) {
          logger.debug("Queue Deleted!");
        } else {
          throw new SempClientException(
              String.format("Can't shutdown the queue %s", queueName));
        }
      } catch (ApiException e) {
        if (e.getResponseBody().contains("NOT_FOUND")) {
          return;
        }
        BrokerConfiguratorBuilder
            .wrapAndRethrowException(e, "Delete queue failed: " + queueName, this.apiClient);
      }
    }
  }
}