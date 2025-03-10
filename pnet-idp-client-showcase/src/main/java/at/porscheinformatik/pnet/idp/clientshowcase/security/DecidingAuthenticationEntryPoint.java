/**
 * 
 */
package at.porscheinformatik.pnet.idp.clientshowcase.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.util.UriComponentsBuilder;

import at.porscheinformatik.idp.openidconnect.PartnerNetOAuth2AuthorizationRequestResolver;
import at.porscheinformatik.idp.saml2.Saml2Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This authentication entry point decides what authentication should be used based on the "authenticationType" query
 * parameter. This is application dependent. The showcase wants to show off different authentication techniques,
 * therefor this entry point.
 * 
 * @author Daniel Furtlehner
 */
public class DecidingAuthenticationEntryPoint implements AuthenticationEntryPoint
{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException
    {
        String uri = buildUri(request).toUriString();

        response.sendRedirect(uri);
    }

    private UriComponentsBuilder buildUri(HttpServletRequest request)
    {
        String authentiationType = request.getParameter("authenticationType");

        if (authentiationType == null)
        {
            throw new IllegalArgumentException("The authenticationType query parameter is mandatory.");
        }

        switch (authentiationType)
        {
            case "oidc":
                return UriComponentsBuilder.fromPath("/oauth2/authorization/pnet");

            // OpenID Connect with multifactor authentication
            case "oidc_mfa":
                return PartnerNetOAuth2AuthorizationRequestResolver
                    .requestNistAuthenticationLevels(UriComponentsBuilder.fromPath("/oauth2/authorization/pnet"), 3);

            case "saml2":
                return UriComponentsBuilder.fromPath("/saml2/authenticate/pnet");

            // SAML 2 with forced authentication
            case "saml2_force":
                return Saml2Utils.forceAuthentication(UriComponentsBuilder.fromPath("/saml2/authenticate/pnet"));

            // SAML 2 with multifactor authentication
            case "saml2_mfa":
                return Saml2Utils
                    .requestNistAuthenticationLevel(UriComponentsBuilder.fromPath("/saml2/authenticate/pnet"), 3);

            default:
                throw new IllegalArgumentException("Unsupported authenticationType " + authentiationType);
        }
    }

}
