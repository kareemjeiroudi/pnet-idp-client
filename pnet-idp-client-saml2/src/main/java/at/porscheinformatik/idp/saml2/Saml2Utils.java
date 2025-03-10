package at.porscheinformatik.idp.saml2;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.security.impl.SecureRandomIdentifierGenerationStrategy;

public class Saml2Utils
{
    private static final String RELAY_STATE_PARAM = "RelayState";
    public static final String SUBJECT_ID_NAME = "urn:oasis:names:tc:SAML:attribute:subject-id";
    public static final String PAIRWISE_ID_NAME = "urn:oasis:names:tc:SAML:attribute:pairwise-id";

    public static final Duration CLOCK_SKEW = Duration.ofMinutes(5);

    private static final String AUTHN_REQUEST_ID_ATTR = "poi.saml2.authn_request_id";
    private static final String FORCE_AUTHENTICATION_PARAM = "forceAuthn";
    private static final String NIST_LEVEL_PARAM = "nistLevel";
    private static final String MAX_SESSION_AGE_PARAM = "maxSessionAge";

    //Specification says between 128 and 160 bit are perfect
    private static final IdentifierGenerationStrategy ID_GENERATOR = new SecureRandomIdentifierGenerationStrategy(20);

    /**
     * @return a random indentifier for saml messages
     */
    public static String generateId()
    {
        return ID_GENERATOR.generateIdentifier();
    }

    public static void storeAuthnRequestId(HttpServletRequest request, String id)
    {
        request.getSession().setAttribute(AUTHN_REQUEST_ID_ATTR, id);
    }

    public static Optional<String> retrieveAuthnRequestId(HttpServletRequest request)
    {
        return Optional.ofNullable((String) request.getSession().getAttribute(AUTHN_REQUEST_ID_ATTR));
    }

    public static UriComponentsBuilder forceAuthentication(UriComponentsBuilder uriComponentsBuilder)
    {
        return uriComponentsBuilder.queryParam(FORCE_AUTHENTICATION_PARAM, true);
    }

    public static boolean isForceAuthentication(HttpServletRequest request)
    {
        return Boolean.valueOf(request.getParameter(FORCE_AUTHENTICATION_PARAM));
    }

    public static UriComponentsBuilder maxSessionAge(UriComponentsBuilder uriComponentsBuilder,
        Integer sessionAgeInSeconds)
    {
        return uriComponentsBuilder.queryParam(MAX_SESSION_AGE_PARAM, sessionAgeInSeconds);
    }

    public static Optional<Integer> retrieveMaxSessionAge(HttpServletRequest request)
    {
        String value = request.getParameter(MAX_SESSION_AGE_PARAM);

        if (value != null)
        {
            return Optional.of(Integer.parseInt(value));
        }

        return Optional.empty();
    }

    public static UriComponentsBuilder requestNistAuthenticationLevel(UriComponentsBuilder uriComponentsBuilder,
        int nistLevel)
    {
        List<AuthnContextClass> supportedValues = AuthnContextClass.getAsLeastAsStrongAs(nistLevel);

        if (supportedValues.isEmpty())
        {
            int maxValue =
                supportedValues.stream().map(AuthnContextClass::getNistLevel).max(Integer::compare).orElse(0);

            throw new IllegalArgumentException(
                String.format("Nist level %s not supported. Please use a lower or equals to %s", nistLevel, maxValue));
        }

        return uriComponentsBuilder.queryParam(NIST_LEVEL_PARAM, nistLevel);
    }

    public static Optional<Integer> getRequestedNistAuthenticationLevel(HttpServletRequest request)
    {
        String value = request.getParameter(NIST_LEVEL_PARAM);

        if (value != null)
        {
            return Optional.of(Integer.parseInt(value));
        }

        return Optional.empty();
    }

    public static UriComponentsBuilder setRelayState(UriComponentsBuilder uriComponentsBuilder, String relayState)
    {
        return uriComponentsBuilder.queryParam(RELAY_STATE_PARAM, relayState);
    }

    public static Optional<String> getRelayState(HttpServletRequest request)
    {
        return Optional.ofNullable(request.getParameter(RELAY_STATE_PARAM));
    }

    /**
     * Removes all SAML Processing related parameters from the query part of the given url, if any.
     * 
     * @param url the url to sanitize
     */
    public static String sanitizeUrl(String url)
    {
        if (!url.contains("?"))
        {
            return url;
        }

        return UriComponentsBuilder
            .fromUriString(url)
            .replaceQueryParam(FORCE_AUTHENTICATION_PARAM)
            .replaceQueryParam(MAX_SESSION_AGE_PARAM)
            .replaceQueryParam(NIST_LEVEL_PARAM)
            .replaceQueryParam(RELAY_STATE_PARAM)
            .toUriString();
    }
}
