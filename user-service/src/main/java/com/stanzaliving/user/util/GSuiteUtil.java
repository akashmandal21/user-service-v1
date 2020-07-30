package com.stanzaliving.user.util;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Objects;

/**
 * @author piyush srivastava "piyush.srivastava@stanzaliving.com"
 *
 * @date 30-July-2020
 */

@Log4j2
@UtilityClass
public class GSuiteUtil {
	public AccessToken getAccessToken(GoogleCredentials googleCredentials) {

		AccessToken token = null;
		try {

			if(null == googleCredentials) {
				return token;
			}
			googleCredentials.refreshIfExpired();

			token = googleCredentials.getAccessToken();

			if (Objects.isNull(token)) {

				googleCredentials.refresh();

				token = googleCredentials.getAccessToken();

			}

		} catch (IOException e) {

			log.error("Exception while getting token for google " + e.getMessage(), e);

		}

		return token;

	}
}
