package it.unibz.inf.ontouml.vp.controllers;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramUIModel;
import it.unibz.inf.ontouml.vp.model.ExpoServiceResult;
import it.unibz.inf.ontouml.vp.model.AbstractionOptions;
import it.unibz.inf.ontouml.vp.model.ontouml.Project;
import it.unibz.inf.ontouml.vp.model.ontouml2vp.IProjectLoader;
import it.unibz.inf.ontouml.vp.model.vp2ontouml.Uml2OntoumlTransformer;
import it.unibz.inf.ontouml.vp.utils.SimpleServiceWorker;
import it.unibz.inf.ontouml.vp.utils.ViewManagerUtils;

import java.io.IOException;
import java.util.List;

public class AbstractionController implements VPActionController {

  // private String elementId;
  private String abstractionRule;

  // public void setElementId(String elementId) {
    // this.elementId = elementId;
  // }

  public void setAbstractionRule(String abstractionRule) {
    this.abstractionRule = abstractionRule;
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
      System.out.println("Starting abstraction service...");
      System.out.println("Serializing project...");
      final String serializedProject = Uml2OntoumlTransformer.transformAndSerialize();
      final String activeDiagramId = ApplicationManager.instance().getDiagramManager().getActiveDiagram().getId();
      final String options = new AbstractionOptions(
              // activeDiagramId,
              // this.elementId,
              this.abstractionRule
      ).toJson();
      System.out.println(serializedProject);
      System.out.println("Project serialized!");

      System.out.println("Requesting diagrams from the abstraction service...");
      final ExpoServiceResult serviceResult =
          ExpoServerAccessController.requestProjectAbstraction(serializedProject, options);
      System.out.println("Request answered by Expose!");

      System.out.println(serviceResult.getIssues());

      // Load project
      System.out.println("Processing abstraction service response...");
      Project updatedProject = serviceResult.getResult();
      if (!context.isCancelled() && updatedProject != null) {
        IProjectLoader.load(updatedProject, true, false);
        ViewManagerUtils.log(serviceResult.getMessage());
        // IDiagramUIModel diagram = ApplicationManager.instance().getProjectManager().getProject().getDiagramById(activeDiagramId);
        // ApplicationManager.instance().getDiagramManager().openDiagram(diagram);
      }
      System.out.println("Abstraction service response processed!");
      System.out.println("Abstraction service concluded.");

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
