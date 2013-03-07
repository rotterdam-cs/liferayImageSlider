package com.rcs.portlet.slider.hook;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.PersistedModel;
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
import java.io.Serializable;
import java.util.*;

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

        setPortalClassLoader();

        JournalArticle article = super.updateArticle(userId, groupId, articleId, version,
                titleMap, descriptionMap, content, type, structureId, templateId, layoutUuid, displayDateMonth, displayDateDay,
                displayDateYear, displayDateHour, displayDateMinute,
                expirationDateMonth, expirationDateDay,
                expirationDateYear, expirationDateHour,
                expirationDateMinute, neverExpire, reviewDateMonth,
                reviewDateDay, reviewDateYear, reviewDateHour,
                reviewDateMinute, neverReview, indexable,
                smallImage, smallImageURL, smallImageFile, images, articleURL, serviceContext);

        setSliderClassLoader();

        //the default locale for article
        Locale locale = LocaleUtil.fromLanguageId(article.getDefaultLocale());

        updateSliderPreferences(article, locale);

        return article;
    }

    public JournalArticle updateArticleTranslation(
            long groupId, String articleId, double version, Locale locale,
            String title, String description, String content,
            Map<String, byte[]> images)
            throws PortalException, SystemException {

        setPortalClassLoader();

        JournalArticle article = super.updateArticleTranslation(groupId, articleId, version, locale,
                title, description, content, images);

        setSliderClassLoader();

        updateSliderPreferences(article, locale);

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
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(article, templateId, viewMode, languageId, page, xmlRequest, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, double version, String templateId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, version, templateId, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String templateId, String viewMode, String languageId, int page, String xmlRequest, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, templateId, viewMode, languageId, page, xmlRequest, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticle addJournalArticle(JournalArticle journalArticle) throws SystemException {
        setPortalClassLoader();
        JournalArticle _journalArticle = super.addJournalArticle(journalArticle);
        setSliderClassLoader();
        return _journalArticle;
    }

    @Override
    public JournalArticle createJournalArticle(long id) {
        setPortalClassLoader();
        JournalArticle journalArticle = super.createJournalArticle(id);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle deleteJournalArticle(long id) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle ja = super.deleteJournalArticle(id);
        setSliderClassLoader();
	return ja;
    }

    @Override
    public JournalArticle deleteJournalArticle(JournalArticle journalArticle) throws SystemException {
        setPortalClassLoader();
        JournalArticle ja = super.deleteJournalArticle(journalArticle);
        setSliderClassLoader();
	return ja;
    }

    @Override
    public List dynamicQuery(DynamicQuery dynamicQuery) throws SystemException {
        setPortalClassLoader();
        List list = super.dynamicQuery(dynamicQuery);
        setSliderClassLoader();
        return list;
    }

    @Override
    public List dynamicQuery(DynamicQuery dynamicQuery, int start, int end) throws SystemException {
        setPortalClassLoader();
        List list = super.dynamicQuery(dynamicQuery, start, end);
        setSliderClassLoader();
        return list;
    }

    @Override
    public List dynamicQuery(DynamicQuery dynamicQuery, int start, int end, OrderByComparator orderByComparator) throws SystemException {
        setPortalClassLoader();
        List list = super.dynamicQuery(dynamicQuery, start, end, orderByComparator);
        setSliderClassLoader();
        return list;
    }

    @Override
    public long dynamicQueryCount(DynamicQuery dynamicQuery) throws SystemException {
        setPortalClassLoader();
        long l = super.dynamicQueryCount(dynamicQuery);
        setSliderClassLoader();
        return l;
    }

    @Override
    public JournalArticle fetchJournalArticle(long id) throws SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.fetchJournalArticle(id);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle getJournalArticle(long id) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.getJournalArticle(id);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public PersistedModel getPersistedModel(Serializable primaryKeyObj) throws PortalException, SystemException {
        setPortalClassLoader();
        PersistedModel persistedModel = super.getPersistedModel(primaryKeyObj);
        setSliderClassLoader();
        return persistedModel;
    }

    @Override
    public JournalArticle getJournalArticleByUuidAndGroupId(String uuid, long groupId) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.getJournalArticleByUuidAndGroupId(uuid, groupId);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public List<JournalArticle> getJournalArticles(int start, int end) throws SystemException {
        return super.getJournalArticles(start, end);
    }

    @Override
    public int getJournalArticlesCount() throws SystemException {
        setPortalClassLoader();
        int journalArticlesCount = super.getJournalArticlesCount();
        setSliderClassLoader();
        return journalArticlesCount;
    }

    @Override
    public JournalArticle updateJournalArticle(JournalArticle journalArticle) throws SystemException {
        setPortalClassLoader();
        JournalArticle _journalArticle = super.updateJournalArticle(journalArticle);
        setSliderClassLoader();
        return _journalArticle;
    }

    @Override
    public JournalArticle updateJournalArticle(JournalArticle journalArticle, boolean merge) throws SystemException {
        setPortalClassLoader();
        JournalArticle _journalArticle = super.updateJournalArticle(journalArticle, merge);
        setSliderClassLoader();
        return _journalArticle;
    }

    @Override
    public String getBeanIdentifier() {
        setPortalClassLoader();
        String beanIdentifier = super.getBeanIdentifier();
        setSliderClassLoader();
        return beanIdentifier;
    }

    @Override
    public void setBeanIdentifier(String beanIdentifier) {
        setPortalClassLoader();
        super.setBeanIdentifier(beanIdentifier);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle addArticle(long userId, long groupId, long classNameId, long classPK, String articleId, boolean autoArticleId, double version, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, String content, String type, String structureId, String templateId, String layoutUuid, int displayDateMonth, int displayDateDay, int displayDateYear, int displayDateHour, int displayDateMinute, int expirationDateMonth, int expirationDateDay, int expirationDateYear, int expirationDateHour, int expirationDateMinute, boolean neverExpire, int reviewDateMonth, int reviewDateDay, int reviewDateYear, int reviewDateHour, int reviewDateMinute, boolean neverReview, boolean indexable, boolean smallImage, String smallImageURL, File smallImageFile, Map<String, byte[]> images, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.addArticle(userId, groupId, classNameId, classPK, articleId, autoArticleId, version, titleMap, descriptionMap, content, type, structureId, templateId, layoutUuid, displayDateMonth, displayDateDay, displayDateYear, displayDateHour, displayDateMinute, expirationDateMonth, expirationDateDay, expirationDateYear, expirationDateHour, expirationDateMinute, neverExpire, reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour, reviewDateMinute, neverReview, indexable, smallImage, smallImageURL, smallImageFile, images, articleURL, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public void addArticleResources(JournalArticle article, boolean addGroupPermissions, boolean addGuestPermissions) throws PortalException, SystemException {
        setPortalClassLoader();
        super.addArticleResources(article, addGroupPermissions, addGuestPermissions);
        setSliderClassLoader();
    }

    @Override
    public void addArticleResources(JournalArticle article, String[] groupPermissions, String[] guestPermissions) throws PortalException, SystemException {
        setPortalClassLoader();
        super.addArticleResources(article, groupPermissions, guestPermissions);
        setSliderClassLoader();
    }

    @Override
    public void addArticleResources(long groupId, String articleId, boolean addGroupPermissions, boolean addGuestPermissions) throws PortalException, SystemException {
        setPortalClassLoader();
        super.addArticleResources(groupId, articleId, addGroupPermissions, addGuestPermissions);
        setSliderClassLoader();
    }

    @Override
    public void addArticleResources(long groupId, String articleId, String[] groupPermissions, String[] guestPermissions) throws PortalException, SystemException {
        setPortalClassLoader();
        super.addArticleResources(groupId, articleId, groupPermissions, guestPermissions);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle checkArticleResourcePrimKey(long groupId, String articleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.checkArticleResourcePrimKey(groupId, articleId, version);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public void checkArticles() throws PortalException, SystemException {
        setPortalClassLoader();
        super.checkArticles();
        setSliderClassLoader();
    }

    @Override
    public void checkNewLine(long groupId, String articleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        super.checkNewLine(groupId, articleId, version);
        setSliderClassLoader();
    }

    @Override
    public void checkStructure(long groupId, String articleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        super.checkStructure(groupId, articleId, version);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle copyArticle(long userId, long groupId, String oldArticleId, String newArticleId, boolean autoArticleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.copyArticle(userId, groupId, oldArticleId, newArticleId, autoArticleId, version);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public void deleteArticle(JournalArticle article, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        super.deleteArticle(article, articleURL, serviceContext);
        setSliderClassLoader();
    }

    @Override
    public void deleteArticle(long groupId, String articleId, double version, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        super.deleteArticle(groupId, articleId, version, articleURL, serviceContext);
        setSliderClassLoader();
    }

    @Override
    public void deleteArticle(long groupId, String articleId, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        super.deleteArticle(groupId, articleId, serviceContext);
        setSliderClassLoader();
    }

    @Override
    public void deleteArticles(long groupId) throws PortalException, SystemException {
        setPortalClassLoader();
        super.deleteArticles(groupId);
        setSliderClassLoader();
    }

    @Override
    public void deleteLayoutArticleReferences(long groupId, String layoutUuid) throws SystemException {
        setPortalClassLoader();
        super.deleteLayoutArticleReferences(groupId, layoutUuid);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle expireArticle(long userId, long groupId, String articleId, double version, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.expireArticle(userId, groupId, articleId, version, articleURL, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public void expireArticle(long userId, long groupId, String articleId, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        super.expireArticle(userId, groupId, articleId, articleURL, serviceContext);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle getArticle(long id) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle article = super.getArticle(id);
        setSliderClassLoader();
        return article;
    }

    @Override
    public JournalArticle getArticle(long groupId, String articleId) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle article = super.getArticle(groupId, articleId);
        setSliderClassLoader();
        return article;
    }

    @Override
    public JournalArticle getArticle(long groupId, String articleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle article = super.getArticle(groupId, articleId, version);
        setSliderClassLoader();
        return article;
    }

    @Override
    public JournalArticle getArticle(long groupId, String className, long classPK) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle article = super.getArticle(groupId, className, classPK);
        setSliderClassLoader();
        return article;
    }

    @Override
    public JournalArticle getArticleByUrlTitle(long groupId, String urlTitle) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle articleByUrlTitle = super.getArticleByUrlTitle(groupId, urlTitle);
        setSliderClassLoader();
        return articleByUrlTitle;
    }

    @Override
    public String getArticleContent(JournalArticle article, String templateId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        String articleContent = super.getArticleContent(article, templateId, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleContent;
    }

    @Override
    public String getArticleContent(long groupId, String articleId, double version, String viewMode, String templateId, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        String articleContent = super.getArticleContent(groupId, articleId, version, viewMode, templateId, languageId, themeDisplay);
        setSliderClassLoader();
        return articleContent;
    }

    @Override
    public String getArticleContent(long groupId, String articleId, double version, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        String articleContent = super.getArticleContent(groupId, articleId, version, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleContent;
    }

    @Override
    public String getArticleContent(long groupId, String articleId, String viewMode, String templateId, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        String articleContent = super.getArticleContent(groupId, articleId, viewMode, templateId, languageId, themeDisplay);
        setSliderClassLoader();
        return articleContent;
    }

    @Override
    public String getArticleContent(long groupId, String articleId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        String articleContent = super.getArticleContent(groupId, articleId, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleContent;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, double version, String templateId, String viewMode, String languageId, int page, String xmlRequest, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, version, templateId, viewMode, languageId, page, xmlRequest, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String viewMode, String languageId, int page, String xmlRequest, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, viewMode, languageId, page, xmlRequest, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public JournalArticleDisplay getArticleDisplay(long groupId, String articleId, String templateId, String viewMode, String languageId, ThemeDisplay themeDisplay) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticleDisplay articleDisplay = super.getArticleDisplay(groupId, articleId, templateId, viewMode, languageId, themeDisplay);
        setSliderClassLoader();
        return articleDisplay;
    }

    @Override
    public List<JournalArticle> getArticles() throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articles = super.getArticles();
        setSliderClassLoader();
        return articles;
    }

    @Override
    public List<JournalArticle> getArticles(long groupId) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articles = super.getArticles(groupId);
        setSliderClassLoader();
        return articles;
    }

    @Override
    public List<JournalArticle> getArticles(long groupId, int start, int end) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articles = super.getArticles(groupId, start, end);
        setSliderClassLoader();
        return articles;
    }

    @Override
    public List<JournalArticle> getArticles(long groupId, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articles = super.getArticles(groupId, start, end, obc);
        setSliderClassLoader();
        return articles;
    }

    @Override
    public List<JournalArticle> getArticles(long groupId, String articleId) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articles = super.getArticles(groupId, articleId);
        setSliderClassLoader();
        return articles;
    }

    @Override
    public List<JournalArticle> getArticlesBySmallImageId(long smallImageId) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> articlesBySmallImageId = super.getArticlesBySmallImageId(smallImageId);
        setSliderClassLoader();
        return articlesBySmallImageId;
    }

    @Override
    public int getArticlesCount(long groupId) throws SystemException {
        setPortalClassLoader();
        int articlesCount = super.getArticlesCount(groupId);
        setSliderClassLoader();
        return articlesCount;
    }

    @Override
    public List<JournalArticle> getCompanyArticles(long companyId, double version, int status, int start, int end) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> companyArticles = super.getCompanyArticles(companyId, version, status, start, end);
        setSliderClassLoader();
        return companyArticles;
    }

    @Override
    public List<JournalArticle> getCompanyArticles(long companyId, int status, int start, int end) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> companyArticles = super.getCompanyArticles(companyId, status, start, end);
        setSliderClassLoader();
        return companyArticles;
    }

    @Override
    public int getCompanyArticlesCount(long companyId, double version, int status, int start, int end) throws SystemException {
        setPortalClassLoader();
        int companyArticlesCount = super.getCompanyArticlesCount(companyId, version, status, start, end);
        setSliderClassLoader();
        return companyArticlesCount;
    }

    @Override
    public int getCompanyArticlesCount(long companyId, int status) throws SystemException {
        setPortalClassLoader();
        int companyArticlesCount = super.getCompanyArticlesCount(companyId, status);
        setSliderClassLoader();
        return companyArticlesCount;
    }

    @Override
    public JournalArticle getDisplayArticle(long groupId, String articleId) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle displayArticle = super.getDisplayArticle(groupId, articleId);
        setSliderClassLoader();
        return displayArticle;
    }

    @Override
    public JournalArticle getDisplayArticleByUrlTitle(long groupId, String urlTitle) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle displayArticleByUrlTitle = super.getDisplayArticleByUrlTitle(groupId, urlTitle);
        setSliderClassLoader();
        return displayArticleByUrlTitle;
    }

    @Override
    public JournalArticle getLatestArticle(long resourcePrimKey) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(resourcePrimKey);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticle(long resourcePrimKey, int status) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(resourcePrimKey, status);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticle(long resourcePrimKey, int status, boolean preferApproved) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(resourcePrimKey, status, preferApproved);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticle(long groupId, String articleId) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(groupId, articleId);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticle(long groupId, String articleId, int status) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(groupId, articleId, status);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticle(long groupId, String className, long classPK) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticle = super.getLatestArticle(groupId, className, classPK);
        setSliderClassLoader();
        return latestArticle;
    }

    @Override
    public JournalArticle getLatestArticleByUrlTitle(long groupId, String urlTitle, int status) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle latestArticleByUrlTitle = super.getLatestArticleByUrlTitle(groupId, urlTitle, status);
        setSliderClassLoader();
        return latestArticleByUrlTitle;
    }

    @Override
    public double getLatestVersion(long groupId, String articleId) throws PortalException, SystemException {
        setPortalClassLoader();
        double latestVersion = super.getLatestVersion(groupId, articleId);
        setSliderClassLoader();
        return latestVersion;
    }

    @Override
    public double getLatestVersion(long groupId, String articleId, int status) throws PortalException, SystemException {
        setPortalClassLoader();
        double latestVersion = super.getLatestVersion(groupId, articleId, status);
        setSliderClassLoader();
        return latestVersion;
    }

    @Override
    public List<JournalArticle> getStructureArticles(long groupId, String structureId) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> structureArticles = super.getStructureArticles(groupId, structureId);
        setSliderClassLoader();
        return structureArticles;
    }

    @Override
    public List<JournalArticle> getStructureArticles(long groupId, String structureId, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> structureArticles = super.getStructureArticles(groupId, structureId, start, end, obc);
        setSliderClassLoader();
        return structureArticles;
    }

    @Override
    public int getStructureArticlesCount(long groupId, String structureId) throws SystemException {
        setPortalClassLoader();
        int structureArticlesCount = super.getStructureArticlesCount(groupId, structureId);
        setSliderClassLoader();
        return structureArticlesCount;
    }

    @Override
    public List<JournalArticle> getTemplateArticles(long groupId, String templateId) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> templateArticles = super.getTemplateArticles(groupId, templateId);
        setSliderClassLoader();
        return templateArticles;
    }

    @Override
    public List<JournalArticle> getTemplateArticles(long groupId, String templateId, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> templateArticles = super.getTemplateArticles(groupId, templateId, start, end, obc);
        setSliderClassLoader();
        return templateArticles;
    }

    @Override
    public int getTemplateArticlesCount(long groupId, String templateId) throws SystemException {
        setPortalClassLoader();
        int templateArticlesCount = super.getTemplateArticlesCount(groupId, templateId);
        setSliderClassLoader();
        return templateArticlesCount;
    }

    @Override
    public boolean hasArticle(long groupId, String articleId) throws SystemException {
        setPortalClassLoader();
        boolean b = super.hasArticle(groupId, articleId);
        setSliderClassLoader();
        return b;
    }

    @Override
    public boolean isLatestVersion(long groupId, String articleId, double version) throws PortalException, SystemException {
        setPortalClassLoader();
        boolean latestVersion = super.isLatestVersion(groupId, articleId, version);
        setSliderClassLoader();
        return latestVersion;
    }

    @Override
    public boolean isLatestVersion(long groupId, String articleId, double version, int status) throws PortalException, SystemException {
        setPortalClassLoader();
        boolean latestVersion = super.isLatestVersion(groupId, articleId, version, status);
        setSliderClassLoader();
        return latestVersion;
    }

    @Override
    public JournalArticle removeArticleLocale(long groupId, String articleId, double version, String languageId) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.removeArticleLocale(groupId, articleId, version, languageId);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public List<JournalArticle> search(long companyId, long groupId, long classNameId, String keywords, Double version, String type, String structureId, String templateId, Date displayDateGT, Date displayDateLT, int status, Date reviewDate, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> search = super.search(companyId, groupId, classNameId, keywords, version, type, structureId, templateId, displayDateGT, displayDateLT, status, reviewDate, start, end, obc);
        setSliderClassLoader();
        return search;
    }

    @Override
    public List<JournalArticle> search(long companyId, long groupId, long classNameId, String articleId, Double version, String title, String description, String content, String type, String structureId, String templateId, Date displayDateGT, Date displayDateLT, int status, Date reviewDate, boolean andOperator, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> search = super.search(companyId, groupId, classNameId, articleId, version, title, description, content, type, structureId, templateId, displayDateGT, displayDateLT, status, reviewDate, andOperator, start, end, obc);
        setSliderClassLoader();
        return search;
    }

    @Override
    public List<JournalArticle> search(long companyId, long groupId, long classNameId, String articleId, Double version, String title, String description, String content, String type, String[] structureIds, String[] templateIds, Date displayDateGT, Date displayDateLT, int status, Date reviewDate, boolean andOperator, int start, int end, OrderByComparator obc) throws SystemException {
        setPortalClassLoader();
        List<JournalArticle> search = super.search(companyId, groupId, classNameId, articleId, version, title, description, content, type, structureIds, templateIds, displayDateGT, displayDateLT, status, reviewDate, andOperator, start, end, obc);
        setSliderClassLoader();
        return search;
    }

    @Override
    public Hits search(long companyId, long groupId, long classNameId, String structureId, String templateId, String keywords, LinkedHashMap<String, Object> params, int start, int end, Sort sort) throws SystemException {
        setPortalClassLoader();
        Hits search = super.search(companyId, groupId, classNameId, structureId, templateId, keywords, params, start, end, sort);
        setSliderClassLoader();
        return search;
    }

    @Override
    public Hits search(long companyId, long groupId, long classNameId, String articleId, String title, String description, String content, String type, String status, String structureId, String templateId, LinkedHashMap<String, Object> params, boolean andSearch, int start, int end, Sort sort) throws SystemException {
        setPortalClassLoader();
        Hits search = super.search(companyId, groupId, classNameId, articleId, title, description, content, type, status, structureId, templateId, params, andSearch, start, end, sort);
        setSliderClassLoader();
        return search;
    }

    @Override
    public int searchCount(long companyId, long groupId, long classNameId, String keywords, Double version, String type, String structureId, String templateId, Date displayDateGT, Date displayDateLT, int status, Date reviewDate) throws SystemException {
        setPortalClassLoader();
        int i = super.searchCount(companyId, groupId, classNameId, keywords, version, type, structureId, templateId, displayDateGT, displayDateLT, status, reviewDate);
        setSliderClassLoader();
        return i;
    }

    @Override
    public int searchCount(long companyId, long groupId, long classNameId, String articleId, Double version, String title, String description, String content, String type, String structureId, String templateId, Date displayDateGT, Date displayDateLT, int status, Date reviewDate, boolean andOperator) throws SystemException {
        setPortalClassLoader();
        int i = super.searchCount(companyId, groupId, classNameId, articleId, version, title, description, content, type, structureId, templateId, displayDateGT, displayDateLT, status, reviewDate, andOperator);
        setSliderClassLoader();
        return i;
    }

    @Override
    public int searchCount(long companyId, long groupId, long classNameId, String articleId, Double version, String title, String description, String content, String type, String[] structureIds, String[] templateIds, Date displayDateGT, Date displayDateLT, int status, Date reviewDate, boolean andOperator) throws SystemException {
        setPortalClassLoader();
        int count = super.searchCount(companyId, groupId, classNameId, articleId, version, title, description, content, type, structureIds, templateIds, displayDateGT, displayDateLT, status, reviewDate, andOperator);
        setSliderClassLoader();
        return count;
    }

    @Override
    public void subscribe(long userId, long groupId) throws PortalException, SystemException {
        setPortalClassLoader();
        super.subscribe(userId, groupId);
        setSliderClassLoader();
    }

    @Override
    public void unsubscribe(long userId, long groupId) throws PortalException, SystemException {
        setPortalClassLoader();
        super.unsubscribe(userId, groupId);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle updateArticle(long userId, long groupId, String articleId, double version, Map<Locale, String> titleMap, Map<Locale, String> descriptionMap, String content, String layoutUuid, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.updateArticle(userId, groupId, articleId, version, titleMap, descriptionMap, content, layoutUuid, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle updateArticle(long userId, long groupId, String articleId, double version, String content, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.updateArticle(userId, groupId, articleId, version, content, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public void updateAsset(long userId, JournalArticle article, long[] assetCategoryIds, String[] assetTagNames, long[] assetLinkEntryIds) throws PortalException, SystemException {
        setPortalClassLoader();
        super.updateAsset(userId, article, assetCategoryIds, assetTagNames, assetLinkEntryIds);
        setSliderClassLoader();
    }

    @Override
    public JournalArticle updateContent(long groupId, String articleId, double version, String content) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.updateContent(groupId, articleId, version, content);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle updateStatus(long userId, JournalArticle article, int status, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.updateStatus(userId, article, status, articleURL, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle updateStatus(long userId, long classPK, int status, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle journalArticle = super.updateStatus(userId, classPK, status, serviceContext);
        setSliderClassLoader();
        return journalArticle;
    }

    @Override
    public JournalArticle updateStatus(long userId, long groupId, String articleId, double version, int status, String articleURL, ServiceContext serviceContext) throws PortalException, SystemException {
        setPortalClassLoader();
        JournalArticle _journalArticle = super.updateStatus(userId, groupId, articleId, version, status, articleURL, serviceContext);
        setSliderClassLoader();
        return _journalArticle;
    }

    @Override
    public void updateTemplateId(long groupId, long classNameId, String oldTemplateId, String newTemplateId) throws SystemException {
        setPortalClassLoader();
        super.updateTemplateId(groupId, classNameId, oldTemplateId, newTemplateId);
        setSliderClassLoader();
    }

    @Override
    public JournalArticleLocalService getWrappedJournalArticleLocalService() {
        setPortalClassLoader();
        JournalArticleLocalService wrappedJournalArticleLocalService = super.getWrappedJournalArticleLocalService();
        setSliderClassLoader();
        return wrappedJournalArticleLocalService;
    }

    @Override
    public void setWrappedJournalArticleLocalService(JournalArticleLocalService journalArticleLocalService) {
        setPortalClassLoader();
        super.setWrappedJournalArticleLocalService(journalArticleLocalService);
        setSliderClassLoader();
    }

    @Override
    public JournalArticleLocalService getWrappedService() {
        setPortalClassLoader();
        JournalArticleLocalService wrappedService = super.getWrappedService();
        setSliderClassLoader();
        return wrappedService;
    }

    @Override
    public void setWrappedService(JournalArticleLocalService journalArticleLocalService) {
        setPortalClassLoader();
        super.setWrappedService(journalArticleLocalService);
        setSliderClassLoader();
    }

    private void setPortalClassLoader() {
        Thread.currentThread().setContextClassLoader(PORTAL_CLASS_LOADER);
    }

    private void setSliderClassLoader() {
        Thread.currentThread().setContextClassLoader(SLIDER_CLASS_LOADER);
    }

    public static final ClassLoader PORTAL_CLASS_LOADER = PortalClassLoaderUtil.getClassLoader();
    public static final ClassLoader SLIDER_CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    private static Log _log = LogFactoryUtil.getLog(ExtJournalArticleService.class);
}
