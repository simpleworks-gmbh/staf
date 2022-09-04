package de.simpleworks.staf.framework.util.neotys;

import static com.neotys.selenium.proxies.helpers.ModeHelper.getSetting;
import static com.neotys.selenium.proxies.helpers.ModeHelper.Mode.DESIGN;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import org.apache.olingo.odata2.api.exception.ODataException;

import com.google.common.base.Optional;
import com.neotys.rest.design.client.DesignAPIClient;
import com.neotys.rest.error.NeotysAPIException;
import com.neotys.selenium.proxies.helpers.ModeHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;

public class DesignAPIClientFactory {

	public static Optional<DesignAPIClient> createDesignAPIClient() {
		final String designAPIURL = getSetting(SeleniumProxyConfig.OPT_DESIGN_API_URL,
				SeleniumProxyConfig.getDesignAPIURL());
		/* How to access NeoLoad. */
		final String designAPIKey = getSetting(SeleniumProxyConfig.OPT_API_KEY, "");

		DesignAPIClient designAPIClient = null;
		try {
			// debugMessage("Connecting to design API server. URL: " + designAPIURL + ", API
			// key: " + designAPIKey);
			designAPIClient = com.neotys.rest.design.client.DesignAPIClientFactory.newClient(designAPIURL,
					designAPIKey);
		} catch (final GeneralSecurityException | IOException | ODataException | URISyntaxException
				| NeotysAPIException e) {

			if (!DESIGN.equals(ModeHelper.getMode())) {
				return Optional.absent();
			}
			// throw exception only when design API client is required.
			throw new RuntimeException("Issue contacting DesignAPI server.", e);
		}
		return Optional.of(designAPIClient);
	}
}
