package it.unibz.inf.ontouml.vp.controllers;

import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPContext;
import com.vp.plugin.action.VPContextActionController;
import com.vp.plugin.model.IModelElement;
import java.awt.event.ActionEvent;

public class AbstractClassController implements VPContextActionController {

    @Override
    public void performAction(VPAction action, VPContext context, ActionEvent event) {
        final IModelElement clickedElement = context.getModelElement();
        final String elementId = clickedElement.getId();

        AbstractionController abstraction = new AbstractionController();
        abstraction.setElementId(elementId);
        abstraction.performAction(action);
    }

    @Override
    public void update(VPAction action, VPContext context) {}
}
