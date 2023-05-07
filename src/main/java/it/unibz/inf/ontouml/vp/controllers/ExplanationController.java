package it.unibz.inf.ontouml.vp.controllers;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import it.unibz.inf.ontouml.vp.model.ExpoServiceResult;
import it.unibz.inf.ontouml.vp.model.ExpoOptions;
import it.unibz.inf.ontouml.vp.model.ontouml.Project;
import it.unibz.inf.ontouml.vp.model.ontouml2vp.IProjectLoader;
import it.unibz.inf.ontouml.vp.model.vp2ontouml.Uml2OntoumlTransformer;
import it.unibz.inf.ontouml.vp.utils.SimpleServiceWorker;
import it.unibz.inf.ontouml.vp.utils.ViewManagerUtils;

import java.io.IOException;
import java.util.List;

public class ExplanationController implements VPActionController {

  private String elementId;
  private String actionType;

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public void setActionType(String actionId) {
    String[] originalId = actionId.split(".");
    this.actionType = originalId[originalId.length-1];
  }

  @Override
  public void performAction(VPAction vpAction) {
    final SimpleServiceWorker worker = new SimpleServiceWorker(this::task);
    worker.execute();
  }

  @Override
  public void update(VPAction vpAction) {}

  private List<String> task(SimpleServiceWorker context) {
    try {
      System.out.println("Starting Expo service...");
      System.out.println("Serializing project...");
      final String serializedProject = Uml2OntoumlTransformer.transformAndSerialize();
      final String options = new ExpoOptions(
              this.elementId,
              this.actionType
      ).toJson();
      System.out.println(serializedProject);
      System.out.println("Project serialized!");

      System.out.println("Requesting results from the Expo service...");
      final ExpoServiceResult serviceResult =
          ExpoServerAccessController.requestProjectExplanation(serializedProject, options, this.actionType);
      System.out.println("Request answered by Expose!");

      System.out.println(serviceResult.getIssues());

      // Load project
      System.out.println("Processing Expo response...");
      Project expoProject = serviceResult.getResult();
      if (!context.isCancelled() && expoProject != null) {
        IProjectLoader.load(expoProject, false, false);
        ViewManagerUtils.log(serviceResult.getMessage());
      }
      System.out.println("Expo service response processed!");
      System.out.println("Expo service concluded.");

      return List.of(serviceResult.getMessage());
    } catch (IOException e) {
      if (!context.isCancelled()) {
        ViewManagerUtils.log(e.getMessage());
      }

      e.printStackTrace();
      return List.of(e.getMessage());
    }
  }
}
