package it.unibz.inf.ontouml.vp.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExpoOptions {
    private String activeElementId;
    private String actionType;

    public ExpoOptions (String activeElementId, String actionType) {
        this.activeElementId = activeElementId;
        this.actionType = actionType;
    }

    public String getActiveElementId() {
        return this.activeElementId;
    }

    public void setActiveElementId(String activeElementId) {
        this.activeElementId = activeElementId;
    }

    public String getActionType() {
        return this.actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String toJson() throws JsonProcessingException {
        String result = "\"node\": \"" + this.activeElementId + "\", ";
        switch (this.actionType) {
            case "foldClass":
                result += "\"long_names\": true, \"mult_relations\": false, ";
                break;
            case "expandClass":
                result += "\"limit\": 10, ";
                break;
            case "focusClass":
                result += "\"hop\": 2, ";
                break;
        }
        return result + "\"in_format\": \"json\", \"out_format\": \"json\"";
    }
}
