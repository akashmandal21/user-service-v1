package com.stanzaliving.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferTo {

    CLUSTER_MANAGER("Cluster Manager"),
    NODAL("Nodal"),
    STANZA_ACCOUNT("Stanza Account");

    private final String name;
}
