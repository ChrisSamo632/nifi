/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "flowFileSummary")
public class FlowFileSummaryDTO {

    private String uri;

    private String uuid;
    private String filename;
    private String mimeType;
    private Integer position;
    private Long size;
    private Long queuedDuration;
    private Long lineageDuration;
    private Long penaltyExpiresIn;
    private Boolean isPenalized;

    private String clusterNodeId; // include when clustered
    private String clusterNodeAddress; // include when clustered

    /**
     * @return the FlowFile uri
     */
    @Schema(description = "The URI that can be used to access this FlowFile."
    )
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the FlowFile uuid
     */
    @Schema(description = "The FlowFile UUID."
    )
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the FlowFile filename
     */
    @Schema(description = "The FlowFile filename."
    )
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the FlowFile filename
     */
    @Schema(description = "The FlowFile mime type."
    )
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the FlowFile's position in the queue.
     */
    @Schema(description = "The FlowFile's position in the queue."
    )
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * @return the FlowFile file size
     */
    @Schema(description = "The FlowFile file size."
    )
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return how long this FlowFile has been enqueued
     */
    @Schema(description = "How long this FlowFile has been enqueued."
    )
    public Long getQueuedDuration() {
        return queuedDuration;
    }

    public void setQueuedDuration(Long queuedDuration) {
        this.queuedDuration = queuedDuration;
    }

    /**
     * @return duration since the FlowFile's greatest ancestor entered the flow
     */
    @Schema(description = "Duration since the FlowFile's greatest ancestor entered the flow."
    )
    public Long getLineageDuration() {
        return lineageDuration;
    }

    public void setLineageDuration(Long lineageDuration) {
        this.lineageDuration = lineageDuration;
    }

    /**
     * @return when the FlowFile will no longer be penalized
     */
    @Schema(description = "How long in milliseconds until the FlowFile penalty expires."
    )
    public Long getPenaltyExpiresIn() {
        return penaltyExpiresIn;
    }

    public void setPenaltyExpiresIn(Long penaltyExpiration) {
        penaltyExpiresIn = penaltyExpiration;
    }

    /**
     * @return if the FlowFile is penalized
     */
    @Schema(description = "If the FlowFile is penalized."
    )
    public Boolean getPenalized() {
        return isPenalized;
    }

    public void setPenalized(Boolean penalized) {
        isPenalized = penalized;
    }

    /**
     * @return The id of the node where this FlowFile resides.
     */
    @Schema(description = "The id of the node where this FlowFile resides."
    )
    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    /**
     * @return label for the node where this FlowFile resides
     */
    @Schema(description = "The label for the node where this FlowFile resides."
    )
    public String getClusterNodeAddress() {
        return clusterNodeAddress;
    }

    public void setClusterNodeAddress(String clusterNodeAddress) {
        this.clusterNodeAddress = clusterNodeAddress;
    }
}
