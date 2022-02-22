package it.unibz.inf.ontouml.vp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractionOptions {
    private String activeDiagramId;

    public AbstractionOptions (String activeDiagramId) {
        this.activeDiagramId = activeDiagramId;
    }

    public String getActiveDiagramId() {
        return activeDiagramId;
    }

    public void setActiveDiagramId(String activeDiagramId) {
        this.activeDiagramId = activeDiagramId;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
