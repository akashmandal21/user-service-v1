/**
 * @author gaurav.likhar
 * @date 20/02/23
 * @project_name user-service
 **/

package com.stanzaliving.user.entity;

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
@Entity(name = "user_app_device_config")
@Table(name = "user_app_device_config",  uniqueConstraints = { @UniqueConstraint(name = "UK_user_app_device_status", columnNames = { "user_id", "app", "device_id", "status" }) })
public class UserAppDeviceConfigEntity extends AbstractJpaEntity {

    private static final long serialVersionUID = -2557796655168870412L;

    @Column(name = "user_id", columnDefinition = "char(40)", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "app", columnDefinition = "varchar(30)")
    private App app;

    @Column(name = "device_id", columnDefinition = "varchar(64)")
    private String deviceId;

}
