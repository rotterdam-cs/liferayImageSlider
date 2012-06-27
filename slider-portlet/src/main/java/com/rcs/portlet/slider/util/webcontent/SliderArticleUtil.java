/*=== new version ===*/
package com.rcs.portlet.slider.util.webcontent;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Attribute;
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

    public static SliderArticle getSliderArticle(JournalArticle journalArticle){

        SliderArticle sliderArticle = new SliderArticle();

        sliderArticle.setArticleId(journalArticle.getArticleId());

        try {
            String content = journalArticle.getContent();

            if (Validator.isNotNull(content)) {

                Document document = SAXReaderUtil.read(content);

                Element rootElement = document.getRootElement();

                Attribute availableLocales = rootElement.attribute("available-locales");
                String[] locales = null;
                if (availableLocales != null) {
                    locales = availableLocales.getValue().split(",");
                }
                sliderArticle.setLocales(locales);

                List<Element> elements = rootElement.elements();

                for (Element element: elements) {

                    String elementName = element.attribute("name").getValue();

                    List<Element> contentElements = element.elements("dynamic-content");

                    for (Element contentElement : contentElements) {

                        String contentLanguageId = contentElement.attribute("language-id") != null ?
                                                            contentElement.attribute("language-id").getValue() :
                                                            null;

                        sliderArticle.setField(elementName, contentElement.getText(), contentLanguageId);
                    }
                }
            }

        } catch (Exception e) {
            _log.error("Can not convert article: " + e.getMessage());
        }

        return sliderArticle;
    }

    public static SliderArticle getSliderArticle(String articleId, long groupId) {

        SliderArticle sliderArticle = new SliderArticle();

        JournalArticle article = null;
        try {
            article = JournalArticleLocalServiceUtil.getLatestArticle(groupId, articleId);
        } catch (Exception e) {
            _log.error("Can not get article: " + e.getMessage());
        }

        if (article != null) {
            sliderArticle = getSliderArticle(article);
        }

        return sliderArticle;
    }

    public static List<SliderArticle> getAllSliderArticles(PortletRequest request){

        List<SliderArticle> sliderArticles = new ArrayList<SliderArticle>();

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        long groupId = themeDisplay.getScopeGroupId();

        List<JournalArticle> allStructureArticles = null;
        try {
            allStructureArticles = JournalArticleLocalServiceUtil.getStructureArticles(groupId, ArticleStructure.getStructureId());
        } catch (SystemException e) {
            _log.error("Can not get article list: " + e.getMessage());
            allStructureArticles = new ArrayList<JournalArticle>();
        }

        List<JournalArticle> latestStructureArticles = new ArrayList<JournalArticle>();

        for (JournalArticle structureArticle : allStructureArticles) {

            JournalArticle latestVersionArticle = null;
            try {
                latestVersionArticle = JournalArticleLocalServiceUtil.getLatestArticle(structureArticle.getGroupId(), structureArticle.getArticleId());
            } catch (Exception e) {
                _log.error("Can not get last version article: " + e.getMessage());
                latestVersionArticle = structureArticle;
            }

            if (!latestStructureArticles.contains(latestVersionArticle)) {
                latestStructureArticles.add(latestVersionArticle);
            }
        }

        for (JournalArticle journalArticle : latestStructureArticles) {

            SliderArticle sliderArticle = getSliderArticle(journalArticle);

            sliderArticles.add(sliderArticle);
        }

        return sliderArticles;
    }
}
