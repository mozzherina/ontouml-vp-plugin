package it.unibz.inf.ontouml.vp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.inf.ontouml.vp.model.*;
import it.unibz.inf.ontouml.vp.model.ontouml.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Class responsible for making requests to the Expose based on standard end points and
 * configured server URL.
 *
 * @author Elena Romanenko
 */
public class ExpoServerAccessController {

  private static final String ABSTRACT_SERVICE_ENDPOINT = "/abstract";
  private static final String FOLD_SERVICE_ENDPOINT = "/fold";
  private static final String EXPAND_SERVICE_ENDPOINT = "/expand";
  private static final String CLUSTER_SERVICE_ENDPOINT = "/cluster";
  private static final String FOCUS_SERVICE_ENDPOINT = "/focus";
  private static final String USER_MESSAGE_BAD_REQUEST =
      "There was a internal plugin error and the service could not be completed.";
  private static final String USER_MESSAGE_REQUEST_WITH_SYNTACTICAL_ERRORS =
      "Unable to execute request on a project containing syntactical errors.";
  private static final String USER_MESSAGE_NOT_FOUND = "Server not found.";
  private static final String USER_MESSAGE_CONNECTION_ERROR = "Unable to reach the server.";
  private static final String USER_MESSAGE_INTERNAL_ERROR = "Internal server error.";
  private static final String USER_MESSAGE_UNKNOWN_ERROR_RESPONSE =
      "Error receiving service response.";


  private static String getServiceRequestBody(String project, String options) {
    return "{ " + options + ", \"origin\": " + project + " }";
  }

  private static String getAbstractionRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomExpoServerEnabled()
            ? config.getExpoServerURL() + ABSTRACT_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_EXPO_SERVER_URL + ABSTRACT_SERVICE_ENDPOINT;
  }

  private static String getFoldRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomExpoServerEnabled()
            ? config.getExpoServerURL() + FOLD_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_EXPO_SERVER_URL + FOLD_SERVICE_ENDPOINT;
  }

  private static String getExpandRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomExpoServerEnabled()
            ? config.getExpoServerURL() + EXPAND_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_EXPO_SERVER_URL + EXPAND_SERVICE_ENDPOINT;
  }

  private static String getClusterRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomExpoServerEnabled()
            ? config.getExpoServerURL() + CLUSTER_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_EXPO_SERVER_URL + CLUSTER_SERVICE_ENDPOINT;
  }

  private static String getFocusRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomExpoServerEnabled()
            ? config.getExpoServerURL() + FOCUS_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_EXPO_SERVER_URL + FOCUS_SERVICE_ENDPOINT;
  }

  private static ExpoServiceResult parseExpoResponse(
          HttpURLConnection connection) throws IOException {
    if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
      return null;
    }

    if (!hasJsonContentType(connection)) {
      throw new IOException(USER_MESSAGE_UNKNOWN_ERROR_RESPONSE);
    }

    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(connection.getInputStream()));
    final String json = reader.lines().parallel().collect(Collectors.joining("\n"));

    Project updatedProject = new ObjectMapper().readValue(json, Project.class);
    return new ExpoServiceResult(updatedProject);
  }

  private static boolean hasJsonContentType(HttpURLConnection connection) {
    return connection.getContentType().contains("application/json");
  }

  public static ExpoServiceResult requestProjectAbstraction(String project, String options)
          throws IOException {
    final String body = getServiceRequestBody(project, options);
    final String url = getAbstractionRequestUrl();
    final HttpURLConnection connection = request(url, body);
    return parseExpoResponse(connection);
  }

  public static ExpoServiceResult requestProjectExplanation(String project, String options, String action)
          throws IOException {
    final String body = getServiceRequestBody(project, options);
    String url = "";
    switch (action) {
      case "foldClass":
        url = getFoldRequestUrl();
        break;
      case "expandClass":
        url = getExpandRequestUrl();
        break;
      case "clusterClass":
        url = getClusterRequestUrl();
        break;
      case "focusClass":
        url = getFocusRequestUrl();
        break;
    }
    final HttpURLConnection connection = request(url, body);
    return parseExpoResponse(connection);
  }

  private static HttpURLConnection request(String url, String body) throws IOException {
    try {
      final HttpURLConnection connection = performRequest(url, body);

      switch (connection.getResponseCode()) {
        case HttpURLConnection.HTTP_OK:
          return connection;
        case HttpURLConnection.HTTP_BAD_REQUEST:
          if (!url.contains("verify")) {
            // failed because the project contains syntactical errors
            throw new IOException(USER_MESSAGE_REQUEST_WITH_SYNTACTICAL_ERRORS);
          } else {
            throw new IOException(USER_MESSAGE_BAD_REQUEST);
          }
        case HttpURLConnection.HTTP_NOT_FOUND:
          throw new IOException(USER_MESSAGE_NOT_FOUND);
        case HttpURLConnection.HTTP_INTERNAL_ERROR:
          throw new IOException(USER_MESSAGE_INTERNAL_ERROR);
        default:
          throw new IOException(USER_MESSAGE_UNKNOWN_ERROR_RESPONSE);
      }
    } catch (SocketException e) {
      throw new IOException(USER_MESSAGE_CONNECTION_ERROR);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(USER_MESSAGE_UNKNOWN_ERROR_RESPONSE);
    }
  }

  private static HttpURLConnection performRequest(String urlString, String body)
      throws IOException {
    final URL url = new URL(urlString);
    final HttpURLConnection request = (HttpURLConnection) url.openConnection();

    request.setRequestMethod("POST");
    request.setRequestProperty("Content-Type", "application/json");
    request.setReadTimeout(120000);
    request.setDoOutput(true);

    final OutputStream requestStream = request.getOutputStream();
    final byte[] requestBody = body.getBytes();

    requestStream.write(requestBody, 0, requestBody.length);
    requestStream.flush();
    requestStream.close();

    return request;
  }
}
