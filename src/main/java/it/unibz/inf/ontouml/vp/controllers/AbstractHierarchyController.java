package it.unibz.inf.ontouml.vp.controllers;

import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;


public class AbstractHierarchyController implements VPActionController {

    @Override
    public void performAction(VPAction vpAction) {
        AbstractionController abstraction = new AbstractionController();
        abstraction.setAbstractionRule("hierarchy");
        abstraction.performAction(vpAction);
    }

    @Override
    public void update(VPAction vpAction) {}
}
