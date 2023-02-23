/**
 * @author gaurav.likhar
 * @date 20/02/23
 * @project_name user-service
 **/

package com.stanzaliving.user.entity;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.sqljpa.entity.AbstractJpaEntity;
import com.stanzaliving.core.user.enums.App;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;


@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_app_session_config")
@Table(name = "user_app_session_config")
//todo -> add unique key constraint here for userId, app, status
public class UserAppSessionConfigEntity extends AbstractJpaEntity {

    private static final long serialVersionUID = -2557796655168870412L;

    @Column(name = "user_id", columnDefinition = "char(40)", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "app", columnDefinition = "varchar(30)")
    private App app;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", columnDefinition = "varchar(40)", nullable = false)
    private AccessLevel accessLevel = AccessLevel.USER;

    @Column(name = "max_login_allowed", columnDefinition = "int(11)")
    private int maxLoginAllowed;

}