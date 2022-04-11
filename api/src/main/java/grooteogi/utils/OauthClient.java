package grooteogi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import grooteogi.dto.OauthDto;
import grooteogi.dto.UserDto;
import grooteogi.exception.ApiException;
import grooteogi.exception.ApiExceptionEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OauthClient {
  private final RestTemplate restTemplate;

  @Value("${custom.oauth2.kakao.client-id}")
  private String kakaoClientId;
  @Value("${custom.oauth2.kakao.client-secret}")
  private String kakaoClientSecret;

  @Autowired
  public OauthClient(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public UserDto kakaoToken(OauthDto oauthDto) {
    String accessToken = "";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", kakaoClientId);
    params.add("client_secret", kakaoClientSecret);
    params.add("redirect_uri", "http://localhost:8080/user/oauth");
    params.add("code", oauthDto.getCode());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    String url = "https://kauth.kakao.com/oauth/token";
    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
      JsonParser parser = new JsonParser();
      JsonElement element = parser.parse(response.getBody());
      accessToken = element.getAsJsonObject().get("access_token").getAsString();
      UserDto userDto = kakaoAuth(accessToken);
      userDto.setType(oauthDto.getType());
      return userDto;
    } catch (RestClientException e) {
      throw new ApiException(ApiExceptionEnum.LOGIN_FAIL_EXCEPTION);
    }
  }

  private UserDto kakaoAuth(String token) {
    UserDto userDto = new UserDto();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    String url = "https://kapi.kakao.com/v2/user/me";
    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
      JsonParser parser = new JsonParser();
      JsonElement element = parser.parse(response.getBody());

      JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
      JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

      userDto.setNickname(properties.get("nickname").getAsString());
      userDto.setEmail(kakaoAccount.get("email").getAsString());
    } catch (RestClientException e) {
      throw new ApiException(ApiExceptionEnum.UNAUTHORIZED_EXCEPTION);
    }
    return userDto;
  }

  public UserDto googleToken(OauthDto oauthDto) {
    return null;
  }

  private UserDto googleAuth(OauthDto oauthDto) {

    return null;
  }
}