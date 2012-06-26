/*=== new version ===*/
package com.rcs.portlet.slider.util.webcontent;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;

import javax.portlet.PortletRequest;
import java.util.ArrayList;
import java.util.List;

public class SliderArticleUtil {

    private static Log _log = LogFactoryUtil.getLog(SliderArticleUtil.class);

    public static SliderArticle getSliderArticle(JournalArticle journalArticle, String languageId){

        SliderArticle sliderArticle = new SliderArticle();

        sliderArticle.setArticleId(journalArticle.getArticleId());

        try {
            String content = journalArticle.getContentByLocale(languageId);

            if (Validator.isNotNull(content)) {

                Document document = SAXReaderUtil.read(content);

                Element rootElement = document.getRootElement();

                List<Element> elements = rootElement.elements();

                for (Element element: elements) {

                    String elementName = element.attribute("name").getValue();

                    List<Element> contentElements = element.elements("dynamic-content");

                    for (Element contentElement : contentElements) {
                        String contentLanguageId = contentElement.attribute("language-id") != null ?
                                                            contentElement.attribute("language-id").getValue() :
                                                            null;
                        if (contentLanguageId == null || "".equals(contentLanguageId)) {

                            sliderArticle.setField(elementName, contentElement.getText());
                            break;

                        } else if (languageId.equals(contentLanguageId)) {

                            sliderArticle.setField(elementName, contentElement.getText());
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            _log.error("Can not convert article: " + e.getMessage());
        }

        return sliderArticle;
    }

    public static SliderArticle getSliderArticle(long articleId, String languageId){

        SliderArticle sliderArticle = new SliderArticle();

        JournalArticle article = null;
        try {
            article = JournalArticleLocalServiceUtil.getArticle(articleId);
        } catch (Exception e) {
            _log.error("Can not get article: " + e.getMessage());
        }

        if (article != null) {
            sliderArticle = getSliderArticle(article, languageId);
        }

        return sliderArticle;
    }

    public static List<SliderArticle> getAllSliderArticles(PortletRequest request){

        List<SliderArticle> sliderArticles = new ArrayList<SliderArticle>();

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        String languageId = themeDisplay.getLanguageId();

        long groupId = themeDisplay.getScopeGroupId();

        ClassLoader cl = PortalClassLoaderUtil.getClassLoader();

        DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(JournalArticle.class, cl)
                .add(PropertyFactoryUtil.forName("groupId").eq(groupId))
                .add(PropertyFactoryUtil.forName("structureId").eq(ArticleStructure.getStructureId()));

        List<JournalArticle> journalArticles = null;
        try {
            journalArticles = JournalArticleLocalServiceUtil.dynamicQuery(dynamicQuery);
        } catch (SystemException e) {
            _log.error("Can not get article list: " + e.getMessage());
            journalArticles = new ArrayList<JournalArticle>();
        }

        for (JournalArticle journalArticle : journalArticles) {

            SliderArticle sliderArticle = getSliderArticle(journalArticle, languageId);

            sliderArticles.add(sliderArticle);
        }

        return sliderArticles;
    }

}
