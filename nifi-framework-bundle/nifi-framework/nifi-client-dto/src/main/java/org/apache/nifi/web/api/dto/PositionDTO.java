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

/**
 * A position on the canvas.
 */
@XmlType(name = "position")
public class PositionDTO {

    private Double x;
    private Double y;

    public PositionDTO() {
    }

    public PositionDTO(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    /* getters / setters */
    /**
     * @return the x coordinate
     */
    @Schema(description = "The x coordinate."
    )
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    /**
     * @return the y coordinate
     */
    @Schema(description = "The y coordinate."
    )
    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

}
