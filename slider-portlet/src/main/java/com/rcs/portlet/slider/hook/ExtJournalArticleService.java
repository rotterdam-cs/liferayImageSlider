package com.rcs.portlet.slider.hook;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalArticleDisplay;
import com.liferay.portlet.journal.service.JournalArticleLocalService;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceWrapper;
import com.rcs.portlet.slider.util.webcontent.SliderArticle;
import com.rcs.portlet.slider.util.webcontent.SliderArticleUtil;

import javax.portlet.PortletPreferences;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExtJournalArticleService extends JournalArticleLocalServiceWrapper {

    public static final String PORTLET_ID = "sliderportlet";
    public static final String PORTLET_ID_PATTERN = PORTLET_ID + "%";

    public ExtJournalArticleService(JournalArticleLocalService journalArticleLocalService) {
        super(journalArticleLocalService);
    }

    public JournalArticle updateArticle(
            long userId, long groupId, String articleId, double version,
            Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
            String content, String type, String structureId, String templateId,
            String layoutUuid, int displayDateMonth, int displayDateDay,
            int displayDateYear, int displayDateHour, int displayDateMinute,
            int expirationDateMonth, int expirationDateDay,
            int expirationDateYear, int expirationDateHour,
            int expirationDateMinute, boolean neverExpire, int reviewDateMonth,
            int reviewDateDay, int reviewDateYear, int reviewDateHour,
            int reviewDateMinute, boolean neverReview, boolean indexable,
            boolean smallImage, String smallImageURL, File smallImageFile,
            Map<String, byte[]> images, String articleURL,
            ServiceContext serviceContext)
            throws PortalException, SystemException {

        JournalArticle article = super.updateArticle(userId, groupId, articleId, version,
                titleMap, descriptionMap, content, type, structureId, templateId, layoutUuid, displayDateMonth, displayDateDay,
                displayDateYear, displayDateHour, displayDateMinute,
                expirationDateMonth, expirationDateDay,
                expirationDateYear, expirationDateHour,
                expirationDateMinute, neverExpire, reviewDateMonth,
                reviewDateDay, reviewDateYear, reviewDateHour,
                reviewDateMinute, neverReview, indexable,
                smallImage, smallImageURL, smallImageFile, images, articleURL, serviceContext);

        if (_log.isInfoEnabled()) {
            _log.info("JournalArticleService hook start.");
        }

        //the default locale for article
        Locale locale = LocaleUtil.fromLanguageId(article.getDefaultLocale());

        updateSliderPreferences(article, locale);

        if (_log.isInfoEnabled()) {
            _log.info("JournalArticleService hook end.");
        }

        return article;
    }

    public JournalArticle updateArticleTranslation(
            long groupId, String articleId, double version, Locale locale,
            String title, String description, String content,
            Map<String, byte[]> images)
            throws PortalException, SystemException {


        JournalArticle article = super.updateArticleTranslation(groupId, articleId, version, locale,
                title, description, content, images);

        if (_log.isInfoEnabled()) {
            _log.info("JournalArticleService hook start.");
        }

        updateSliderPreferences(article, locale);

        if (_log.isInfoEnabled()) {
            _log.info("JournalArticleService hook end.");
        }

        return article;
    }

    private void updateSliderPreferences(JournalArticle article, Locale locale) {

        try {
            //Obtain portal classLoader
            ClassLoader classLoader = PortalClassLoaderUtil.getClassLoader();

            long groupId = article.getGroupId();
            String articleId = article.getArticleId();

            //Iterate over layouts
            List<Layout> layouts = LayoutLocalServiceUtil.getLayouts(groupId, false);
            for (Layout layout : layouts) {

                if (layout.getLayoutType() != null) {

                    //Iterate over all Slider portlets
                    DynamicQuery portletDynamicQuery = DynamicQueryFactoryUtil.forClass(Portlet.class, classLoader).add(PropertyFactoryUtil.forName("portletId").like(PORTLET_ID_PATTERN));
                    List<Portlet> portlets = PortletLocalServiceUtil.dynamicQuery(portletDynamicQuery);
                    for (Portlet portlet : portlets) {

                        //Get layout properties - in order to obtain full portletId
                        HashMap<String, String> layoutPropertiesMap = layout.getTypeSettingsProperties();

                        for (String layoutValue : layoutPropertiesMap.values()) {

                            if (layoutValue.startsWith(PORTLET_ID)) {

                                // the portletId is the initial part before first "," if extra info is present
                                String portletId = layoutValue.contains(StringPool.COMMA) ?
                                                    layoutValue.substring(0, layoutValue.indexOf(StringPool.COMMA)) :
                                                    layoutValue;

                                //get preferences for portlet
                                List<com.liferay.portal.model.PortletPreferences> portletPrefs = PortletPreferencesLocalServiceUtil.getPortletPreferences(layout.getPlid(), portletId);

                                //iterate over preferences
                                for (com.liferay.portal.model.PortletPreferences prefs : portletPrefs) {

                                    //get javax.portlet.PortletPreferences for portlet
                                    PortletPreferences portletPreferences = PortletPreferencesLocalServiceUtil.getPreferences(portlet.getCompanyId(), prefs.getOwnerId(), prefs.getOwnerType(), layout.getPlid(), portletId);

                                    Map<String, String[]> preferencesMap = portletPreferences.getMap();

                                    for (String key : preferencesMap.keySet()) {

                                        String languageId = locale.toString();

                                        if (key.contains(articleId) && key.endsWith(languageId)) {

                                            String[] values = preferencesMap.get(key);

                                            SliderArticle sliderArticle = SliderArticleUtil.getSliderArticle(article);

                                            String _id = values[4];
                                            String _order = values[5];

                                            String updatedValues[] = new String[]{
                                                sliderArticle.getTitle(languageId),
                                                sliderArticle.getLink(languageId),
                                                sliderArticle.getText(languageId),
                                                sliderArticle.getImage(languageId),
                                                _id,
                                                _order
                                            };

                                            if (_log.isInfoEnabled()) {
                                                _log.info("Update preferences with key='" + key + "' for portlet '" + portletId + "'.");
                                            }

                                            portletPreferences.setValues(key, updatedValues);
                                            portletPreferences.store();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            _log.error("Can not update Slider preferences: " + e.getMessage());
        }

    }

    @Override
    public JournalArticleDisplay getArticleDisplay(JournalArticle article, String templateId, String viewMode, String languageId, int page, String xmlRequest, ThemeDisplay themeDisplay) throws PortalException, SystemException {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader portalClassLoader = PortalClassLoaderUtil.getClassLoader();
        Thread.currentThread().setContextClassLoader(portalClassLoader);

        JournalArticleDisplay articleDisplay = super.getArticleDisplay(article, templateId, viewMode, languageId, page, xmlRequest, themeDisplay);

        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, double version, String templateId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader portalClassLoader = PortalClassLoaderUtil.getClassLoader();
        Thread.currentThread().setContextClassLoader(portalClassLoader);

        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, version, templateId, viewMode, languageId, themeDisplay);//To change body of overridden methods use File | Settings | File Templates.

        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String templateId, String viewMode, String languageId, int page, String xmlRequest, ThemeDisplay themeDisplay) throws PortalException, SystemException {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader portalClassLoader = PortalClassLoaderUtil.getClassLoader();
        Thread.currentThread().setContextClassLoader(portalClassLoader);

        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, templateId, viewMode, languageId, page, xmlRequest, themeDisplay);//To change body of overridden methods use File | Settings | File Templates.

        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader portalClassLoader = PortalClassLoaderUtil.getClassLoader();
        Thread.currentThread().setContextClassLoader(portalClassLoader);

        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, viewMode, languageId, themeDisplay);//To change body of overridden methods use File | Settings | File Templates.

        Thread.currentThread().setContextClassLoader(currentClassLoader);

        return articleDisplay;
    }

    private static Log _log = LogFactoryUtil.getLog(ExtJournalArticleService.class);
}
