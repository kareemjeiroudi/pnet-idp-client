/**
 * 
 */
package at.porscheinformatik.idp.openidconnect;

import static at.porscheinformatik.idp.openidconnect.PartnerNetOAuth2AuthorizationRequestResolver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.web.util.UriComponentsBuilder.*;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * @author Daniel Furtlehner
 */
public class PartnerNetOAuth2AuthorizationRequestResolverTest
{
    private static final String BASE_URI = "/oauth/authorize";
    private static final String CLIENT_ID = "me_myself_and_i";

    @Test
    public void testAcrParameter()
    {
        PartnerNetOAuth2AuthorizationRequestResolver resolver = buildResolver();

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(BASE_URI + "/pnet");
        builder = requestNistAuthenticationLevels(builder, 2, 3);
        MockHttpServletRequest request = buildRequest(builder);

        OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request);

        UriComponents requestUri = fromHttpUrl(authorizationRequest.getAuthorizationRequestUri()).build(true);

        MultiValueMap<String, String> queryParams = requestUri.getQueryParams();
        assertThat(requestUri.getScheme(), equalTo("https"));
        assertThat(requestUri.getHost(), equalTo("idp.com"));
        assertThat(requestUri.getPath(), equalTo("/oauth2/authorize"));
        assertThat(queryParams.getFirst("client_id"), equalTo(CLIENT_ID));
        assertThat(queryParams.getFirst("response_type"), equalTo("code"));
        assertThat(queryParams.get("redirect_uri"), contains("https://localhost:8443/redirect/uri"));
        assertThat(queryParams.getFirst("state"), not(emptyOrNullString()));
        assertThat(queryParams.getFirst("claims"),
            equalTo(UriUtils
                .encodeQueryParam("{\"id_token\":{\"acr\": {\"values\": [\"2,3\"], \"essential\": true}}}",
                    Charset.forName("UTF-8"))));
    }

    protected MockHttpServletRequest buildRequest(UriComponentsBuilder builder)
    {
        UriComponents uri = builder.build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo(uri.getPath());
        request.setQueryString(uri.getQuery());

        for (Entry<String, List<String>> param : uri.getQueryParams().entrySet())
        {
            request.addParameter(param.getKey(), param.getValue().toArray(new String[param.getValue().size()]));
        }

        return request;
    }

    private PartnerNetOAuth2AuthorizationRequestResolver buildResolver()
    {
        ClientRegistrationRepository clientRegistrationRepository = buildClientRegistrationRepository();
        return new PartnerNetOAuth2AuthorizationRequestResolver(clientRegistrationRepository, BASE_URI);
    }

    private ClientRegistrationRepository buildClientRegistrationRepository()
    {
        ClientRegistration registration = ClientRegistration //
            .withRegistrationId("pnet")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientId(CLIENT_ID)
            .redirectUri("https://localhost:8443/redirect/uri")
            .authorizationUri("https://idp.com/oauth2/authorize")
            .tokenUri("https://idp.com/oauth2/token")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
