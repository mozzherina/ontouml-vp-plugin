package it.unibz.inf.ontouml.vp.model;

import it.unibz.inf.ontouml.vp.model.ontouml.Project;

import java.util.List;

public class ExpoServiceResult extends ServiceResult<Project> {

  public ExpoServiceResult() {
    super();
  }

  public ExpoServiceResult(Project result, List<ServiceIssue> issues) {
    super(result, issues);
  }

  public ExpoServiceResult(Project result) {
    super(result);
  }

  @Override
  public String getMessage() {
    return "The request to Expo has concluded";
  }
}
