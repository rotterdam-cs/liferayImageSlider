/*=== new version ===*/
package com.rcs.portlet.slider.util.webcontent;

import com.liferay.util.portlet.PortletProps;

public class ArticleStructure {

    private static String structureId;

    public static String getStructureId(){

        if (structureId == null) {
            structureId = PortletProps.get("article.structure.id");
        }
        return structureId;

    }
}
