package com.vpm.projectserver.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagTemplate {

    /*
     * itemId field matches object's ID in database
     *
     * This is to help frontend use 'key' for mapping data to components
     */
    private long itemId;
    private String tagName;

}
