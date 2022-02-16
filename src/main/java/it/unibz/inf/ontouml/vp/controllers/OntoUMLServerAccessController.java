package it.unibz.inf.ontouml.vp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibz.inf.ontouml.vp.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Class responsible for making requests to the OntoUML Server based on standard end points and
 * configured server URL.
 *
 * @author Claudenir Fonseca
 * @author Victor Viola
 */
public class OntoUMLServerAccessController {

  private static final String TRANSFORM_GUFO_SERVICE_ENDPOINT = "/v1/transform/gufo";
  private static final String VERIFICATION_SERVICE_ENDPOINT = "/v1/verify";
  private static final String MODULARIZATION_SERVICE_ENDPOINT = "/v1/modularize";
  private static final String ABSTRACTION_SERVICE_ENDPOINT = "/v1/abstract";
  private static final String USER_MESSAGE_BAD_REQUEST =
      "There was a internal plugin error and the service could not be completed.";
  private static final String USER_MESSAGE_REQUEST_WITH_SYNTACTICAL_ERRORS =
      "Unable to execute request on a project containing syntactical errors.";
  private static final String USER_MESSAGE_NOT_FOUND = "Server not found.";
  private static final String USER_MESSAGE_CONNECTION_ERROR = "Unable to reach the server.";
  private static final String USER_MESSAGE_INTERNAL_ERROR = "Internal server error.";
  private static final String USER_MESSAGE_UNKNOWN_ERROR_RESPONSE =
      "Error receiving service response.";

  private static String getServiceRequestBody(String project) {
    return "{\"options\": null, \"project\": " + project + "}";
  }

  private static String getServiceRequestBody(String project, String options) {
    return "{\"options\": " + options + ", \"project\": " + project + "}";
  }

  private static String getModularizationRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomServerEnabled()
        ? config.getServerURL() + MODULARIZATION_SERVICE_ENDPOINT
        : ProjectConfigurations.DEFAULT_SERVER_URL + MODULARIZATION_SERVICE_ENDPOINT;
  }

  private static String getAbstractionRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomServerEnabled()
            ? config.getServerURL() + ABSTRACTION_SERVICE_ENDPOINT
            : ProjectConfigurations.DEFAULT_SERVER_URL + ABSTRACTION_SERVICE_ENDPOINT;
  }

  private static String getVerificationRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomServerEnabled()
        ? config.getServerURL() + VERIFICATION_SERVICE_ENDPOINT
        : ProjectConfigurations.DEFAULT_SERVER_URL + VERIFICATION_SERVICE_ENDPOINT;
  }

  private static String getTransformationToGufoRequestUrl() {
    final ProjectConfigurations config = Configurations.getInstance().getProjectConfigurations();
    return config.isCustomServerEnabled()
        ? config.getServerURL() + TRANSFORM_GUFO_SERVICE_ENDPOINT
        : ProjectConfigurations.DEFAULT_SERVER_URL + TRANSFORM_GUFO_SERVICE_ENDPOINT;
  }

  private static <T extends ServiceResult<?>> T parseResponse(
      HttpURLConnection connection, Class<T> _class) throws IOException {
    if (connection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
      return null;
    }

    if (!hasJsonContentType(connection)) {
      throw new IOException(USER_MESSAGE_UNKNOWN_ERROR_RESPONSE);
    }

    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(connection.getInputStream()));
    final String json = reader.lines().parallel().collect(Collectors.joining("\n"));
    return new ObjectMapper().readValue(json, _class);
  }

  private static boolean hasJsonContentType(HttpURLConnection connection) {
    return connection.getContentType().contains("application/json");
  }

  public static ModularizationServiceResult requestProjectModularization(String project)
      throws IOException {
    final String body = getServiceRequestBody(project);
    final String url = getModularizationRequestUrl();
    final HttpURLConnection connection = request(url, body);

    return parseResponse(connection, ModularizationServiceResult.class);
  }

  public static AbstractionServiceResult requestProjectAbstraction(String project)
          throws IOException {
    final String body = getServiceRequestBody(project);
    final String url = getAbstractionRequestUrl();
    final HttpURLConnection connection = request(url, body);

    return parseResponse(connection, AbstractionServiceResult.class);
  }

  public static VerificationServiceResult requestModelVerification(String project)
      throws IOException {
    final String url = getVerificationRequestUrl();
    final String body = getServiceRequestBody(project);
    final HttpURLConnection connection = request(url, body);

    return parseResponse(connection, VerificationServiceResult.class);
  }

  public static GufoTransformationServiceResult requestModelTransformationToGufo(
      String project, String options) throws IOException {
    final String url = getTransformationToGufoRequestUrl();
    final String body = getServiceRequestBody(project, options);
    final HttpURLConnection connection = request(url, body);

    return parseResponse(connection, GufoTransformationServiceResult.class);
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

  //  public static GufoTransformationServiceResult requestProjectTransformationToGufo(
  //      String project, String options) {
  //    final String body = getServiceRequestBody(project, options);
  //    final String url = getTransformationToGufoRequestUrl();
  //
  //    try {
  //      final HttpURLConnection connection = performRequest(url, body);
  //
  //      switch (connection.getResponseCode()) {
  //        case HttpURLConnection.HTTP_OK:
  //          if (hasJsonContentType(connection)) {
  //            return parseResponse(connection, GufoTransformationServiceResult.class);
  //          }
  //        case HttpURLConnection.HTTP_BAD_REQUEST:
  //        case HttpURLConnection.HTTP_NOT_FOUND:
  //        case HttpURLConnection.HTTP_INTERNAL_ERROR:
  //        default:
  //          System.err.println("Attention! Transformation request was not processed correctly");
  //          System.err.println("Status Code: " + connection.getResponseCode());
  //      }
  //    } catch (IOException ioException) {
  //      ioException.printStackTrace();
  //    }
  //
  //    return null;
  //  }

  private static HttpURLConnection performRequest(String urlString, String body)
      throws IOException {
    final URL url = new URL(urlString);
    final HttpURLConnection request = (HttpURLConnection) url.openConnection();

    request.setRequestMethod("POST");
    request.setRequestProperty("Content-Type", "application/json");
    request.setReadTimeout(60000);
    request.setDoOutput(true);

    final OutputStream requestStream = request.getOutputStream();
    final byte[] requestBody = body.getBytes();

    requestStream.write(requestBody, 0, requestBody.length);
    requestStream.flush();
    requestStream.close();

    return request;
  }
}
