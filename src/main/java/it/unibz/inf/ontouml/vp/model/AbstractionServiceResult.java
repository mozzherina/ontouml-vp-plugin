package it.unibz.inf.ontouml.vp.model;

import it.unibz.inf.ontouml.vp.model.ontouml.Project;

import java.util.List;

public class AbstractionServiceResult extends ServiceResult<Project> {

  public AbstractionServiceResult() {
    super();
  }

  public AbstractionServiceResult(Project result, List<ServiceIssue> issues) {
    super(result, issues);
  }

  public AbstractionServiceResult(Project result) {
    super(result);
  }

  @Override
  public String getMessage() {
    return "The abstraction request has concluded";
  }
}
