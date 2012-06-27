package com.rcs.portlet.slider.util.webcontent;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SliderArticle implements Serializable {

    private String articleId; //articleId of JournalArticle
    private Map<String,String> titleMap;
    private Map<String,String> linkMap;
    private Map<String,String> textMap;
    private Map<String,String> imageMap;
    private String[] locales;

    public static final String defaultLanguageId = Locale.getDefault().toString();

    public SliderArticle() {
    }

    public void setField(String name, String value, String languageId) {
        if (languageId == null) {
            languageId = defaultLanguageId;
        }
        if ("title".equals(name)) {
            if (titleMap == null) {
                titleMap = new HashMap<String, String>();
            }
            titleMap.put(languageId, value);
        } else if ("link".equals(name)) {
            if (linkMap == null) {
                linkMap = new HashMap<String, String>();
            }
            linkMap.put(languageId, value);
        } else if ("text".equals(name)) {
            if (textMap == null) {
                textMap = new HashMap<String, String>();
            }
            textMap.put(languageId, value);
        } else if ("image".equals(name)) {
            if (imageMap == null) {
                imageMap = new HashMap<String, String>();
            }
            imageMap.put(languageId, value);
        }
    }

    public String getTitle(String languageId) {
        if (titleMap != null) {
            return titleMap.containsKey(languageId) ?
                    titleMap.get(languageId) :
                    titleMap.get(defaultLanguageId);
        }
        return "";
    }

    public String getLink(String languageId) {
        if (linkMap != null) {
            return linkMap.containsKey(languageId) ?
                    linkMap.get(languageId) :
                    linkMap.get(defaultLanguageId);
        }
        return "";
    }


    public String getText(String languageId) {
        if (textMap != null) {
            return textMap.containsKey(languageId) ?
                    textMap.get(languageId) :
                    textMap.get(defaultLanguageId);
        }
        return "";
    }


    public String getImage(String languageId) {
        if (imageMap != null) {
            return imageMap.containsKey(languageId) ?
                    imageMap.get(languageId) :
                    imageMap.get(defaultLanguageId);
        }
        return "";
    }

    public Map<String, String> getTitleMap() {
        return titleMap;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public void setTitleMap(Map<String, String> titleMap) {
        this.titleMap = titleMap;
    }

    public Map<String, String> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(Map<String, String> linkMap) {
        this.linkMap = linkMap;
    }

    public Map<String, String> getTextMap() {
        return textMap;
    }

    public void setTextMap(Map<String, String> textMap) {
        this.textMap = textMap;
    }

    public Map<String, String> getImageMap() {
        return imageMap;
    }

    public void setImageMap(Map<String, String> imageMap) {
        this.imageMap = imageMap;
    }

    public String[] getLocales() {
        return locales;
    }

    public void setLocales(String[] locales) {
        this.locales = locales;
    }
}
