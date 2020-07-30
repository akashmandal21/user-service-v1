package com.stanzaliving.user.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.User;
import com.google.api.services.directory.model.Users;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.stanzaliving.user.service.GSuiteService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Log4j2
public class GSuiteServiceImpl implements GSuiteService {


    @Autowired
    GoogleCredentials googleCredentials;

    @Value("$spring.application.name")
    private String applicationName;

    private AccessToken getAccessToken() {

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

    @Override
    public List<String> getUsers() {

        List<Member> memberList = new ArrayList<>();
        List<User> userList = new ArrayList<>();

        try {
            Directory directory = getDirectory();

            Directory.Users.List usersRequest = directory.users().list();
            do {
                Users usersResponse = usersRequest.setShowDeleted("true").execute();
                userList.addAll(usersResponse.getUsers());
                usersRequest.setPageToken(usersResponse.getNextPageToken());
            } while (usersRequest.getPageToken() != null && usersRequest.getPageToken().length() > 0);

            memberList = directory.members().list("stranzaliving.com").execute().getMembers();

        } catch (GeneralSecurityException | IOException e) {
            log.error("Exception while getting users from google " + e.getMessage(), e);
        }

        memberList.stream().forEach(member -> log.info(member.getStatus() + member.getEmail() + member.getRole() + member));
        userList.stream().forEach(user -> log.info(user.getEmails().toString() + user.getDeletionTime().toString()));

        return Collections.EMPTY_LIST;
    }

    private Directory getDirectory() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        googleCredentials.refreshIfExpired();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);

        return new Directory.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(applicationName)
                .build();
    }

}
