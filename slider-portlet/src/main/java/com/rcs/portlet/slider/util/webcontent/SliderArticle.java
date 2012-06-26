/*=== new version ===*/
package com.rcs.portlet.slider.util.webcontent;


public class SliderArticle {

    private String articleId; //articleId of JournalArticle
    private String title;
    private String link;
    private String text;
    private String image;

    public SliderArticle() {

    }

    public SliderArticle(String title, String link, String text, String image) {
        this.title = title;
        this.link = link;
        this.text = text;
        this.image = image;
    }

    public void setField(String name, String value) {
        if ("title".equals(name)) {
            this.title = value;
        } else if ("link".equals(name)) {
            this.link = value;
        } else if ("text".equals(name)) {
            this.text = value;
        } else if ("image".equals(name)) {
            this.image = value;
        }
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
