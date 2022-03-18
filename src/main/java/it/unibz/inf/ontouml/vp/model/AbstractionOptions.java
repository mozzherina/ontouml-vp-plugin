package it.unibz.inf.ontouml.vp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AbstractionOptions {
    private String activeDiagramId;
    private String activeElementId;
    private String abstractionRule;

    public AbstractionOptions (String activeDiagramId, String activeElementId, String abstractionRule) {
        this.activeDiagramId = activeDiagramId;
        this.activeElementId = activeElementId;
        this.abstractionRule = abstractionRule;
    }

    public String getActiveDiagramId() {
        return this.activeDiagramId;
    }

    public void setActiveDiagramId(String activeDiagramId) {
        this.activeDiagramId = activeDiagramId;
    }

    public String getActiveElementId() {
        return this.activeElementId;
    }

    public void setActiveElementId(String activeElementId) {
        this.activeElementId = activeElementId;
    }

    public String getAbstractionRule() {
        return this.abstractionRule;
    }

    public void setAbstractionRule(String abstractionRule) {
        this.abstractionRule = abstractionRule;
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
